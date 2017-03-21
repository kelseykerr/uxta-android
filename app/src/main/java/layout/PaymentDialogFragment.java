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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.PaymentDetails;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 12/8/16.
 */

public class PaymentDialogFragment extends DialogFragment {
    private Context context;
    private PaymentDialogFragment.OnFragmentInteractionListener mListener;
    private User user;
    private TextView destinationText;
    private ImageButton destinationBtn;
    private ImageButton chevronRight;
    private TextView addAccountText;
    private PaymentDetails paymentDetails;
    private RelativeLayout getPaidCard;
    private TextView creditCardText;
    private ImageButton chevronRightPay;
    private ImageButton ccIcon;
    private TextView addCCText;
    public static RelativeLayout loadingScreen;
    private RelativeLayout paymentsCard;
    public static ScrollView paymentsScreen;


    public static PaymentDialogFragment newInstance() {
        PaymentDialogFragment fragment = new PaymentDialogFragment();
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
        final View view = inflater.inflate(R.layout.fragment_payments_dialog, container, false);
        loadingScreen = (RelativeLayout) view.findViewById(R.id.loading_screen);
        paymentsScreen = (ScrollView) view.findViewById(R.id.payments_screen);
        loadingScreen.setVisibility(View.VISIBLE);
        paymentsScreen.setVisibility(View.GONE);
        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_payments);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        destinationText = (TextView) view.findViewById(R.id.payment_destination);
        destinationBtn = (ImageButton) view.findViewById(R.id.destination_icon);
        chevronRight = (ImageButton) view.findViewById(R.id.right_chevron);
        addAccountText = (TextView) view.findViewById(R.id.add_account_text);
        getPaidCard = (RelativeLayout) view.findViewById(R.id.get_paid_card);
        paymentsCard = (RelativeLayout) view.findViewById(R.id.payments_card);
        creditCardText = (TextView) view.findViewById(R.id.credit_card_text);
        chevronRightPay = (ImageButton) view.findViewById(R.id.right_chevron_pay);
        addCCText = (TextView) view.findViewById(R.id.add_cc_text);
        ccIcon = (ImageButton) view.findViewById(R.id.cc_icon);
        getPaymentDetails();
        return view;
    }

    private void getPaymentDetails() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me/payments");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    responseCode = conn.getResponseCode();
                    String output = AppUtils.getResponseContent(conn);
                    if (responseCode != 200) {
                        throw new IOException(output);
                    }
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        paymentDetails = mapper.readValue(output, PaymentDetails.class);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to get " +
                                "payment info from server: " + e.getMessage());
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get payment info from server: " + e.getMessage());
                    return responseCode != null ? responseCode : 500;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                loadingScreen.setVisibility(View.GONE);
                paymentsScreen.setVisibility(View.VISIBLE);
                if (i != null && i.equals(200)) {
                    if (paymentDetails.getBankAccountLast4() == null)  {
                        addAccountText.setVisibility(View.VISIBLE);
                        destinationText.setVisibility(View.GONE);
                        destinationBtn.setVisibility(View.GONE);
                        getPaidCard.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDestinationDialogFragment pddf = PaymentDestinationDialogFragment.newInstance(null, null);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                        chevronRight.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDestinationDialogFragment pddf = PaymentDestinationDialogFragment.newInstance(null, null);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                    } else {
                        String destText = "";
                        destText = "bank account: " + "****" + paymentDetails.getBankAccountLast4();
                        destinationText.setText(destText);
                        destinationBtn.setVisibility(View.VISIBLE);
                        final String dText = destText;
                        getPaidCard.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDestinationDialogFragment pddf = PaymentDestinationDialogFragment.newInstance(paymentDetails, dText);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                        chevronRight.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDestinationDialogFragment pddf = PaymentDestinationDialogFragment.newInstance(paymentDetails, dText);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                    }

                    if (paymentDetails.getCcMaskedNumber() == null) {
                        addCCText.setVisibility(View.VISIBLE);
                        creditCardText.setVisibility(View.GONE);
                        ccIcon.setVisibility(View.GONE);
                        paymentsCard.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDetailsDialogFragment pddf = PaymentDetailsDialogFragment.newInstance(null);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                        chevronRightPay.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDetailsDialogFragment pddf = PaymentDetailsDialogFragment.newInstance(null);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                    } else {
                        addCCText.setVisibility(View.GONE);
                        ccIcon.setVisibility(View.VISIBLE);
                        creditCardText.setText("Credit Card: " + paymentDetails.getCcMaskedNumber());
                        creditCardText.setVisibility(View.VISIBLE);
                        paymentsCard.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDetailsDialogFragment pddf = PaymentDetailsDialogFragment.newInstance(paymentDetails);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                        chevronRightPay.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                PaymentDetailsDialogFragment pddf = PaymentDetailsDialogFragment.newInstance(paymentDetails);
                                pddf.show(getFragmentManager(), "dialog");
                            }
                        });
                    }
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
            mListener = (PaymentDialogFragment.OnFragmentInteractionListener) context;
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
        mListener = (PaymentDialogFragment.OnFragmentInteractionListener) activity;

    }

    public static void showLoadingScreen() {
        loadingScreen.setVisibility(View.VISIBLE);
        paymentsScreen.setVisibility(View.GONE);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

}
