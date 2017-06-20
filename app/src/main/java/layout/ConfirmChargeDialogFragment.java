package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.InputFilterMinMax;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Transaction;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 10/1/16.
 */
public class ConfirmChargeDialogFragment extends DialogFragment {
    private String description;
    private Double originalPrice;
    private Double calculatedPrice;
    private User user;
    private Context context;
    private View view;
    private String transactionId;
    private RelativeLayout confirmChargeScreen;
    private RelativeLayout spinnerScreen;
    private EditText price;
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String CALCULATED_PRICE = "CALCULATED_PRICE";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String TAG = "ConfirmChargeDialogFrag";


    public static ConfirmChargeDialogFragment newInstance(Double calculatedPrice, String description,
                                                          String transactionId) {
        ConfirmChargeDialogFragment fragment = new ConfirmChargeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DESCRIPTION, description);
        args.putDouble(CALCULATED_PRICE, calculatedPrice);
        args.putString(TRANSACTION_ID, transactionId);
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
        if (getArguments() != null) {
            description = getArguments().getString(DESCRIPTION);
            calculatedPrice = getArguments().getDouble(CALCULATED_PRICE);
            originalPrice = calculatedPrice;
            transactionId = getArguments().getString(TRANSACTION_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_charge_dialog, container, false);
        confirmChargeScreen = (RelativeLayout) view.findViewById(R.id.confirm_charge_screen);
        confirmChargeScreen.setVisibility(View.VISIBLE);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);
        spinnerScreen.setVisibility(View.GONE);
        TextView chargeDescription = (TextView) view.findViewById(R.id.charge_description);
        chargeDescription.setText(description);
        price = (EditText) view.findViewById(R.id.final_price);
        BigDecimal formattedValue = AppUtils.formatCurrency(calculatedPrice);

        price.setText(formattedValue.toString());
        price.setFilters(new InputFilter[]{ new InputFilterMinMax(0, originalPrice)});
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_confirm_charge);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        final Button submitBtn = (Button) view.findViewById(R.id.submit_charge_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitBtn.setEnabled(false);
                if (validateForm()) {
                    confirmChargeScreen.setVisibility(View.GONE);
                    spinnerScreen.setVisibility(View.VISIBLE);
                    String offer = price.getText().toString();
                    confirmCharge(submitBtn, offer);
                } else {
                    submitBtn.setEnabled(true);
                }
            }
        });
        this.view = view;
        return view;

    }

    private boolean validateForm() {
        String p = price.getText().toString();
        if (p.isEmpty()) {
            price.setError("price cannot be empty");
            return false;
        } else {
            double doublePrice = Double.parseDouble(p);
            if (doublePrice != 0 && doublePrice < Constants.MINIMUM_OFFER_PRICE) {
                price.setError("price can only be $0.00 or greater than $0.50");
                return false;
            } else {
                price.setError(null);
            }
            return true;
        }


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


    private void confirmCharge(final Button submitBtn, final String offer) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/transactions/" + transactionId + "/price", "PUT", user);
                    Transaction t = new Transaction();
                    double p = Double.parseDouble(offer);
                    if (p != originalPrice) {
                        t.setPriceOverride(p);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(t);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "PUT /price response code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR could not confirm price: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("confirmed charge");
                    dismiss();
                } else {
                    ((MainActivity) getActivity()).goToHistory("there was a problem creating the charge");
                    dismiss();
                }
            }
        }.execute();
    }
}
