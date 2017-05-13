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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import iuxta.nearby.AppUtils;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Transaction;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 10/4/16.
 */
public class ConfirmExchangeOverrideDialogFragment extends DialogFragment {
    private User user;
    private Context context;
    private View view;
    private String transactionId;
    private String exchangeTime;
    private String description;
    private boolean isSeller = false;
    private static final String TAG = "ConfirmExchangeOverride";

    public static ConfirmExchangeOverrideDialogFragment newInstance(String exchangeTime,
                                                                    String description,
                                                                    String transactionId,
                                                                    boolean isSeller) {
        ConfirmExchangeOverrideDialogFragment fragment = new ConfirmExchangeOverrideDialogFragment();
        Bundle args = new Bundle();
        args.putString("EXCHANGE_TIME", exchangeTime);
        args.putString("DESCRIPTION", description);
        args.putString("TRANSACTION_ID", transactionId);
        args.putBoolean("IS_SELLER", isSeller);
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
            exchangeTime = getArguments().getString("EXCHANGE_TIME");
            transactionId = getArguments().getString("TRANSACTION_ID");
            description = getArguments().getString("DESCRIPTION");
            isSeller = getArguments().getBoolean("IS_SELLER");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_exchange_override_dialog, container, false);
        /*TextView chargeDescription = (TextView) view.findViewById(R.id.charge_description);
        chargeDescription.setText(description);*/
        TextView eTimeView = (TextView) view.findViewById(R.id.exchange_time);
        eTimeView.setText(exchangeTime);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_confirm_override);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView descriptionView = (TextView) view.findViewById(R.id.description);
        descriptionView.setText(description);
        Button confirmBtn = (Button) view.findViewById(R.id.confirm_exchange_button);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Transaction t = new Transaction();
                t.setId(transactionId);
                Transaction.ExchangeOverride override = new Transaction.ExchangeOverride();
                override.buyerAccepted = true;
                override.sellerAccepted = true;
                if (isSeller) {
                    t.setReturnOverride(override);
                } else {
                    t.setExchangeOverride(override);
                }
                sendResponse(t);
            }
        });
        Button declineBtn = (Button) view.findViewById(R.id.decline_exchange_button);
        declineBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Transaction t = new Transaction();
                t.setId(transactionId);
                Transaction.ExchangeOverride override = new Transaction.ExchangeOverride();
                if (isSeller) {
                    override.buyerAccepted = true;
                    override.sellerAccepted = false;
                    t.setReturnOverride(override);
                } else {
                    override.buyerAccepted = false;
                    override.sellerAccepted = true;
                    t.setExchangeOverride(override);
                }
                sendResponse(t);
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


    public void sendResponse(final Transaction t) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    String apiPath = "/transactions/" + transactionId + "/exchange";
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "PUT", user);
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(t);
                    byte[] outputInBytes = json.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "PUT /exchange response code : " + responseCode);
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "could not post exchange override: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("submitted response to exchange override");
                    dismiss();
                }

            }
        }.execute();
    }

}
