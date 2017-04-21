package com.mihanjk.web;

import com.mihanjk.model.Forecast;
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
    @Autowired
    private PollenActivityService pollenActivityService;

    @RequestMapping("/getForecast")
    @Scheduled(fixedRate = 600000)
//    public Map<String, List<Forecast>> getForecast(@RequestParam(value = "type", defaultValue = "all") String type)
    public Map<String, List<Forecast>> getForecast()
            throws Exception {
        System.out.println("Execute request: " + Calendar.getInstance().getTime());
        return pollenActivityService.getForecastData();
    }
}
