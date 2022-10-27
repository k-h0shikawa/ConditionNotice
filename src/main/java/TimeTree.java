import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Properties;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TimeTree {

    String accessToken = "";
    String url = "https://timetreeapis.com";
    String calender_id = "";


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
        }catch (IOException e){
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }

    public String readEvent(){
        // 予定を取得する
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(this.url + "/calendars/" + this.calender_id))
                .header("Authorization", "Bearer " + this.accessToken)
                .header("Accept", "application/vnd.timetree.v1+json")
                .header("Content-Type", "application/json")
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return this.accessToken;
    }

    public void writeEvent(){
        // 予定を登録する
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(this.url + "/calendars/" + this.calender_id))
                .header("Authorization", "Bearer " + this.accessToken)
                .header("Accept", "application/vnd.timetree.v1+json")
                .header("Content-Type", "application/json")
                .build();
    }
}
