import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';

const baseUrl = process.env.AUTH_TEST_BASE_URL ?? 'http://localhost:8080/ejb-jpa-wildfly/api';
const email = process.env.APP_ADMIN_EMAIL;
const senha = process.env.APP_ADMIN_PASSWORD;
assert.ok(email && senha, 'Configure APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD para testar.');

async function request(path, { method = 'GET', body, token, status = 200 } = {}) {
  const response = await fetch(baseUrl + path, {
    method,
    headers: {
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: 'Bearer ' + token } : {})
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  assert.equal(response.status, status, method + ' ' + path);
  const text = await response.text();
  return { response, data: text && response.headers.get('content-type')?.includes('application/json') ? JSON.parse(text) : text };
}

await request('/clientes', { status: 401 });
await request('/auth/me', { status: 401 });
await request('/auth/me', { token: 'a.b.c', status: 401 });
await request('/auth/login', { method: 'POST', body: { email, senha: 'senha-incorreta' }, status: 401 });
await request('/auth/login', { method: 'POST', body: { email: 'invalido', senha }, status: 400 });
const login = await request('/auth/login', { method: 'POST', body: { email, senha } });
assert.equal(login.data.tokenType, 'Bearer');
assert.ok(login.data.expiresIn > 0);
assert.equal(login.response.headers.get('cache-control'), 'no-store');
assert.equal(login.response.headers.get('set-cookie'), null, 'Login JWT não deve criar sessão.');
const token = login.data.accessToken;
const me = await request('/auth/me', { token });
assert.ok(me.data.id && me.data.perfis.includes('ADMIN'), 'EJB deve reconhecer o administrador.');
assert.ok(!('senha' in me.data) && !('senhaHash' in me.data));
await request('/clientes', { token });
await request('/auth/me', { status: 401 }); // O request anterior não deve autenticar o próximo.
const [header, payload, signature] = token.split('.');
const claims = JSON.parse(Buffer.from(payload, 'base64url').toString());
claims.sub = '999999';
const tampered = header + '.' + Buffer.from(JSON.stringify(claims)).toString('base64url') + '.' + signature;
await request('/auth/me', { token: tampered, status: 401 });

// Cria um operador para provar que seus privilégios diferem dos do administrador.
const operador = { nome: 'Operador de teste', email: 'teste-' + randomUUID() + '@exemplo.com', senha: randomUUID(), perfis: ['OPERADOR'] };
const novo = await request('/usuarios', { method: 'POST', body: operador, token, status: 201 });
assert.ok(novo.data.id && !('senhaHash' in novo.data));
await request('/usuarios', { method: 'POST', body: operador, token, status: 409 });
const loginOperador = await request('/auth/login', { method: 'POST', body: operador });
await request('/auth/me', { token: loginOperador.data.accessToken });
await request('/clientes', { token: loginOperador.data.accessToken });
const cliente = { nome: 'Cliente de teste', email: 'cliente-' + randomUUID() + '@exemplo.com' };
await request('/clientes', { method: 'POST', body: cliente, token: loginOperador.data.accessToken, status: 204 });
const clientes = await request('/clientes', { token: loginOperador.data.accessToken });
assert.ok(clientes.data.some(item => item.email === cliente.email), 'Operador deve conseguir persistir e consultar clientes.');
await request('/usuarios', { method: 'POST', body: operador, token: loginOperador.data.accessToken, status: 403 });
let limitado = false;
for (let i = 0; i < 11; i++) {
  const response = await fetch(baseUrl + '/auth/login', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, senha: 'senha-incorreta' })
  });
  if (response.status === 429) {
    assert.equal(response.headers.get('retry-after'), '60');
    limitado = true;
    break;
  }
  assert.equal(response.status, 401);
}
assert.ok(limitado, 'Login deve limitar tentativas.');
console.log('OK: login, validação, JWT adulterado, ausência de sessão, identidade EJB, persistência, permissões ADMIN/OPERADOR e limite de tentativas.');
console.log('O usuário e o cliente de teste criados permanecem no banco; execute em um ambiente de desenvolvimento isolado.');
