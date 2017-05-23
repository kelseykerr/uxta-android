package iuxta.nearby;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONObject;

import iuxta.nearby.model.User;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView info;
    private LoginButton facebookLoginButton;
    private SignInButton googleSignInBtn;
    private static final int RC_SIGN_IN_GOOGLE = 9001;
    private Button fb;
    private Button google;
    private CallbackManager callbackManager;
    private User user;
    public static GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LoginActivity";
    private ProgressDialog mProgressDialog;
    private TextView termsText;


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (hasAccessToken()) {
            goToMainActivity();
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (hasAccessToken()) {
            goToMainActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (user != null && user.getAuthMethod() != null && user.getAuthMethod().equals(Constants.GOOGLE_AUTH_METHOD)) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        } else if (hasAccessToken()) {
            goToMainActivity();
        }
    } // Always call the superclass method first

    @Override
    public void onRestart() {
        super.onRestart();
        if (hasAccessToken()) {
            goToMainActivity();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient = GoogleApiClientSingleton.getInstance(googleApiClient).getGoogleApiClient();
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        user = PrefUtils.getCurrentUser(LoginActivity.this);
        if(hasAccessToken() && user.getId() != null){
            Log.i("Current token: ", user.getAccessToken() + " ****************");
            goToMainActivity();
        }
        info = (TextView) findViewById(R.id.info);
        fb = (Button) findViewById(R.id.fb);
        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                facebookLoginSuccess(loginResult);
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }
        });
        google = (Button) findViewById(R.id.google_auth);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                google.setEnabled(false);
                if (user != null) {
                    user.setAuthMethod(Constants.GOOGLE_AUTH_METHOD);
                }
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
            }
        });
        googleSignInBtn = (SignInButton) findViewById(R.id.google_sign_in_button);
        termsText = (TextView) findViewById(R.id.terms_text);
        String termsTxt=getResources().getString(R.string.termsText);
        termsText.setMovementMethod(LinkMovementMethod.getInstance());
        termsText.setText(Html.fromHtml(termsTxt));
    }

    public void onClick(View v) {
        if (v == fb) {
            facebookLoginButton.performClick();
            if (user != null) {
                user.setAuthMethod(Constants.FB_AUTH_METHOD);
            }
        }
    }

    public void facebookLoginSuccess(final LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            Log.i(TAG, "token-> " + loginResult.getAccessToken().getToken());
                            user = new User();
                            user.setAuthMethod(Constants.FB_AUTH_METHOD);
                            user.setFacebookId(object.getString("id").toString());
                            user.setAccessToken(loginResult.getAccessToken().getToken());
                            PrefUtils.setCurrentUser(user, LoginActivity.this);
                            SharedAsyncMethods.getUserInfoFromServer(user, LoginActivity.this);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            Thread.sleep(2000);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            if (user != null) {
                user.setAuthMethod(Constants.GOOGLE_AUTH_METHOD);
            }
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            try {
                Log.i(TAG, "token-> " + acct.getIdToken());
                user = new User();
                user.setGoogleId(acct.getId());
                user.setAccessToken(acct.getIdToken());
                user.setAuthMethod(Constants.GOOGLE_AUTH_METHOD);
                PrefUtils.setCurrentUser(user, LoginActivity.this);
                SharedAsyncMethods.getUserInfoFromServer(user, LoginActivity.this);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                Thread.sleep(2000);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
            Log.e(TAG, "error signing in: " + result.toString() + "**" + result.getStatus().getStatusCode());
            google.setEnabled(true);
            //TODO: show error message
        }
    }

    public void handleGoogleSignout(final Context context) {
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "User Logged out");
                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API Client Connection Suspended");
            }

        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private boolean hasAccessToken() {
        return user != null && user.getAccessToken() != null;
    }

    private void goToMainActivity() {
        Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(homeIntent);
        finish();
    }

}
