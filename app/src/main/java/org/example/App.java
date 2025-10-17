package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class App {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("FCGI_PORT", "1337"));
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FCGI Server started on port " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try {
            String response = "Content-type: text/html\r\n\r\n" +
                            "<html><body><h1>Hello from FCGI!</h1></body></html>";
            
            clientSocket.getOutputStream().write(response.getBytes());
            clientSocket.getOutputStream().flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Этот метод оставьте для совместимости с тестами
    public String getGreeting() {
        return "Hello World!";
    }
}