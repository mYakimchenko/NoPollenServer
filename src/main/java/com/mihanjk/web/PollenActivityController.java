package com.mihanjk.web;

import com.mihanjk.model.Database;
import com.mihanjk.model.Forecast;
import com.mihanjk.services.NotificationService;
import com.mihanjk.services.PollenActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
public class PollenActivityController {
    private final PollenActivityService pollenActivityService;

    @Autowired
    public PollenActivityController(PollenActivityService pollenActivityService) {
        this.pollenActivityService = pollenActivityService;
    }

    @RequestMapping("/getMoscowForecast")
    @Scheduled(fixedRate = 600000)
    public Map<String, List<Forecast>> getForecast() throws Exception {
        System.err.println("Execute request allergotop: " + Calendar.getInstance().getTime());
        return pollenActivityService.getForecastData();
    }

    @RequestMapping("/checkNNForecast")
    @Scheduled(fixedRate = 660000)
    public void createNNForecastTemplate() throws Exception {
        System.err.println("Execute request: nikaNN" + Calendar.getInstance().getTime());
        pollenActivityService.createTemplate();
    }

    @RequestMapping("/sendNotificationNN")
    public String sendNNNotification() {
        System.err.println("Send notification: " + Calendar.getInstance().getTime());
        return NotificationService.sendNotification(Database.NN_PATH_DATABASE, pollenActivityService.getNNData());
    }
}
