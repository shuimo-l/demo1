package com.demo.demo1.mongodb;

import java.util.List;
import java.util.Set;

/**
 * @author: liuxl
 * @date: 2018-11-10 17:08
 * @description:
 */
public class FirstCatalog {

    private String categorys;
    private Set<String> urls;

    public String getCategorys() {
        return categorys;
    }

    public void setCategorys(String categorys) {
        this.categorys = categorys;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }
}
