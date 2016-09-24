package superstartupteam.nearby.model;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;

/**
 * Created by kerrk on 9/13/16.
 */
public class GetResponses extends AsyncTask<String, Void, List<History>> {
    public AsyncResponse delegate = null;

    @Override
    protected List<History> doInBackground(String... var1) {
        try {
            URL url = new URL(Constants.NEARBY_API_PATH + "/users/me/history");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(Constants.AUTH_HEADER, var1[0]);
            String output = AppUtils.getResponseContent(conn);
            try {
                List<History> recentHistory = AppUtils.jsonStringToHistoryList(output);
                return recentHistory;
            } catch (IOException e) {
                Log.e("Error", "Received an error while trying to fetch " +
                        "requests from server, please try again later!");
            }
        } catch (IOException e) {
            Log.e("ERROR ", "Could not get requests: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<History> history) {
        delegate.processFinish(history);
    }

}
