package com.mihanjk.web;

import com.mihanjk.model.Forecast;
import com.mihanjk.services.PollenActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PollenActivityController {
    @Autowired
    private PollenActivityService pollenActivityService;

    //TODO: decide what happened with Exception
    @RequestMapping("/getForecast")
    public List<Forecast> getForecast(@RequestParam(value = "type", defaultValue = "all") String type) throws Exception {
        return pollenActivityService.getData(type);
    }

}
