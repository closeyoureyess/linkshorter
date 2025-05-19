package com.effective.linkshorter;

import org.springframework.boot.SpringApplication;

public class TestLinkshorterApplication {

	public static void main(String[] args) {
		SpringApplication.from(LinkshorterApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
