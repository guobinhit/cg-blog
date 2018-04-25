package com.hit.controller;

import com.hit.domain.Person;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class PersonFormController extends SimpleFormController {

    public PersonFormController() {
        // 设置 command
        this.setCommandClass(Person.class);
    }

    // 提交后，交给业务处理
    protected void doSubmitAction(Object command) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
    }
}
