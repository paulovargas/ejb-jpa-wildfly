package br.com.exemplo.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(title = "API EJB, JPA e WildFly", version = "1.0.0",
        description = "API de demonstração com autenticação JWT. Faça login e informe o accessToken em Authorize. "
            + "ADMIN pode criar usuários; ADMIN e OPERADOR podem consultar e cadastrar clientes."),
    servers = @Server(url = "../api", description = "API nesta instalação"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
    description = "Informe somente o accessToken retornado pelo login, sem o prefixo Bearer.")
public class RestConfig extends Application {}
