package org.example;

import minispring.annotation.Autowired;
import minispring.annotation.SpringBootApplication;
import minispring.core.MiniApplicationContext;
import org.example.src.ServiceA;

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
    }
}
