package com.example.webapp.util;

public class AreaChecker {
    
    public static boolean checkHit(Double x, Double y, Double r) {
        if (x == null || y == null || r == null) {
            return false;
        }
        
        // 1-я четверть: окружность с радиусом R/2 и центром в точке (0;0)
        if (x >= 0 && y >= 0) {
            return (x * x + y * y) <= (r/2) * (r/2);
        }
        
        // 2-я четверть: ничего
        if (x < 0 && y >= 0) {
            return false;
        }
        
        // 3-я четверть: квадрат с шириной R и высотой R (от 0 до -R по обеим осям)
        if (x < 0 && y < 0) {
            return (x >= -r) && (y >= -r);
        }
        
        // 4-я четверть: треугольник, ограниченный прямой с координатами начала (0; -R) и концом (R/2; 0)
        if (x >= 0 && y < 0) {
            return (y >= (2 * x - r)) && (y >= -r) && (x <= r/2);
        }
        
        return false;
    }
}