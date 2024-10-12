package org.proxy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class RouteUtil {
    private static List<Route> routes;
    public static Optional<Route> getRoute(String url) throws IOException {
        if (routes == null) {
            routes = loadFromResource();
        }
        System.out.println(url);
        return routes.stream().filter(x -> url.contains(x.getKeyword())).findFirst();
    }

    private static List<Route> loadFromResource() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("route.json");
        assert inputStream != null;
        return objectMapper.readValue(inputStream, new TypeReference<List<Route>>() {
        });
    }
}
