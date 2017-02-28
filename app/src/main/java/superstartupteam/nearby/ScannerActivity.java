package superstartupteam.nearby;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import layout.EnterCodeFragment;
import layout.HistoryFragment;
import layout.SimpleScannerFragment;
import superstartupteam.nearby.model.User;

public class ScannerActivity extends AppCompatActivity implements
        SimpleScannerFragment.OnFragmentInteractionListener {
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private Button changeInputBtn;
    private Boolean qrFrag = true;
    private ImageButton backBtn;
    private String transactionId;
    private User user;
    private String heading;
    FragmentManager fragmentManager = getFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(ScannerActivity.this);
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            transactionId = extras.getString("TRANSACTION_ID");
            heading = extras.getString("HEADER");
        }
        setContentView(R.layout.activity_scanner);
        TextView header = (TextView) findViewById(R.id.confirm_exchange_text);
        header.setText(heading);
        changeInputBtn = (Button) findViewById(R.id.change_input_button);
        backBtn = (ImageButton) findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                .replace(R.id.scanner_layout, new SimpleScannerFragment(), Constants.SCANNER_FRAGMENT_TAG)
                .commit();
        changeInputBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qrFrag) {
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                            .replace(R.id.scanner_layout, new EnterCodeFragment(), Constants.SCANNER_FRAGMENT_TAG)
                            .commit();
                    qrFrag = false;
                    changeInputBtn.setText("Scan QR Code");
                } else {
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                            .replace(R.id.scanner_layout, new SimpleScannerFragment(), Constants.SCANNER_FRAGMENT_TAG)
                            .commit();
                    qrFrag = true;
                    changeInputBtn.setText("Type In Code");
                }

            }
        });
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.i("QR Scanner", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{android.Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity act = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(act, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(findViewById(R.id.scanner_layout), R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


    public void onFragmentInteraction(Uri url, String nextFragment) {
        Log.i("ScannerActivity", "onFragmentInteraction> arg = " + nextFragment);
    }

    public void verifyCode(final String code) {
        // AsyncTask<Params, Progress, Result>
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/transactions/" + transactionId
                            + "/code/" + code);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty("Content-Type", "application/json");
                    responseCode = conn.getResponseCode();
                    Log.i("PUT /transactions/code", "Response Code: " + responseCode);
                    if (responseCode != 204) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not verify exchange code: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 204) {
                    Intent intent=new Intent();
                    intent.putExtra("MESSAGE", "Confirmed Exchange!");
                    setResult(2,intent);
                    finish();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.scanner_layout), "Code did not match or is expired.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }.execute();
    }

}
