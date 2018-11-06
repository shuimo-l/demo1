package com.demo.demo1.controller;

import com.demo.demo1.exception.LoginLostException;
import com.demo.demo1.service.SpiderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: liuxl
 * @date: 2018-11-05 17:54
 * @description:
 */
@Controller
public class AmazonController {

    public static Logger logger = LoggerFactory.getLogger(AmazonController.class);
    @Autowired
    private SpiderServiceImpl spiderService;

    @GetMapping("/login")
    public String index() {
        return "login.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model, HttpSession session) {
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        try {
            spiderService.login(username, password);
            session.setAttribute("loginUser", username);
            return "redirect:/search.html";
        } catch (LoginLostException e) {
            logger.error("LoginLostException,登录失效", e);
            model.addAttribute("msg", e.getMessage());
            return "login.html";
        } catch (Exception e) {
            logger.error("异常", e);
            model.addAttribute("msg", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/search")
    public String search(Model model, String q, HttpSession session) {

        try {
            Object username = session.getAttribute("loginUser");

            List<String> urls = spiderService.search(q, String.valueOf(username));
//            List<String> urls = new ArrayList<>();
//            for (int i = 0; i < 100; i++) {
//                urls.add("https://www.amazon.com/dp/B07CBTT2T5");
//            }
            model.addAttribute("urls", urls);
        } catch (LoginLostException e) {
            logger.error("LoginLostException,登录失效", e);
            model.addAttribute("msg", e.getMessage());
            return "login.html";
        } catch (Exception e) {
            logger.error("异常", e);
            model.addAttribute("msg", e.getMessage());
            return "search.html";
        }
        model.addAttribute("q", q);
        return "search";
    }
}