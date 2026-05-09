package com.motorplus.motorplus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MotorplusApplicationTests {

	@Test
	@Disabled("Requiere conexión a PostgreSQL - se omite en CI")
	void contextLoads() {
	}

}
