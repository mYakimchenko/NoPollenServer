package com.mihanjk.model;

public class AllergenMoscow {
    private final String name;
    private final String currentLevel;
    private final String tomorrowLevel;

    public AllergenMoscow(String name, String currentLevel, String tomorrowLevel) {
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
