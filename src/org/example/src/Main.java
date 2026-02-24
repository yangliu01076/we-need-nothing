package org.example.src;

import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Component;
import org.example.minispring.annotation.PostConstruct;

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
