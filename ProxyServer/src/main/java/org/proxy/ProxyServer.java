package org.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {

    // Target base URL to which we will forward the requests
    private static final String TARGET_BASE_URL = "http://localhost";
    public static void main(String[] args) throws Exception {
        // Create an HTTP server that listens on port 3001
        HttpServer server = HttpServer.create(new InetSocketAddress(3005), 0);
        System.out.println("Starting proxy server on port 3005");
        server.setExecutor(null);  // Attach executor to the server
        // Define a context for the server and set the request handler
        server.createContext("/", new ProxyHandler());
        server.start();
    }
}
