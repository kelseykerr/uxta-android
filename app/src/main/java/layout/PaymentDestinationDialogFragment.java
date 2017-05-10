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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.PaymentDetails;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 12/9/16.
 */

public class PaymentDestinationDialogFragment extends DialogFragment {
    private Context context;
    private PaymentDestinationDialogFragment.OnFragmentInteractionListener mListener;
    private User user;
    private static PaymentDetails paymentDetails;
    private ImageButton backToPayments;
    private TextView routingNumber;
    private TextView paymentDestination;
    private RelativeLayout updateBtn;
    private ImageButton destinationIcon;
    private EditText bankAcct;
    private EditText bankRoutingNumber;
    private TextInputLayout accntNumberLayout;
    private TextInputLayout routingNumberLayout;
    private Button saveBtn;
    private RelativeLayout updatingAccountScreen;
    private RelativeLayout infoScreen;
    private String errorMessage;
    public static final String TAG = "PaymentDestinationDialo";

    public static PaymentDestinationDialogFragment newInstance(PaymentDetails pymtDetails, String destinationTxt) {
        paymentDetails = pymtDetails;
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
        updateBtn = (RelativeLayout) view.findViewById(R.id.update_destination);
        routingNumber = (TextView) view.findViewById(R.id.routing_number);
        paymentDestination = (TextView) view.findViewById(R.id.payment_destination);
        destinationIcon = (ImageButton) view.findViewById(R.id.destination_icon);
        bankAcct = (EditText) view.findViewById(R.id.bank_acct);
        accntNumberLayout = (TextInputLayout) view.findViewById(R.id.bank_acct_layout);
        routingNumberLayout = (TextInputLayout) view.findViewById(R.id.routing_number_layout);
        bankRoutingNumber = (EditText) view.findViewById(R.id.bank_routing_number);
        saveBtn = (Button) view.findViewById(R.id.save_btn);
        if (paymentDetails != null) {
            showExistingInfo();
        } else {
            showEnterNewInfo();
        }
        return view;
    }

    private void showExistingInfo() {
        bankAcct.setVisibility(View.GONE);
        accntNumberLayout.setVisibility(View.GONE);
        routingNumber.setText("routing number: " + paymentDetails.getRoutingNumber());
        paymentDestination.setText("account number: " + paymentDetails.getBankAccountLast4());
        routingNumberLayout.setVisibility(View.GONE);
        bankRoutingNumber.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBtn.setEnabled(false);
                setUpdateBtnClick();
                updateBtn.setEnabled(true);
            }
        });
    }

    private void showEnterNewInfo() {
        destinationIcon.setVisibility(View.GONE);
        routingNumber.setVisibility(View.GONE);
        paymentDestination.setVisibility(View.GONE);
        updateBtn.setVisibility(View.GONE);
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
        bankAcct.setVisibility(View.VISIBLE);
        accntNumberLayout.setVisibility(View.VISIBLE);
        routingNumberLayout.setVisibility(View.VISIBLE);
        bankRoutingNumber.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);
    }

    private boolean validateDestinationInfo() {
        boolean valid = true;
        String rNumber = bankRoutingNumber.getText().toString();
        if (rNumber.isEmpty()) {
            valid = false;
            routingNumberLayout.setError("you must enter the routing number");
        } else if (rNumber.length() < 9) {
            valid = false;
            routingNumberLayout.setError("routing number must be 9 digits");
        } else {
            routingNumberLayout.setError(null);
        }
        String aNumber = bankAcct.getText().toString();
        if (aNumber.isEmpty()) {
            valid = false;
            accntNumberLayout.setError("you must enter an account number");
        } else if (aNumber.length() < 4 && aNumber.length() > 17) {
            valid = false;
            accntNumberLayout.setError("please enter a valid account number");
        } else {
            accntNumberLayout.setError(null);
        }
        return valid;
    }

    private void setUpdateBtnClick() {
        showEnterNewInfo();
        //updatingAccountScreen.setVisibility(View.VISIBLE);
    }

    private void setSaveBtnClick() {
        infoScreen.setVisibility(View.GONE);
        updatingAccountScreen.setVisibility(View.VISIBLE);

        // TODO: in prod uncomment below. In sandbox use test routing/acct numbers
        String sBank = bankAcct.getText().toString();
        user.setBankAccountNumber(sBank);
        String sRouting = bankRoutingNumber.getText().toString();
        user.setBankRoutingNumber(sRouting);
        // TODO: fetch token instead of passing bank account info
        /*user.setBankAccountNumber("000123456789");
        user.setBankRoutingNumber("110000000");*/

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/stripe/bank");
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
                    Log.i(TAG, "POST /stripe/bank response code : " + responseCode);
                    if (responseCode != 200) {
                        String message = AppUtils.getResponseContent(conn);
                        throw new IOException(message);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not update bank account: " + e.getMessage());
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
                    AccountFragment.dismissPaymentDialog();
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
