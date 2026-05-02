package com.example.webapp.bean;

import com.example.webapp.model.RequestResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ResultsBean implements Serializable {
    private List<RequestResult> results = new CopyOnWriteArrayList<>();

    public ResultsBean() {}

    public void addResult(RequestResult result) {
        results.add(result);
        if (results.size() > 20) {
            results.remove(0);
        }
    }

    public List<RequestResult> getResults() {
        return new ArrayList<>(results);
    }

    public void clearResults() {
        results.clear();
    }
}