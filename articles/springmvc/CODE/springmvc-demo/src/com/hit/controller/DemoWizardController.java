package com.hit.controller;

import com.hit.domain.Person;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class DemoWizardController extends AbstractWizardFormController {

    // 在构造函数中初始化 command 对象
    public DemoWizardController() {
        // 页面封装数据到 command 对象，对应的实体为 Person
        this.setCommandClass(Person.class);
        // 帮助页面实现回显功能
        this.setCommandName("p");
    }

    // 最终完成后提交
    @Override
    protected ModelAndView processFinish(HttpServletRequest request,
                                         HttpServletResponse response, Object command, BindException e) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
        return new ModelAndView("index");
    }

    // 取消填写，返回第一个页面
    @Override
    protected ModelAndView processCancel(HttpServletRequest request,
                                         HttpServletResponse response, Object command, BindException e) throws Exception {
        return new ModelAndView("wizard/PersonInfo");
    }
}

