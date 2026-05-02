<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Результат проверки</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tables.css">
</head>
<body>
    <div class="container">
        <h1>Результат проверки попадания точки</h1>
        
        <div class="info">
            <p><strong>Текущее время сервера:</strong> ${currentTime}</p>
            <p><strong>Время выполнения скрипта:</strong> ${currentResult.executionTime} мс</p>
            <p><strong>Результат проверки:</strong> 
                Точка с координатами (${currentResult.x}, ${currentResult.y}) при R=${currentResult.r} - 
                <strong style="color: ${currentResult.hit ? '#155724' : '#721c24'};">
                    ${currentResult.hit ? 'ПОПАДАНИЕ В ОБЛАСТЬ' : 'ПРОМАХ'}
                </strong>
            </p>
        </div>

        <c:if test="${not empty resultsHistory}">
            <h2>История выполненных проверок</h2>
            <table class="results-table">
                <thead>
                    <tr>
                        <th>Координата X</th>
                        <th>Координата Y</th>
                        <th>Параметр R</th>
                        <th>Результат</th>
                        <th>Время запроса</th>
                        <th>Время выполнения</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="result" items="${resultsHistory}">
                        <tr class="${result.hit ? 'hit-row' : 'miss-row'}">
                            <td>${result.x}</td>
                            <td>${result.y}</td>
                            <td>${result.r}</td>
                            <td><strong>${result.hit ? 'ПОПАДАНИЕ' : 'ПРОМАХ'}</strong></td>
                            <td>${result.timestamp}</td>
                            <td>${result.executionTime} мс</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>

        <div style="text-align: center; margin-top: 20px;">
            <a href="${pageContext.request.contextPath}/controller" class="back-link">
                Вернуться к форме ввода
            </a>
        </div>
    </div>
</body>
</html>