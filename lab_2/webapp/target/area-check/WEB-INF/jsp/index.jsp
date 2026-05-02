<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Лабораторная работа №2 - Проверка попадания в область</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/forms.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tables.css">
</head>
<body>
    <div class="header">
        <h1>Лабораторная работа №2 - Проверка попадания в область</h1>
        <div id="header-info">
            ФИО: Карандашева Анастасия Денисовна | Группа: P3332 | Вариант: 368278
        </div>
    </div>

    <div class="container">
        <h2>Проверка попадания точки в область на координатной плоскости</h2>
        
        <table class="form-table">
            <thead>
                <tr>
                    <th colspan="2" style="text-align: center; background-color: #34495e; color: white;">
                        Описание области попадания
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td width="30%"><strong>1-я четверть:</strong></td>
                    <td>Прямоугольная область: 0 ≤ x ≤ R, 0 ≤ y ≤ R/2</td>
                </tr>
                <tr>
                    <td><strong>2-я четверть:</strong></td>
                    <td>Область попадания отсутствует</td>
                </tr>
                <tr>
                    <td><strong>3-я четверть:</strong></td>
                    <td>Круговая область: x² + y² ≤ (R/2)², x ≤ 0, y ≤ 0</td>
                </tr>
                <tr>
                    <td><strong>4-я четверть:</strong></td>
                    <td>Треугольная область: 0 ≤ x ≤ R, -R ≤ y ≤ 0, x - y ≤ R</td>
                </tr>
            </tbody>
        </table>

        <form id="checkForm" action="${pageContext.request.contextPath}/controller" method="GET">
            <input type="hidden" name="fromCanvas" id="fromCanvasField" value="false">
        <table class="form-table">
            <thead>
                <tr>
                    <th colspan="2" style="text-align: center; background-color: #34495e; color: white;">
                        Ввод параметров для проверки
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td width="30%">
                        <label for="x" class="form-label">Координата X:</label>
                    </td>
                    <td width="70%">
                        <div class="button-group">
                            <c:forEach var="xVal" items="${[-4,-3,-2,-1,0,1,2,3,4]}">
                                <button type="button" class="x-btn" data-value="${xVal}">${xVal}</button>
                            </c:forEach>
                        </div>
                        <input type="hidden" id="x" name="x" required>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="y" class="form-label">Координата Y (-5 до 5):</label>
                    </td>
                    <td>
                        <input type="text" id="y" name="y" required
                               placeholder="Введите число от -5 до 5"
                               pattern="^-?\d+(\.\d+)?$"
                               title="Введите число от -5 до 5">
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="r" class="form-label">Параметр R (1 до 4):</label>
                    </td>
                    <td>
                        <input type="text" id="r" name="r" required
                               placeholder="Введите число от 1 до 4"
                               pattern="^[1-4](\.\d+)?$"
                               title="Введите число от 1 до 4">
                    </td>
                    
                </tr>
                <tr>
                    <td colspan="2" style="text-align: center;">
                        <button type="submit" class="submit-btn" id="submitBtn">
                            Проверить попадание
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
        </form>
        
        <div class="graph-container">
            <h3>Интерактивная область</h3>
            <canvas id="coordinateCanvas" width="400" height="400"></canvas>
            <div id="canvasMessage" class="canvas-message"></div>
            <div id="pointInfo" class="point-info" style="display: none;">
                Текущая точка: (<span id="pointX">0</span>, <span id="pointY">0</span>)
                <br>
                <small>Исторические точки для R=<span id="currentRValue">0</span>: 
                       <span id="historyPointsCount">0</span> показано</small>
            </div>
        </div>
        
        <c:if test="${not empty resultsBean.results}">
            <h3>История выполненных проверок</h3>
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
                    <c:forEach var="result" items="${resultsBean.results}" varStatus="status">
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
    </div>

    <script>
        let currentX = null;
        let currentY = null;
        let currentR = null;
        
        const canvas = document.getElementById('coordinateCanvas');
        const ctx = canvas.getContext('2d');

        function drawCoordinateSystem() {
            const width = canvas.width;
            const height = canvas.height;
            const centerX = width / 2;
            const centerY = height / 2;
            const scale = 35;

            // Очистка canvas
            ctx.clearRect(0, 0, width, height);

            // Сетка
            ctx.strokeStyle = '#e0e0e0';
            ctx.lineWidth = 1;
            for (let i = -5; i <= 5; i++) {
                ctx.beginPath();
                ctx.moveTo(centerX + i * scale, 0);
                ctx.lineTo(centerX + i * scale, height);
                ctx.stroke();
                
                ctx.beginPath();
                ctx.moveTo(0, centerY + i * scale);
                ctx.lineTo(width, centerY + i * scale);
                ctx.stroke();
            }

            // Оси
            ctx.strokeStyle = '#000';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(0, centerY);
            ctx.lineTo(width, centerY);
            ctx.stroke();
            
            ctx.beginPath();
            ctx.moveTo(centerX, 0);
            ctx.lineTo(centerX, height);
            ctx.stroke();

            // Подписи осей
            ctx.fillStyle = '#000';
            ctx.font = '12px Arial';
            for (let i = -4; i <= 4; i++) {
                if (i !== 0) {
                    ctx.fillText(i, centerX + i * scale - 5, centerY + 15);
                    ctx.fillText(-i, centerX - 15, centerY + i * scale + 5);
                }
            }

            // Рисование области, если задан R
            if (currentR) {
                ctx.fillStyle = 'rgba(52, 152, 219, 0.3)';
                ctx.strokeStyle = '#2980b9';
                ctx.lineWidth = 2;

                // 1-я четверть: прямоугольник (0 ≤ x ≤ R, 0 ≤ y ≤ R/2)
                ctx.beginPath();
                ctx.rect(centerX, centerY - (currentR/2) * scale, 
                        currentR * scale, (currentR/2) * scale);
                ctx.fill();
                ctx.stroke();

                // 2-я четверть: ничего не рисуем

                // 3-я четверть: круг радиусом R/2 (x² + y² ≤ (R/2)²)
                ctx.beginPath();
                ctx.arc(centerX, centerY, (currentR/2) * scale, Math.PI, Math.PI / 2, true);
                ctx.lineTo(centerX, centerY);
                ctx.closePath();
                ctx.fill();
                ctx.stroke();

                // 4-я четверть: треугольник (0 ≤ x ≤ R, -R ≤ y ≤ 0, x - y ≤ R)
                ctx.beginPath();
                ctx.moveTo(centerX, centerY);
                ctx.lineTo(centerX + currentR * scale, centerY);
                ctx.lineTo(centerX, centerY + currentR * scale);
                ctx.closePath();
                ctx.fill();
                ctx.stroke();
            }

            // Отрисовка ВСЕХ точек из истории для текущего R
            drawAllHistoryPoints();

            // Отрисовка текущей точки (если есть координаты)
            if (currentX !== null && currentY !== null) {
                const hit = checkHitVisual(currentX, currentY, currentR);
                drawPoint(currentX, currentY, hit, true); // true - это текущая точка
            }
        }

        // Функция для отрисовки всех точек из истории
        function drawAllHistoryPoints() {
            // Получаем все строки таблицы с результатами
            const resultRows = document.querySelectorAll('.results-table tbody tr');
            let pointsCount = 0;
            
            resultRows.forEach(row => {
                try {
                    // Извлекаем данные из строки таблицы
                    const cells = row.cells;
                    const x = parseFloat(cells[0].textContent);
                    const y = parseFloat(cells[1].textContent);
                    const r = parseFloat(cells[2].textContent);
                    const hit = cells[3].textContent.trim() === 'ПОПАДАНИЕ';
                    
                    drawPoint(x, y, hit, false); // false - это историческая точка
                    pointsCount++;
                } catch (e) {
                    console.log('Ошибка при обработке строки истории:', e);
                }
            });
            
            // Обновляем счетчик точек
            if (currentR) {
                document.getElementById('currentRValue').textContent = currentR;
                document.getElementById('historyPointsCount').textContent = pointsCount;
            }
        }

        function drawPoint(x, y, hit, isCurrentPoint) {
            const centerX = canvas.width / 2;
            const centerY = canvas.height / 2;
            const scale = 35;

            // Разные стили для текущей и исторических точек
            if (isCurrentPoint) {
                // Текущая точка - больше и с обводкой
                ctx.beginPath();
                ctx.arc(centerX + x * scale, centerY - y * scale, 6, 0, 2 * Math.PI);
                ctx.fillStyle = hit ? '#27ae60' : '#e74c3c';
                ctx.fill();
                ctx.strokeStyle = '#000';
                ctx.lineWidth = 2;
                ctx.stroke();
                
            } else {
                // Исторические точки - меньше и прозрачнее
                ctx.beginPath();
                ctx.arc(centerX + x * scale, centerY - y * scale, 4, 0, 2 * Math.PI);
                ctx.fillStyle = hit ? 'rgba(39, 174, 96, 0.7)' : 'rgba(231, 76, 60, 0.7)';
                ctx.fill();
                ctx.strokeStyle = hit ? '#27ae60' : '#e74c3c';
                ctx.lineWidth = 1;
                ctx.stroke();
            }
            
            // Обновляем информацию о текущей точке
            if (isCurrentPoint) {
                document.getElementById('pointX').textContent = x;
                document.getElementById('pointY').textContent = y;
                document.getElementById('pointInfo').style.display = 'block';
            }
        }

        // Функция проверки попадания для визуализации (такая же как в сервлете)
        function checkHitVisual(x, y, r) {
            if (x >= 0 && y >= 0) {
                return (x <= r) && (y <= r/2);
            }
            if (x < 0 && y >= 0) {
                return false;
            }
            if (x < 0 && y < 0) {
                return (x*x + y*y) <= (r/2)*(r/2);
            }
            if (x >= 0 && y < 0) {
                return (x <= r) && (y >= -r) && (x - y <= r);
            }
            return false;
        }

        // Обработка клика по canvas
        canvas.addEventListener('click', function(event) {
            const rect = canvas.getBoundingClientRect();
            const x = event.clientX - rect.left;
            const y = event.clientY - rect.top;
            
            const centerX = canvas.width / 2;
            const centerY = canvas.height / 2;
            const scale = 35;
            
            const coordX = (x - centerX) / scale;
            const coordY = (centerY - y) / scale;
            
            if (!currentR) {
                document.getElementById('canvasMessage').textContent = 
                    'Сначала установите параметр R для определения координат точки';
                return;
            }
            
            // Устанавливаем значения в форму
            const roundedX = Math.round(coordX * 2) / 2;
            const roundedY = Math.round(coordY * 2) / 2;
            
            currentX = roundedX;
            currentY = roundedY;
            
            document.getElementById('x').value = roundedX;
            document.getElementById('y').value = roundedY;
            document.getElementById('r').value = currentR;
            
            // Отмечаем соответствующую кнопку X
            document.querySelectorAll('.x-btn').forEach(btn => {
                btn.classList.remove('active');
                const btnValue = parseFloat(btn.getAttribute('data-value'));
                if (Math.abs(btnValue - roundedX) < 0.1) {
                    btn.classList.add('active');
                }
            });
            
            drawCoordinateSystem();

            document.getElementById('fromCanvasField').value = 'true';
            document.getElementById('checkForm').submit();
        });

        // Обработчики изменений полей ввода
        document.getElementById('r').addEventListener('input', function() {
            const rValue = parseFloat(this.value);
            if (!isNaN(rValue) && rValue >= 1 && rValue <= 4) {
                currentR = rValue;
                document.getElementById('canvasMessage').textContent = '';
                drawCoordinateSystem(); // Теперь перерисовывает все точки
            } else {
                currentR = null;
                drawCoordinateSystem();
            }
        });

        document.getElementById('y').addEventListener('input', function() {
            const yValue = parseFloat(this.value);
            if (!isNaN(yValue) && yValue >= -5 && yValue <= 5) {
                currentY = yValue;
                drawCoordinateSystem();
            } else {
                currentY = null;
                drawCoordinateSystem();
            }
        });

        document.querySelectorAll('.x-btn').forEach(button => {
            button.addEventListener('click', function() {
                document.querySelectorAll('.x-btn').forEach(btn => {
                    btn.classList.remove('active');
                });
                this.classList.add('active');
                const xValue = parseFloat(this.getAttribute('data-value'));
                document.getElementById('x').value = xValue;
                currentX = xValue;
                drawCoordinateSystem();
                if (typeof validateForm !== 'undefined') {
                    validateForm();
                }
            });
        });

        // Инициализация при загрузке
        document.addEventListener('DOMContentLoaded', function() {
            drawCoordinateSystem();
        });
    </script>
</body>
</html>