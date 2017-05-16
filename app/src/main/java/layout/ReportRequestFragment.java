package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Flag;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.User;

/**
 * Created by kelseykerr on 5/15/17.
 */

public class ReportRequestFragment extends DialogFragment {
    private static Request request;
    private static Response response;
    private User user;
    private static User userToReport;
    private static Boolean hasRequest = false;
    private static Boolean hasResponse = false;
    private Context context;
    private TextInputLayout additionalDetailsLayout;
    private TextInputEditText additionalDetails;
    private View view;
    private ImageButton closeBtn;
    private RelativeLayout spinnerScreen;
    private ScrollView scrollView;
    private Button blockUserBtn;
    private Button flagBtn;
    private TextView reportTitle;
    private static final String TAG = "ReportRequestFragment";


    public ReportRequestFragment() {

    }

    public static ReportRequestFragment newInstance(Request req, Response resp, User userReporting, boolean hasReq, boolean hasResp) {
        request = req;
        response = resp;
        userToReport = userReporting;
        hasRequest = hasReq;
        hasResponse = hasResp;
        ReportRequestFragment fragment = new ReportRequestFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // request a window without the title
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        View view = inflater.inflate(R.layout.fragment_report_dialog, container, false);
        reportTitle = (TextView) view.findViewById(R.id.report_title_text);
        if (hasRequest) {
            reportTitle.setText("Report Request");
        } else {
            reportTitle.setText("Report Response");
        }
        additionalDetails = (TextInputEditText) view.findViewById(R.id.additional_details);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.processing);
        spinnerScreen.setVisibility(View.GONE);
        scrollView = (ScrollView) view.findViewById(R.id.scrollview);
        scrollView.setVisibility(View.VISIBLE);
        blockUserBtn = (Button) view.findViewById(R.id.block_user_button);
        flagBtn = (Button) view.findViewById(R.id.flag_request_button);
        blockUserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                blockUserBtn.setEnabled(false);
                flagBtn.setEnabled(false);
                blockUser();
            }
        });
        flagBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                blockUserBtn.setEnabled(false);
                flagBtn.setEnabled(false);
                if (hasRequest) {
                    flagRequest();
                } else {
                    flagResponse();
                }
            }
        });
        closeBtn = (ImageButton) view.findViewById(R.id.cancel);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        this.view = view;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            //TODO: This doesn't work!!
            dialog.getWindow().setWindowAnimations(R.style.RequestDialog);
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }

    private void blockUser() {
        final String notes = additionalDetails.getText().toString();
        scrollView.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String blockId = hasRequest ? request.getUser().getId() : response.getSellerId();
                    String apiPath = "/users/" + blockId + "/flags";
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "POST", user);
                    Flag flag = new Flag(notes);
                    ObjectMapper mapper = new ObjectMapper();
                    String flagJson = mapper.writeValueAsString(flag);
                    byte[] outputInBytes = flagJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    String output = AppUtils.getResponseContent(conn);
                    Log.i(TAG, "POST /users/id/flags response code : " + responseCode);
                    if (responseCode != 200) {
                        Log.e(TAG, "Couldn't block user: " + responseCode + ": " + output);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "Could not create user flag: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                spinnerScreen.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                blockUserBtn.setEnabled(true);
                flagBtn.setEnabled(true);
                if (responseCode != null && responseCode == 200) {
                    dismiss();
                    if (hasRequest) {
                        ((MainActivity) getActivity()).goToHome("successfully blocked user");
                    } else {
                        HistoryFragment.dismissViewRequestFragment();
                        ((MainActivity) getActivity()).goToHistory("successfully blocked user");
                    }
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, "Could not block user at this time. Please contact us at info.nearby.app@gmail.com", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }

        }.execute();
    }

    private void flagRequest() {
        final String notes = additionalDetails.getText().toString();
        scrollView.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + request.getId() + "/flags";
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "POST", user);
                    Flag flag = new Flag(notes);
                    ObjectMapper mapper = new ObjectMapper();
                    String flagJson = mapper.writeValueAsString(flag);
                    byte[] outputInBytes = flagJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();
                    responseCode = conn.getResponseCode();
                    String output = AppUtils.getResponseContent(conn);
                    Log.i(TAG, "POST /requests/id/flags response code : " + responseCode);
                    if (responseCode != 200) {
                        Log.e(TAG, "Couldn't flag request user: " + responseCode + ": " + output);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "Could not flag request " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if(isAdded()){
                    getResources().getString(R.string.app_name);
                }
                spinnerScreen.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                blockUserBtn.setEnabled(true);
                flagBtn.setEnabled(true);
                if (responseCode != null && responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHome("successfully flagged request");
                } else {
                    String msg = "Could not flag request at this time. Please contact us at info.nearby.app@gmail.com";
                    if (responseCode == 406) {
                        msg = "You already flagged this request. It will be reviewed shortly. Thanks!";
                    }
                    Snackbar snackbar = Snackbar
                            .make(view, msg, Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();

    }

    private void flagResponse() {
        final String notes = additionalDetails.getText().toString();
        scrollView.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + request.getId() + "/responses/" + response.getId() + "/flags";
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "POST", user);
                    Flag flag = new Flag(notes);
                    ObjectMapper mapper = new ObjectMapper();
                    String flagJson = mapper.writeValueAsString(flag);
                    byte[] outputInBytes = flagJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();
                    responseCode = conn.getResponseCode();
                    String output = AppUtils.getResponseContent(conn);
                    Log.i(TAG, "POST responses/id/flags response code : " + responseCode);
                    if (responseCode != 200) {
                        Log.e(TAG, "Couldn't flag response: " + responseCode + ": " + output);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "Could not flag response " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if(isAdded()){
                    getResources().getString(R.string.app_name);
                }
                spinnerScreen.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                blockUserBtn.setEnabled(true);
                flagBtn.setEnabled(true);
                if (responseCode != null && responseCode == 200) {
                    dismiss();
                    HistoryFragment.dismissViewRequestFragment();
                    ((MainActivity) getActivity()).goToHistory("successfully flagged response");
                } else {
                    String msg = "Could not flag response at this time. Please contact us at info.nearby.app@gmail.com";
                    if (responseCode == 406) {
                        msg = "You already flagged this response. It will be reviewed shortly. Thanks!";
                    }
                    Snackbar snackbar = Snackbar
                            .make(view, msg, Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }
}
