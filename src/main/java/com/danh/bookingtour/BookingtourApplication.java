package com.danh.bookingtour;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.danh.bookingtour.mapper")
public class BookingtourApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingtourApplication.class, args);
	}

}
