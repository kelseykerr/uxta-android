package superstartupteam.nearby;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 10/15/16.
 */
public class SharedAsyncMethods {

    public static void getUserInfoFromServer(final User user, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    Log.i("***user***", output);
                    try {
                        User userFromServer = AppUtils.jsonStringToPojo(User.class, output);
                        userFromServer.setFacebookId(user.getFacebookId());
                        userFromServer.setAccessToken(user.getAccessToken());
                        PrefUtils.setCurrentUser(userFromServer, context);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to read " +
                                "user info from server: " + e.getMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get user info from server: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
