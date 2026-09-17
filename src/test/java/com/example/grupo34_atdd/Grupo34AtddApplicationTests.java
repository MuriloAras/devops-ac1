package com.example.grupo34_atdd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class Grupo34AtddApplicationTests {

    @Test
    void contextLoads() {
        Grupo34AtddApplication app = new Grupo34AtddApplication();
        assertNotNull(app);
    }

}
