package com.pyx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/10/31 11:18
 */
@Configuration
public class ServiceConfig {

    @Value("${test.address.city}")
    private String address;

    @Value("${test.age}")
    private String age;

    @Value("${test.name}")
    private String name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceConfig() {
    }

}
