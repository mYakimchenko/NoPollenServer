package com.mihanjk.model;

public class Forecast {
    private final String name;
    private final String currentLevel;
    private final String tomorrowLevel;

    public Forecast(String name, String currentLevel, String tomorrowLevel) {
        this.name = name;
        this.currentLevel = currentLevel;
        this.tomorrowLevel = tomorrowLevel;
    }

    public String getName() {
        return name;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public String getTomorrowLevel() {
        return tomorrowLevel;
    }
}
