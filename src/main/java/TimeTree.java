import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Properties;
import java.net.URI;
import org.json.JSONObject;

public class TimeTree {

    String accessToken = "";
    String url = "https://timetreeapis.com";
    String calender_id = "";
    HttpRequest request = null;


    public TimeTree() {
        // API TOKENの読み込み
        Properties properties = new Properties();
        try(
                FileInputStream file = new FileInputStream("src/main/resources/timeTree.properties");
                InputStreamReader input = new InputStreamReader(file, Charset.defaultCharset());
            ){
            properties.load(input);
            this.accessToken = properties.getProperty("ACCESS_TOKEN");
            this.calender_id = properties.getProperty("CALENDER_ID");
            this.request = HttpRequest.newBuilder(
                            URI.create(this.url + "/calendars/" + this.calender_id))
                    .header("Authorization", "Bearer " + this.accessToken)
                    .header("Accept", "application/vnd.timetree.v1+json")
                    .header("Content-Type", "application/json")
                    .build();
        }catch (IOException e){
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

        //

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(this.url + "/calendars/" + this.calender_id + "/events"))
                .header("Authorization", "Bearer " + this.accessToken)
                .header("Accept", "application/vnd.timetree.v1+json")
                .header("Content-Type", "application/json")
                .build();



    }

    public JSONObject convertJsonFormat(){
        JSONObject attributes = new JSONObject();
        attributes.put("category", "schedule");
        attributes.put("title", "javaです\uD83D\uDE0A");
        attributes.put("all_day", "true");
        attributes.put("start_at", "2022-10-04T00:00:00.000Z");
        attributes.put("start_timezone", "UTC");
        attributes.put("end_at", "2022-10-04T00:00:00.000Z");
        attributes.put("end_timezone", "UTC");
        attributes.put("description", "これはテストです");

        JSONObject relationships_data = new JSONObject();
        relationships_data.put("id", this.calender_id);
        relationships_data.put("type", "label");

        JSONObject label = new JSONObject();
        label.put("label", relationships_data);

        JSONObject event = new JSONObject();
        event.put("attributes", attributes);
        event.put("relationships", label);

        JSONObject data = new JSONObject();
        data.put("data", event);


        return data;
    }
}
