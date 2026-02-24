package org.example.src;

import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Component;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Component
public class ServiceA {
    @Autowired
    private ServiceB serviceB;

    public String sayHello() {
        System.out.println("I am A, calling B...");
        serviceB.sayHi();
        return "Hello, I am A";
    }
}
