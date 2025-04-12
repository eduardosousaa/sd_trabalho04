import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class EscravoNumeros {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/numeros", new NumerosHandler());
        server.createContext("/status", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        // garante que ele possa lidar com múltiplas requisições simultaneamente
        server.setExecutor(Executors.newFixedThreadPool(2));
        System.out.println("Escravo Números rodando na porta 8082");
        server.start();
    }

    static class NumerosHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); return;
            }

            System.out.println("[ESCRAVO-NUMEROS] Requisição recebida.");

            String input = new String(exchange.getRequestBody().readAllBytes());
            long count = input.chars().filter(Character::isDigit).count();

            String response = "{\"numeros\": " + count + "}";
            byte[] bytes = response.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
