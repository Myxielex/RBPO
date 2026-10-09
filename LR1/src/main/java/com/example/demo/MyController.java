package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Привет мир!";
    }

    @GetMapping("/numbers")
    public int[] getNumbers() {
        return new int[]{10, 20, 30};
    }
}
