package org.example;

import org.example.minispring.annotation.SpringBootApplication;
import org.example.minispring.core.MiniApplicationContext;

/**
 * @author duoyian
 * @date 2026/2/14
 */
@SpringBootApplication
public class SpringTest {


    public static void main(String[] args) {
        System.out.println("=== Start Mini Spring ===");
        MiniApplicationContext context = new MiniApplicationContext(SpringTest.class);
//        ServiceA serviceA = (ServiceA) context.getBean("serviceA");
//        serviceA.sayHello();
        System.out.println("=== End Mini Spring ===");
    }
}
