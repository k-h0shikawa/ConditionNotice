package com.conditionnotice.ConditionNotice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class ConditionNoticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConditionNoticeApplication.class, args);
	}

}
