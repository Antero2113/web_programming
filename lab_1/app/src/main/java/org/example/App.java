package org.example;

import com.fastcgi.FCGIInterface;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class App {
    private static List<RequestResult> history = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
        int count = 0;
        FCGIInterface fcgi = new FCGIInterface();
        
        // FastCGI main loop
        while (fcgi.FCGIaccept() >= 0) {
            count++;
            long startTime = System.currentTimeMillis();
            
            try {
                // Получаем параметры из FastCGI environment
                String requestMethod = System.getProperty("REQUEST_METHOD", "GET");
                String scriptName = System.getProperty("SCRIPT_NAME", "");
                String acceptHeader = System.getProperty("HTTP_ACCEPT", "");
                String contentType = System.getProperty("CONTENT_TYPE", "");
                
                Map<String, String> params = getRequestParameters(requestMethod, contentType);
                
                // Определяем тип ответа (JSON или HTML)
                boolean wantsJson = acceptHeader.contains("application/json") || 
                                   scriptName.contains("/api/") ||
                                   scriptName.contains("/check");
                
                // Обрабатываем запрос
                String response = processRequest(params, startTime, wantsJson);
                
                // Отправляем ответ
                if (wantsJson) {
                    System.out.println("Content-type: application/json; charset=utf-8");
                } else {
                    System.out.println("Content-type: text/html; charset=utf-8");
                }
                System.out.println();
                System.out.print(response);
                
            } catch (Exception e) {
                // Отправляем ошибку в JSON формате
                System.out.println("Content-type: application/json; charset=utf-8");
                System.out.println();
                String errorJson = "{\"error\": true, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                System.out.print(errorJson);
            }
        }
    }
    
    private static Map<String, String> getRequestParameters(String requestMethod, String contentType) throws IOException {
        Map<String, String> params = new HashMap<>();
        
        if ("GET".equals(requestMethod)) {
            String queryString = System.getProperty("QUERY_STRING", "");
            parseQueryString(queryString, params);
        } else if ("POST".equals(requestMethod)) {
            String contentLengthStr = System.getProperty("CONTENT_LENGTH", "0");
            int contentLength = Integer.parseInt(contentLengthStr);
            
            if (contentLength > 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                char[] body = new char[contentLength];
                int bytesRead = reader.read(body, 0, contentLength);
                String postData = new String(body, 0, bytesRead);
                parseQueryString(postData, params);
            }
        }
        
        return params;
    }
    
    private static void parseQueryString(String query, Map<String, String> params) {
        if (query == null || query.isEmpty()) {
            return;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 1) {
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
                try {
                    String decodedValue = java.net.URLDecoder.decode(value, "UTF-8");
                    params.put(key, decodedValue);
                } catch (UnsupportedEncodingException e) {
                    params.put(key, value);
                }
            }
        }
    }
    
    private static String processRequest(Map<String, String> params, long startTime, boolean wantsJson) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        
        if (params.isEmpty()) {
            if (wantsJson) {
                return "{\"error\": true, \"message\": \"No parameters provided\"}";
            } else {
                return createErrorResponse("No parameters provided", currentTime, startTime);
            }
        }
        
        ValidationResult validation = validateParameters(params);
        if (!validation.isValid) {
            if (wantsJson) {
                return createJsonErrorResponse(validation.message, currentTime, startTime);
            } else {
                return createErrorResponse(validation.message, currentTime, startTime);
            }
        }
        
        double x = Double.parseDouble(params.get("x"));
        double y = Double.parseDouble(params.get("y"));
        double r = Double.parseDouble(params.get("r"));
        
        boolean hit = checkHit(x, y, r);
        
        RequestResult result = new RequestResult(x, y, r, hit, currentTime);
        history.add(result);
        
        if (history.size() > 20) {
            history.remove(0);
        }
        
        if (wantsJson) {
            return createJsonResponse(currentTime, startTime, result);
        } else {
            return createSuccessResponse(currentTime, startTime, result);
        }
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
            
            if (x < -5 || x > 5 || y < -5 || y > 5) {
                return new ValidationResult(false, "X and Y must be between -5 and 5");
            }
            
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Invalid number format");
        }
        
        return new ValidationResult(true, "");
    }
    
    private static boolean checkHit(double x, double y, double r) {
        if (x >= 0 && y >= 0) {
            return false;
        }
        
        if (x < 0 && y >= 0) {
            return (x >= -r) && (y <= r/2);
        }
        
        if (x < 0 && y < 0) {
            return (x*x + y*y) <= r*r;
        }
        
        if (x >= 0 && y < 0) {
            return (x <= r) && (y >= -r) && (x - y <= r);
        }
        
        return false;
    }
    
    private static String createJsonResponse(String currentTime, long startTime, RequestResult currentResult) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"error\": false,\n")
            .append("  \"currentTime\": \"").append(currentTime).append("\",\n")
            .append("  \"scriptTime\": ").append(scriptTime).append(",\n")
            .append("  \"currentResult\": {\n")
            .append("    \"x\": ").append(currentResult.x).append(",\n")
            .append("    \"y\": ").append(currentResult.y).append(",\n")
            .append("    \"r\": ").append(currentResult.r).append(",\n")
            .append("    \"hit\": ").append(currentResult.hit).append(",\n")
            .append("    \"timestamp\": \"").append(currentResult.timestamp).append("\"\n")
            .append("  },\n")
            .append("  \"history\": [\n");
        
        for (int i = history.size() - 1; i >= 0; i--) {
            RequestResult result = history.get(i);
            json.append("    {\n")
                .append("      \"x\": ").append(result.x).append(",\n")
                .append("      \"y\": ").append(result.y).append(",\n")
                .append("      \"r\": ").append(result.r).append(",\n")
                .append("      \"hit\": ").append(result.hit).append(",\n")
                .append("      \"timestamp\": \"").append(result.timestamp).append("\"\n")
                .append("    }");
            if (i > 0) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n")
            .append("}");
        
        return json.toString();
    }
    
    private static String createJsonErrorResponse(String message, String currentTime, long startTime) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        return "{\n" +
               "  \"error\": true,\n" +
               "  \"message\": \"" + message.replace("\"", "\\\"") + "\",\n" +
               "  \"currentTime\": \"" + currentTime + "\",\n" +
               "  \"scriptTime\": " + scriptTime + "\n" +
               "}";
    }
    
    private static String createSuccessResponse(String currentTime, long startTime, RequestResult currentResult) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html><head><title>Результат</title></head><body>\n")
            .append("<h1>Результаты проверки</h1>\n")
            .append("<p><strong>Текущее время:</strong> ").append(currentTime).append("</p>\n")
            .append("<p><strong>Время работы скрипта:</strong> ").append(scriptTime).append(" мс</p>\n")
            .append("<p><strong>Результат:</strong> ").append(currentResult.hit ? "ПОПАДАНИЕ" : "ПРОМАХ").append("</p>\n")
            .append("<h2>История запросов</h2>\n")
            .append("<table border='1'>\n")
            .append("<tr><th>X</th><th>Y</th><th>R</th><th>Результат</th><th>Время</th></tr>\n");
        
        for (int i = history.size() - 1; i >= 0; i--) {
            RequestResult result = history.get(i);
            html.append("<tr>")
                .append("<td>").append(result.x).append("</td>")
                .append("<td>").append(result.y).append("</td>")
                .append("<td>").append(result.r).append("</td>")
                .append("<td>").append(result.hit ? "ПОПАДАНИЕ" : "ПРОМАХ").append("</td>")
                .append("<td>").append(result.timestamp).append("</td>")
                .append("</tr>\n");
        }
        
        html.append("</table>\n")
            .append("</body></html>");
        
        return html.toString();
    }
    
    private static String createErrorResponse(String message, String currentTime, long startTime) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        return "<!DOCTYPE html>\n" +
               "<html><body>\n" +
               "<h1>Ошибка</h1>\n" +
               "<p>" + message + "</p>\n" +
               "<p><strong>Текущее время:</strong> " + currentTime + "</p>\n" +
               "<p><strong>Время работы скрипта:</strong> " + scriptTime + " мс</p>\n" +
               "</body></html>";
    }
    
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
}