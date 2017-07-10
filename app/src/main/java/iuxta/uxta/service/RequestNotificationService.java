package iuxta.uxta.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import iuxta.uxta.AppUtils;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.model.User;

/**
 * Created by kerrk on 10/12/16.
 */
public class RequestNotificationService extends IntentService implements LocationListener {

    private static final String TAG = "RequestNotificationServ";
    private User user;
    public static LatLng latLng;

    public RequestNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Service running");
        user = PrefUtils.getCurrentUser(this);
        if (user != null && latLng != null) {
            final boolean hasNetwork = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            //only get notifications if app is in background
            Log.i(TAG, "currentLatLng: " + latLng.latitude + " * " + latLng.longitude);
            if (!AppUtils.isAppInForeground(this) && hasNetwork) {
                MainActivity.getRequests(latLng);
            }
        } else {
            Log.i(TAG, "user or latLng are null");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "location changed*****");
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}
