package com.demo.demo1.service;

import com.demo.demo1.exception.LoginLostException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: liuxl
 * @date: 2018-11-05 11:32
 * @description:
 */
@Service
public class SpiderServiceImpl implements ISpiderService {
    public static Logger logger = LoggerFactory.getLogger(SpiderServiceImpl.class);

    WebDriver driver = null;
    Set<Cookie> cookies = null;
    Set<String> urls = null;
    Map<String, String> account = new HashMap();

    @PostConstruct
    public void init(){
        logger.info("初始化登录!");
        login("Amgsy@outlook.com", "He010626@");
    }

    @Override
    public void login(String username, String password) {
        logger.info("username:{}, password:{}:", username, password);
        String s = account.get(username);
        //如果账号已经在登录的集合中
        if (StringUtils.hasText(s) && s.equals(password)) {
            return;
        }
        openDriver();
        driver.get("https://sellercentral.amazon.com/ap/signin?openid.pape.max_auth_age=0&openid.return_to=https%3A%2F%2Fsellercentral.amazon.com%2Fhome&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.assoc_handle=sc_na_amazon_v2&openid.mode=checkid_setup&language=zh_CN&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&pageId=sc_na_amazon_v2&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&ssoResponse=eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiQTI1NktXIn0.wBMtZaAcgyreXZtH1_mtWyKytNFqngUEciz_EIYxlIswgTtJDwIA_w.CN_2A5Ks6Dg34DkG.ef5ubam-klI9U1FzxIQ0S-Fph5sIbBvHZZvXKYHzW5M7DI-XVp15mt8lReQbLKS90FZDXvm30rhit20PbQxSOSebWNc9IUdkfJSfJdjtDunAlJQ6VulKtGDzierqEI6vNG4IW2YVx1_IHcLuOLfYwcfn_O-q2BoXkCgx-4cB4XmC6DZvM-hR6ZDRDpvQMQxtYWhHKBMRfeIk-MaVeLdTRI-p6fTJGzAl_H5on3GVZC5eOH8Y_dlgwGBpTz6wL__m50cdzeY.xlyFaO934Z2X_nKBJq-Klw");
        driver.findElement(By.id("ap_email")).sendKeys(username);
        driver.findElement(By.id("ap_password")).sendKeys(password);
        logger.info("开始点击登录按钮");
        driver.findElement(By.id("signInSubmit")).click();
        logger.info("点击登录按钮后");
        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        checkLoginStatus(username, doc);
        account.put(username, password);
        cookies = driver.manage().getCookies();
        logger.info("登录成功");
        quitDriver();
    }

    private void checkLoginStatus(String username, Document doc) {
        Element ap_email = doc.getElementById("ap_email");
        if (ap_email != null) {
            logger.info("pageSource:{}", doc);
            account.remove(username);
            throw new LoginLostException("登录失效");
        }
    }
    public void openDriver() {
        logger.info("启动浏览器...");
//        System.setProperty("webdriver.chrome.driver", "/usr/local/service/chromedriver");
        System.setProperty("webdriver.chrome.driver", "d:\\Administrator\\Downloads\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        options.addArguments("lang=zh_CN.UTF-8 ;q=0.9");
        options.addArguments("no-sandbox");//禁用沙盒 就是被这个参数搞了一天
        Map<String, Object> prefs = new HashMap<String, Object>();
        //设置不显示图片
        prefs.put("profile.managed_default_content_settings.images", 2);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.MINUTES);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
        if (cookies != null) {
            driver.get("https://www.baidu.com");
            for (Cookie cookie : cookies) {
                driver.manage().addCookie(cookie);
            }
        }
        logger.info("driver:{}", driver);
    }

    @Override
    public Set<String> search(String q, String username) {
        logger.info("执行search(), q:{}, username:{}", q, username);
        openDriver();
        Assert.hasText(q, "必须填写搜索关键字");
        //执行第一页
        Document doc = getPageSource(q, 1);
        checkLoginStatus(username, doc);
        //检查是否可以查询到该商品信息
        Elements text = doc.getElementsContainingText("我们无法找到任何符合下列信息的商品");
        Assert.isTrue(text.size() == 0, "我们无法找到任何符合下列信息的商品:" + q);
        //获取结果个数
        int total = getResultNumber(doc);
        int a = ((total - 1) / 10) + 1;
        int pageSize = a < 100 ? a : 100;
        urls = new LinkedHashSet();
        executeOnePage(doc, q);
        logger.info("pageSize:{}", pageSize);
        logger.info("{}执行到第1页",q);
        logger.info("urls.size:{}", urls.size());
        for (int i = 2; i <= pageSize; i++) {
            logger.info("{}执行到第{}页", q, i);
            doc = getPageSource(q, i);
            executeOnePage(doc, q);
            logger.info("urls.size:{}", urls.size());
        }
        logger.info("执行完成");
        //关闭驱动
        quitDriver();
        logger.info("");
        return urls;
    }

    public void quitDriver() {
        driver.quit();
        logger.info("退出浏览器...");
    }

    private void executeOnePage(Document doc, String q) {
        //获取所有"显示商品变体"按钮的element
        Elements elements = doc.getElementsByAttributeValue("data-csm", "showVariationsClick");
        //逐条处理"显示商品变体"按钮
        for (Element element : elements) {
            String asin = element.val();
            //点击显示商品变体
            String url = clickShowVariations(asin);
            if (StringUtils.hasText(url)) {
                urls.add(url);
            }
        }
    }

    private Document getPageSource(String q, Integer page) {
        driver.get("https://sellercentral.amazon.com/productsearch?q=" + q + "&page=" + page);
        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        return doc;
    }

    private int getResultNumber(Document doc) {
        Element productsFooter = doc.getElementById("products-footer");
        Elements totalProductsCount = productsFooter.getElementsByClass("total-products-count");
        Elements total = totalProductsCount.select("b");
        Integer a = Integer.parseInt(total.get(0).text());
        Integer b = Integer.parseInt(total.get(1).text());
        Integer c = Integer.parseInt(total.get(2).text());
        int max = Math.max(Math.max(a, b), c);
        return max;
    }

    @Test
    public void test1() throws IOException {
        Document parse = Jsoup.parse(new File("D:\\Administrator\\Desktop\\1234.html"), "utf-8");
        Elements elementsByClass = parse.getElementsByClass("child-variation-expander");
        for (Element byClass : elementsByClass) {
            //查询符合规范的数据
            Elements text = byClass.getElementsContainingText("您需要获得批准，才能发布此品牌的商品");
            //如果找到就获取该条数据的ASIN号
            if (text != null) {
                Elements qualifyToSellClick = byClass.getElementsByAttributeValue("data-csm", "qualifyToSellClick");
                if (qualifyToSellClick == null) {
                    logger.info("qualifyToSellClick查不到");
                    continue;
                }
                String href = qualifyToSellClick.attr("href");
                if (StringUtils.hasText(href)) {
                    //把数据添加到到集合
                    String[] split = href.split("=");
                    if (split.length > 1) {
                    } else {
                        logger.info("href:{}", href);
                    }
                }
                //找到一条就行了,可以退出了
                break;

            }
        }
    }

    private String clickShowVariations(String asin) {

        driver.get("https://sellercentral.amazon.com/productsearch/children?page=1&asin=" + asin + "&searchRank=1");
        String pageSource = driver.getPageSource();
        Document parse = Jsoup.parse(pageSource);
        //获取所有"有商品发布限制"中的数据
        Elements elementsByClass = parse.getElementsByClass("child-variation-expander");
        for (Element byClass : elementsByClass) {
            //查询符合规范的数据
            Elements text = byClass.getElementsContainingText("您需要获得批准，才能发布此品牌的商品");
            //如果找到就获取该条数据的ASIN号
            if (text != null) {
                Elements qualifyToSellClick = byClass.getElementsByAttributeValue("data-csm", "qualifyToSellClick");
                if (qualifyToSellClick == null) {
                    logger.info("qualifyToSellClick查不到");
                    continue;
                }
                String href = qualifyToSellClick.attr("href");
                if (StringUtils.hasText(href)) {
                    //把数据添加到到集合
                    String[] split = href.split("=");
                    if (split.length > 1) {
                        return "https://www.amazon.com/dp/" + split[1];
                    } else {
                        logger.info("href:{}", href);
                    }
                }
                //找到一条就行了,可以退出了
                break;

            }
        }
        return null;

    }
}