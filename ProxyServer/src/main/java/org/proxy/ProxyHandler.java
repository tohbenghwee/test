package org.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ProxyHandler implements HttpHandler {
    private static final String TARGET_BASE_URL = "localhost";
    private static final String TARGET_SCHEME = "http";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        executorService.submit(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String originalUrl = exchange.getRequestURI().toString();
                Optional<Route> route = RouteUtil.getRoute(originalUrl);
                if(route.isEmpty()) {
                    route = Optional.of(new Route("","", 8000, ""));
                }
                System.out.println(route.get());
                String targetUrl = originalUrl.replace("api/v1",route.get().getReplace());
                System.out.println("Proxying request to: " + targetUrl);
                // Create the appropriate request based on the HTTP method
                HttpRequestBase httpRequest = createHttpRequest(exchange, targetUrl);
                // Optionally set headers (can customize as needed)
                httpRequest.setHeader("x-custom-header", "myCustomHeaderValue");
                httpRequest.setHeader("Authorization", "Bearer my-token");
                // Forward 'x-forwarded-for' if present
                String forwardedFor = exchange.getRequestHeaders().getFirst("x-forwarded-for");
                if (forwardedFor != null) {
                    httpRequest.setHeader("x-forwarded-for", forwardedFor);
                }
                HttpHost target = new HttpHost(TARGET_BASE_URL, route.get().getPort(), TARGET_SCHEME);
                // Execute the request
                HttpResponse targetResponse = httpClient.execute(target, httpRequest);
                String responseBody = EntityUtils.toString(targetResponse.getEntity());
                // Forward the response status and headers to the client
                String contentType = targetResponse.getEntity().getContentType().getValue();
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(targetResponse.getStatusLine().getStatusCode(), responseBody.length());
                // Send the response body back to the client
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write("Internal Server Error".getBytes());
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    // Helper method to create the appropriate HttpRequest based on the incoming HTTP method
    private HttpRequestBase createHttpRequest(HttpExchange exchange, String targetUrl) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase(); // Get the HTTP method
        HttpRequestBase request;

        switch (method) {
            case "POST":
                HttpPost postRequest = new HttpPost(targetUrl);
                postRequest.setEntity(new InputStreamEntity(exchange.getRequestBody())); // Forward request body
                request = postRequest;
                break;
            case "PUT":
                HttpPut putRequest = new HttpPut(targetUrl);
                putRequest.setEntity(new InputStreamEntity(exchange.getRequestBody())); // Forward request body
                request = putRequest;
                break;
            case "DELETE":
                request = new HttpDelete(targetUrl);
                break;
            case "HEAD":
                request = new HttpHead(targetUrl);
                break;
            case "OPTIONS":
                request = new HttpOptions(targetUrl);
                break;
            case "PATCH":
                HttpPatch patchRequest = new HttpPatch(targetUrl);
                patchRequest.setEntity(new InputStreamEntity(exchange.getRequestBody())); // Forward request body
                request = patchRequest;
                break;
            case "GET":
            default:
                // Default method is GET if no match
                request = new HttpGet(targetUrl);
                break;
        }

        return request;
    }
}
