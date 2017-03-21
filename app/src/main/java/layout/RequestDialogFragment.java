package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.model.Category;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.User;
import iuxta.nearby.service.RequestNotificationService;

/**
 * Created by kerrk on 8/23/16.
 */
public class RequestDialogFragment extends DialogFragment
        implements AdapterView.OnItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {
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
    private MapFragment mapFragment;
    private GoogleMap map;
    private CameraUpdate cu;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private FragmentManager fm;
    private FragmentTransaction ft;
    public static Boolean homeLocation = false;
    private LatLng latLng;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker currLocationMarker;
    private RelativeLayout mapView;
    private ScrollView newRequestSV;
    private RelativeLayout spinnerScreen;
    private int zoomLevel = 15;


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

        if (request != null) {
            Button btn = (Button) view.findViewById(R.id.create_request_button);
            btn.setText("update request");
            TextView dialogTitle = (TextView) view.findViewById(R.id.new_request_text);
            dialogTitle.setText("Edit Request");
        }
        newRequestSV = (ScrollView) view.findViewById(R.id.new_request_sv);
        itemNameLayout = (TextInputLayout) view.findViewById(R.id.request_name_layout);
        itemName = (EditText) view.findViewById(R.id.request_name);
        itemName.setBackground(itemName.getBackground().getConstantState().newDrawable());

        description = (EditText) view.findViewById(R.id.request_description);

        //Let's not do categories for the MVP...we can add this back in later
        /*categorySpinner = (Spinner) view.findViewById(R.id.request_category);
        ArrayAdapter<String> categoryAdapter;
        //TODO: this is crazy...why can't I use the list above. must fix.
        List<String> c = new ArrayList<>();
        c.add(Constants.SELECT_CATEGORY_STRING);
        c.add("tools");
        categoryAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, c);
        categorySpinner.setAdapter(categoryAdapter);*/
        rentalSpinner = (Spinner) view.findViewById(R.id.rental_spinner);
        ArrayAdapter<String> rentBuyAdapter;
        List<String> rentBuyList = new ArrayList<>();
        rentBuyList.add("rent");
        rentBuyList.add("buy");
        rentBuyAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, rentBuyList);
        rentBuyAdapter.setDropDownViewResource(R.layout.spinner_item);
        rentalSpinner.setAdapter(rentBuyAdapter);
        rentalSpinner.setSelection(0);
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
                request = null;
                dismiss();
            }
        });
        this.view = view;
        if (request != null) {
            itemName.setText(request.getItemName());
            if (request.getRental()) {
                rentalSpinner.setSelection(0);
            } else {
                rentalSpinner.setSelection(1);
            }
            // TODO: update this as we add categories
            if (request.getCategory() != null) {
                //categorySpinner.setSelection(1);
            }
            description.setText(request.getDescription());
        } else {
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.request_map);
            fm = this.getFragmentManager();
            ft = fm.beginTransaction();
            mapFragment.getMapAsync(this);

            mapView = (RelativeLayout) view.findViewById(R.id.request_map_view);
            mapView.setVisibility(View.VISIBLE);
            TextView mapText = (TextView) view.findViewById(R.id.map_text);
            mapText.setVisibility(View.VISIBLE);
            setupLocationSpinner(view);
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
        request.setRental(rentalSpinner.getSelectedItem().toString().equals("rent"));
        /*if (!categorySpinner.getSelectedItem().toString().equals(Constants.SELECT_CATEGORY_STRING)) {
            String cat = categorySpinner.getSelectedItem().toString();
            for (Category c : categories) {
                if (c.getName().equals(cat)) {
                    request.setCategory(c);
                }
            }
        }*/
        request.setItemName(itemName.getText().toString());
        request.setDescription(description.getText().toString());
    }

    private Request createNewRequestObject(Double lat, Double lng) {
        Request newRequest = new Request();
        newRequest.setItemName(itemName.getText().toString());
        newRequest.setDescription(description.getText().toString());
        newRequest.setType(Request.Type.item);
        newRequest.setRental(rentalSpinner.getSelectedItem().toString().equals("rent"));
        /*if (!categorySpinner.getSelectedItem().toString().equals(Constants.SELECT_CATEGORY_STRING)) {
            String cat = categorySpinner.getSelectedItem().toString();
            for (Category c : categories) {
                if (c.getName().equals(cat)) {
                    newRequest.setCategory(c);
                }
            }
        }*/
        newRequest.setPostDate(new Date());
        newRequest.setLatitude(lat);
        newRequest.setLongitude(lng);
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
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/" + request.getId());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty("Content-Type", "application/json");

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
        final Double lat = currLocationMarker.getPosition() != null
                ? currLocationMarker.getPosition().latitude : PrefUtils.latLng.latitude;
        final Double lng = currLocationMarker.getPosition() != null
                ? currLocationMarker.getPosition().longitude : PrefUtils.latLng.longitude;
        // AsyncTask<Params, Progress, Result>
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage;

            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    conn.setRequestProperty("Content-Type", "application/json");

                    Request newRequest = createNewRequestObject(lat, lng);
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
                    URL url = new URL(Constants.NEARBY_API_PATH + "/categories");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
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

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (cu != null) {
                    //map.moveCamera(cu);
                }
            }
        });
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setScrollGesturesEnabled(false);
            map.setOnMarkerClickListener(this);
        } catch (SecurityException e) {
            Log.e("map permission error: ", "unable to get user's current location, " + e.getMessage());
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                if (currLocationMarker != null) {
                    currLocationMarker.remove();
                }
                currLocationMarker = map.addMarker(new MarkerOptions()
                        .position(latlng)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            }
        });
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {
            Log.e("map permission error: ", "unable to get user's last location, " + e.getMessage());
        }
        if (mLastLocation != null) {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            PrefUtils.setLatLng(latLng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Request location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = map.addMarker(markerOptions);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } catch (SecurityException e) {
            Log.e("map permission error: ", "unable to request location updates, " + e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (homeLocation != null && homeLocation) {
            return;
        }
        updateMapFocus(new LatLng(location.getLatitude(), location.getLongitude()));
        //If you only need one location, unregister the listener
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        fm = this.getFragmentManager();
        if (fm != null) {
            ft = fm.beginTransaction();
        }
    }

    private void updateMapFocus(LatLng ll) {
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(ll.latitude, ll.longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Log.i("Location", "longitude: " + latLng.longitude + " latitude: " + latLng.latitude);
        RequestNotificationService.latLng = latLng;
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = map.addMarker(markerOptions);
        //zoom to current position:
        updateZoom(currLocationMarker, null);
    }

    private void updateZoom(Marker marker, LatLng latLng) {
        CircleOptions options = new CircleOptions();
        options.center(marker != null ? marker.getPosition() : latLng);
        //Radius in meters
        options.radius(AppUtils.DEFAULT_REQUEST_RADIUS * 1609.344);
        options.strokeWidth(10);

        double radius = options.getRadius();
        double scale = radius / 500;
        zoomLevel = (int) Math.floor((16 - Math.log(scale) / Math.log(2)));

        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(zoomLevel)
                .target(marker != null ? marker.getPosition() : latLng).build();
        cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cu);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        return true;
    }

    public void setupLocationSpinner(View view) {
        Spinner locationSpinner = (Spinner) view.findViewById(R.id.location_spinner);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item,
                getResources().getStringArray(R.array.locationItems));
        locationSpinner.setAdapter(locationAdapter);
        if (user.getHomeLongitude() != null && user.getHomeLatitude() != null) {
            locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (selectedItem.equals("current location")) {
                        homeLocation = false;
                        if (latLng != null) {
                            updateMapFocus(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()));
                        }
                    } else {
                        homeLocation = true;
                        if (currLocationMarker != null) {
                            currLocationMarker.remove();
                        }
                        latLng = new LatLng(user.getHomeLatitude(), user.getHomeLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        currLocationMarker = map.addMarker(markerOptions);

                        //zoom to current position:
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng).zoom(zoomLevel).build();

                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }
                } // to close the onItemSelected

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            locationSpinner.setVisibility(View.VISIBLE);
        } else {
            locationSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.app.Fragment fragment = getFragmentManager()
                .findFragmentById(R.id.request_map);
        if (null != fragment) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
    }


}
