package layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewOfferDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewOfferDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewOfferDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private String requestId;
    private Boolean isRental;
    private User user;
    private Context context;
    private OnFragmentInteractionListener mListener;
    private Spinner offerTypeSpinner;
    private Button submitOfferBtn;
    private TextInputLayout offerPriceLayout;
    private EditText offerPrice;
    private List<String> offerTypes = new ArrayList<>();
    private View view;
    private EditText pickupLocation;
    private TextInputLayout pickupTimeLayout;
    private EditText pickupTime;
    private TextView returnLocation;
    private TextInputLayout returnTimeLayout;
    private EditText returnTime;
    private Date exchangeDate;
    private Date returnDate;
    private ScrollView scrollView;
    private RelativeLayout spinnerScreen;
    private TextInputLayout descriptionLayout;
    private EditText description;
    private AppCompatCheckBox enableMessages;
    private static final String TAG = "NewOfferDialogFragment";


    public NewOfferDialogFragment() {
        // Required empty public constructor
    }

    public static NewOfferDialogFragment newInstance(String requestId, Boolean isRental) {
        NewOfferDialogFragment fragment = new NewOfferDialogFragment();
        Bundle args = new Bundle();
        args.putString("REQUEST_ID", requestId);
        args.putBoolean("RENTAL", isRental);
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
        if (getArguments() != null) {
            requestId = getArguments().getString("REQUEST_ID");
            isRental = getArguments().getBoolean("RENTAL");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_offer_dialog, container, false);
        /*offerTypes.add("flat");
        offerTypes.add("per hour");
        offerTypes.add("per day");
        ArrayAdapter<String> offerTypeAdapter;
        offerTypeSpinner = (Spinner) view.findViewById(R.id.offer_type);
        offerTypeAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, offerTypes);
        offerTypeSpinner.setAdapter(offerTypeAdapter);
        offerTypeSpinner.setOnItemSelectedListener(this);*/
        scrollView = (ScrollView) view.findViewById(R.id.scrollview);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);
        submitOfferBtn = (Button) view.findViewById(R.id.submit_offer_button);
        submitOfferBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitOfferBtn.setEnabled(false);
                if (validateForm()) {
                    scrollView.setVisibility(View.GONE);
                    spinnerScreen.setVisibility(View.VISIBLE);
                    createOffer(v);
                } else {
                    submitOfferBtn.setEnabled(true);
                }
            }
        });
        offerPriceLayout = (TextInputLayout) view.findViewById(R.id.offer_price_layout);
        offerPrice = (EditText) view.findViewById(R.id.offer_price);
        descriptionLayout = (TextInputLayout) view.findViewById(R.id.description_layout);
        description = (EditText) view.findViewById(R.id.description);

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        pickupLocation = (EditText) view.findViewById(R.id.pickup_location);
        pickupTimeLayout = (TextInputLayout) view.findViewById(R.id.pickup_time_layout);
        pickupTime = (EditText) view.findViewById(R.id.pickup_time);
        pickupTime.setKeyListener(null);
        returnLocation = (EditText) view.findViewById(R.id.return_location);
        returnTimeLayout = (TextInputLayout) view.findViewById(R.id.return_time_layout);
        returnTime = (EditText) view.findViewById(R.id.return_time);
        returnTime.setKeyListener(null);
        if (isRental != null && !isRental) {
            returnLocation.setVisibility(View.GONE);
            returnTime.setVisibility(View.GONE);
        } else {
            setDateFunctionality(returnTime, true);
        }

        setDateFunctionality(pickupTime, false);

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

        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_offer);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exchangeDate = null;
                returnDate = null;
                clearErrors();
                dismiss();
            }
        });
        enableMessages = (AppCompatCheckBox) view.findViewById(R.id.enable_messages);
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void createOffer(View v) {
        // AsyncTask<Params, Progress, Result>
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage;

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    String apiPath = "/requests/" + requestId + "/responses";
                    HttpURLConnection conn = AppUtils.getHttpConnection(apiPath, "POST", user);
                    Response newResponse = createNewResponseObject();
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(newResponse);
                    Log.i("response json: ", responseJson);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();
                    responseCode = conn.getResponseCode();
                    Log.i("POST /responses", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String output = AppUtils.getResponseContent(conn);
                        errorMessage = output;
                        throw new IOException(errorMessage);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not create offer: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode != null && responseCode == 200) {
                    submitOfferBtn.setEnabled(true);
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory("successfully created offer");
                } else if (responseCode != null && responseCode == 406) {
                    scrollView.setVisibility(View.VISIBLE);
                    spinnerScreen.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage != null ? errorMessage : "Could not create offer.", Constants.LONG_SNACK)
                            .setAction("SHOW OFFERS", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dismiss();
                                    ((MainActivity) getActivity()).goToHistory(null);
                                }
                            });
                    snackbar.show();
                } else {
                    scrollView.setVisibility(View.VISIBLE);
                    spinnerScreen.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage != null ? errorMessage : "Could not create offer.", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();
    }

    private void clearErrors() {
        offerPriceLayout.setError(null);
        pickupTimeLayout.setError(null);
        returnTimeLayout.setError(null);
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private Response createNewResponseObject() {
        Response response = new Response();
        response.setRequestId(requestId);
        response.setSellerId(user.getId());
        response.setExchangeLocation(pickupLocation.getText().toString());
        response.setDescription(description.getText().toString());
        if (exchangeDate != null) {
            response.setExchangeTime(exchangeDate);
        }
        response.setReturnLocation(returnLocation.getText().toString());
        if (returnDate != null) {
            response.setReturnTime(returnDate);
        }
        String offerString = offerPrice.getText().toString();
        if (!offerString.isEmpty()) {
            offerString = offerString.replace("$", "");
            double offer = Double.parseDouble(offerString);
            response.setOfferPrice(offer);
        }
        response.setPriceType("flat");
        response.setMessagesEnabled(enableMessages.isChecked());
        return response;
    }

    private void setDateFunctionality(final EditText time, final boolean isReturn) {

        time.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    openDatePicker(time, isReturn);
                    return true;
                }
                return true;
            }
        });
    }

    public void openDatePicker(final EditText time, final boolean isReturn) {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(context, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                final Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                new TimePickerDialog(context, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date date = calendar.getTime();
                        Date current = new Date();
                        if (isReturn) {
                            if (exchangeDate != null) {
                                Log.i(TAG, "Date selected: " + date.getTime());
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
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy hh:mm a");
                        String formattedDate = formatter.format(date);

                        time.setText(formattedDate);
                        if (isReturn) {
                            returnDate = date;
                        } else {
                            exchangeDate = date;
                        }

                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }
}
