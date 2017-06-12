package layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.FullScreenImageActivity;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.Response;
import iuxta.nearby.model.User;

import static android.app.Activity.RESULT_OK;
import static layout.RequestDialogFragment.isDownloadsDocument;
import static layout.RequestDialogFragment.isExternalStorageDocument;
import static layout.RequestDialogFragment.isMediaDocument;

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
    private final String TAG = "ViewOfferDialogFragment";
    private LinearLayout photoLayout;
    private TextView photosText;
    private ImageButton addPhotos;
    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private ImageView delete1;
    private ImageView delete2;
    private ImageView delete3;
    private ProgressBar spinner1;
    private ProgressBar spinner2;
    private ProgressBar spinner3;
    private static final int SELECT_PICTURE = 19;
    public List<Bitmap> bitmaps;


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
        bitmaps = new ArrayList<>();
        pickupLocation = (EditText) view.findViewById(R.id.pickup_location);
        pickupLocation.setText(response.getExchangeLocation());
        returnLocation = (EditText) view.findViewById(R.id.return_location);
        returnLocation.setText(response.getReturnLocation());
        returnTimeLayout = (TextInputLayout) view.findViewById(R.id.return_time_layout);
        returnTime = (EditText) view.findViewById(R.id.return_time);
        returnTime.setKeyListener(null);
        if (request.getType().equals(Request.Type.buying) || request.getType().equals(Request.Type.selling)) {
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
        if (request.getType().equals(Request.Type.loaning) || request.getType().equals(Request.Type.selling)) {
            description.setHint("Message");
        }
        description.setText(response.getDescription());
        addPhotos = (ImageButton) view.findViewById(R.id.add_photos);
        addPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPhotos();
            }
        });
        //if this is the buyer, don't allow them to edit the description
        if (!response.getResponderId().equals(user.getId())) {
            description.setEnabled(false);
            addPhotos.setVisibility(View.GONE);
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

        photoLayout = (LinearLayout) view.findViewById(R.id.photo_layout);
        photosText = (TextView) view.findViewById(R.id.photos_text);
        photo1 = (ImageView) view.findViewById(R.id.photo_1);
        photo1.setVisibility(View.GONE);
        photo2 = (ImageView) view.findViewById(R.id.photo_2);
        photo2.setVisibility(View.GONE);
        photo3 = (ImageView) view.findViewById(R.id.photo_3);
        photo3.setVisibility(View.GONE);
        delete1 = (ImageView) view.findViewById(R.id.delete_1);
        delete2 = (ImageView) view.findViewById(R.id.delete_2);
        delete3 = (ImageView) view.findViewById(R.id.delete_3);
        setDeleteClick(delete1, 1);
        setDeleteClick(delete2, 2);
        setDeleteClick(delete3, 3);
        spinner1 = (ProgressBar) view.findViewById(R.id.loading_spinner_1);
        spinner2 = (ProgressBar) view.findViewById(R.id.loading_spinner_2);
        spinner3 = (ProgressBar) view.findViewById(R.id.loading_spinner_3);
        if (response.getPhotos() == null && !response.getResponderId().equals(user.getId())) {
            photosText.setVisibility(View.GONE);
            photoLayout.setVisibility(View.GONE);
        } else {
            photoLayout.setVisibility(View.VISIBLE);
            boolean responder = response.getResponderId().equals(user.getId());
            if (!responder) {
                delete1.setVisibility(View.GONE);
                delete2.setVisibility(View.GONE);
                delete3.setVisibility(View.GONE);
            }
            for (int i = 0; i < response.getPhotos().size(); i++) {
                try {
                    File dir = context.getCacheDir();
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File f = File.createTempFile(response.getPhotos().get(i), null, dir);
                    ImageView photo = null;
                    ProgressBar spinner = null;
                    if (i == 0) {
                        spinner1.setVisibility(View.VISIBLE);
                        if (responder) {
                            delete1.setVisibility(View.VISIBLE);
                        }
                        spinner = spinner1;
                        photo = photo1;
                        photo1.setVisibility(View.VISIBLE);
                    } else if (i == 1) {
                        spinner2.setVisibility(View.VISIBLE);
                        if (responder) {
                            delete2.setVisibility(View.VISIBLE);
                        }
                        spinner = spinner2;
                        photo = photo2;
                        photo2.setVisibility(View.VISIBLE);
                    } else if (i == 2) {
                        addPhotos.setVisibility(View.GONE);
                        if (responder) {
                            delete3.setVisibility(View.VISIBLE);
                        }
                        spinner3.setVisibility(View.VISIBLE);
                        spinner = spinner3;
                        photo = photo3;
                        photo3.setVisibility(View.VISIBLE);
                    }
                    ((MainActivity) getActivity()).fetchPreviewPhoto(response.getPhotos().get(i), f, context, photo, spinner, null, this);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }
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
        if (response.getResponderId().equals(user.getId())) {
            messageSellerBtn.setVisibility(View.GONE);
            rejectRequestBtn.setText("withdraw offer");
        } else if (response.getMessagesEnabled() == null || !response.getMessagesEnabled() || response.getResponder() == null || response.getResponder().getPhone() == null) {
            messageSellerBtn.setVisibility(View.GONE);
        } else {
            messageSellerBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    PackageManager packageManager = ((MainActivity)getActivity()).getPackageManager();
                    List activities = packageManager.queryIntentActivities(smsIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    boolean isIntentSafe = activities.size() > 0;
                    if (!isIntentSafe) {
                        Snackbar snackbar = Snackbar
                                .make(v, "no messaging app found", Constants.LONG_SNACK);
                        snackbar.show();
                    } else {
                        messageSellerBtn.setEnabled(false);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        String phone;
                        phone = response.getResponder().getPhone().replace("-", "");
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
                    r.setResponder(null);
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
                        if (request.getType().equals(Request.Type.loaning) || request.getType().equals(Request.Type.selling)) {
                            response.setSellerStatus(Response.SellerStatus.ACCEPTED);
                        } else {
                            response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                        }
                    } else {
                        if (request.getType().equals(Request.Type.loaning) || request.getType().equals(Request.Type.selling)) {
                            response.setBuyerStatus(Response.BuyerStatus.ACCEPTED);
                        } else {
                            response.setSellerStatus(Response.SellerStatus.ACCEPTED);
                        }
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    User seller = response.getResponder();
                    response.setResponder(null);
                    String responseJson = mapper.writeValueAsString(response);
                    Log.i("updated response: ", responseJson);
                    response.setResponder(seller);
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
        if (request.isRental()) {
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

    public void setImageClick(ImageView image, final Uri uri) {
        image.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(context, FullScreenImageActivity.class);
                fullScreenIntent.setData(uri);
                startActivity(fullScreenIntent);
            }
        });
    }


    private void openDatePicker(final EditText textView, final boolean pickup) {
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
                        if (!pickup) {
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

                        textView.setText(formattedDate);
                        if (!pickup) {
                            returnDate = date;
                        } else {
                            exchangeDate = date;
                        }

                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
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

    public void setDeleteClick(final ImageView deleteBtn, final int order) {
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.setEnabled(false);
                photo1.setImageResource(R.drawable.ic_insert_photo_black_24dp);
                photo2.setImageResource(R.drawable.ic_insert_photo_black_24dp);
                photo3.setImageResource(R.drawable.ic_insert_photo_black_24dp);
                delete1.setVisibility(View.GONE);
                delete2.setVisibility(View.GONE);
                delete3.setVisibility(View.GONE);
                ((MainActivity) getActivity()).deletePhoto(response.getPhotos().get(order -1));
                response.getPhotos().remove(order - 1);
                bitmaps.remove(order - 1);
                for (int i = 0; i < response.getPhotos().size(); i++) {
                    if (i==0) {
                        photo1.setImageBitmap(bitmaps.get(0));
                        setDeleteClick(delete1, 1);
                        delete1.setVisibility(View.VISIBLE);
                    } else if (i == 1) {
                        photo2.setImageBitmap(bitmaps.get(1));
                        setDeleteClick(delete2, 2);
                        delete2.setVisibility(View.VISIBLE);
                    } else if (i ==2) {
                        photo3.setImageBitmap(bitmaps.get(2));
                        setDeleteClick(delete3, 3);
                        delete3.setVisibility(View.VISIBLE);
                    }
                }
                addPhotos.setVisibility(View.VISIBLE);
                deleteBtn.setEnabled(true);
            }
        });
    }

    private void addPhotos() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 19) {
            // For Android KitKat, we use a different intent to ensure
            // we can
            // get the file path from the returned intent URI
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.setType("image/*");
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        }

        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }


    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri imageUri = data.getData();
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    File f = ((MainActivity) getActivity()).compressFile(bm);
                    if (response.getPhotos() == null || response.getPhotos().size() == 0) {
                        photo1.setImageBitmap(bm);
                        photo1.setVisibility(View.VISIBLE);
                        delete1.setVisibility(View.VISIBLE);
                        setImageClick(photo1, imageUri);
                    } else if (response.getPhotos().size() == 1) {
                        photo2.setImageBitmap(bm);
                        photo2.setVisibility(View.VISIBLE);
                        delete2.setVisibility(View.VISIBLE);
                        setImageClick(photo2, imageUri);
                    } else if (response.getPhotos().size() == 2) {
                        photo3.setImageBitmap(bm);
                        photo3.setVisibility(View.VISIBLE);
                        delete3.setVisibility(View.VISIBLE);
                        setImageClick(photo3, imageUri);
                        addPhotos.setVisibility(View.GONE);
                    }
                    String key = MainActivity.uploadPhoto(f);
                    response.getPhotos().add(key);
                    bitmaps.add(bm);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
