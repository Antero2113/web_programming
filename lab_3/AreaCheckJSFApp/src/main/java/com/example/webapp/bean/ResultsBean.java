package com.example.webapp.bean;

import com.example.webapp.entity.PointResult;
import com.example.webapp.service.PointService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class ResultsBean implements Serializable {
    
    private List<PointResult> results;
    
    @EJB
    private PointService pointService;
    
    @PostConstruct
    public void init() {
        results = new ArrayList<>();
        // Загружаем результаты из БД при инициализации
        loadResultsFromDatabase();
    }
    
    private void loadResultsFromDatabase() {
        try {
            String sessionId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
            results = pointService.getPointsBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            results = new ArrayList<>();
        }
    }
    
    public void addResult(PointResult result) {
        results.add(0, result); // Добавляем в начало списка
    }
    
    public List<PointResult> getResults() {
        return results;
    }
    
    public void setResults(List<PointResult> results) {
        this.results = results;
    }
}