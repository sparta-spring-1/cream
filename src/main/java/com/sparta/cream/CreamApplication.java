package com.sparta.cream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CreamApplication {

  public static void main(String[] args) {
    SpringApplication.run(CreamApplication.class, args);
  }

}
