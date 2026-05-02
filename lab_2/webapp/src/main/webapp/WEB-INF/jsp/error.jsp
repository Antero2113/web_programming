<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Ошибка</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="container">
        <h1>Ошибка</h1>
        <div class="error-message">
            <p>${error}</p>
        </div>
        <div style="text-align: center; margin-top: 20px;">
            <a href="${pageContext.request.contextPath}/controller" class="back-link">
                Вернуться к форме ввода
            </a>
        </div>
    </div>
</body>
</html>