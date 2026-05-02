package com.example.webapp.service;

import com.example.webapp.entity.PointResult;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class PointService {
    
    @PersistenceContext(unitName = "areaCheckPU")
    private EntityManager entityManager;
    
    public void savePoint(PointResult point) {
        try {
            entityManager.persist(point);
            entityManager.flush(); // Принудительно сохраняем изменения
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка сохранения точки в БД", e);
        }
    }
    
    public List<PointResult> getAllPoints() {
        return entityManager.createQuery("SELECT p FROM PointResult p ORDER BY p.timestamp DESC", PointResult.class)
                .getResultList();
    }
    
    public List<PointResult> getPointsBySession(String sessionId) {
        return entityManager.createQuery("SELECT p FROM PointResult p WHERE p.sessionId = :sessionId ORDER BY p.timestamp DESC", PointResult.class)
                .setParameter("sessionId", sessionId)
                .getResultList();
    }
}