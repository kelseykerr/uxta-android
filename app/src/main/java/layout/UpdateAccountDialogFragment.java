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
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.SharedAsyncMethods;
import superstartupteam.nearby.model.User;


public class UpdateAccountDialogFragment extends DialogFragment {
    private Context context;
    private User user;
    private EditText addressLine1;
    private EditText addressLine2;
    private EditText city;
    private EditText state;
    private EditText zip;
    private EditText email;
    private EditText phone;
    private SwitchCompat notificationsNearHome;
    private SwitchCompat notificationsNearby;
    private Double currentRadius;
    private OnFragmentInteractionListener mListener;
    private String errorMessage;
    private View view;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountFragment.
     */
    public static UpdateAccountDialogFragment newInstance() {
        UpdateAccountDialogFragment fragment = new UpdateAccountDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_update_account_dialog, container, false);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_edit_profile);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        addressLine1 = (EditText) view.findViewById(R.id.address_line1);
        addressLine1.setText(user.getAddress());
        addressLine2 = (EditText) view.findViewById(R.id.address_line2);
        addressLine2.setText(user.getAddressLine2());
        city = (EditText) view.findViewById(R.id.city);
        city.setText(user.getCity());
        state = (EditText) view.findViewById(R.id.state);
        state.setText(user.getState());
        zip = (EditText) view.findViewById(R.id.zipcode);
        zip.setText(user.getZip());
        email = (EditText) view.findViewById(R.id.email);
        email.setText(user.getEmail());
        phone = (EditText) view.findViewById(R.id.phone);
        phone.setText(user.getPhone());
        notificationsNearHome = (SwitchCompat) view.findViewById(R.id.notifications_near_home);
        if (user.getHomeLocationNotifications() != null) {
            notificationsNearHome.setChecked(user.getHomeLocationNotifications());
        } else {
            notificationsNearHome.setChecked(false);
        }
        notificationsNearby = (SwitchCompat) view.findViewById(R.id.notifications_nearby);
        if (user.getCurrentLocationNotifications() != null) {
            notificationsNearby.setChecked(user.getCurrentLocationNotifications());
        } else {
            notificationsNearby.setChecked(false);
        }


        Spinner radiusSpinner = (Spinner) view.findViewById(R.id.radius_spinner);
        List<Double> radiusList = new ArrayList<>();
        radiusList.add(.1);
        radiusList.add(.25);
        radiusList.add(.5);
        radiusList.add(1D);
        radiusList.add(5D);
        radiusList.add(10D);
        ArrayAdapter<Double> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, radiusList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radiusSpinner.setAdapter(dataAdapter);
        int spinnerPosition = dataAdapter.getPosition(user.getNotificationRadius());
        if (spinnerPosition >= 0) {
            radiusSpinner.setSelection(spinnerPosition);
        } else {
            radiusSpinner.setSelection(0);
        }
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Double radius = (Double) parentView.getItemAtPosition(position);
                currentRadius = radius;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        Button saveBtn = (Button) view.findViewById(R.id.save_profile_button);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setAddress(addressLine1.getText().toString());
                user.setAddressLine2(addressLine2.getText().toString());
                user.setCity(city.getText().toString());
                user.setState(state.getText().toString());
                user.setZip(zip.getText().toString());
                user.setCurrentLocationNotifications(notificationsNearby.isChecked());
                user.setHomeLocationNotifications(notificationsNearHome.isChecked());
                user.setNewRequestNotificationsEnabled(notificationsNearby.isChecked() || notificationsNearHome.isChecked());
                user.setNotificationRadius(currentRadius);
                user.setEmail(email.getText().toString());
                user.setPhone(phone.getText().toString());
                updateUser();
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



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri url, String nextFragment);
    }

    private void updateUser() {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
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
                        throw new IOException(conn.getResponseMessage());
                    }
                    SharedAsyncMethods.getUserInfoFromServer(user, context);
                    return responseCode;
                } catch (IOException e) {
                    errorMessage = "Could not update account: " + e.getMessage();
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
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("successfully updated account info");
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

        }.execute();
    }
}