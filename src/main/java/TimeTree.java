import java.io.*;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Properties;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class TimeTree {

    String accessToken = "";
    String url = "https://timetreeapis.com";
    String calenderId = "";
    HttpRequest request = null;
    final Logger logger = Logger.getLogger("TimeTreeClass");


    public TimeTree() {
        // API TOKENの読み込み
        Properties properties = new Properties();
        try{
            // this.accessToken = properties.getProperty("ACCESS_TOKEN");
            this.accessToken = System.getenv("TIMETREEACCESSTOKEN");
            System.out.println(this.accessToken);
            // this.calender_id = properties.getProperty("timeTreeCalenderId");
            this.calenderId = System.getenv("TIMETREECALENDERID");
            System.out.println(this.calenderId);
            this.request = HttpRequest.newBuilder(
                            URI.create(this.url + "/calendars/" + this.calenderId))
                    .header("Authorization", "Bearer " + this.accessToken)
                    .header("Accept", "application/vnd.timetree.v1+json")
                    .header("Content-Type", "application/json")
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }

    public String readEvent(){
        // 予定を取得する
        HttpClient client = HttpClient.newHttpClient();
        String ret_val = "";
        try {
            var response = client.send(this.request, HttpResponse.BodyHandlers.ofString());
            ret_val = response.body();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return ret_val;
    }

    /**
     * 予定を登録する
     */
    public void writeEvent(){

        HttpURLConnection connection = null;
        try{
            String event = convertJsonFormat();

            URL url = new URL(this.url + "/calendars/" + this.calenderId + "/events");

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Authorization", "Bearer " + this.accessToken);
            connection.setRequestProperty("Accept", "application/vnd.timetree.v1+json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            connection.connect();
            PrintStream ps = new PrintStream(connection.getOutputStream());
            ps.print(event);
            ps.close();


            int responseStatus = connection.getResponseCode();

            if(responseStatus != 201){
                InputStream stream = connection.getErrorStream();
                if (null == stream) {
                    System.out.println("InputStream を参照します");
                    stream = connection.getInputStream();
                }
                String line = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                while ((line = br.readLine()) != null) {
                    logger.log(Level.WARNING, line);
                }
            }else{
                logger.log(Level.INFO, "イベント登録処理が完了しました。");
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(connection != null) connection.disconnect();
        }
    }

    private String convertJsonFormat(){
        JSONObject attributes = new JSONObject();
        attributes.put("category", "schedule");
        attributes.put("title", "register event by java");
        attributes.put("all_day", true);
        attributes.put("start_at", "2022-10-05T00:00:00.000Z");
        attributes.put("start_timezone", "UTC");
        attributes.put("end_at", "2022-10-05T00:00:00.000Z");
        attributes.put("end_timezone", "UTC");
        attributes.put("description", "これはテストです");

        JSONObject relationships_data = new JSONObject();
        relationships_data.put("id", this.calenderId + ",1");
        relationships_data.put("type", "label");

        JSONObject label = new JSONObject();
        label.put("data", relationships_data);

        JSONObject relationships = new JSONObject();
        relationships.put("label", label);


        JSONObject data = new JSONObject();
        data.put("relationships", relationships);
        data.put("attributes", attributes);

        JSONObject event = new JSONObject();
        event.put("data", data);


        return event.toString();
    }
}
