package superstartupteam.nearby.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 10/12/16.
 */
//public class RequestNotificationService extends Service implements LocationListener {
public class RequestNotificationService extends IntentService implements LocationListener {

    private User user;
    public static LatLng latLng;

    public RequestNotificationService() {
        super("RequestNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("RNotificationService", "Service running");
        user = PrefUtils.getCurrentUser(this);
        if (user != null && latLng != null) {
            final boolean hasNetwork = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            //only get notifications if app is in background
            Log.i("RequestNotificationSrvc", "currentLatLng: " + latLng.latitude + " * " + latLng.longitude);
            if (!AppUtils.isAppInForeground(this) && hasNetwork) {
                getRequests();
            }
        } else {
            Log.i("NotificationService", "user or latLng are null");
        }
    }

    private void getRequests() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/notifications" +
                            "?latitude=" + latLng.latitude + "&longitude=" + latLng.longitude);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    int responseCode = conn.getResponseCode();
                    Log.i("GET /notifications", "Response Code : " + responseCode);
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get notifications: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

            }
        }.execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("RequestNotifications", "location changed*****");
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}
