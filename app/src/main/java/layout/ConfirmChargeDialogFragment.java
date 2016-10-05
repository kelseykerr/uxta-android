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
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.InputFilterMinMax;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Transaction;
import superstartupteam.nearby.model.User;

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

    public static ConfirmChargeDialogFragment newInstance(Double calculatedPrice, String description, String transactionId) {
        ConfirmChargeDialogFragment fragment = new ConfirmChargeDialogFragment();
        Bundle args = new Bundle();
        args.putString("DESCRIPTION", description);
        args.putDouble("CALCULATED_PRICE", calculatedPrice);
        args.putString("TRANSACTION_ID", transactionId);
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
            description = getArguments().getString("DESCRIPTION");
            calculatedPrice = getArguments().getDouble("CALCULATED_PRICE");
            originalPrice = calculatedPrice;
            transactionId = getArguments().getString("TRANSACTION_ID");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_charge_dialog, container, false);
        TextView chargeDescription = (TextView) view.findViewById(R.id.charge_description);
        chargeDescription.setText(description);
        EditText price = (EditText) view.findViewById(R.id.final_price);
        price.setText(calculatedPrice.toString());
        price.setFilters(new InputFilter[]{ new InputFilterMinMax(0, originalPrice)});
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_confirm_charge);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        Button submitBtn = (Button) view.findViewById(R.id.submit_charge_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmCharge();
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


    private void confirmCharge() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/transactions/"
                            + transactionId + "/price");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());

                    conn.setRequestProperty("Content-Type", "application/json");

                    Transaction t = new Transaction();
                    if (calculatedPrice != originalPrice) {
                        t.setPriceOverride(calculatedPrice);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(t);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    Integer responseCode = conn.getResponseCode();
                    Log.i("PUT /price", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not confirm price: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    dismiss();
                }

            }
        }.execute();
    }
}
