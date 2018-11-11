package com.demo.demo1.service;

import javax.servlet.http.HttpSession;

/**
 * @author: liuxl
 * @date: 2018-11-05 17:56
 * @description:
 */
public interface ISpiderService {

    public void login(String username, String password);

    void search(String q, String username, HttpSession session);
}
