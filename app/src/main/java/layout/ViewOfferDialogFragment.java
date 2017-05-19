package layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.User;

/**
 * Created by kerrk on 9/16/16.
 */
public class ViewOfferDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static Response response;
    private static Request request;
    private HistoryFragment.OnFragmentInteractionListener mListener;
    private Context context;
    private TextInputLayout offerPriceLayout;
    private EditText offerPrice;
    private Spinner offerType;
    private TextInputLayout descriptionLayout;
    private EditText description;
    private EditText pickupLocation;
    private EditText returnLocation;
    private Button updateRequestBtn;
    private Button rejectRequestBtn;
    private Button messageSellerBtn;
    private ImageButton backButton;
    private TextInputLayout pickupTimeLayout;
    private EditText pickupTime;
    private TextInputLayout returnTimeLayout;
    private EditText returnTime;
    private User user;
    private View view;
    private Date returnDate;
    private Date exchangeDate;
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");



    public ViewOfferDialogFragment() {

    }

    public static ViewOfferDialogFragment newInstance(Response r, Request req) {
        response = r;
        request = req;
        ViewOfferDialogFragment fragment = new ViewOfferDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        View view = inflater.inflate(R.layout.fragment_view_offer_dialog, container, false);
        pickupTimeLayout = (TextInputLayout) view.findViewById(R.id.pickup_time_layout);
        pickupTimeLayout.setErrorEnabled(true);
        pickupTime = (EditText) view.findViewById(R.id.pickup_time);
        pickupTime.setKeyListener(null);
        setDateTimeFunctionality(pickupTime, true);

        pickupLocation = (EditText) view.findViewById(R.id.pickup_location);
        pickupLocation.setText(response.getExchangeLocation());
        returnLocation = (EditText) view.findViewById(R.id.return_location);
        returnLocation.setText(response.getReturnLocation());
        returnTimeLayout = (TextInputLayout) view.findViewById(R.id.return_time_layout);
        returnTime = (EditText) view.findViewById(R.id.return_time);
        returnTime.setKeyListener(null);
        if (!request.getRental()) {
            returnLocation.setVisibility(View.GONE);
            returnTime.setVisibility(View.GONE);
        } else {
            this.view = view;
            setDateTimeFunctionality(returnTime, false);
        }
        pickupLocation.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            openDatePicker(pickupTime, false);
                        }
                        return true;
                    }
                });

        returnLocation.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            openDatePicker(returnTime, false);
                        }
                        return true;
                    }
                });
        descriptionLayout = (TextInputLayout) view.findViewById(R.id.description_layout);
        description = (EditText) view.findViewById(R.id.description);
        description.setText(response.getDescription());
        //if this is the buyer, don't allow them to edit the description
        if (!response.getSellerId().equals(user.getId())) {
            description.setEnabled(false);
        } else {
            description.setEnabled(true);
        }
        offerPriceLayout = (TextInputLayout) view.findViewById(R.id.offer_price_layout);
        offerPrice = (EditText) view.findViewById(R.id.response_offer_price);
        offerPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String str = offerPrice.getText().toString();
                if (str.isEmpty() || str.startsWith("$")) {
                    return;
                }
                offerPrice.setText("$" + offerPrice.getText().toString());
                offerPrice.setSelection(offerPrice.getText().toString().length());

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        BigDecimal formattedValue = AppUtils.formatCurrency(response.getOfferPrice());
        offerPrice.setText(formattedValue.toString());
        backButton = (ImageButton) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        updateRequestBtn = (Button) view.findViewById(R.id.accept_offer_button);
        updateRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateRequestBtn.setEnabled(false);
                updateResponse();
            }
        });
        rejectRequestBtn = (Button) view.findViewById(R.id.reject_offer_button);
        rejectRequestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rejectRequestBtn.setEnabled(false);
                declineResponse();
            }
        });
        messageSellerBtn = (Button) view.findViewById(R.id.message_seller_button);
        if (response.getSellerId().equals(user.getId())) {
            messageSellerBtn.setVisibility(View.GONE);
            rejectRequestBtn.setText("withdraw offer");
        } else if (response.getMessagesEnabled() == null || !response.getMessagesEnabled() || response.getSeller() == null || response.getSeller().getPhone() == null) {
            messageSellerBtn.setVisibility(View.GONE);
        } else {
            messageSellerBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    PackageManager packageManager = ((MainActivity)getActivity()).getPackageManager();
                    List activities = packageManager.queryIntentActivities(smsIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    boolean isIntentSafe = activities.size() > 0;
                    if (isIntentSafe) {
                        Snackbar snackbar = Snackbar
                                .make(v, "no messaging app found", Constants.LONG_SNACK);
                        snackbar.show();
                    } else {
                        messageSellerBtn.setEnabled(false);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        String phone;
                        phone = response.getSeller().getPhone().replace("-", "");
                        smsIntent.putExtra("address", phone);
                        smsIntent.putExtra("sms_body","");
                        context.startActivity(Intent.createChooser(smsIntent, "SMS:"));
                        messageSellerBtn.setEnabled(true);
                    }
                }
            });
        }
        this.view = view;
        return view;
    }

    private void declineResponse() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + request.getId() + "/responses/" + response.getId();
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "PUT", user);
                    if (request.getUser().getId().equals(user.getId())) {
                        response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    Response r = response;
                    r.setSeller(null);
                    if (user.getId().equals(request.getUser().getId())) {
                        r.setBuyerStatus(Response.BuyerStatus.DECLINED);
                    } else {
                        r.setSellerStatus(Response.SellerStatus.WITHDRAWN);
                    }
                    String responseJson = mapper.writeValueAsString(r);
                    Log.i("updated response: ", responseJson);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                rejectRequestBtn.setEnabled(true);
                HistoryFragment.dismissViewRequestFragment();
                if (responseCode == 200) {
                    dismiss();
                    if (user.getId().equals(request.getUser().getId())) {
                        ((MainActivity) getActivity()).goToHistory("successfully declined offer");
                    } else {
                        ((MainActivity) getActivity()).goToHistory("successfully withdrew offer");
                    }
                }
            }
        }.execute();
    }

    private void updateResponse() {
        boolean goodForm = validateForm();
        if (!goodForm) {
            updateRequestBtn.setEnabled(true);
            return;
        }
        updateResponseObject();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + request.getId() + "/responses/" + response.getId();
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "PUT", user);
                    if (request.getUser().getId().equals(user.getId())) {
                        response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                    } else {
                        response.setSellerStatus(Response.SellerStatus.ACCEPTED);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    User seller = response.getSeller();
                    response.setSeller(null);
                    String responseJson = mapper.writeValueAsString(response);
                    Log.i("updated response: ", responseJson);
                    response.setSeller(seller);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                updateRequestBtn.setEnabled(true);
                HistoryFragment.dismissViewRequestFragment();
                if (responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("successfully updated offer");
                    dismiss();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(view, "could not update offer", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();
    }

    private void updateResponseObject() {
        String offerStr = offerPrice.getText().toString();
        if (!offerStr.isEmpty()) {
            offerStr = offerStr.replace("$", "");
        }
        double offer = Double.parseDouble(offerStr);
        response.setOfferPrice(offer);
        /*String type = offerType.getSelectedItem().toString();
        if (type.equals("per day")) {
            type= "per_day";
        } else if (type.equals("per hour")) {
            type = "per_hour";
        }
        response.setPriceType(type);*/
        response.setExchangeLocation(pickupLocation.getText().toString());
        if (!pickupTime.getText().toString().isEmpty()) {
            response.setExchangeTime(exchangeDate);
        }
        if (request.getRental()) {
            response.setReturnLocation(returnLocation.getText().toString());
            if (!returnTime.getText().toString().isEmpty()) {
                response.setReturnTime(returnDate);
            }
        }
    }

    private boolean validateForm() {
        boolean good = true;
        String offer = offerPrice.getText().toString();
        if (offer.isEmpty()) {
            offerPriceLayout.setError("price cannot be empty");
            good = false;
        } else {
            offer = offer.substring(1);
            double price = Double.parseDouble(offer);
            if (price != 0 && price < Constants.MINIMUM_OFFER_PRICE) {
                good = false;
                offerPriceLayout.setError("price can only be $0.00 or greater than $0.50");
            } else {
                offerPriceLayout.setError(null);
            }
        }
        if (exchangeDate != null && exchangeDate.before(new Date())) {
            pickupTimeLayout.setError("this must be a date in the future");
            good = false;
        }
        if (returnDate != null && returnDate.before(new Date())) {
            returnTimeLayout.setError("this must be a date in the future");
            good = false;
        }
        return good;
    }

    private void setDateTimeFunctionality(final EditText textView, final boolean pickup) {
        if (pickup ? response.getExchangeTime() != null : response.getReturnTime() != null) {
            String formattedTime = pickup ? formatter.format(response.getExchangeTime()) : formatter.format(response.getReturnTime());
            textView.setText(formattedTime);
            if (pickup) {
                exchangeDate = response.getExchangeTime();
            } else {
                returnDate = response.getReturnTime();
            }
        }

        textView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    openDatePicker(textView, pickup);
                    return true;
                }
                return false;
            }
        });
    }

    private void openDatePicker(final EditText textView, final boolean pickup) {
        // date time picker
        final View dateTimeView = View.inflate(context, R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new Builder(context).create();

        dateTimeView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dateTimeView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dateTimeView.findViewById(R.id.time_picker);
                Date date = AppUtils.getCalendarDate(datePicker, timePicker);
                Date current = new Date();
                if (!pickup) {
                    if (exchangeDate != null) {
                        date = date.before(exchangeDate) ? exchangeDate : date;
                    } else {
                        date = date.before(current) ? current : date;
                    }
                } else {
                    date = date.before(current) ? current : date;
                    if (returnDate != null) {
                        date = date.after(returnDate) ? returnDate : date;
                    }
                }
                String formattedDate = formatter.format(date);

                textView.setText(formattedDate);
                if (!pickup) {
                    returnDate = date;
                } else {
                    exchangeDate = date;
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dateTimeView);
        alertDialog.show();
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
        if (context instanceof HistoryFragment.OnFragmentInteractionListener) {
            mListener = (HistoryFragment.OnFragmentInteractionListener) context;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        String value = (String) parent.getItemAtPosition(position);

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
