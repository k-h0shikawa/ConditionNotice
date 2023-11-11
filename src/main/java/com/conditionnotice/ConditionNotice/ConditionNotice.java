package com.conditionnotice.ConditionNotice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class ConditionNotice {

    private final Logger logger = LoggerFactory.getLogger(ConditionNotice.class);

    @Autowired
    private GmailFetch gmailFetch;

    @Autowired
    private GoogleCalendarFetch googleCalendarFetch;

    @Autowired
    private GoogleCalendarInsert googleCalendarInsert;

    // @Scheduled(fixedRate = 10000)
    @Scheduled(cron="0 1 * * *")
    public void registerNextMenstrualDay() {
        logger.info("ConditionNotice開始");

        logger.info("gmailから日付を取得開始");
        var body = gmailFetch.fetchMailBody();
        // メールを取得できなければ終了
        if(body.equals("")){
            logger.info("メールを正常に取得できませんでした");
            return ;
        }

        var nextMenstrualDate = trimNextMenstrualDate(body);
        logger.info("次回の生理予定日 : " + nextMenstrualDate);

        var isRegistered = googleCalendarFetch.isNextMenstrualDayRegistered(nextMenstrualDate);
        if(!isRegistered) googleCalendarInsert.insertEvent(nextMenstrualDate);

        logger.info("ConditionNotice正常終了");
    }

    private LocalDateTime trimNextMenstrualDate(String body) {

        // 特定の文字が含まれる行を取得
        var nextMenstrualDate = Arrays.stream(body.split("\n"))
                .filter(line -> line.contains("次回の生理予定日"))
                .map(line -> line.substring(line.indexOf("：") + 1))
                .findFirst()
                .orElse("");

        var nextMenstrualDateList= Arrays.asList(nextMenstrualDate.split("年|月|日"));

        var year = Integer.parseInt(nextMenstrualDateList.get(0));
        var month = Integer.parseInt(nextMenstrualDateList.get(1));
        var day = Integer.parseInt(nextMenstrualDateList.get(2));

        return LocalDateTime.of(year, month, day, 0, 0, 0);
    }
}