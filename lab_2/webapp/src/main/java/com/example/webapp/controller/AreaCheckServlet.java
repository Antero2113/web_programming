package com.example.webapp.controller;

import com.example.webapp.model.RequestResult;
import com.example.webapp.bean.ResultsBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/area-check")
public class AreaCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Получаем параметры (валидация теперь в фильтре)
            double x = Double.parseDouble(request.getParameter("x"));
            double y = Double.parseDouble(request.getParameter("y"));
            double r = Double.parseDouble(request.getParameter("r"));
            
            // Проверка попадания
            boolean hit = checkHit(x, y, r);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Создаем результат
            RequestResult result = new RequestResult(x, y, r, hit, executionTime);
            
            // Сохраняем в сессию
            HttpSession session = request.getSession();
            ResultsBean resultsBean = (ResultsBean) session.getAttribute("resultsBean");
            if (resultsBean == null) {
                resultsBean = new ResultsBean();
                session.setAttribute("resultsBean", resultsBean);
            }
            resultsBean.addResult(result);
            
            // Передаем данные на JSP
            request.setAttribute("currentResult", result);
            request.setAttribute("currentTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            request.setAttribute("resultsHistory", resultsBean.getResults());
            
            request.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            // Эта ошибка может возникнуть только если фильтр пропустил невалидные данные
            request.setAttribute("error", "Некорректный формат чисел");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Внутренняя ошибка сервера: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }
    
    private boolean checkHit(double x, double y, double r) {
        // 1-я четверть: прямоугольник (0 <= x <= R, 0 <= y <= R/2)
        if (x >= 0 && y >= 0) {
            return (x <= r) && (y <= r/2);
        }
    
        // 2-я четверть: ничего - всегда false
        if (x < 0 && y >= 0) {
            return false;
        }
    
        // 3-я четверть: круг радиусом R/2 (x² + y² <= (R/2)²)
        if (x < 0 && y < 0) {
            return (x*x + y*y) <= (r/2)*(r/2);
        }
    
        // 4-я четверть: треугольник (0 <= x <= R, -R <= y <= 0, x - y <= R)
        if (x >= 0 && y < 0) {
            return (x <= r) && (y >= -r) && (x - y <= r);
        }
    
        return false;
    }  
}