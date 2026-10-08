# EJB, JPA e WildFly — API com JWT

Projeto de demonstração de Java 11, Jakarta EE 8, JAX-RS, CDI, EJB, JPA/JTA e MariaDB. O login emite JWT e usa Jakarta Security para estabelecer a identidade no container, inclusive nos serviços EJB.

## Implementado

- Login com email e senha, JWT HS256 com validade padrão de 15 minutos.
- Validação de assinatura, algoritmo, emissor, audiência, identidade e datas.
- Senhas com PBKDF2-HMAC-SHA256, salt aleatório e 600.000 iterações, por meio da API Jakarta Security.
- Perfis ADMIN e OPERADOR, autorização no servidor e nos EJBs.
- Consulta do usuário atual e criação de usuários por administrador.
- Primeiro administrador criado apenas quando o banco está sem usuários, usando variáveis de ambiente.
- Limite de 10 chamadas de login por minuto por endereço remoto, em memória por instância.
- Criação e listagem de clientes protegidas por autenticação.
- Swagger UI com exemplos, respostas HTTP e autenticação Bearer JWT; OpenAPI JSON e YAML gerados no build.

A auditoria de alterações ainda não foi implementada. A identidade obtida por SessionContext.getCallerPrincipal() é o ID do usuário e prepara essa integração.

## Pré-requisitos

- JDK 11 e Maven 3.8 ou superior.
- WildFly compatível com Jakarta EE 8 / javax.*. A integração automatizada utiliza 26.1.3.Final. WildFly 27+ requer migração da stack.
- MariaDB com banco e usuário da aplicação previamente criados.
- Node.js 18+ somente para o teste HTTP opcional.

## Configuração local (PowerShell)

Execute os comandos a partir da raiz do projeto. Defina as variáveis na mesma sessão que iniciará o WildFly:

```powershell
$env:WILDFLY_HOME = 'C:\caminho\wildfly-26.1.3.Final'
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$env:JWT_TTL_SECONDS = '900'
$env:APP_ADMIN_EMAIL = 'admin@exemplo.com'
$env:APP_ADMIN_PASSWORD = Read-Host 'Senha inicial de 12 a 128 caracteres' -AsSecureString | ConvertFrom-SecureString -AsPlainText
$env:DB_HOST = 'localhost'
$env:DB_PORT = '3306'
$env:DB_NAME = 'ejb_jpa_wildfly'
$env:DB_USER = 'ejb_app'
$env:DB_PASSWORD = Read-Host 'Senha do banco' -AsSecureString | ConvertFrom-SecureString -AsPlainText
mvn -B package
mvn -B dependency:copy '-Dartifact=org.mariadb.jdbc:mariadb-java-client:3.3.3' '-DoutputDirectory=target'
& "$env:WILDFLY_HOME\bin\standalone.bat"
```

O exemplo de leitura de senha usa PowerShell 7. Em Windows PowerShell 5.1, use um gerenciador de segredos ou defina as variáveis com o mecanismo de configuração do seu ambiente.

Mantenha a chave JWT estável entre reinícios e compartilhada entre instâncias. Não gere uma nova chave a cada início fora de testes: isso invalida os tokens anteriores. A chave deve decodificar em exatamente 32 bytes; o TTL aceito é de 60 a 3600 segundos. Não há segredo padrão no código.

As credenciais do administrador são usadas apenas para criar o primeiro usuário; não redefinem a senha em deploys seguintes. Após essa criação, remova APP_ADMIN_PASSWORD do ambiente de execução e use as credenciais já persistidas.

A referência de variáveis está em .env.example. Arquivos .env não são carregados automaticamente pelo WildFly.

Com o servidor iniciado, abra outra sessão de PowerShell na raiz do projeto e configure o datasource e a segurança antes de fazer deploy:

```powershell
$env:WILDFLY_HOME = 'C:\caminho\wildfly-26.1.3.Final'
& "$env:WILDFLY_HOME\bin\jboss-cli.bat" --connect --file=config/data-source.cli
& "$env:WILDFLY_HOME\bin\jboss-cli.bat" --connect --file=config/security.cli
& "$env:WILDFLY_HOME\bin\jboss-cli.bat" --connect '--command=deploy target/ejb-jpa-wildfly.war --force'
```

O datasource usa java:/jdbc/ejb-jpa-wildfly, conforme persistence.xml. As expressões DB_* são resolvidas pelo processo do servidor, que precisa receber essas variáveis ao iniciar. Os scripts CLI criam recursos ausentes; não sobrescrevem configurações existentes.

O domínio jwt-app é mapeado para ApplicationDomain no Undertow e no subsistema EJB. integrated-jaspi=false permite que a identidade validada pelo mecanismo da aplicação seja reconhecida pelo WildFly sem exigir usuários em seu realm interno. A autorização continua sendo aplicada pelo container.

## Endpoints

Base local: http://localhost:8080/ejb-jpa-wildfly/api

| Método | Caminho | Acesso |
| --- | --- | --- |
| POST | /auth/login | Público |
| GET | /auth/me | ADMIN ou OPERADOR |
| POST | /usuarios | ADMIN |
| GET | /clientes | ADMIN ou OPERADOR |
| POST | /clientes | ADMIN ou OPERADOR |

Login:

```http
POST /ejb-jpa-wildfly/api/auth/login
Content-Type: application/json

{"email":"admin@exemplo.com","senha":"sua-senha"}
```

Resposta:

```json
{"accessToken":"<jwt>","tokenType":"Bearer","expiresIn":900}
```

Nas chamadas protegidas, envie Authorization: Bearer <jwt>. O token contém sub (ID), roles, iss, aud, iat, exp e jti; não contém senha. O banco é consultado a cada chamada para verificar se o usuário está ativo e obter seus perfis atuais. Mudanças de perfil ou desativação não precisam aguardar a expiração do JWT.

Criação de operador, usando o token de um administrador:

```json
{
  "nome": "Operador",
  "email": "operador@exemplo.com",
  "senha": "uma-senha-com-12-ou-mais-caracteres",
  "perfis": ["OPERADOR"]
}
```

O campo perfis é opcional e tem OPERADOR como padrão. Usuários e hashes não são expostos diretamente: as respostas utilizam DTOs.

Os principais erros são 400 para entrada inválida, 401 para credenciais/token inválidos, 403 para acesso sem permissão, 409 para email já cadastrado e 429 para limite de login. Respostas 401 incluem WWW-Authenticate; 429 inclui Retry-After: 60.

## Swagger e OpenAPI

Depois do deploy, abra **http://localhost:8080/ejb-jpa-wildfly/swagger-ui/**.

1. Abra **Autenticação → POST /auth/login**, clique em **Try it out** e informe o email e a senha do seu usuário. As senhas mostradas nos exemplos são ilustrativas.
2. Clique em **Execute** e copie o campo **accessToken** da resposta.
3. Clique em **Authorize**, cole somente o token (sem o prefixo Bearer) e confirme.
4. Execute **GET /auth/me** ou os endpoints de clientes. Para criar usuários, utilize um token de ADMIN.

A documentação e seus arquivos são públicos; os endpoints de negócio continuam exigindo JWT e seus respectivos perfis. O token informado na UI fica apenas em memória e é removido ao recarregar a página.

Arquivos OpenAPI disponíveis na mesma instalação:

- http://localhost:8080/ejb-jpa-wildfly/openapi/openapi.json
- http://localhost:8080/ejb-jpa-wildfly/openapi/openapi.yaml

O Maven gera esses arquivos a partir das anotações Swagger dos recursos REST e DTOs durante o comando mvn package. O caminho relativo do servidor acompanha o contexto do WAR, sem fixar host ou porta. Os arquivos da interface são incluídos no WAR por WebJar; o navegador não precisa acessar CDN ou enviar a especificação a um validador externo.

Ao adicionar ou modificar um endpoint, atualize suas anotações de operação, schemas, respostas e segurança e gere novamente o WAR. A configuração principal está em RestConfig.java e a interface em src/main/swagger-ui.

## Testes

```powershell
mvn -B package
# Teste completo, com WildFly isolado em target e H2 em memória:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\test-wildfly.ps1
```

O teste isolado baixa o WildFly 26.1.3.Final, usa portas HTTP 9280 e de gerenciamento 11190, cria credenciais aleatórias, configura segurança, faz deploy, verifica Swagger/OpenAPI, testa as chamadas e encerra o processo. Não utiliza suas instalações existentes nem o banco MariaDB. Use -PortOffset para escolher outras portas. Os logs ficam em target/integration.

Para executar somente as verificações HTTP contra uma instalação de desenvolvimento já configurada:

```powershell
$env:AUTH_TEST_BASE_URL = 'http://localhost:8080/ejb-jpa-wildfly/api'
$env:APP_ADMIN_EMAIL = 'admin@exemplo.com'
# Defina APP_ADMIN_PASSWORD com a senha do administrador existente.
node scripts/test-swagger.mjs
node scripts/test-auth.mjs
```

Esse teste cria um operador com email aleatório, que permanece no banco. Execute em uma instalação de desenvolvimento isolada.

## Limites desta etapa

O login é stateless: não cria sessão e exige o token em cada requisição. Para sair, o cliente descarta o token; ele continua válido até expirar. Não há endpoint de logout, lista de revogação ou refresh token nesta etapa.

A API exige HTTPS fora do desenvolvimento local. Não há configuração de CORS para frontend em outra origem. O limitador utiliza o endereço remoto do servidor, sem confiar em X-Forwarded-For; em múltiplas instâncias ou atrás de proxy, a política deve ser aplicada também na infraestrutura.

O schema continua usando hibernate.hbm2ddl.auto=update, herdado do projeto inicial. Migrações, auditoria e um fluxo de negócio transacional são próximas etapas. A integração H2 comprova o fluxo de segurança; a conexão e o comportamento com MariaDB precisam ser verificados no ambiente configurado.

## Referências

- [Jakarta Security / PBKDF2](https://jakarta.ee/specifications/platform/8/apidocs/javax/security/enterprise/identitystore/pbkdf2passwordhash)
- [WildFly: integração Elytron e Jakarta Security](https://docs.wildfly.org/26/WildFly_Elytron_Security.html#elytron-and-jakarta-ee-security)
- [Swagger Core e geração OpenAPI](https://github.com/swagger-api/swagger-core/tree/master/modules/swagger-maven-plugin)
- [Swagger UI](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/)
- [JJWT 0.12.6](https://github.com/jwtk/jjwt/blob/0.12.6/README.adoc)
- [OWASP: armazenamento de senhas](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
