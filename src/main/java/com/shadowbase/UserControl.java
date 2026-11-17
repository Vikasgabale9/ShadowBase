package com.shadowbase;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserControl {

    @GetMapping
    public String getUsers() {
        return "Welcome to ShadowBase!";
    }
}