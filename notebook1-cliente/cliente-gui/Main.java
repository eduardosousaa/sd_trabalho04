import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cliente - Enviar Texto");
        JButton button = new JButton("Selecionar Arquivo");

        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    byte[] content = Files.readAllBytes(selectedFile.toPath());
                    String response = enviarParaServidor(content);
                    JOptionPane.showMessageDialog(frame, "Resposta do servidor:\n" + response);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Erro ao ler/enviar arquivo");
                }
            }
        });

        frame.add(button);
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static String enviarParaServidor(byte[] content) throws IOException {
        // URL url = new URL("http://<IP_DO_NOTEBOOK_2>:8080/processar");
        URL url = new URL("http://localhost:8080/processar");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "text/plain");
        con.getOutputStream().write(content);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null)
            response.append(line);
        in.close();
        return response.toString();
    }
}
