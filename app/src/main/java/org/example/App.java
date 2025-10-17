package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class App {
    private static List<RequestResult> history = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("FCGI_PORT", "1337"));
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FastCGI Server started on port " + port);
            
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
    long startTime = System.currentTimeMillis();
    try {
        System.out.println("New connection received");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();
        
        // Читаем первую строку запроса
        String requestLine = in.readLine();
        if (requestLine == null) {
            clientSocket.close();
            return;
        }
        
        System.out.println("Request line: " + requestLine);
        
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            clientSocket.close();
            return;
        }
        
        String method = requestParts[0];
        String path = requestParts[1];
        
        System.out.println("Method: " + method + ", Path: " + path);
        
        // Пропускаем остальные заголовки
        String header;
        while ((header = in.readLine()) != null && !header.isEmpty()) {
            // Просто читаем заголовки до пустой строки
        }
        
        Map<String, String> params = new HashMap<>();
        String response;
        
       if (path.startsWith("/") && path.contains("?")) {
    // Парсинг параметров из URL
    String query = path.split("\\?")[1];
    parseQueryString(query, params);
    System.out.println("Parsed parameters: " + params);
    
    // Обработка запроса проверки попадания
    response = processRequest(params, startTime);
    
} else {
    // Для всех других путей возвращаем статическую HTML форму
    response = getStaticHtmlForm();
}
        
        // Отправка HTTP ответа
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n" +
                            "Content-Length: " + response.getBytes().length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" +
                            response;
        
        out.write(httpResponse.getBytes());
        out.flush();
        System.out.println("Response sent successfully");
        
    } catch (Exception e) {
        System.err.println("Error handling request: " + e.getMessage());
        e.printStackTrace();
        try {
            String errorResponse = "HTTP/1.1 500 Internal Server Error\r\n\r\nServer Error";
            clientSocket.getOutputStream().write(errorResponse.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Метод для возврата статической HTML формы
private static String getStaticHtmlForm() {
    try {
        // Читаем файл index.html из ресурсов или возвращаем строку
        return "<!DOCTYPE html>" +
               "<html lang='ru'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<title>Проверка попадания в область</title>" +
               "<style>" +
               ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               ".form-group { margin-bottom: 15px; }" +
               "label { display: block; margin-bottom: 5px; }" +
               "input, select { width: 100%; padding: 8px; }" +
               "button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; }" +
               "button:hover { background-color: #0056b3; }" +
               ".coordinate-system { margin: 20px 0; text-align: center; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<h1>Проверка попадания точки в область</h1>" +
               "<div class='coordinate-system'>" +
               "<p><strong>Область:</strong> 1-я четверть (прямоугольник), 4-я четверть (треугольник), 3-я четверть (1/4 окружности)</p>" +
               "</div>" +
               "<form action='/check' method='GET'>" +
               "<div class='form-group'>" +
               "<label for='x'>Координата X (-5 до 5):</label>" +
               "<input type='text' id='x' name='x' required placeholder='Например: 1.5'>" +
               "</div>" +
               "<div class='form-group'>" +
               "<label for='y'>Координата Y (-5 до 5):</label>" +
               "<input type='text' id='y' name='y' required placeholder='Например: -2.3'>" +
               "</div>" +
               "<div class='form-group'>" +
               "<label for='r'>Параметр R (> 0):</label>" +
               "<input type='text' id='r' name='r' required placeholder='Например: 3'>" +
               "</div>" +
               "<button type='submit'>Проверить попадание</button>" +
               "</form>" +
               "</div>" +
               "</body>" +
               "</html>";
    } catch (Exception e) {
        return "<html><body><h1>Error loading form</h1></body></html>";
    }
}

// // Добавьте этот метод для отображения формы
// private static String getHtmlForm() {
//     // Возвращает содержимое вашего index.html
//     return "<!DOCTYPE html>" +
//            "<html>" +
//            "<head><title>Area Check</title></head>" +
//            "<body>" +
//            "<h1>Проверка попадания в область</h1>" +
//            "<form action='/check' method='GET'>" +
//            "X: <input type='text' name='x'><br>" +
//            "Y: <input type='text' name='y'><br>" +
//            "R: <input type='text' name='r'><br>" +
//            "<input type='submit' value='Проверить'>" +
//            "</form>" +
//            "</body></html>";
// }
    
    private static void parseQueryString(String query, Map<String, String> params) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
    
    private static String processRequest(Map<String, String> params, long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        
        // Валидация параметров
        ValidationResult validation = validateParameters(params);
        if (!validation.isValid) {
            return createErrorResponse(validation.message, currentTime, startTime);
        }
        
        double x = Double.parseDouble(params.get("x"));
        double y = Double.parseDouble(params.get("y"));
        double r = Double.parseDouble(params.get("r"));
        
        // Проверка попадания в область
        boolean hit = checkHit(x, y, r);
        
        // Сохранение результата
        RequestResult result = new RequestResult(x, y, r, hit, currentTime);
        history.add(result);
        
        // Ограничение истории (последние 20 запросов)
        if (history.size() > 20) {
            history.remove(0);
        }
        
        return createSuccessResponse(currentTime, startTime);
    }
    
    private static ValidationResult validateParameters(Map<String, String> params) {
        if (!params.containsKey("x") || !params.containsKey("y") || !params.containsKey("r")) {
            return new ValidationResult(false, "Missing required parameters: x, y, r");
        }
        
        try {
            double x = Double.parseDouble(params.get("x"));
            double y = Double.parseDouble(params.get("y"));
            double r = Double.parseDouble(params.get("r"));
            
            if (r <= 0) {
                return new ValidationResult(false, "R must be positive");
            }
            
            // Проверка допустимых значений (можно настроить по заданию)
            if (x < -5 || x > 5 || y < -5 || y > 5) {
                return new ValidationResult(false, "X and Y must be between -5 and 5");
            }
            
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Invalid number format");
        }
        
        return new ValidationResult(true, "");
    }
    
    private static boolean checkHit(double x, double y, double r) {
        // Проверка попадания в 1-ю четверть (прямоугольник)
        if (x >= 0 && y >= 0 && x <= r/2 && y <= r) {
            return true;
        }
        
        // Проверка попадания в 4-ю четверть (треугольник)
        if (x >= 0 && y <= 0 && x <= r/2 && y >= -r && (x + Math.abs(y)) <= r/2) {
            return true;
        }
        
        // Проверка попадания в 3-ю четверть (окружность 1/4)
        if (x <= 0 && y <= 0 && (x*x + y*y) <= (r/2)*(r/2)) {
            return true;
        }
        
        return false;
    }
    
    private static String createSuccessResponse(String currentTime, long startTime) {
    long scriptTime = System.currentTimeMillis() - startTime;
    
    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>")
        .append("<html lang='ru'>")
        .append("<head>")
        .append("<meta charset='UTF-8'>")
        .append("<title>Area Check Result</title>")
        .append("<style>")
        .append("table { border-collapse: collapse; width: 100%; }")
        .append("th, td { border: 1px solid black; padding: 8px; text-align: center; }")
        .append("th { background-color: #f2f2f2; }")
        .append(".hit { background-color: #90EE90; }")
        .append(".miss { background-color: #FFB6C1; }")
        .append("</style>")
        .append("</head>")
        .append("<body>")
        .append("<h1>Результаты проверки попадания в область</h1>")
        .append("<p><strong>Текущее время:</strong> ").append(currentTime).append("</p>")
        .append("<p><strong>Время работы скрипта:</strong> ").append(scriptTime).append(" мс</p>")
        .append("<table>")
        .append("<tr><th>X</th><th>Y</th><th>R</th><th>Результат</th><th>Время запроса</th></tr>");
    
    for (int i = history.size() - 1; i >= 0; i--) {
        RequestResult result = history.get(i);
        html.append("<tr class='").append(result.hit ? "hit" : "miss").append("'>")
            .append("<td>").append(result.x).append("</td>")
            .append("<td>").append(result.y).append("</td>")
            .append("<td>").append(result.r).append("</td>")
            .append("<td>").append(result.hit ? "Попадание" : "Промах").append("</td>")
            .append("<td>").append(result.timestamp).append("</td>")
            .append("</tr>");
    }
    
    html.append("</table>")
        .append("<br>")
        .append("<a href='/'>Новая проверка</a>")  // ← ИЗМЕНИЛИ ЗДЕСЬ
        .append("</body>")
        .append("</html>");
    
    return html.toString();
}
    
private static String createErrorResponse(String message, String currentTime, long startTime) {
    long scriptTime = System.currentTimeMillis() - startTime;
    
    return "<!DOCTYPE html>" +
           "<html lang='ru'>" +
           "<head><meta charset='UTF-8'><title>Error</title></head>" +
           "<body>" +
           "<h1>Ошибка</h1>" +
           "<p>" + message + "</p>" +
           "<p><strong>Текущее время:</strong> " + currentTime + "</p>" +
           "<p><strong>Время работы скрипта:</strong> " + scriptTime + " мс</p>" +
           "<a href='/'>Вернуться</a>" +  // ← ИЗМЕНИЛИ ЗДЕСЬ
           "</body></html>";
}
    
    // Вспомогательные классы
    static class RequestResult {
        double x, y, r;
        boolean hit;
        String timestamp;
        
        RequestResult(double x, double y, double r, boolean hit, String timestamp) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.hit = hit;
            this.timestamp = timestamp;
        }
    }
    
    static class ValidationResult {
        boolean isValid;
        String message;
        
        ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
    
    // Метод для совместимости с тестами
    public String getGreeting() {
        return "FastCGI Area Check Server";
    }
}