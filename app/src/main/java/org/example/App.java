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
        
        // ВАЖНО: Все отладочные сообщения в stderr
        System.err.println("FastCGI Area Check Server started");
        
        // FastCGI main loop
        while (fcgi.FCGIaccept() >= 0) {
            count++;
            long startTime = System.currentTimeMillis();
            
            try {
                // Получаем параметры из FastCGI environment
                String queryString = System.getProperty("QUERY_STRING", "");
                String requestMethod = System.getProperty("REQUEST_METHOD", "GET");
                String scriptName = System.getProperty("SCRIPT_NAME", "");
                
                // ВАЖНО: Отладочные сообщения в stderr
                System.err.println("FastCGI Request #" + count + ": " + requestMethod + " " + scriptName + "?" + queryString);
                
                Map<String, String> params = new HashMap<>();
                
                // Парсим параметры из QUERY_STRING
                if (!queryString.isEmpty()) {
                    parseQueryString(queryString, params);
                    System.err.println("Parsed parameters: " + params);
                }
                
                // Обрабатываем запрос
                String response = processRequest(params, startTime);
                
                // ВАЖНО: Только HTTP ответ в stdout
                System.out.println("Content-type: text/html; charset=utf-8");
                System.out.println(); // Пустая строка - разделитель заголовков и тела
                System.out.print(response);
                
            } catch (Exception e) {
                // ВАЖНО: Ошибки в stderr
                System.err.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
                
                // HTTP ошибка в stdout
                System.out.println("Content-type: text/html; charset=utf-8");
                System.out.println(); // Пустая строка
                System.out.println("<html><body><h1>Server Error</h1><p>" + e.getMessage() + "</p></body></html>");
            }
        }
    }
    
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
        
        // Если нет параметров, возвращаем HTML форму
        if (params.isEmpty()) {
            return getHtmlForm();
        }
        
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
        
        return createSuccessResponse(currentTime, startTime, result);
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
    
    private static String getHtmlForm() {
        return "<!DOCTYPE html>\n" +
               "<html lang='ru'>\n" +
               "<head>\n" +
               "    <meta charset='UTF-8'>\n" +
               "    <title>Проверка попадания в область</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        h1 { color: #333; text-align: center; }\n" +
               "        .form-group { margin-bottom: 15px; }\n" +
               "        label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }\n" +
               "        input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 16px; }\n" +
               "        button { padding: 12px 30px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }\n" +
               "        button:hover { background-color: #0056b3; }\n" +
               "        .coordinate-system { margin: 20px 0; padding: 15px; background: #f8f9fa; border-radius: 4px; border-left: 4px solid #007bff; }\n" +
               "        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n" +
               "        th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }\n" +
               "        th { background-color: #f8f9fa; font-weight: bold; }\n" +
               "        .hit { background-color: #d4edda; }\n" +
               "        .miss { background-color: #f8d7da; }\n" +
               "        .info { background: #e7f3ff; padding: 15px; border-radius: 4px; margin: 15px 0; }\n" +
               "        .back-link { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #6c757d; color: white; text-decoration: none; border-radius: 4px; }\n" +
               "        .back-link:hover { background: #545b62; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class='container'>\n" +
               "        <h1>Проверка попадания точки в область</h1>\n" +
               "        \n" +
               "        <div class='coordinate-system'>\n" +
               "            <h3>Описание области:</h3>\n" +
               "            <p><strong>1-я четверть:</strong> прямоугольник (0 ≤ x ≤ R/2, 0 ≤ y ≤ R)</p>\n" +
               "            <p><strong>4-я четверть:</strong> треугольник (0 ≤ x ≤ R/2, -R ≤ y ≤ 0, x - y ≤ R/2)</p>\n" +
               "            <p><strong>3-я четверть:</strong> ¼ окружности (x² + y² ≤ (R/2)², x ≤ 0, y ≤ 0)</p>\n" +
               "        </div>\n" +
               "\n" +
               "        <form action='' method='GET'>\n" +
               "            <div class='form-group'>\n" +
               "                <label for='x'>Координата X (-5 до 5):</label>\n" +
               "                <input type='text' id='x' name='x' required placeholder='Например: 1.5'>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div class='form-group'>\n" +
               "                <label for='y'>Координата Y (-5 до 5):</label>\n" +
               "                <input type='text' id='y' name='y' required placeholder='Например: -2.3'>\n" +
               "            </div>\n" +
               "            \n" +
               "            <div class='form-group'>\n" +
               "                <label for='r'>Параметр R (> 0):</label>\n" +
               "                <input type='text' id='r' name='r' required placeholder='Например: 3'>\n" +
               "            </div>\n" +
               "            \n" +
               "            <button type='submit'>Проверить попадание</button>\n" +
               "        </form>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String createSuccessResponse(String currentTime, long startTime, RequestResult currentResult) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang='ru'>\n")
            .append("<head>\n")
            .append("    <meta charset='UTF-8'>\n")
            .append("    <title>Результаты проверки</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n")
            .append("        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n")
            .append("        h1 { color: #333; text-align: center; }\n")
            .append("        .info { background: #e7f3ff; padding: 15px; border-radius: 4px; margin: 15px 0; }\n")
            .append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
            .append("        th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }\n")
            .append("        th { background-color: #f8f9fa; font-weight: bold; }\n")
            .append("        .hit { background-color: #d4edda; }\n")
            .append("        .miss { background-color: #f8d7da; }\n")
            .append("        .current-result { background: #fff3cd; font-weight: bold; }\n")
            .append("        .back-link { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }\n")
            .append("        .back-link:hover { background: #0056b3; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class='container'>\n")
            .append("        <h1>Результаты проверки попадания в область</h1>\n")
            .append("        \n")
            .append("        <div class='info'>\n")
            .append("            <p><strong>Текущее время:</strong> ").append(currentTime).append("</p>\n")
            .append("            <p><strong>Время работы скрипта:</strong> ").append(scriptTime).append(" мс</p>\n")
            .append("            <p><strong>Текущий результат:</strong> Точка (").append(currentResult.x)
            .append(", ").append(currentResult.y).append(") при R=").append(currentResult.r)
            .append(" - <strong>").append(currentResult.hit ? "ПОПАДАНИЕ" : "ПРОМАХ").append("</strong></p>\n")
            .append("        </div>\n")
            .append("        \n")
            .append("        <h2>История запросов</h2>\n")
            .append("        <table>\n")
            .append("            <tr>\n")
            .append("                <th>X</th>\n")
            .append("                <th>Y</th>\n")
            .append("                <th>R</th>\n")
            .append("                <th>Результат</th>\n")
            .append("                <th>Время запроса</th>\n")
            .append("            </tr>\n");
        
        for (int i = history.size() - 1; i >= 0; i--) {
            RequestResult result = history.get(i);
            boolean isCurrent = (result == currentResult);
            String rowClass = result.hit ? "hit" : "miss";
            if (isCurrent) {
                rowClass += " current-result";
            }
            
            html.append("            <tr class='").append(rowClass).append("'>\n")
                .append("                <td>").append(result.x).append("</td>\n")
                .append("                <td>").append(result.y).append("</td>\n")
                .append("                <td>").append(result.r).append("</td>\n")
                .append("                <td><strong>").append(result.hit ? "ПОПАДАНИЕ" : "ПРОМАХ").append("</strong></td>\n")
                .append("                <td>").append(result.timestamp).append("</td>\n")
                .append("            </tr>\n");
        }
        
        html.append("        </table>\n")
            .append("        \n")
            .append("        <a href='' class='back-link'>Новая проверка</a>\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
        
        return html.toString();
    }
    
    private static String createErrorResponse(String message, String currentTime, long startTime) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        return "<!DOCTYPE html>\n" +
               "<html lang='ru'>\n" +
               "<head>\n" +
               "    <meta charset='UTF-8'>\n" +
               "    <title>Ошибка</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        h1 { color: #dc3545; }\n" +
               "        .error { background: #f8d7da; color: #721c24; padding: 15px; border-radius: 4px; margin: 15px 0; }\n" +
               "        .back-link { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class='container'>\n" +
               "        <h1>Ошибка</h1>\n" +
               "        <div class='error'>\n" +
               "            <p>" + message + "</p>\n" +
               "        </div>\n" +
               "        <p><strong>Текущее время:</strong> " + currentTime + "</p>\n" +
               "        <p><strong>Время работы скрипта:</strong> " + scriptTime + " мс</p>\n" +
               "        <a href='' class='back-link'>Вернуться к форме</a>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
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