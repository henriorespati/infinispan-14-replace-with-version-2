package com.edw.controller;

import com.edw.helper.GenerateCacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 *  com.edw.controller.IndexController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 10 Jul 2024 15:54
 */
@RestController
public class IndexController {
    @Autowired
    private GenerateCacheHelper generateCacheHelper;

    @GetMapping(path = "/")
    public String init() {
        generateCacheHelper.generate();
        return "good";
    }

}
