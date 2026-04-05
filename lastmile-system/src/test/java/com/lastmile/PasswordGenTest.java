package com.lastmile;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("test123");
        System.out.println("Hash for test123: " + hash);
    }
}
