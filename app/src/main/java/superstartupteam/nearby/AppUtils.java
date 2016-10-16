package superstartupteam.nearby;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import superstartupteam.nearby.model.BaseEntity;
import superstartupteam.nearby.model.History;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.User;

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
            Log.e("***", "error converting string to response list: " + e.getMessage());
            throw new IOException(e);
        }
    }

    public static List<History> jsonStringToHistoryList(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<History> pojos = mapper.readValue(jsonString, new TypeReference<List<History>>() {});
            return pojos;
        } catch (IOException e) {
            Log.e("***", "error converting string to history list: " + e.getMessage());
            throw new IOException(e);
        }
    }

    public static String getResponseContent(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
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
        long diffDays = diff / (1000 * 60 * 60 * 24);
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

    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
