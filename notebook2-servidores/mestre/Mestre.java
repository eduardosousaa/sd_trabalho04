import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Mestre {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/processar", new ProcessarHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Mestre iniciado na porta 8080");
        server.start();
    }

    static class ProcessarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); return;
            }

            InputStream is = exchange.getRequestBody();
            String input = new String(is.readAllBytes());

            final String[] respostaLetras = new String[1];
            final String[] respostaNumeros = new String[1];

            System.out.println("Mestre iniciado na porta 8080");

            System.out.println("[MESTRE] Requisição recebida do cliente.");
            System.out.println("[MESTRE] Texto recebido: " + input);

            Thread t1 = new Thread(() -> {
                try {
                    if (escravoDisponivel("http://escravo-letras:8081/status")) {
                        System.out.println("[MESTRE] Enviando texto para escravo-letras...");
                        respostaLetras[0] = enviarParaEscravo(input, "http://escravo-letras:8081/letras");
                        System.out.println("[MESTRE] Resposta do escravo-letras: " + respostaLetras[0]);
                    } else {
                        System.out.println("[MESTRE] Escravo-letras indisponível.");
                        respostaLetras[0] = "{\"letras\": -1}";
                    }
                } catch (IOException e) {
                    System.out.println("[MESTRE] Falha ao comunicar com escravo-letras.");
                    respostaLetras[0] = "{\"letras\": -1}";
                }
            });
            
            Thread t2 = new Thread(() -> {
                try {
                    if (escravoDisponivel("http://escravo-numeros:8082/status")) {
                        System.out.println("[MESTRE] Enviando texto para escravo-numeros...");
                        respostaNumeros[0] = enviarParaEscravo(input, "http://escravo-numeros:8082/numeros");
                        System.out.println("[MESTRE] Resposta do escravo-numeros: " + respostaNumeros[0]);
                    } else {
                        System.out.println("[MESTRE] Escravo-numeros indisponível.");
                        respostaNumeros[0] = "{\"numeros\": -1}";
                    }
                } catch (IOException e) {
                    System.out.println("[MESTRE] Falha ao comunicar com escravo-numeros.");
                    respostaNumeros[0] = "{\"numeros\": -1}";
                }
            });            

            t1.start();
            t2.start();
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException ignored) {}

            String respostaFinal = "{" + 
                respostaLetras[0].replaceAll("[{}]", "") + "," +
                respostaNumeros[0].replaceAll("[{}]", "") + "}";

            byte[] responseBytes = respostaFinal.getBytes();
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private boolean escravoDisponivel(String url) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(1000); // 1 segundo
                con.connect();
                return con.getResponseCode() == 200;
            } catch (IOException e) {
                return false;
            }
        }        

        private String enviarParaEscravo(String input, String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "text/plain");
            con.getOutputStream().write(input.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            return in.readLine();
        }
    }
}
