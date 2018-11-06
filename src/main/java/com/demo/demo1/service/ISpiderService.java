package com.demo.demo1.service;

import java.util.List;

/**
 * @author: liuxl
 * @date: 2018-11-05 17:56
 * @description:
 */
public interface ISpiderService {

    public void login(String userName, String password);

    List<String> search(String q, String username);
}
