import assert from 'node:assert/strict';

const apiUrl = process.env.AUTH_TEST_BASE_URL ?? 'http://localhost:8080/ejb-jpa-wildfly/api';
const appUrl = apiUrl.replace(/\/api\/?$/, '/');
const uiResponse = await fetch(new URL('swagger-ui/', appUrl));
assert.equal(uiResponse.status, 200, 'Swagger UI deve abrir sem token.');
const html = await uiResponse.text();
assert.ok(html.includes('id="swagger-ui"'));
assert.ok(!html.includes('${swagger-ui.version}'), 'Versão da UI deve ser resolvida no build.');
for (const [, path] of html.matchAll(/(?:src|href)="([^"]+)"/g)) {
  const response = await fetch(new URL(path, uiResponse.url));
  assert.equal(response.status, 200, 'Recurso Swagger: ' + path);
}
const specUrl = new URL('openapi/openapi.json', appUrl);
const specResponse = await fetch(specUrl);
assert.equal(specResponse.status, 200, 'OpenAPI JSON deve abrir sem token.');
const spec = await specResponse.json();
assert.ok(spec.openapi.startsWith('3.'));
assert.equal(spec.info.title, 'API EJB, JPA e WildFly');
assert.equal(new URL(spec.servers[0].url, specUrl).href, apiUrl.replace(/\/$/, ''));
assert.equal(spec.components.securitySchemes.bearerAuth.type, 'http');
assert.equal(spec.components.securitySchemes.bearerAuth.scheme, 'bearer');
assert.ok(!spec.security?.length, 'Não deve haver segurança global aplicada ao login.');
const login = spec.paths['/auth/login'].post;
assert.ok(!login.security?.length, 'Login deve ser público na documentação.');
assert.ok(login.requestBody.required);
assert.equal(login.responses['200'].content['application/json'].schema.$ref, '#/components/schemas/LoginResponse');
assert.ok(login.responses['429']);
for (const [path, method] of [['/auth/me', 'get'], ['/usuarios', 'post'], ['/clientes', 'get'], ['/clientes', 'post']]) {
  assert.deepEqual(spec.paths[path][method].security, [{ bearerAuth: [] }], 'Segurança de ' + method + ' ' + path);
}
assert.ok(spec.paths['/usuarios'].post.responses['201']);
assert.ok(spec.paths['/usuarios'].post.responses['403']);
assert.ok(spec.paths['/clientes'].post.responses['204']);
assert.equal(spec.components.schemas.LoginRequest.properties.senha.writeOnly, true);
assert.equal(spec.components.schemas.CriarUsuarioRequest.properties.senha.format, 'password');
assert.ok(!('senhaHash' in spec.components.schemas.UsuarioResponse.properties));
const yamlResponse = await fetch(new URL('openapi/openapi.yaml', appUrl));
assert.equal(yamlResponse.status, 200);
console.log('OK: Swagger UI e assets locais, OpenAPI JSON/YAML, rotas, respostas, login público e esquema JWT.');
