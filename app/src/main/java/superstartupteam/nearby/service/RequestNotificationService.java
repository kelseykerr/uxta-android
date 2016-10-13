package superstartupteam.nearby.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 10/12/16.
 */
public class RequestNotificationService extends Service implements LocationListener {

    private User user;
    private LatLng latLng;

    private enum State {
        IDLE, WORKING;
    }

    private static State state;

    private LocationManager locationManager;
    private PowerManager.WakeLock wakeLock;

    static {
        state = State.IDLE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationUpdaterService");
        user = PrefUtils.getCurrentUser(this);
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (state == State.IDLE) {
            state = State.WORKING;
            this.wakeLock.acquire();
            user = PrefUtils.getCurrentUser(this);
            //only get notifications if app is in background
            Log.i("RequestNotificationSrvc", "currentLatLng: " + latLng.latitude + " * " + latLng.longitude);
            if (!AppUtils.isAppInForeground(this)) {

            }

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        state = State.IDLE;
        if (this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
    }

    private void getRequests() {
        // send to server in background thread. you might want to start AsyncTask here
    }

    private void onSendingFinished() {
        // call this after sending finished to stop the service
        this.stopSelf(); //stopSelf will call onDestroy and the WakeLock releases.
        //Be sure to call this after everything is done (handle exceptions and other stuff) so you release a wakeLock
        //or you will end up draining battery like hell
    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }
}
