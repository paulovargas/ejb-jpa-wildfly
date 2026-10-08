param(
  [int]$PortOffset = 1200
)
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$integrationDir = Join-Path $projectDir 'target\integration'
$wildflyDir = Join-Path $integrationDir 'wildfly-26.1.3.Final'
$javaExecutable = (Get-Command java.exe).Source
$nodeExecutable = (Get-Command node.exe).Source
$server = $null
$previousLocation = Get-Location
$variableNames = @('JWT_SECRET_BASE64', 'JWT_TTL_SECONDS', 'APP_ADMIN_EMAIL', 'APP_ADMIN_PASSWORD', 'AUTH_TEST_BASE_URL')
$previousEnvironment = @{}
foreach ($name in $variableNames) { $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
try {
  Set-Location -LiteralPath $projectDir
  & mvn -B package
  if ($LASTEXITCODE -ne 0) { throw 'Build ou testes unitários falharam.' }
  if (-not (Test-Path -LiteralPath $wildflyDir)) {
    & mvn -B dependency:copy '-Dartifact=org.wildfly:wildfly-dist:26.1.3.Final:zip' '-DoutputDirectory=target/integration'
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível baixar o WildFly de teste.' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory((Join-Path $integrationDir 'wildfly-dist-26.1.3.Final.zip'), $integrationDir)
  }
  $configurationPath = Join-Path $wildflyDir 'standalone\configuration\standalone.xml'
  [xml]$configuration = Get-Content -LiteralPath $configurationPath
  $ds = $configuration.SelectSingleNode('//*[local-name()="datasource" and (@pool-name="ExampleDS" or @pool-name="ejb-jpa-wildfly")]')
  $ds.SetAttribute('jndi-name', 'java:/jdbc/ejb-jpa-wildfly')
  $ds.SetAttribute('pool-name', 'ejb-jpa-wildfly')
  $ds.SelectSingleNode('*[local-name()="connection-url"]').InnerText = 'jdbc:h2:mem:auth_test;DB_CLOSE_DELAY=-1'
  $configuration.SelectSingleNode('//*[local-name()="default-bindings"]').SetAttribute('datasource', 'java:/jdbc/ejb-jpa-wildfly')
  $configuration.Save($configurationPath)
  $httpPort = 8080 + $PortOffset
  $managementPort = 9990 + $PortOffset
  foreach ($port in @($httpPort, $managementPort)) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
      throw "Porta $port ocupada. Escolha outro PortOffset."
    }
  }
  $secretBytes = New-Object byte[] 32
  $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
  try { $rng.GetBytes($secretBytes) } finally { $rng.Dispose() }
  $env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($secretBytes)
  $env:JWT_TTL_SECONDS = '900'
  $env:APP_ADMIN_EMAIL = 'admin@exemplo.com'
  $env:APP_ADMIN_PASSWORD = [Guid]::NewGuid().ToString()
  $env:AUTH_TEST_BASE_URL = "http://127.0.0.1:$httpPort/ejb-jpa-wildfly/api"
  $stdoutPath = Join-Path $integrationDir 'server-stdout.log'
  $stderrPath = Join-Path $integrationDir 'server-stderr.log'
  $serverArguments = @('-Xms128m', '-Xmx512m', "-Djboss.home.dir=$wildflyDir", "-Djboss.server.base.dir=$wildflyDir/standalone", "-Djboss.socket.binding.port-offset=$PortOffset", '-jar', "$wildflyDir/jboss-modules.jar", '-mp', "$wildflyDir/modules", 'org.jboss.as.standalone', '-b', '127.0.0.1', '-bmanagement', '127.0.0.1')
  $server = Start-Process -FilePath $javaExecutable -ArgumentList $serverArguments -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
  Write-Output 'WildFly de teste iniciado com banco H2 em memória.'
  $ready = $false
  for ($i = 0; $i -lt 90; $i++) {
    if ($server.HasExited) { throw "Servidor encerrou. Consulte $stdoutPath e $stderrPath." }
    if (Test-Path -LiteralPath $stdoutPath) {
      $log = Get-Content -LiteralPath $stdoutPath -Raw
      if ($log -match 'WFLYSRV0025') { $ready = $true; break }
    }
    Start-Sleep -Seconds 1
  }
  if (-not $ready) { throw 'Tempo limite ao iniciar o WildFly.' }
  $cliArguments = @("-Djboss.cli.config=$wildflyDir/bin/jboss-cli.xml", '-jar', "$wildflyDir/jboss-modules.jar", '-mp', "$wildflyDir/modules", 'org.jboss.as.cli', '--connect', "--controller=127.0.0.1:$managementPort")
  & $javaExecutable @cliArguments '--file=config/security.cli'
  if ($LASTEXITCODE -ne 0) { throw 'Configuração de segurança falhou.' }
  & $javaExecutable @cliArguments '--command=deploy target/ejb-jpa-wildfly.war --force'
  if ($LASTEXITCODE -ne 0) { throw 'Deploy falhou. Consulte os logs do servidor.' }
  & $nodeExecutable 'scripts/test-swagger.mjs'
  if ($LASTEXITCODE -ne 0) { throw 'Testes Swagger/OpenAPI falharam.' }
  & $nodeExecutable 'scripts/test-auth.mjs'
  if ($LASTEXITCODE -ne 0) { throw 'Testes HTTP falharam.' }
  Write-Output 'Integração validada no WildFly 26.1.3.Final com H2. MariaDB requer validação no ambiente configurado.'
} finally {
  if ($server -and -not $server.HasExited) { Stop-Process -Id $server.Id -Force }
  foreach ($name in $variableNames) { [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process') }
  Set-Location -LiteralPath $previousLocation
}
