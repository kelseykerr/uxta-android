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
import android.support.design.widget.TextInputLayout;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.PaymentDetails;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 12/9/16.
 */

public class PaymentDestinationDialogFragment extends DialogFragment {
    private Context context;
    private PaymentDestinationDialogFragment.OnFragmentInteractionListener mListener;
    private User user;
    private static PaymentDetails paymentDetails;
    private static String destinationText;
    private ImageButton backToPayments;
    private TextView paymentDestinationText;
    private TextView routingNumber;
    private RelativeLayout removeBtn;
    private ImageButton destinationIcon;
    private EditText bankAcct;
    private EditText bankRoutingNumber;
    private Spinner paymentDestinationSpinner;
    private static final String VENMO_EMAIL_STRING = "venmo - link by email";
    private static final String VENMO_PHONE_STRING = "venmo - link by mobile phone";
    private static final String BANK_STRING = "deposit directly to bank";
    private TextInputLayout accntNumberLayout;
    private TextInputLayout routingNumberLayout;
    private Button saveBtn;
    private RelativeLayout updatingAccountScreen;
    private RelativeLayout infoScreen;
    private String errorMessage;

    public static PaymentDestinationDialogFragment newInstance(PaymentDetails pymtDetails, String destinationTxt) {
        paymentDetails = pymtDetails;
        destinationText = destinationTxt;
        PaymentDestinationDialogFragment fragment = new PaymentDestinationDialogFragment();
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
        final View view = inflater.inflate(R.layout.fragment_payment_destination_dialog, container, false);
        updatingAccountScreen = (RelativeLayout) view.findViewById(R.id.updating_account_screen);
        updatingAccountScreen.setVisibility(View.GONE);
        infoScreen = (RelativeLayout) view.findViewById(R.id.info_screen);
        backToPayments = (ImageButton) view.findViewById(R.id.back_to_payments);
        backToPayments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        paymentDestinationText = (TextView) view.findViewById(R.id.payment_destination);
        paymentDestinationText.setText(destinationText);
        removeBtn = (RelativeLayout) view.findViewById(R.id.remove_destination);
        routingNumber = (TextView) view.findViewById(R.id.routing_number);
        destinationIcon = (ImageButton) view.findViewById(R.id.destination_icon);
        bankAcct = (EditText) view.findViewById(R.id.bank_acct);
        accntNumberLayout = (TextInputLayout) view.findViewById(R.id.bank_acct_layout);
        routingNumberLayout = (TextInputLayout) view.findViewById(R.id.routing_number_layout);
        bankRoutingNumber = (EditText) view.findViewById(R.id.bank_routing_number);
        saveBtn = (Button) view.findViewById(R.id.save_btn);
        paymentDestinationSpinner = (Spinner) view.findViewById(R.id.payment_destination_spinner);
        if (paymentDetails != null) {
            if (paymentDetails.getDestination().toLowerCase().equals("bank")) {
                routingNumber.setText("routing number: " + paymentDetails.getRoutingNumber());
            } else {
                routingNumber.setVisibility(View.GONE);
            }
            routingNumberLayout.setVisibility(View.GONE);
            bankRoutingNumber.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            paymentDestinationSpinner.setVisibility(View.GONE);
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeBtn.setEnabled(false);
                    setRemoveBtnClick();
                    removeBtn.setEnabled(true);
                }
            });
        } else {
            destinationIcon.setVisibility(View.GONE);
            paymentDestinationText.setVisibility(View.GONE);
            routingNumber.setVisibility(View.GONE);
            removeBtn.setVisibility(View.GONE);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveBtn.setEnabled(false);
                    if (validateDestinationInfo()) {
                        setSaveBtnClick();
                    }
                    saveBtn.setEnabled(true);
                }
            });
            configureDestinationSpinner(view);
        }
        return view;
    }


    private void configureDestinationSpinner(View v) {
        List<String> destinations = new ArrayList<>();
        destinations.add(VENMO_EMAIL_STRING);
        destinations.add(VENMO_PHONE_STRING);
        destinations.add(BANK_STRING);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item, destinations);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        paymentDestinationSpinner.setAdapter(adapter);
        if (user.getFundDestination() != null) {
            switch (user.getFundDestination()) {
                case "email":
                    paymentDestinationSpinner.setSelection(1);
                    break;
                case "mobile_phone":
                    paymentDestinationSpinner.setSelection(2);
                    break;
                case "bank":
                    routingNumberLayout.setVisibility(View.VISIBLE);
                    accntNumberLayout.setVisibility(View.VISIBLE);
                    paymentDestinationSpinner.setSelection(3);
                    break;
            }
        }
        paymentDestinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                String destination = (String) parentView.getItemAtPosition(position);
                switch (destination) {
                    case VENMO_EMAIL_STRING:
                        routingNumberLayout.setVisibility(View.GONE);
                        accntNumberLayout.setVisibility(View.GONE);
                        break;
                    case VENMO_PHONE_STRING:
                        routingNumberLayout.setVisibility(View.GONE);
                        accntNumberLayout.setVisibility(View.GONE);
                        break;
                    case BANK_STRING:
                        routingNumberLayout.setVisibility(View.VISIBLE);
                        accntNumberLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private boolean validateDestinationInfo() {
        boolean valid = true;
        String value = paymentDestinationSpinner.getSelectedItem().toString();
        if (!value.equals(VENMO_EMAIL_STRING) && !value.equals(VENMO_PHONE_STRING) && !value.equals(BANK_STRING)) {
            valid = false;
        }
        if (value.equals(BANK_STRING)) {
            String rNumber = bankRoutingNumber.getText().toString();
            if (rNumber.isEmpty()) {
                valid = false;
                routingNumberLayout.setError("you must enter the routing number");
            } else {
                routingNumberLayout.setError(null);
            }
            String aNumber = bankAcct.getText().toString();
            if (aNumber.isEmpty()) {
                valid = false;
                accntNumberLayout.setError("you must enter an account number");
            } else {
                accntNumberLayout.setError(null);
            }
        }
        return valid;
    }

    private void setRemoveBtnClick() {
        infoScreen.setVisibility(View.GONE);
        updatingAccountScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/braintree/merchant");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("DELETE");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    //conn.setRequestProperty("Content-Type", "application/json");

                    responseCode = conn.getResponseCode();
                    Log.i("DELETE /merchant", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String message = AppUtils.getResponseContent(conn);
                        throw new IOException(message);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not remove payment destination: " + e.getMessage());
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("Could not remove payment destination: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    user.setRemovedMerchantDestination(true);
                    PrefUtils.setCurrentUser(user, context);
                    AccountFragment.dismissPaymentDialog();
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("removed payment destination");
                }
            }
        }.execute();

    }

    private void setSaveBtnClick() {
        infoScreen.setVisibility(View.GONE);
        updatingAccountScreen.setVisibility(View.VISIBLE);
        /**
         * TODO: when switching to production, uncomment the below snippet.
         * TODO: In sandbox we want to use the test cc so destination will always be bank
         * */
                /*String destination = paymentDestinationSpinner.getSelectedItem().toString();
                switch (destination) {
                    case VENMO_EMAIL_STRING:
                        user.setFundDestination("email");
                        break;
                    case VENMO_PHONE_STRING:
                        user.setFundDestination("mobile_phone");
                        break;
                    case BANK_STRING:
                        user.setFundDestination("bank");
                        break;

                }*/
        user.setFundDestination("bank");

        // TODO: in prod uncomment below. In sandbox use test routing/acct numbers
                /*String sBank = bank_acct.getText().toString();
                user.setBankAccountNumber(sBank);
                String sRouting = routing_number.getText().toString();
                user.setBankRoutingNumber(sRouting);*/
        user.setBankAccountNumber("1123581321");
        user.setBankRoutingNumber("071101307");

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/braintree/merchant");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty("Content-Type", "application/json");

                    ObjectMapper mapper = new ObjectMapper();
                    String userJson = mapper.writeValueAsString(user);
                    Log.i("updated user json: ", userJson);
                    byte[] outputInBytes = userJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("POST/braintree/merchant", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String message = AppUtils.getResponseContent(conn);
                        throw new IOException(message);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update payment destination: " + e.getMessage());
                    errorMessage = e.getMessage();
                    responseCode = 400;
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    AccountFragment.dismissPaymentDialog();
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("updated payment destination");
                } else {
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("Could not update payment destination: " + errorMessage);
                }
            }
        }.execute();
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
        if (context instanceof UpdateAccountDialogFragment.OnFragmentInteractionListener) {
            mListener = (PaymentDestinationDialogFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
        mListener = (PaymentDestinationDialogFragment.OnFragmentInteractionListener) activity;

    }

    protected void onAttachToContext(Context context) {
        this.context = context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

}
