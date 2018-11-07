package com.demo.demo1.service;

import java.util.Set;

/**
 * @author: liuxl
 * @date: 2018-11-05 17:56
 * @description:
 */
public interface ISpiderService {

    public void login(String userName, String password);

    Set<String> search(String q, String username);
}
