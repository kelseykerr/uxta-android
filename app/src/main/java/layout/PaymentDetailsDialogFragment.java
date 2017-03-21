package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.SharedAsyncMethods;
import iuxta.nearby.model.PaymentDetails;
import iuxta.nearby.model.User;

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
    private TextView ccType;
    private RelativeLayout updateBtn;
    private RelativeLayout newCardLayout;
    private TextInputLayout ccLayout;
    private EditText newCcNumber;
    private TextInputLayout expDateLayout;
    private EditText newExpDate;
    private Button saveBtn;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/yy", Locale.US);
    private String mLastInput;
    private TextInputLayout cvcLayout;
    private EditText cvcNumber;
    public static final String TAG = "PaymentDetailsDialogFra";


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
        ccType = (TextView) view.findViewById(R.id.cc_type);
        ccNumber = (TextView) view.findViewById(R.id.cc_number);
        expDate = (TextView) view.findViewById(R.id.exp_date);
        updateBtn = (RelativeLayout) view.findViewById(R.id.update_card);
        newCardLayout = (RelativeLayout) view.findViewById(R.id.new_card_layout);
        ccLayout = (TextInputLayout) view.findViewById(R.id.credit_card_layout);
        newCcNumber = (EditText) view.findViewById(R.id.credit_card);
        cvcLayout = (TextInputLayout) view.findViewById(R.id.credit_card_cvc_layout);
        cvcNumber = (EditText) view.findViewById(R.id.credit_card_cvc);
        expDateLayout = (TextInputLayout) view.findViewById(R.id.exp_date_layout);
        newExpDate = (EditText) view.findViewById(R.id.new_exp_date);
        saveBtn = (Button) view.findViewById(R.id.save_btn);
        if (paymentDetails != null) {
            showPaymentDetails();
        } else {
            showAddPaymentInput();
        }
            return view;
    }

    private void showPaymentDetails() {
        newCardLayout.setVisibility(View.GONE);
        ccType.setText(paymentDetails.getCcType());
        ccNumber.setText(paymentDetails.getCcMaskedNumber());
        if (paymentDetails.getCcExpDate() != null) {
            if (paymentDetails.getCcExpDate().indexOf("/") == 1) {
                paymentDetails.setCcExpDate("0" + paymentDetails.getCcExpDate());
            }
            expDate.setText("exp: " + paymentDetails.getCcExpDate());
        }
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBtn.setEnabled(false);
                showAddPaymentInput();
                updateBtn.setEnabled(true);
            }
        });
    }

    private void showAddPaymentInput() {
        newCardLayout.setVisibility(View.VISIBLE);
        setExpDateWatcher();
        ccNumber.setVisibility(View.GONE);
        expDate.setVisibility(View.GONE);
        updateBtn.setVisibility(View.GONE);
        ccIcon.setVisibility(View.GONE);
        ccType.setVisibility(View.GONE);
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

    private boolean validatePaymentInfo() {
        boolean valid = true;
        String ccValue = newCcNumber.getText().toString();
        if (ccValue.isEmpty() || ccValue.length() < 12 || ccValue.length() > 19) {
            valid = false;
            ccLayout.setError("you must enter a valid credit card number");
        } else {
            ccLayout.setError(null);
        }
        String cvcNum = cvcNumber.getText().toString();
        if (cvcNum.isEmpty() || cvcNum.length() < 3) {
            valid = false;
            cvcLayout.setError("please enter a valid CVV number");
        } else {
            cvcLayout.setError(null);
        }
        String expValue = newExpDate.getText().toString();
        if (expValue.isEmpty()) {
            valid = false;
            expDateLayout.setError("you must enter the credit card's expiration date");
        } else if (expValue.length() < 5) {
            valid = false;
            expDateLayout.setError("please enter a valid expiration date");
        } else {
            Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
            currentYear = currentYear % 100;
            Integer toTest = Integer.parseInt(expValue.substring(expValue.length()-2, expValue.length()));
            if (toTest < currentYear) {
                valid = false;
                expDateLayout.setError("your credit card has expired, please enter a valid credit card");
            } else if (toTest == currentYear) {
                toTest = Integer.parseInt(expValue.substring(0, expValue.length()-3));
                Integer currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                if (toTest <= currentMonth) {
                    valid = false;
                    expDateLayout.setError("your credit card has expired, please enter a valid credit card");
                } else {
                    expDateLayout.setError(null);
                }
            } else {
                expDateLayout.setError(null);
            }
        }
        return valid;
    }

    private void setSaveBtnClick() {
        //TODO: uncomment below and remove the manual set of cc number & exp date
        /*user.setCreditCardNumber(newCcNumber.getText().toString());
        user.setCcExpirationDate(newExpDate.getText().toString());*/
        Card card = new Card("4242424242424242", 5, 19, cvcNumber.getText().toString());
        if (!card.validateCard()) {
            // Show errors
            Log.e(TAG, "Card was not valid");
            saveBtn.setEnabled(true);
            return;
        }
        try {
            Stripe stripe = new Stripe(Constants.STRIPE_TEST_KEY);
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            user.setStripeCCToken(token.getId());
                            infoScreen.setVisibility(View.GONE);
                            updatingPaymentScreen.setVisibility(View.VISIBLE);
                            SharedAsyncMethods.updateUserPayment(user, context, ((MainActivity) getActivity()));
                            PaymentDialogFragment.showLoadingScreen();
                            dismiss();
                        }
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(context,
                                    error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        } catch (AuthenticationException e) {
            Log.e(TAG, "Could not authenticate with Stripe. Is the key valid?");
        }
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
