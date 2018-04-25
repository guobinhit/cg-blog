package com.hit.controller;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import com.hit.domain.Person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class DemoCommandController extends AbstractCommandController {

    // 在构造函数中初始化 command 对象
    public DemoCommandController() {
        // 页面封装数据到 command 对象，对应的实体为 Person
        this.setCommandClass(Person.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
                                  Object command, BindException e) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
        return null;
    }
}
