package com.alpha.controller;

import com.alpha.model.Customer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController("/input")
public class InputController {

    @RequestMapping(method = RequestMethod.GET)
    public String sayHello() {
        return "Hello";
    }

    @RequestMapping(method = RequestMethod.POST)
    public void getInput(@RequestBody Customer customer) {
        System.out.println("In controller");
    }
}
