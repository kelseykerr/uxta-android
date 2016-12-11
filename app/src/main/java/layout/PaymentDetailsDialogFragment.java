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
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.SharedAsyncMethods;
import superstartupteam.nearby.model.PaymentDetails;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 12/11/16.
 */

public class PaymentDetailsDialogFragment extends DialogFragment {
    private static PaymentDetails paymentDetails;
    private User user;
    private Context context;
    private PaymentDetailsDialogFragment.OnFragmentInteractionListener mListener;
    private RelativeLayout updatingPaymentScreen;
    private RelativeLayout infoScreen;
    private ImageButton backToPayments;
    private ImageButton ccIcon;
    private TextView ccNumber;
    private TextView expDate;
    private RelativeLayout removeBtn;
    private RelativeLayout newCardLayout;
    private TextInputLayout ccLayout;
    private EditText newCcNumber;
    private TextInputLayout expDateLayout;
    private EditText newExpDate;
    private Button saveBtn;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yy", Locale.US);
    private String mLastInput;


    public static PaymentDetailsDialogFragment newInstance(PaymentDetails pymtDetails) {
        paymentDetails = pymtDetails;
        PaymentDetailsDialogFragment fragment = new PaymentDetailsDialogFragment();
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
        final View view = inflater.inflate(R.layout.fragment_payment_details_dialog, container, false);
        updatingPaymentScreen = (RelativeLayout) view.findViewById(R.id.updating_payment_screen);
        infoScreen = (RelativeLayout) view.findViewById(R.id.info_screen);
        backToPayments = (ImageButton) view.findViewById(R.id.back_to_payments);
        backToPayments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        ccIcon = (ImageButton) view.findViewById(R.id.cc_icon);
        ccNumber = (TextView) view.findViewById(R.id.cc_number);
        expDate = (TextView) view.findViewById(R.id.exp_date);
        removeBtn = (RelativeLayout) view.findViewById(R.id.remove_card);
        newCardLayout = (RelativeLayout) view.findViewById(R.id.new_card_layout);
        ccLayout = (TextInputLayout) view.findViewById(R.id.credit_card_layout);
        newCcNumber = (EditText) view.findViewById(R.id.credit_card);
        expDateLayout = (TextInputLayout) view.findViewById(R.id.exp_date_layout);
        newExpDate = (EditText) view.findViewById(R.id.new_exp_date);
        saveBtn = (Button) view.findViewById(R.id.save_btn);
        if (paymentDetails != null) {
            newCardLayout.setVisibility(View.GONE);
            ccNumber.setText(paymentDetails.getCcMaskedNumber());
            expDate.setText(paymentDetails.getCcExpDate());
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeBtn.setEnabled(false);
                    setRemoveBtnClick();
                    removeBtn.setEnabled(true);
                }
            });
        } else {
            newCardLayout.setVisibility(View.VISIBLE);
            setExpDateWatcher();
            ccNumber.setVisibility(View.GONE);
            expDate.setVisibility(View.GONE);
            removeBtn.setVisibility(View.GONE);
            ccIcon.setVisibility(View.GONE);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveBtn.setEnabled(false);
                    if (validatePaymentInfo()) {
                        setSaveBtnClick();
                    }
                    saveBtn.setEnabled(true);
                }
            });
        }
            return view;
    }

    private boolean validatePaymentInfo() {
        boolean valid = true;
        String ccValue = newCcNumber.getText().toString();
        if (ccValue.isEmpty()) {
            valid = false;
            ccLayout.setError("you must enter a valid credit card number");
        } else {
            ccLayout.setError(null);
        }
        String expValue = newExpDate.getText().toString();
        if (expValue.isEmpty()) {
            valid = false;
            expDateLayout.setError("you must enter the credit card's expiration date");
        } else {
            expDateLayout.setError(null);
        }
        return valid;
    }

    private void setSaveBtnClick() {
        //TODO: uncomment below and remove the manual set of cc number & exp date
        /*user.setCreditCardNumber(newCcNumber.getText().toString());
        user.setCcExpirationDate(newExpDate.getText().toString());*/
        user.setCreditCardNumber("4111111111111111");
        user.setCcExpirationDate("05/19");
        MainActivity.updatedUser = user;

        PrefUtils.setCurrentUser(MainActivity.updatedUser, context);
        String nextFragment = " ";
        Uri url = null;
        infoScreen.setVisibility(View.GONE);
        updatingPaymentScreen.setVisibility(View.VISIBLE);
        PaymentDialogFragment.showLoadingScreen();
        mListener.onFragmentInteraction(url, nextFragment, Constants.FPPR_REGISTER_BRAINTREE_CUSTOMER);
        dismiss();
    }

    private void setRemoveBtnClick() {
        infoScreen.setVisibility(View.GONE);
        updatingPaymentScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/braintree/customer");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("DELETE");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());

                    responseCode = conn.getResponseCode();
                    Log.i("DELETE /customer", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String message = AppUtils.getResponseContent(conn);
                        throw new IOException(message);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not remove payment info: " + e.getMessage());
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("Could not remove payment info: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    SharedAsyncMethods.getUserInfoFromServer(user, context);
                    AccountFragment.dismissPaymentDialog();
                    dismiss();
                    ((MainActivity) getActivity()).goToAccount("removed payment info");
                }
            }
        }.execute();

    }


    private void setExpDateWatcher() {
        newExpDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Your query to fetch Data
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                Calendar expiryDateDate = Calendar.getInstance();
                try {
                    expiryDateDate.setTime(dateFormatter.parse(input));
                } catch (java.text.ParseException e) {
                    if (s.length() == 2 && !mLastInput.endsWith("/")) {
                        int month = Integer.parseInt(input);
                        if (month <= 12) {
                            newExpDate.setText(newExpDate.getText().toString() + "/");
                            newExpDate.setSelection(newExpDate.getText().toString().length());
                        }
                    } else if (s.length() == 2 && mLastInput.endsWith("/")) {
                        int month = Integer.parseInt(input);
                        if (month <= 12) {
                            newExpDate.setText(newExpDate.getText().toString().substring(0, 1));
                            newExpDate.setSelection(newExpDate.getText().toString().length());
                        } else {
                            newExpDate.setText("");
                            newExpDate.setSelection(newExpDate.getText().toString().length());
                            Toast.makeText(context, "Enter a valid month", Toast.LENGTH_LONG).show();
                        }
                    } else if (s.length() == 1) {
                        int month = Integer.parseInt(input);
                        if (month > 1) {
                            newExpDate.setText("0" + newExpDate.getText().toString() + "/");
                            newExpDate.setSelection(newExpDate.getText().toString().length());
                        }
                    }
                    mLastInput = newExpDate.getText().toString();
                    return;
                }
            }

        });

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
            mListener = (PaymentDetailsDialogFragment.OnFragmentInteractionListener) context;
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
        mListener = (PaymentDetailsDialogFragment.OnFragmentInteractionListener) activity;

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
