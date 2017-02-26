package layout;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Transaction;
import superstartupteam.nearby.model.User;


/**
 * Created by kerrk on 10/5/16.
 */
public class ExchangeOverrideDialogFragment extends DialogFragment {

    private String transactionId;
    private User user;
    private View view;
    private String heading;
    private Context context;
    private TextView exchangeTimeLabel;
    private TextView exchangeTime;
    private Date exchangeTimeDate;
    private LinearLayout exchangeTimeBorder;
    private boolean initialExchangeOverride = true;
    private String description;
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");
    private ExchangeOverrideDialogFragment.OnFragmentInteractionListener mListener;



    public static ExchangeOverrideDialogFragment newInstance(String transactionId, String heading,
                                                             String description, boolean initialExchange) {
        ExchangeOverrideDialogFragment fragment = new ExchangeOverrideDialogFragment();
        Bundle args = new Bundle();
        args.putString("TRANSACTION_ID", transactionId);
        args.putString("HEADING", heading);
        args.putString("DESCRIPTION", description);
        args.putBoolean("INITIAL_EXCHANGE", initialExchange);
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
            heading = getArguments().getString("HEADING");
            description = getArguments().getString("DESCRIPTION");
            initialExchangeOverride = getArguments().getBoolean("INITIAL_EXCHANGE");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof ExchangeCodeDialogFragment.OnFragmentInteractionListener) {
            mListener = (ExchangeOverrideDialogFragment.OnFragmentInteractionListener) context;
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
    }

    protected void onAttachToContext(Context context) {

        this.context = context;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange_override_dialog, container, false);
        TextView dialogHeader = (TextView) view.findViewById(R.id.exchange_override_text);
        dialogHeader.setText(heading);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_exchange_override);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        exchangeTimeLabel = (TextView) view.findViewById(R.id.exchange_time_label);
        exchangeTime = (TextView) view.findViewById(R.id.exchange_time);
        String htmlString = "Exchange Time";
        exchangeTimeLabel.setVisibility(View.GONE);
        exchangeTime.setText(Html.fromHtml(htmlString));
        exchangeTimeBorder = (LinearLayout) view.findViewById(R.id.exchange_time_border);

        exchangeTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return exchangeTimeTouchEvent(event);
            }
        });

        Button cancelOverrideBtn = (Button) view.findViewById(R.id.cancel_override_button);
        Button submitOverrideBtn = (Button) view.findViewById(R.id.submit_override_button);

        cancelOverrideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        submitOverrideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (exchangeTime.getText() == null) {
                        nullExchangeTimeSnack();
                    }
                    Transaction t = new Transaction();
                    t.setId(transactionId);
                    Transaction.ExchangeOverride override = new Transaction.ExchangeOverride();
                    override.time = exchangeTimeDate;
                    if (initialExchangeOverride) {
                        t.setExchangeOverride(override);
                    } else {
                        t.setReturnOverride(override);
                    }
                    sendOverrideToServer(t);
                } catch (Exception e) {
                    badExchangeTimeSnack(exchangeTime.getText().toString());
                }
            }
        });

        this.view = view;
        return view;
    }

    private boolean exchangeTimeTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // date time picker
            final View dateTimeView = View.inflate(context, R.layout.date_time_picker, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

            dateTimeView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatePicker datePicker = (DatePicker) dateTimeView.findViewById(R.id.date_picker);
                    TimePicker timePicker = (TimePicker) dateTimeView.findViewById(R.id.time_picker);

                    Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            timePicker.getCurrentHour(),
                            timePicker.getCurrentMinute());

                    Date newPickupTime = new Date(calendar.getTimeInMillis());
                    String formattedDate = formatter.format(newPickupTime);
                    exchangeTimeDate = newPickupTime;
                    exchangeTime.setText(formattedDate);
                    exchangeTimeLabel.setVisibility(View.VISIBLE);
                    alertDialog.dismiss();
                }
            });
            alertDialog.setView(dateTimeView);
            alertDialog.show();
            return true;
        }
        return false;
    }

    private void nullExchangeTimeSnack() {
        Snackbar snackbar = Snackbar
                .make(view, "You must enter an exchange time", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void badExchangeTimeSnack(String text) {
        Snackbar snackbar = Snackbar
                .make(view, "Could not extract exchange date from [" + text + "]", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void sendOverrideToServer(final Transaction t) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/transactions/"
                            + transactionId + "/exchange");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(t);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    Integer responseCode = conn.getResponseCode();
                    Log.i("POST /exchange", "Response Code : " + responseCode);
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not post exchange override: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("successfully submitted exchange override");
                    dismiss();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, "could not submit exchange override", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                HistoryFragment.dismissViewTransactionFragment();
            }

        }.execute();
    }

}
