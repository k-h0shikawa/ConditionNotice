package com.conditionnotice.ConditionNotice;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class GoogleCalendarFetch {
    
    private final Logger logger = LoggerFactory.getLogger(GoogleCalendarFetch.class);

    private final String APPLICATION_NAME = "ConditionNotice";

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private final String CLIENT_SECRET_DIR = "client_secret.json";
    private final String CREDENTIALS_FOLDER = "credentials/google-calendar/fetch";

    // 共有用ID
    private final String CALENDAR_ID = System.getenv("GOOGLE_CALENDAR_ID")	;

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // 秘密鍵の読み込み
        var in = new FileInputStream(CLIENT_SECRET_DIR);
        var reader = new InputStreamReader(in);
        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        // 認証の設定
        var flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline").build();
        // 認証を行う
        var credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");        //returns an authorized Credential object.
        return credential;
    }

    public boolean isNextMenstrualDayRegistered(LocalDateTime nextMenstrualDate){
        logger.info(" -- イベント取得 -- ");
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        // minusHours(9)はAsia時間にするための帳尻合わせ
        var formattedStartDateTime = nextMenstrualDate.minusHours(9).format(formatter);
        var formattedEndDateTime = nextMenstrualDate.minusHours(9).plusDays(1).format(formatter);

        try {
            var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            var service =
                    new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();

            var start = new DateTime(formattedStartDateTime);
            var end = new DateTime(formattedEndDateTime);
            var events = service.events().list(CALENDAR_ID).setTimeMin(start).setTimeMax(end).execute();

            if(isRegistered(events.getItems(), "\uD83D\uDC3C")){
                logger.info("既に登録済みです");
                return true;
            }else{
                logger.info("まだ登録されていません");
                return false;
            }
        } catch (IOException |GeneralSecurityException e) {
            logger.error("", e);
        }
        return false;
    }

    private boolean isRegistered(List<Event> events, String targetSummary) {
        return events.stream().anyMatch(it -> it.getSummary().equals(targetSummary));
    }

}
