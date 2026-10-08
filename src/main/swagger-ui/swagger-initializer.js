window.addEventListener('load', function () {
  window.ui = SwaggerUIBundle({
    url: new URL('../openapi/openapi.json', window.location.href).href,
    dom_id: '#swagger-ui',
    deepLinking: true,
    persistAuthorization: false,
    validatorUrl: null,
    displayRequestDuration: true,
    defaultModelsExpandDepth: 1,
    presets: [SwaggerUIBundle.presets.apis],
    layout: 'BaseLayout'
  });
});
