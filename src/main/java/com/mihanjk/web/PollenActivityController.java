package com.mihanjk.web;

import com.mihanjk.model.Forecast;
import com.mihanjk.services.PollenActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PollenActivityController {
    @Autowired
    private PollenActivityService pollenActivityService;

    @RequestMapping("/getForecast")
    public Map<String, List<Forecast>> getForecast(@RequestParam(value = "type", defaultValue = "all") String type) throws Exception {
//        pollenActivityService.updateData(type);
        //TODO make refactoring
//        try {
        return pollenActivityService.parseData(type);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new HashMap<>();
//        }
    }
}
