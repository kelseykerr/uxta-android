package iuxta.nearby;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import iuxta.nearby.model.BaseEntity;
import iuxta.nearby.model.History;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 8/21/16.
 */
public class AppUtils {

    private static final String TAG = "AppUtils";
    public static final Currency USD = Currency.getInstance("USD");
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    public static final Double DEFAULT_REQUEST_RADIUS = .25;


     public static <T extends BaseEntity> T jsonStringToPojo(Class<T> c, String jsonString) throws IOException {
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
            Log.e(TAG, "error converting string to response list: " + e.getMessage());
            throw new IOException(e);
        }
    }

    public static List<History> jsonStringToHistoryList(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<History> pojos = mapper.readValue(jsonString, new TypeReference<List<History>>() {});
            return pojos;
        } catch (IOException e) {
            Log.e(TAG, "error converting string to history list: " + e.getMessage());
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
        if (start == null) {
            return "";
        }
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

    public static boolean validateString(String s) {
        return s != null && s.length() > 0;
    }

    public static BigDecimal formatCurrency(Double value) {
        BigDecimal formattedValue = BigDecimal.valueOf(value);
        return formattedValue.setScale(AppUtils.USD.getDefaultFractionDigits(), AppUtils.DEFAULT_ROUNDING);
    }

    public static boolean canAddPayments(User user) {
        return validateField(user.getFirstName()) &&
                validateField(user.getLastName()) &&
                validateField(user.getPhone()) &&
                validateField(user.getEmail()) &&
                validateField(user.getDateOfBirth()) &&
                validateField(user.getAddress()) &&
                validateField(user.getCity()) &&
                validateField(user.getState()) &&
                validateField(user.getZip());
    }

    public static boolean validateField(String field) {
        return field != null && !field.isEmpty();
    }

}
