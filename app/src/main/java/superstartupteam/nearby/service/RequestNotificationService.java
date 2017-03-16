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
import superstartupteam.nearby.MainActivity;
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
                MainActivity.getRequests(latLng);
            }
        } else {
            Log.i("NotificationService", "user or latLng are null");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("RequestNotifications", "location changed*****");
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}
