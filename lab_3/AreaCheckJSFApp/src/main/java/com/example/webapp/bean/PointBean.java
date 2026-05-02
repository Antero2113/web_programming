package com.example.webapp.bean;

import com.example.webapp.entity.PointResult;
import com.example.webapp.service.PointService;
import com.example.webapp.util.AreaChecker;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@ManagedBean
@SessionScoped
public class PointBean implements Serializable {
    
    private Double x;
    private Double y;
    private Double r = 2.0;
    private List<Double> xValues;
    
    @ManagedProperty("#{resultsBean}")
    private ResultsBean resultsBean;
    
    @Inject
    private PointService pointService;
    
    @PostConstruct
    public void init() {
        xValues = Arrays.asList(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0);
    }
    
    public String checkPoint() {
        long startTime = System.currentTimeMillis();
        
        try {
            boolean hit = AreaChecker.checkHit(x, y, r);
            long executionTime = System.currentTimeMillis() - startTime;
            
            String sessionId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
            PointResult result = new PointResult(x, y, r, hit, executionTime, "sessionId");
            
            // Сохраняем в базу данных
            pointService.savePoint(result);
            
            // Добавляем в список результатов
            resultsBean.addResult(result);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
    
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
    
    // Геттеры и сеттеры
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
    
    public Double getR() { return r; }
    public void setR(Double r) { this.r = r; }
    
    public List<Double> getXValues() { return xValues; }
    public void setXValues(List<Double> xValues) { this.xValues = xValues; }
    
    public ResultsBean getResultsBean() { return resultsBean; }
    public void setResultsBean(ResultsBean resultsBean) { this.resultsBean = resultsBean; }
}