package iuxta.uxta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import iuxta.uxta.service.RequestNotificationService;

/**
 * Created by kerrk on 10/12/16.
 *
 * Hits the server to fetch recurring notifications (recent requests)
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RequestNotificationService.class);
        context.startService(i);
    }
}
