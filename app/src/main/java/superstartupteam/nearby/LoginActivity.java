package superstartupteam.nearby;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import superstartupteam.nearby.model.User;

public class LoginActivity extends AppCompatActivity {

    private TextView info;
    private LoginButton loginButton;
    private Button fb;
    private CallbackManager callbackManager;
    private User user;

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if(user != null && user.getAccessToken() != null){
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if(user != null && user.getAccessToken() != null){
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(user != null && user.getAccessToken() != null){
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    } // Always call the superclass method first

    @Override
    public void onRestart() {
        super.onRestart();
        if(user != null && user.getAccessToken() != null){
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        user = PrefUtils.getCurrentUser(LoginActivity.this);
        if(user != null && user.getAccessToken() != null && user.getId() != null){
            Log.i("Current token: ", user.getAccessToken() + " ****************");
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
        info = (TextView) findViewById(R.id.info);
        fb = (Button) findViewById(R.id.fb);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    Log.i("token-> ", loginResult.getAccessToken().getToken() +
                                            " <- token");
                                    user = new User();
                                    user.setFacebookId(object.getString("id").toString());
                                    user.setAccessToken(loginResult.getAccessToken().getToken());
                                    PrefUtils.setCurrentUser(user, LoginActivity.this);

                                    SharedAsyncMethods.getUserInfoFromServer(user, LoginActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
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
    }

    /*public void getUserInfoFromServer() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    Log.i("***user***", output);
                    try {
                        User userFromServer = AppUtils.jsonStringToPojo(User.class, output);
                        userFromServer.setFacebookId(user.getFacebookId());
                        userFromServer.setAccessToken(user.getAccessToken());
                        PrefUtils.setCurrentUser(userFromServer, LoginActivity.this);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to read " +
                                "user info from server: " + e.getMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get user info from server: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }*/

    public void onClick(View v) {
        if (v == fb) {
            loginButton.performClick();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
