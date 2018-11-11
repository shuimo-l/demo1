package com.demo.demo1.controller;

import com.demo.demo1.exception.LoginLostException;
import com.demo.demo1.mongodb.Goods;
import com.demo.demo1.service.SpiderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Set;

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
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

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
            return "redirect:/quickSearch";
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
            Assert.hasText(q, "请填写搜索关键字");
            Object username = session.getAttribute("loginUser");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    spiderService.search(q, String.valueOf(username), session);
                }
            });
            t.start();

        } catch (LoginLostException e) {
            logger.error("LoginLostException,登录失效", e);
            model.addAttribute("msg", e.getMessage());
            return "login.html";
        } catch (Exception e) {
            logger.error("异常", e);
            model.addAttribute("msg", e.getMessage());
            return "search.html";
        }
        return "search";
    }

    @GetMapping("/quickSearch")
    public String quickSearch(Model model, String q, HttpSession session) {
        if (!StringUtils.hasText(q)) {
            model.addAttribute("msg", "请填写搜索关键字");
        } else {
            Goods goods = spiderService.getByMongodb(q);
            if (goods == null) {
                model.addAttribute("sizeZero", "没有查询到信息,请先到搜索页面查询");
            }
            model.addAttribute("q", q);
            model.addAttribute("goods", goods);
        }
        return "quick_search";
    }

}