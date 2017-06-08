package com.mihanjk.model;

public class ForecastNN {
    private String name;
    private String currentLevel;
    private int concentration;

    public ForecastNN() {
    }

    public ForecastNN(String name, String currentLevel, int concentration) {
        this.name = name;
        this.currentLevel = currentLevel;
        this.concentration = concentration;
    }

    public String getName() {
        return name;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public int getConcentration() {
        return concentration;
    }
}
