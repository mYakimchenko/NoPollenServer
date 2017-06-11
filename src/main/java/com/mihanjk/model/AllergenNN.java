package com.mihanjk.model;

public class AllergenNN {
    private String name;
    private String currentLevel;
    private int concentration;

    public AllergenNN() {
    }

    public AllergenNN(String name, String currentLevel, int concentration) {
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
