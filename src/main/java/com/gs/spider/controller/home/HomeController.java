package com.gs.spider.controller.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.gs.spider.controller.BaseController;

/**
 * Created by gsh199449 on 2016/11/24.
 */
@Controller
@RequestMapping("/")
public class HomeController extends BaseController {
    private final static Logger LOG = LogManager.getLogger(HomeController.class);

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    public ModelAndView home() {
    	ModelAndView modelAndView = new ModelAndView("panel/welcome/welcome");
    	modelAndView.addObject("appName", "在线采集平台")
    		.addObject("appVersion", "0.7")
    		.addObject("onlineDocumentation","https://gsh199449.github.io/gather_platform_pages/");
        return modelAndView;
    }

}

