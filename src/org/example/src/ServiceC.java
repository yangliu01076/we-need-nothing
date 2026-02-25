package org.example.src;

import org.example.minispring.annotation.Bean;
import org.example.minispring.annotation.Component;

/**
 * @author duoyian
 * @date 2026/2/25
 */
@Component
public class ServiceC {

    @Bean
    public ServiceD serviceD() {
        return new ServiceD();
    }
}
