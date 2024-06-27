/**
 * NOTE(2024.06.27): 기본값에서 서버에서 제공하는 파일 URL로 변경하였다.
 */
const OPENAPI_FILE_URL = "/swagger-ui/openapi3.yaml";

window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: OPENAPI_FILE_URL,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
