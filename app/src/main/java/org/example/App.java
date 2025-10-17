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
                String acceptHeader = System.getProperty("HTTP_ACCEPT", "");
                
                System.err.println("FastCGI Request #" + count + ": " + requestMethod + " " + scriptName + "?" + queryString);
                
                Map<String, String> params = new HashMap<>();
                
                // Парсим параметры из QUERY_STRING
                if (!queryString.isEmpty()) {
                    parseQueryString(queryString, params);
                    System.err.println("Parsed parameters: " + params);
                }
                
                // Определяем тип ответа (JSON или HTML)
                boolean wantsJson = acceptHeader.contains("application/json") || 
                                   scriptName.contains("/api/");
                
                // Обрабатываем запрос
                String response = processRequest(params, startTime, wantsJson);
                
                // Отправляем ответ
                if (wantsJson) {
                    System.out.println("Content-type: application/json; charset=utf-8");
                } else {
                    System.out.println("Content-type: text/html; charset=utf-8");
                }
                System.out.println(); // Пустая строка - разделитель
                System.out.print(response);
                
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
                
                // Отправляем ошибку в JSON формате
                System.out.println("Content-type: application/json; charset=utf-8");
                System.out.println();
                String errorJson = "{\"error\": true, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                System.out.print(errorJson);
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
    
    private static String processRequest(Map<String, String> params, long startTime, boolean wantsJson) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        
        // Если нет параметров, возвращаем HTML форму
        if (params.isEmpty() && !wantsJson) {
            return getHtmlForm();
        } else if (params.isEmpty() && wantsJson) {
            return "{\"error\": true, \"message\": \"No parameters provided\"}";
        }
        
        // Валидация параметров
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
        
        // Проверка попадания в область
        boolean hit = checkHit(x, y, r);
        
        // Сохранение результата
        RequestResult result = new RequestResult(x, y, r, hit, currentTime);
        history.add(result);
        
        // Ограничение истории (последние 20 запросов)
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
    // 1-ая четверть: мимо (x >= 0, y >= 0)
    if (x >= 0 && y >= 0) {
        return false;
    }
    
    // 2-ая четверть: по x от -R до 0, по y от 0 до R/2 (x < 0, y >= 0)
    if (x < 0 && y >= 0) {
        return (x >= -r) && (y <= r/2);
    }
    
    // 3-я четверть: сектор круга радиусом R (x < 0, y < 0)
    if (x < 0 && y < 0) {
        return (x*x + y*y) <= r*r;
    }
    
    // 4-ая четверть: треугольник с катетами R (x >= 0, y < 0)
    if (x >= 0 && y < 0) {
        return (x <= r) && (y >= -r) && (x - y <= r);
    }
    
    return false;
    }
    
    private static String getHtmlForm() {
        // Упрощенная HTML форма для статического обслуживания
        return "<!DOCTYPE html>\n" +
               "<html lang='ru'>\n" +
               "<head>\n" +
               "    <meta charset='UTF-8'>\n" +
               "    <title>Проверка попадания в область</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
               "        .form-group { margin-bottom: 15px; }\n" +
               "        label { display: block; margin-bottom: 5px; font-weight: bold; }\n" +
               "        input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }\n" +
               "        button { padding: 12px 30px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }\n" +
               "        button:hover { background-color: #0056b3; }\n" +
               "        #result { margin-top: 20px; }\n" +
               "        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n" +
               "        th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }\n" +
               "        th { background-color: #f8f9fa; }\n" +
               "        .hit { background-color: #d4edda; }\n" +
               "        .miss { background-color: #f8d7da; }\n" +
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
               "        <form id='checkForm'>\n" +
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
               "        \n" +
               "        <div id='result'></div>\n" +
               "    </div>\n" +
               "    \n" +
               "    <script>\n" +
               "        document.getElementById('checkForm').addEventListener('submit', function(e) {\n" +
               "            e.preventDefault();\n" +
               "            \n" +
               "            const formData = new FormData(this);\n" +
               "            const params = new URLSearchParams(formData);\n" +
               "            \n" +
               "            // AJAX запрос к FastCGI API\n" +
               "            fetch('/api/check?' + params.toString(), {\n" +
               "                headers: {\n" +
               "                    'Accept': 'application/json'\n" +
               "                }\n" +
               "            })\n" +
               "            .then(response => response.json())\n" +
               "            .then(data => {\n" +
               "                displayResults(data);\n" +
               "            })\n" +
               "            .catch(error => {\n" +
               "                console.error('Error:', error);\n" +
               "                document.getElementById('result').innerHTML = '<div style=\"color: red;\">Ошибка: ' + error + '</div>';\n" +
               "            });\n" +
               "        });\n" +
               "        \n" +
               "        function displayResults(data) {\n" +
               "            if (data.error) {\n" +
               "                document.getElementById('result').innerHTML = '<div style=\"color: red;\">Ошибка: ' + data.message + '</div>';\n" +
               "                return;\n" +
               "            }\n" +
               "            \n" +
               "            let html = '<div class=\"info\">' +\n" +
               "                '<p><strong>Текущее время:</strong> ' + data.currentTime + '</p>' +\n" +
               "                '<p><strong>Время работы скрипта:</strong> ' + data.scriptTime + ' мс</p>' +\n" +
               "                '<p><strong>Текущий результат:</strong> Точка (' + data.currentResult.x + ', ' + data.currentResult.y + ') при R=' + data.currentResult.r + \n" +
               "                ' - <strong>' + (data.currentResult.hit ? 'ПОПАДАНИЕ' : 'ПРОМАХ') + '</strong></p>' +\n" +
               "                '</div>';\n" +
               "            \n" +
               "            if (data.history && data.history.length > 0) {\n" +
               "                html += '<h2>История запросов</h2>' +\n" +
               "                    '<table>' +\n" +
               "                    '<tr><th>X</th><th>Y</th><th>R</th><th>Результат</th><th>Время запроса</th></tr>';\n" +
               "                \n" +
               "                data.history.forEach(result => {\n" +
               "                    const rowClass = result.hit ? 'hit' : 'miss';\n" +
               "                    html += '<tr class=\"' + rowClass + '\">' +\n" +
               "                        '<td>' + result.x + '</td>' +\n" +
               "                        '<td>' + result.y + '</td>' +\n" +
               "                        '<td>' + result.r + '</td>' +\n" +
               "                        '<td><strong>' + (result.hit ? 'ПОПАДАНИЕ' : 'ПРОМАХ') + '</strong></td>' +\n" +
               "                        '<td>' + result.timestamp + '</td>' +\n" +
               "                        '</tr>';\n" +
               "                });\n" +
               "                \n" +
               "                html += '</table>';\n" +
               "            }\n" +
               "            \n" +
               "            document.getElementById('result').innerHTML = html;\n" +
               "        }\n" +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String createJsonResponse(String currentTime, long startTime, RequestResult currentResult) {
        long scriptTime = System.currentTimeMillis() - startTime;
        
        StringBuilder json = new StringBuilder();
        json.append("{\n")
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
        // Старая HTML версия для обратной совместимости
        long scriptTime = System.currentTimeMillis() - startTime;
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html><body>\n")
            .append("<h1>Результаты проверки</h1>\n")
            .append("<p><strong>Текущее время:</strong> ").append(currentTime).append("</p>\n")
            .append("<p><strong>Время работы скрипта:</strong> ").append(scriptTime).append(" мс</p>\n")
            .append("<p><strong>Результат:</strong> ").append(currentResult.hit ? "ПОПАДАНИЕ" : "ПРОМАХ").append("</p>\n")
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
    
    public String getGreeting() {
        return "FastCGI Area Check Server with AJAX";
    }
}