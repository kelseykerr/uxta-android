package layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import iuxta.uxta.AppUtils;
import iuxta.uxta.Constants;
import iuxta.uxta.FullScreenImageActivity;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.model.Category;
import iuxta.uxta.model.Request;
import iuxta.uxta.model.User;
import iuxta.uxta.service.RequestNotificationService;

import static android.app.Activity.RESULT_OK;

/**
 * Created by kerrk on 8/23/16.
 */
public class RequestDialogFragment extends DialogFragment
        implements AdapterView.OnItemSelectedListener {
    public static Request request;
    private User user;
    private Context context;
    private Spinner typeSpinner;
    private OnFragmentInteractionListener mListener;
    private List<Category> categories;
    private List<String> categoryNames = new ArrayList<>();
    private Spinner categorySpinner;
    private Spinner rentalSpinner;
    private Button requestBtn;
    private Button closeRequestBtn;
    private TextInputLayout itemNameLayout;
    private EditText itemName;
    private EditText description;
    private View view;
    private ScrollView newRequestSV;
    private RelativeLayout spinnerScreen;
    private TextView photosText;
    private ImageButton addPhotos;
    private LinearLayout photoLayout;
    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private ImageView delete1;
    private ImageView delete2;
    private ImageView delete3;
    private ProgressBar spinner1;
    private ProgressBar spinner2;
    private ProgressBar spinner3;
    private static final String RENT_TEXT = "request to rent an item";
    private static final String BUY_TEXT = "request to buy an item";
    private static final String SELL_TEXT = "sell an item";
    private static final String LOAN_TEXT = "list a rentable item";
    private static final int SELECT_PICTURE = 17;
    private List<String> photos;
    public Bitmap[] bitmaps;
    private static final String TAG = "RequestDialogFragment";


    public RequestDialogFragment() {

    }

    public static RequestDialogFragment newInstance() {
        RequestDialogFragment fragment = new RequestDialogFragment();
        request = null;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static RequestDialogFragment newInstance(Request r) {
        request = r;
        RequestDialogFragment fragment = new RequestDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        getCategories();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_dialog, container, false);
        photos = new ArrayList<>();
        bitmaps = new Bitmap[3];
        photoLayout = (LinearLayout) view.findViewById(R.id.photo_layout);
        photo1 = (ImageView) view.findViewById(R.id.photo_1);
        photo2 = (ImageView) view.findViewById(R.id.photo_2);
        photo3 = (ImageView) view.findViewById(R.id.photo_3);
        delete1 = (ImageView) view.findViewById(R.id.delete_1);
        delete2 = (ImageView) view.findViewById(R.id.delete_2);
        delete3 = (ImageView) view.findViewById(R.id.delete_3);
        spinner1 = (ProgressBar) view.findViewById(R.id.loading_spinner_1);
        spinner2 = (ProgressBar) view.findViewById(R.id.loading_spinner_2);
        spinner3 = (ProgressBar) view.findViewById(R.id.loading_spinner_3);
        if (request != null) {
            Button btn = (Button) view.findViewById(R.id.create_request_button);
            btn.setText("update");
            TextView dialogTitle = (TextView) view.findViewById(R.id.new_request_text);
            dialogTitle.setText("Edit Item");
            if (request.getPhotos() != null) {
                photos = request.getPhotos();
                setPhotos();
            }
        }
        newRequestSV = (ScrollView) view.findViewById(R.id.new_request_sv);
        itemNameLayout = (TextInputLayout) view.findViewById(R.id.request_name_layout);
        itemName = (EditText) view.findViewById(R.id.request_name);
        itemName.setBackground(itemName.getBackground().getConstantState().newDrawable());

        description = (EditText) view.findViewById(R.id.request_description);
        photosText = (TextView) view.findViewById(R.id.photos_text);
        addPhotos = (ImageButton) view.findViewById(R.id.add_photos);
        addPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPhotos();
            }
        });
        setDeleteClick(delete1, 1);
        setDeleteClick(delete2, 2);
        setDeleteClick(delete3, 3);
        for (int k = 0; k < photos.size(); k++) {
            if (k == 0) {
                delete1.setVisibility(View.VISIBLE);
            } else if (k == 1) {
                delete2.setVisibility(View.VISIBLE);
            } else if (k == 2) {
                delete3.setVisibility(View.VISIBLE);
                addPhotos.setVisibility(View.GONE);
            }
        }

        rentalSpinner = (Spinner) view.findViewById(R.id.rental_spinner);
        ArrayAdapter<String> rentBuyAdapter;
        List<String> rentBuyList = new ArrayList<>();
        rentBuyList.add(RENT_TEXT);
        rentBuyList.add(BUY_TEXT);
        rentBuyList.add(SELL_TEXT);
        rentBuyList.add(LOAN_TEXT);
        rentBuyAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, rentBuyList);
        rentBuyAdapter.setDropDownViewResource(R.layout.spinner_item);
        rentalSpinner.setAdapter(rentBuyAdapter);
        rentalSpinner.setSelection(0);
        rentalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0 || position == 1) {
                    photoLayout.setVisibility(View.GONE);
                    photosText.setVisibility(View.GONE);
                    addPhotos.setVisibility(View.GONE);
                } else {
                    photoLayout.setVisibility(View.VISIBLE);
                    photosText.setVisibility(View.VISIBLE);
                    addPhotos.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                photoLayout.setVisibility(View.GONE);
                photosText.setVisibility(View.GONE);
                addPhotos.setVisibility(View.GONE);
            }

        });
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);

        requestBtn = (Button) view.findViewById(R.id.create_request_button);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requestBtn.setEnabled(false);
                if (validateNewRequest()) {
                    newRequestSV.setVisibility(View.GONE);
                    spinnerScreen.setVisibility(View.VISIBLE);
                    if (request != null) {
                        updateRequest();
                    } else {
                        createRequest(v);
                    }
                } else {
                    requestBtn.setEnabled(true);
                }
            }
        });

        closeRequestBtn = (Button) view.findViewById(R.id.close_request_button);
        if (request == null) {
            closeRequestBtn.setVisibility(View.GONE);
        } else {
            closeRequestBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    request.setExpireDate(new Date(new Date().getTime() - 60000));
                    updateRequest();
                }
            });
        }
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel_request);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (request == null) {
                    for (String photo:photos) {
                        ((MainActivity) getActivity()).deletePhoto(photo);
                    }
                }
                request = null;
                dismiss();
            }
        });
        this.view = view;
        if (request != null) {
            itemName.setText(request.getItemName());
            if (request.getType().equals(Request.Type.renting)) {
                rentalSpinner.setSelection(0);
            } else if (request.getType().equals(Request.Type.buying)) {
                rentalSpinner.setSelection(1);
            } else if (request.getType().equals(Request.Type.selling)) {
                rentalSpinner.setSelection(2);
            } else if (request.getType().equals(Request.Type.loaning)) {
                rentalSpinner.setSelection(3);
            }
            // TODO: update this as we add categories
            if (request.getCategory() != null) {
                //categorySpinner.setSelection(1);
            }
            description.setText(request.getDescription());
        }
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

    private void updateRequestObject() {
        request.setRental(rentalSpinner.getSelectedItem().toString().equals(RENT_TEXT));
        /*if (!categorySpinner.getSelectedItem().toString().equals(Constants.SELECT_CATEGORY_STRING)) {
            String cat = categorySpinner.getSelectedItem().toString();
            for (Category c : categories) {
                if (c.getName().equals(cat)) {
                    request.setCategory(c);
                }
            }
        }*/
        setRequestType(request);
        request.setItemName(itemName.getText().toString());
        request.setDescription(description.getText().toString());
        request.setPhotos(photos);
    }

    private void setRequestType(Request request) {
        switch (rentalSpinner.getSelectedItem().toString()) {
            case RENT_TEXT:
                request.setType(Request.Type.renting);
                return;
            case BUY_TEXT:
                request.setType(Request.Type.buying);
                return;
            case SELL_TEXT:
                request.setType(Request.Type.selling);
                return;
            case LOAN_TEXT:
                request.setType(Request.Type.loaning);
                return;
            default:
                request.setType(Request.Type.renting);
                return;
        }
    }

    private Request createNewRequestObject() {
        Request newRequest = new Request();
        newRequest.setItemName(itemName.getText().toString());
        newRequest.setDescription(description.getText().toString());
        newRequest.setPhotos(photos);
        boolean isRental = rentalSpinner.getSelectedItem().toString().equals(RENT_TEXT);
        setRequestType(newRequest);
        newRequest.setRental(isRental);
        /*if (!categorySpinner.getSelectedItem().toString().equals(Constants.SELECT_CATEGORY_STRING)) {
            String cat = categorySpinner.getSelectedItem().toString();
            for (Category c : categories) {
                if (c.getName().equals(cat)) {
                    newRequest.setCategory(c);
                }
            }
        }*/
        newRequest.setPostDate(new Date());
        newRequest.setLocation(null);
        return newRequest;
    }

    private boolean validateNewRequest() {
        boolean valid = true;
        String itemString = itemName.getText().toString();
        if (itemString.isEmpty()) {
            itemNameLayout.setError("item name cannot be empty");
            valid = false;
        }
        return valid;
    }

    private void showNoNetworkSnack() {
        Snackbar.make(view, R.string.noNetworkConnection,
                Constants.LONG_SNACK)
                .setAction("open settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    private void showErrorSnack() {
        Snackbar.make(view, "Unable to create request at this time",
                Constants.LONG_SNACK).show();
    }

    private void updateRequest() {
        if (!MainActivity.isNetworkConnected()) {
            showNoNetworkSnack();
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/requests/" + request.getId(), "PUT", user);
                    updateRequestObject();
                    ObjectMapper mapper = new ObjectMapper();
                    // we don't need to update the location or user info
                    Request r = request;
                    r.setUser(null);
                    r.setLocation(null);
                    String requestJson = mapper.writeValueAsString(r);
                    Log.i("updated request: ", requestJson);
                    byte[] outputInBytes = requestJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("PUT /api/requests", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String output = AppUtils.getResponseContent(conn);
                        throw new IOException(output);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update request: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                HistoryFragment.dismissViewRequestFragment();
                if (responseCode == 200) {
                    ((MainActivity) getActivity()).goToHistory("successfully updated request");
                    dismiss();
                } else {
                    ((MainActivity) getActivity()).goToHistory("could not update request at this time");
                    dismiss();
                }
            }
        }.execute();
    }

    private void createRequest(View v) {
        if (!MainActivity.isNetworkConnected()) {
            showNoNetworkSnack();
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage;

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/requests", "POST", user);
                    Request newRequest = createNewRequestObject();
                    ObjectMapper mapper = new ObjectMapper();
                    String requestJson = mapper.writeValueAsString(newRequest);
                    byte[] outputInBytes = requestJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("POST /api/requests", "Response Code : " + responseCode);
                    if (responseCode != 200) {
                        String output = AppUtils.getResponseContent(conn);
                        errorMessage = output;
                        throw new IOException(output);
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not create new request: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory("successfully created request");
                } else {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory(errorMessage != null ?
                            errorMessage : "Could not create request at this time");
                }
            }
        }.execute();
    }

    public void getCategories() {
        if (!MainActivity.isNetworkConnected()) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/categories", "GET", user);
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        categories = mapper.readValue(output, new TypeReference<List<Category>>() {
                        });
                        categoryNames.add(Constants.SELECT_CATEGORY_STRING);
                        for (int i = 0; i < categories.size(); i++) {
                            categoryNames.add(categories.get(i).getName());
                        }
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to fetch " +
                                "categories from server, please try again later!");
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get categories: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        String value = (String) parent.getItemAtPosition(position);

        // Get requests within that radius
        if (value.equals("service")) {
            rentalSpinner.setVisibility(View.GONE);
        } else {
            rentalSpinner.setVisibility(View.VISIBLE);
        }

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
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

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri imageUri = data.getData();
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    File f = ((MainActivity) getActivity()).compressFile(bm);
                    if (photos == null || photos.size() == 0) {
                        photo1.setImageBitmap(bm);
                        delete1.setVisibility(View.VISIBLE);
                        setImageClick(photo1, imageUri);
                        bitmaps[0] = bm;
                    } else if (photos.size() == 1) {
                        photo2.setImageBitmap(bm);
                        delete2.setVisibility(View.VISIBLE);
                        setImageClick(photo2, imageUri);
                        bitmaps[1] = bm;
                    } else if (photos.size() == 2) {
                        photo3.setImageBitmap(bm);
                        delete3.setVisibility(View.VISIBLE);
                        setImageClick(photo3, imageUri);
                        addPhotos.setVisibility(View.GONE);
                        bitmaps[2] = bm;
                    }
                    String key = MainActivity.uploadPhoto(f);
                    photos.add(key);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setPhotos() {
        for (int i= 0; i < photos.size(); i++) {
            //only allow 3 photos
            if (i > 2) {
                break;
            }
            try {
                File dir = context.getCacheDir();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File f = File.createTempFile(photos.get(i), null, dir);
                ImageView photo = null;
                ImageView delete = null;
                ProgressBar spinner = null;
                if (i==0) {
                    spinner1.setVisibility(View.VISIBLE);
                    spinner = spinner1;
                    photo = photo1;
                    delete = delete1;
                } else if (i==1) {
                    spinner2.setVisibility(View.VISIBLE);
                    spinner = spinner2;
                    photo = photo2;
                    delete = delete2;
                } else if (i==2) {
                    spinner3.setVisibility(View.VISIBLE);
                    spinner = spinner3;
                    photo = photo3;
                    delete = delete3;
                }
                ((MainActivity) getActivity()).fetchPhoto(photos.get(i), f, context, this, photo, delete, spinner, i);
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }

    public void setImageClick(ImageView image, final Uri uri) {
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(context, FullScreenImageActivity.class);
                fullScreenIntent.setData(uri);
                startActivity(fullScreenIntent);
            }
        });
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
                ((MainActivity) getActivity()).deletePhoto(photos.get(order -1));
                photos.remove(order - 1);
                List<Bitmap> list = new ArrayList<Bitmap>(Arrays.asList(bitmaps));
                list.remove(order - 1);
                bitmaps = list.toArray(bitmaps);
                for (int i = 0; i < photos.size(); i++) {
                    if (i==0) {
                        photo1.setImageBitmap(bitmaps[0]);
                        setDeleteClick(delete1, 1);
                        delete1.setVisibility(View.VISIBLE);
                    } else if (i == 1) {
                        photo2.setImageBitmap(bitmaps[1]);
                        setDeleteClick(delete2, 2);
                        delete2.setVisibility(View.VISIBLE);
                    } else if (i ==2) {
                        photo3.setImageBitmap(bitmaps[2]);
                        setDeleteClick(delete3, 3);
                        delete3.setVisibility(View.VISIBLE);
                    }
                }
                addPhotos.setVisibility(View.VISIBLE);
                deleteBtn.setEnabled(true);
            }
        });
    }

}
