package com.example.webapp.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "POINT_RESULTS")
@NamedQueries({
    @NamedQuery(name = "PointResult.findBySession", 
                query = "SELECT p FROM PointResult p WHERE p.sessionId = :sessionId ORDER BY p.timestamp DESC")
})
public class PointResult implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "point_seq")
    @SequenceGenerator(name = "point_seq", sequenceName = "POINT_RESULTS_SEQ", allocationSize = 1)
    private Long id;
    
    @Column(name = "X_VALUE", nullable = false)
    private Double x;
    
    @Column(name = "Y_VALUE", nullable = false)
    private Double y;
    
    @Column(name = "R_VALUE", nullable = false)
    private Double r;
    
    @Column(name = "HIT_RESULT", nullable = false)
    private Boolean hit;
    
    @Column(name = "EXECUTION_TIME")
    private Long executionTime;
    
    @Column(name = "SESSION_ID", length = 100)
    private String sessionId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIMESTAMP", nullable = false)
    private Date timestamp;
    
    // Конструктор по умолчанию (БЕЗ ИЗМЕНЕНИЙ)
    public PointResult() {
        this.timestamp = new Date();
    }
    
    // Конструктор с параметрами (ДОБАВЬТЕ ЭТОТ)
    public PointResult(Double x, Double y, Double r, Boolean hit, Long executionTime, String sessionId) {
        this(); // вызываем конструктор по умолчанию
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.executionTime = executionTime;
        this.sessionId = sessionId;
    }
    
    // Геттеры и сеттеры БЕЗ ИЗМЕНЕНИЙ
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
    
    public Double getR() { return r; }
    public void setR(Double r) { this.r = r; }
    
    public Boolean getHit() { return hit; }
    public void setHit(Boolean hit) { this.hit = hit; }
    
    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}