package iuxta.nearby;

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
import layout.SimpleScannerFragment;
import iuxta.nearby.model.User;

public class ScannerActivity extends AppCompatActivity implements
        SimpleScannerFragment.OnFragmentInteractionListener {
    // permission request codes need to be < 256
    private Button changeInputBtn;
    private Boolean qrFrag = true;
    private ImageButton backBtn;
    private String transactionId;
    private User user;
    private String heading;
    FragmentManager fragmentManager = getFragmentManager();
    private static final String TAG = "ScannerActivity";
    private static final int REQUEST_CAMERA = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(ScannerActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(android.Manifest.permission.CAMERA,
                    R.string.permission_camera_rationale, REQUEST_CAMERA);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.fragment_enter_code);
            TextView header = (TextView) findViewById(R.id.confirm_exchange_text);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                transactionId = extras.getString("TRANSACTION_ID");
                heading = extras.getString("HEADER");
                header.setText(heading);
            }
            changeInputBtn = (Button) findViewById(R.id.change_input_button);
            backBtn = (ImageButton) findViewById(R.id.back_button);
            backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                    .replace(R.id.scanner_layout, new EnterCodeFragment(), Constants.CODE_FRAGMENT_TAG)
                    .commit();
            changeInputBtn.setVisibility(View.GONE);
        } else {
            setContentView(R.layout.activity_scanner);
            TextView header = (TextView) findViewById(R.id.confirm_exchange_text);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                transactionId = extras.getString("TRANSACTION_ID");
                heading = extras.getString("HEADER");
                header.setText(heading);
            }
            changeInputBtn = (Button) findViewById(R.id.change_input_button);
            changeInputBtn.setVisibility(View.VISIBLE);
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

    }


    private void requestPermission(final String permission, int rationale, final int requestCode) {
        Log.e(TAG, permission + " permission has NOT been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            final Activity act = this;
            Snackbar.make(findViewById(R.id.scanner_layout), rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(act,
                                    new String[]{permission},
                                    requestCode);
                        }
                    })
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    requestCode);
        }
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
                            .make(findViewById(R.id.scanner_layout), "Code did not match or is expired.", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();
    }

}
