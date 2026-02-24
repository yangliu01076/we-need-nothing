package org.example.src;

import minispring.annotation.Autowired;
import minispring.annotation.Component;
import minispring.annotation.PostConstruct;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Component
public class Main {

    @Autowired
    private ServiceA serviceA;

    @PostConstruct
    public void init() {
        System.out.println("Main init");
//        serviceA.sayHello();
    }
}
