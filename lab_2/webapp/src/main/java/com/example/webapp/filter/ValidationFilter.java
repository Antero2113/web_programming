package com.example.webapp.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/controller")
public class ValidationFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        System.out.println("=== ValidationFilter called ===");
        
        // Получаем параметры
        String fromCanvas = httpRequest.getParameter("fromCanvas");
        String x = httpRequest.getParameter("x");
        String y = httpRequest.getParameter("y");
        String r = httpRequest.getParameter("r");
        
        System.out.println("Parameters - fromCanvas: '" + fromCanvas + "', x: " + x + ", y: " + y + ", r: " + r);
        
        // ЕСЛИ ВСЕ ПАРАМЕТРЫ ОТСУТСТВУЮТ - это начальная загрузка страницы
        if (x == null && y == null && r == null) {
            System.out.println("Initial page load - skipping validation");
            chain.doFilter(request, response);
            return;
        }
        
        // ЕСЛИ ЕСТЬ ПАРАМЕТРЫ - проверяем fromCanvas
        boolean isFromCanvas = "true".equals(fromCanvas);
        System.out.println("isFromCanvas: " + isFromCanvas);
        
        if (isFromCanvas) {
            System.out.println("Canvas click - skipping validation");
        } else {
            System.out.println("Form submit - validating parameters");
            if (!validateParameters(x, y, r)) {
                System.out.println("Validation FAILED");
                request.setAttribute("error", "Некорректные параметры");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
                dispatcher.forward(request, response);
                return;
            }
            System.out.println("Validation PASSED");
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean validateParameters(String x, String y, String r) {
        try {
            // Проверяем что все параметры присутствуют
            if (x == null || y == null || r == null || 
                x.trim().isEmpty() || y.trim().isEmpty() || r.trim().isEmpty()) {
                System.out.println("Missing parameters");
                return false;
            }
            
            double xVal = Double.parseDouble(x);
            double yVal = Double.parseDouble(y);
            double rVal = Double.parseDouble(r);
            
            // Проверяем диапазоны
            boolean isValidX = (xVal >= -5 && xVal <= 5);
            boolean isValidY = (yVal >= -5 && yVal <= 5);
            boolean isValidR = (rVal >= 1 && rVal <= 4);
            
            boolean isValid = isValidX && isValidY && isValidR;
            
            System.out.println("Validation details - X:" + isValidX + " Y:" + isValidY + " R:" + isValidR);
            return isValid;
                    
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("ValidationFilter initialized for /controller");
    }
    
    @Override
    public void destroy() {}
}