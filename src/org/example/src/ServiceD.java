package org.example.src;

import org.example.common.utils.StringUtil;

/**
 * @author duoyian
 * @date 2026/2/25
 */
public class ServiceD {

    private String name;

    public String sayHello() {
        String string = StringUtil.isEmpty(name) ? "Hello, I am D" : "Hello, I am " + name;
        System.out.println(string);
        return string;
    }

    public void setName(String c) {
        this.name = c;
    }
}
