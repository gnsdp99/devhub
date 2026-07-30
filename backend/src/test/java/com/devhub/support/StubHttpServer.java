package com.devhub.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public final class StubHttpServer {

    private final HttpServer server;

    private StubHttpServer(HttpServer server) {
        this.server = server;
    }

    public static StubHttpServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        return new StubHttpServer(server);
    }

    public void stop() {
        server.stop(0);
    }

    /**
     * @return 핸들러를 등록한 경로의 URL
     */
    public String serve(String path, HttpHandler handler) {
        server.createContext(path, handler);
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    public static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}