package impulusecontrol.lend;

import android.content.Intent;
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
import android.widget.TextView;

import org.json.JSONObject;

import impulusecontrol.lend.model.User;

public class LoginActivity extends AppCompatActivity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private User user;

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if(user != null && user.getAccessToken() != null){
            Intent homeIntent = new Intent(LoginActivity.this, LandingActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onStart() {
        super.onStart();  // Always call the superclass method first
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        user = PrefUtils.getCurrentUser(LoginActivity.this);
        if(user != null && user.getAccessToken() != null){
            Log.e("Current token: ", user.getAccessToken());
            Log.e("***", "**come on");
            Intent homeIntent = new Intent(LoginActivity.this, LandingActivity.class);
            startActivity(homeIntent);
            finish();
        }
        info = (TextView) findViewById(R.id.info);
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
                                    Log.e("token: ", loginResult.getAccessToken().getToken());
                                    user = new User();
                                    user.setFacebookId(object.getString("id").toString());
                                    String email = object.has("email") ?
                                            object.getString("email").toString() : null;
                                    user.setEmail(email);
                                    user.setName(object.getString("name").toString());
                                    String gender = object.has("gender") ?
                                            object.getString("gender").toString() : null;
                                    user.setGender(gender);
                                    user.setUserId(loginResult.getAccessToken().getUserId());
                                    user.setAccessToken(loginResult.getAccessToken().getToken());
                                    PrefUtils.setCurrentUser(user, LoginActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
