package org.example.src;

import org.example.minispring.annotation.Autowired;
import org.example.minispring.annotation.Component;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@Component
public class ServiceB {
    @Autowired
    private ServiceA serviceA;

    public void sayHi() {
        System.out.println("I am B, called by A.");
    }
}
