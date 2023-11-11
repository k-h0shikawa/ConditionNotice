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
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Component
public class GmailFetch {

    private final Logger logger = LoggerFactory.getLogger(GmailFetch.class);

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private final String CLIENT_SECRET_DIR = "client_secret.json";
    private final String CREDENTIALS_FOLDER = "credentials/gmail";

    private final String APPLICATION_NAME = "ConditionNotice";
    private final String USER = "me";

    private final String Label_ID = System.getenv("GMAIL_LABEL_ID");


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
        var credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public String fetchMailBody(){

        try {
            // HTTP通信
            var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            var credential = getCredentials(HTTP_TRANSPORT);

            // Gmailにアクセス
            var service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            // メールを全て取得
            var messagesResponse = service.users().messages().list(USER)
                    .setLabelIds(List.of(Label_ID)).execute();

            // 最新のメール内容を取得
            var latestMessage = service.users()
                    .messages().get(USER, messagesResponse.getMessages().get(0).getId()).execute();


            // 本文の取得
            // Base64からbyte列へデコード
            var bodyBytes = Base64.decodeBase64(latestMessage.getPayload().getBody().getData());
            // byte列からStringに変換
            var body = new String(bodyBytes, "UTF-8");

            return body;

        } catch (IOException | GeneralSecurityException e) {
            logger.error("", e);
        }
        return "";
    }
}
