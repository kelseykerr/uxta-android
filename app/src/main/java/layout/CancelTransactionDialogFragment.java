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
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Transaction;
import iuxta.nearby.model.User;

public class CancelTransactionDialogFragment extends DialogFragment {
    private User user;
    private Context context;
    private View view;
    private String transactionId;
    private EditText cancelReason;
    private RelativeLayout spinnerScreen;
    private RelativeLayout cancelTransactionScreen;
    private Button submitBtn;

    public static CancelTransactionDialogFragment newInstance(String transactionId) {
        CancelTransactionDialogFragment fragment = new CancelTransactionDialogFragment();
        Bundle args = new Bundle();
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
            transactionId = getArguments().getString("TRANSACTION_ID");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancel_transaction_dialog, container, false);
        cancelTransactionScreen = (RelativeLayout) view.findViewById(R.id.cancel_transaction_screen);
        cancelTransactionScreen.setVisibility(View.VISIBLE);
        cancelReason = (EditText) view.findViewById(R.id.cancel_reason);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_transaction_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        submitBtn = (Button) view.findViewById(R.id.submit_transaction_cancellation);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);
        spinnerScreen.setVisibility(View.GONE);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitBtn.setEnabled(false);
                if (validateForm()) {
                    cancelTransactionScreen.setVisibility(View.GONE);
                    spinnerScreen.setVisibility(View.VISIBLE);
                    cancelTransaction();
                } else {
                    submitBtn.setEnabled(true);
                }
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

    private boolean validateForm() {
        final String cancelText = cancelReason.getText().toString();
        if (cancelText == null || cancelText.length() < 3) {
            cancelReason.setError("please explain why you are canceling the transaction");
            return false;
        } else {
            cancelReason.setError(null);
            return true;
        }
    }

    private void cancelTransaction() {
        new AsyncTask<Void, Void, Integer>() {

            final String cancelText = cancelReason.getText().toString();
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/transactions/" + transactionId, "DELETE", user);
                    Transaction t = new Transaction();
                    t.setCanceledReason(cancelText);
                    t.setId(transactionId);
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(t);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    Integer responseCode = conn.getResponseCode();
                    Log.i("DELETE transaction", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not delete transaction: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory("successfully canceled transaction");
                } else {
                    cancelTransactionScreen.setVisibility(View.VISIBLE);
                    spinnerScreen.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);
                    Snackbar snackbar = Snackbar
                            .make(view, "Could not cancel transaction, contact support for more details.", Constants.LONG_SNACK);
                    snackbar.show();
                }

            }
        }.execute();
    }
}
