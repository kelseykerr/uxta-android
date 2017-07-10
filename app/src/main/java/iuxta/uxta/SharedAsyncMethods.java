package iuxta.uxta;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import layout.AccountFragment;
import layout.HomeFragment;
import iuxta.uxta.model.User;

import static android.content.Context.WIFI_SERVICE;

public class SharedAsyncMethods {

    public static String errorMessage;

    public static void getUserInfoFromServer(final User user, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty(Constants.IP_HEADER, ip);
                    Integer responseCode = conn.getResponseCode();
                    String output = AppUtils.getResponseContent(conn);

                    if (responseCode != 200) {
                        throw new IOException(output);
                    }

                    try {
                        User userFromServer = AppUtils.jsonStringToPojo(User.class, output);
                        userFromServer.setFacebookId(user.getFacebookId());
                        userFromServer.setAccessToken(user.getAccessToken());
                        userFromServer.setAuthMethod(user.getAuthMethod());
                        PrefUtils.setCurrentUser(userFromServer, context);
                        HomeFragment.user = userFromServer;
                        RequestAdapter.user = userFromServer;
                        MainActivity.user = userFromServer;
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
    }

    public static void updateUser(final User user, final Context context) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return executeUpdate(user, context);
            }
        }.execute();
    }

    public static void updateUserPayment(final User user, final Context context,
                                       final MainActivity mainActivity) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/stripe/creditcard");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty("Content-Type", "application/json");
                    ObjectMapper mapper = new ObjectMapper();
                    String updateJson = mapper.writeValueAsString(user);
                    Log.i("updated user: ", updateJson);
                    byte[] outputInBytes = updateJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();

                    Log.i("POST /creditcard", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String message = AppUtils.getResponseContent(conn);
                        throw new IOException(message);
                    }
                    SharedAsyncMethods.getUserInfoFromServer(user, context);
                    return responseCode;
                } catch (IOException e) {
                    errorMessage = "Could not update user payment: " + e.getMessage();
                    Log.e("ERROR ", errorMessage);
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }
                    AccountFragment.dismissPaymentDialog();
                    mainActivity.goToAccount("successfully updated payment info");
                } else {
                    AccountFragment.dismissPaymentDialog();
                    mainActivity.goToAccount(errorMessage);
                }
            }

        }.execute();
    }

    public static void updateUser(final User user, final Context context,
                                  final MainActivity mainActivity, final View view) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return executeUpdate(user, context);
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }
                    AccountFragment.dismissUpdateAccountDialog();
                    mainActivity.goToAccount("successfully updated account info");
                } else {
                    AccountFragment.dismissUpdateAccountDialog();
                    mainActivity.goToAccount(errorMessage);
                }
            }

        }.execute();
    }

    private static Integer executeUpdate(User user, Context context) {
        Integer responseCode;
        try {
            URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
            conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
            conn.setRequestProperty("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String updateJson = mapper.writeValueAsString(user);
            Log.i("updated account: ", updateJson);
            byte[] outputInBytes = updateJson.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(outputInBytes);
            os.close();

            responseCode = conn.getResponseCode();

            Log.i("PUT /users/me", "Response Code : " + responseCode);
            if (responseCode != 200) {
                String message = AppUtils.getResponseContent(conn);
                throw new IOException(message);
            }
            SharedAsyncMethods.getUserInfoFromServer(user, context);
            return responseCode;
        } catch (IOException e) {
            errorMessage = "Could not update account: " + e.getMessage();
            Log.e("ERROR ", errorMessage);
        }
        return 0;
    }

}
