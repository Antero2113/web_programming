package com.example.webapp.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String x = request.getParameter("x");
        String y = request.getParameter("y");
        String r = request.getParameter("r");
        String fromCanvas = request.getParameter("fromCanvas");
        
        System.out.println("=== ControllerServlet called ===");
        System.out.println("fromCanvas parameter: " + fromCanvas);
        
        // Если параметры присутствуют, делегируем обработку AreaCheckServlet
        if (x != null && y != null && r != null && 
            !x.trim().isEmpty() && !y.trim().isEmpty() && !r.trim().isEmpty()) {
            
            // Сохраняем fromCanvas в атрибутах запроса для передачи дальше
            if (fromCanvas != null) {
                request.setAttribute("fromCanvas", fromCanvas);
            } else {
                request.setAttribute("fromCanvas", "false"); // значение по умолчанию
            }
            
            System.out.println("Forwarding to /area-check with fromCanvas: " + request.getAttribute("fromCanvas"));
            request.getRequestDispatcher("/area-check").forward(request, response);
        } else {
            // Иначе показываем форму
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
        }
    }
}