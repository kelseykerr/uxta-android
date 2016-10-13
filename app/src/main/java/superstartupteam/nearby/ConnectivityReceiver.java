package superstartupteam.nearby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;

import superstartupteam.nearby.service.RequestNotificationService;

/**
 * Created by kerrk on 10/12/16.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent wakeupIntent = PendingIntent.getService(context, 0,
                new Intent(context, RequestNotificationService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //make sure the phone is connected to the network
        final boolean hasNetwork = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (hasNetwork) {
            // start service now for doing once
            context.startService(new Intent(context, RequestNotificationService.class));

            // schedule service for every 15 minutes
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, wakeupIntent);
        } else {
            alarmManager.cancel(wakeupIntent);
        }
    }
}
