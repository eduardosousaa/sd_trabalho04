import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class EscravoLetras {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/letras", new LetrasHandler());

        // Endpoint /status para o mestre verificar disponibilidade
        server.createContext("/status", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        // garante que ele possa lidar com múltiplas requisições simultaneamente
        server.setExecutor(Executors.newFixedThreadPool(2));
        System.out.println("Escravo Letras rodando na porta 8081");
        server.start();
    }

    static class LetrasHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); return;
            }

            System.out.println("[ESCRAVO-LETRAS] Requisição recebida.");

            String input = new String(exchange.getRequestBody().readAllBytes());
            long count = input.chars().filter(Character::isLetter).count();

            String response = "{\"letras\": " + count + "}";
            byte[] bytes = response.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
