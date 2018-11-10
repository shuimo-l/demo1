package com.demo.demo1.service;

import java.util.Set;

/**
 * @author: liuxl
 * @date: 2018-11-05 17:56
 * @description:
 */
public interface ISpiderService {

    public void login(String userName, String password);

    void search(String q, String username);
}
