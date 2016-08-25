package impulusecontrol.lend;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;

import impulusecontrol.lend.model.Request;

/**
 * Created by kerrk on 8/21/16.
 */
public class AppUtils {

     public static <T extends BaseEntity> T jsonStringToPojo(Class<T> c, String jsonString) throws IOException{
         ObjectMapper mapper = new ObjectMapper();
         T pojo = mapper.readValue(jsonString, c);
         return pojo;
    }

    public static List<Request> jsonStringToRequestList(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Request> pojos = mapper.readValue(jsonString, new TypeReference<List<Request>>() {});
            return pojos;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static String getResponseContent(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        Log.i("GET /api/requests", "Response Code : " + responseCode);
        BufferedReader br;
        if (responseCode <= 200  && responseCode <= 299) {
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        }  else {
            br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
        }
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        return sb.toString();
    }

    public static String getTimeDiffString(Date start) {
        long diff = new Date().getTime() - start.getTime();

        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffDays = (int) diff / (1000 * 60 * 60 * 24);

        if (diffDays > 1) {
            return diffDays + " days ago";
        } else if (diffDays == 1) {
            return diffDays + " day ago";
        } else if (diffHours >= 1) {
            return diffHours + " hours ago";
        } else if (diffMinutes > 1) {
            return diffMinutes + " minutes ago";
        } else {
            return "a moment ago";
        }
    }
}
