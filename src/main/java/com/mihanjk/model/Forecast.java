package com.mihanjk.model;

public class Forecast {
    private final String name;
    private final String currentLevelIcon;
    private final String forecastTomorrowIcon;

    public Forecast(String name, String currentLevelIcon, String forecastTomorrowIcon) {
        this.name = name;
        this.currentLevelIcon = currentLevelIcon;
        this.forecastTomorrowIcon = forecastTomorrowIcon;
    }

    public String getName() {
        return name;
    }

    public String getCurrentLevelIcon() {
        return currentLevelIcon;
    }

    public String getForecastTomorrowIcon() {
        return forecastTomorrowIcon;
    }
}
