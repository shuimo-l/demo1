package com.demo.demo1.utils;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    public static BasicCookieStore cookieStore = new BasicCookieStore();
    private static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(5000)
            .setConnectTimeout(10000)
            .setSocketTimeout(10000).build();

    /**
     * 测出超时重试机制为了防止超时不生效而设置
     * 如果直接放回false,不重试
     * 这里会根据情况进行判断是否重试
     */
  /*  private static HttpRequestRetryHandler retry = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                log.info("NoHttpResponseException异常,重置httpClient");
                System.out.println("NoHttpResponseException异常");
                httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .setRetryHandler(retry)
                        .build();
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return true;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// ssl握手异常
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        }
    };*/
    private static CloseableHttpClient httpClient = getInstance();

    private static CloseableHttpClient getInstance() {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    public static void main(String[] args) throws InterruptedException, IOException {
        String s = HttpUtils.sendGet("http://www.baidu.com");
        System.out.println(s);
        HttpPost httpPost = new HttpPost("www.baidu.com");
        NameValuePair valuePair = new BasicNameValuePair("name", "tom");

        // 编码
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(Arrays.asList(valuePair), Consts.UTF_8);
        httpPost.setEntity(formEntity);
        System.out.println(httpPost.getURI());
        System.out.println(EntityUtils.toString(httpPost.getEntity()));
    }

    public static String sendGet(String url) {
        return sendGet(url, null);
    }

    public static String sendGet(String url, String cookies) {
        String result = "";
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", userAgent);
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            if (cookies != null) {
                httpGet.setHeader("Cookie", cookies);
            }

            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (NoHttpResponseException ne) {
            log.error("NoHttpResponseException异常,重置httpClient,url:" + url, ne);
            httpClient = getInstance();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendGet(url, cookies);
        } catch (Exception e) {
            log.error("处理失败:url:" + url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 发送HttpGet请求 * * @param url * 请求地址 * @return 返回字符串
     */
    public static byte[] sendGetByte(String url) {
        byte[] result = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", userAgent);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toByteArray(entity);
            }
        } catch (NoHttpResponseException ne) {
            log.error("NoHttpResponseException异常,重置httpClient,url:" + url, ne);
            httpClient = getInstance();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendGetByte(url);
        } catch (Exception e) {
            log.error("处理失败:url:" + url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 发送HttpPost请求，参数为map * * @param url * 请求地址 * @param map * 请求参数 * @return 返回字符串
     */
    public static String sendPost(String url, Map<String, String> map) {
        // 设置参数
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return sendPost(url, formparams);
    }

    private static String executePost(HttpPost httpPost) {
        String result = "";
        httpPost.setHeader("User-Agent", userAgent);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        } catch (NoHttpResponseException ne) {
            String params = null;
            try {
                params = EntityUtils.toString(httpPost.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.error("NoHttpResponseException异常,重置httpClient对象,url:" + httpPost.getURI() + ",params:" + params, ne);

            httpClient = getInstance();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //重新调用
            executePost(httpPost);
        } catch (IOException e) {
            String params = null;
            try {
                params = EntityUtils.toString(httpPost.getEntity());
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            log.error("url:" + httpPost.getURI() + ",params:" + params, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return result;
    }

    public static String getHeader(String url, String jsonStr, String headerName) {
        // 字符串编码
        StringEntity entity = new StringEntity(jsonStr, Consts.UTF_8);
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(entity);

        return executeHeaderPost(httpPost, headerName);
    }

    private static String executeHeaderPost(HttpPost httpPost, String headerName) {
        httpPost.setHeader("User-Agent", userAgent);
        String result = "";
        CloseableHttpResponse response = null;
        try {

            response = httpClient.execute(httpPost);

            Header header = response.getFirstHeader(headerName);
            if (header != null) {
                result = header.getValue();
            } else {
                httpClient = getInstance();
                log.info("header获取失败: response:{}, responseEntity:{}", response, EntityUtils.toString(response.getEntity()));
            }
        } catch (NoHttpResponseException ne) {
            httpClient = getInstance();
            String params = null;
            try {
                params = EntityUtils.toString(httpPost.getEntity());
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            log.error("NoHttpResponseException异常,重置httpClient对象,url:" + httpPost.getURI() + ", params:" + params, ne);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //重新调用
            executeHeaderPost(httpPost, headerName);
        } catch (IOException e) {
            String params = null;
            try {
                params = EntityUtils.toString(httpPost.getEntity());
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            log.error("url:" + httpPost.getURI() + ",params:" + params, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String getHeader(String url, List<NameValuePair> valuePairs, String headerName) {
        HttpPost httpPost = new HttpPost(url);
        if (valuePairs != null && valuePairs.size() > 0) {
            // 编码
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
            httpPost.setEntity(formEntity);
        }
        return executeHeaderPost(httpPost, headerName);
    }

    public static String sendPost(String url, List<NameValuePair> valuePairs) {
        HttpPost httpPost = new HttpPost(url);
        if (valuePairs != null && valuePairs.size() > 0) {
            // 编码
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
            httpPost.setEntity(formEntity);
        }
        return executePost(httpPost);
    }

    public static String sendPost(String url, String jsonStr) {
        return sendPost(url, jsonStr, null);
    }

    /**
     * 发送HttpPost请求，参数为json字符串 * * @param url * @param jsonStr * @return
     */
    public static String sendPost(String url, String jsonStr, Map<String, String> headers) {
        // 字符串编码
        StringEntity entity = new StringEntity(jsonStr, Consts.UTF_8);
        // 设置content-type
        entity.setContentType("application/json");
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            Set<Map.Entry<String, String>> entrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // 接收参数设置
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(entity);
        return executePost(httpPost);
    }

    /**
     * 发送不带参数的HttpPost请求 * * @param url * @return
     */
    public static String sendPost(String url) {
        return sendPost(url, (List<NameValuePair>) null);
    }
}