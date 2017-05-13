package iuxta.nearby;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by kerrk on 1/24/17.
 */
public class GoogleApiClientSingleton extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleApiClientSingleto";
    private static GoogleApiClientSingleton instance;

    private static GoogleApiClient mGoogleApiClient = null;

    private GoogleApiClientSingleton() {
    }

    public static GoogleApiClientSingleton getInstance(GoogleApiClient googleApiClient) {
        if(instance == null) {
            instance = new GoogleApiClientSingleton();
            if (mGoogleApiClient == null) {
                mGoogleApiClient = googleApiClient;
            }

        }
        return instance;
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
