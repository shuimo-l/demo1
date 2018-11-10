package com.demo.demo1.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * @author: liuxl
 * @date: 2018-11-10 14:52
 * @description:
 */
@Document(collection = "spider")
public class Goods implements Serializable {

    @Id
    private String keyword;

    private List<FirstCatalog> firstCatalogs;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<FirstCatalog> getFirstCatalogs() {
        return firstCatalogs;
    }

    public void setFirstCatalogs(List<FirstCatalog> firstCatalog) {
        this.firstCatalogs = firstCatalog;
    }
}
