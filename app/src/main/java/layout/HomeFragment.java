package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.RequestAdapter;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;
import superstartupteam.nearby.service.NearbyMessagingService;
import superstartupteam.nearby.service.RequestNotificationService;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        AdapterView.OnItemSelectedListener {

    private GoogleMap map;
    private MapView mapView;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker currLocationMarker;
    LatLng latLng;
    FragmentManager fm;
    FragmentTransaction ft;
    MapFragment mapFragment;
    private Context context;
    private User user;
    private List<Request> requests = new ArrayList<>();
    private RecyclerView recList;
    private RequestAdapter requestAdapter;
    private List<Marker> requestMarkers = new ArrayList<>();
    private TextView noResults;
    private ScrollView listView;
    private RelativeLayout requestMapView;
    private Double currentRadius;
    private CameraUpdate cu;
    private Map<Double, String> radiusMap = new HashMap<Double, String>();
    private View view;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Boolean homeLocation;
    private Location currentLocation;


    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        view = v;
        mapFragment = (MapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        mapFragment.getMapAsync(this);
        user = PrefUtils.getCurrentUser(context);
        listView = (ScrollView) v.findViewById(R.id.list_view);
        listView.setVisibility(View.GONE);
        requestMapView = (RelativeLayout) v.findViewById(R.id.map_view);

        Spinner locationSpinner = (Spinner) v.findViewById(R.id.location_spinner);
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
                        getRequests(currentRadius, false);
                    } else {
                        homeLocation = true;
                        if (currLocationMarker != null) {
                            currLocationMarker.remove();
                        }
                        latLng = new LatLng(user.getHomeLatitude(), user.getHomeLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Home Location");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        currLocationMarker = map.addMarker(markerOptions);

                        //zoom to current position:
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng).zoom(15).build();

                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                        getRequests(currentRadius, true);
                    }
                } // to close the onItemSelected
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            locationSpinner.setVisibility(View.VISIBLE);
        } else {
            locationSpinner.setVisibility(View.GONE);
        }


        // Spinner element
        Spinner spinner = (Spinner) v.findViewById(R.id.radius_spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> radiusList = new ArrayList<>();
        radiusMap.put(.1, ".1 mile radius");
        radiusMap.put(.25, ".25 mile radius");
        radiusMap.put(.5, ".5 mile radius");
        radiusMap.put(1.0, "1 mile radius");
        radiusMap.put(5.0, "5 mile radius");
        radiusMap.put(10.0, "10 mile radius");
        radiusList.add(radiusMap.get(.1));
        radiusList.add(radiusMap.get(.25));
        radiusList.add(radiusMap.get(.5));
        radiusList.add(radiusMap.get(1.0));
        radiusList.add(radiusMap.get(5.0));
        radiusList.add(radiusMap.get(10.0));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, radiusList);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        noResults = (TextView) v.findViewById(R.id.no_results);
        noResults.setVisibility(View.GONE);
        getRequests(currentRadius, false);
        return v;
    }

    private void setMarkerClick() {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                boolean goodMerchantStatus = user.getMerchantStatus() != null &&
                        user.getMerchantStatus().toString().toLowerCase().equals("active");
                if (user.getMerchantId() != null && goodMerchantStatus) {
                    try {
                        for (Request r:requests) {
                            if (r.getLatitude().equals(marker.getPosition().latitude) &&
                                    r.getLongitude().equals(marker.getPosition().longitude) &&
                                    marker.getTitle().equals(r.getItemName())) {
                                showDialog(r.getId());
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("marker click", " could not find request from marker index [" +
                                marker.getSnippet() + "]");
                    }
                } else {
                    String title;
                    boolean showAction = false;
                    if (user.getMerchantStatus() != null &&
                            user.getMerchantStatus().toString().toLowerCase().equals("pending")) {
                        title = "Your merchant account is pending, please try again later";
                    } else {
                        showAction = true;
                        if (user.getMerchantStatusMessage() != null) {
                            title = user.getMerchantStatusMessage();
                        } else {
                            title = "Please link your bank account or venmo account to your profile";
                        }
                    }
                    Snackbar snack = Snackbar.make(view.getRootView(), title,
                            Snackbar.LENGTH_LONG);
                    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                            snack.getView().getRootView().getLayoutParams();
                    params.setMargins(params.leftMargin,
                            params.topMargin,
                            params.rightMargin,
                            params.bottomMargin + 150);
                    if (showAction) {
                        snack.setAction("update account", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                                AccountFragment.updateAccountDialog.show(getFragmentManager(), "dialog");
                            }
                        });
                    }
                    snack.getView().getRootView().setLayoutParams(params);
                    snack.show();
                }

            }
        });
    }

    public void showDialog(String itemId) {
        DialogFragment newFragment = NewOfferDialogFragment.newInstance(itemId);
        newFragment.show(getFragmentManager(), "dialog");
    }



    public void getRequests(final Double radius, final boolean homeAddress) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (latLng == null) {
                    return null;
                }
                try {
                    Double r = radius != null ? radius : currentRadius;
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests?radius=" + r +
                            "&latitude=" + (homeAddress ? user.getHomeLatitude() : latLng.latitude)
                            + "&longitude=" + (homeAddress ? user.getHomeLongitude() : latLng.longitude) +
                            "&includeMine=false&expired=false");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    int responseCode = conn.getResponseCode();
                    Log.i("GET /requests", "Response Code : " + responseCode);
                    try {
                        requests = AppUtils.jsonStringToRequestList(output);
                    } catch (IOException e) {
                        Log.e("Error", "Received an error while trying to fetch " +
                                "requests from server, please try again later!");
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get requests: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (requestAdapter != null) {
                    requestAdapter.swap(requests);
                }
                if (requestMarkers != null) {
                    //remove old markers
                    for (Marker m : requestMarkers) {
                        m.remove();
                    }
                }
                requestMarkers.clear();
                if (requests.size() < 1) {
                    noResults.setVisibility(View.VISIBLE);
                    if (latLng != null && !homeLocation) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                        PrefUtils.setLatLng(latLng);
                    } else if (homeLocation != null && homeLocation && user.getHomeLongitude() != null
                            && user.getHomeLatitude() != null) {
                        LatLng home = new LatLng(user.getHomeLatitude(), user.getHomeLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(home).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }
                    return;
                } else {
                    noResults.setVisibility(View.GONE);
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(currLocationMarker.getPosition());
                int i = 0;
                setMarkerClick();
                for (Request request : requests) {
                    LatLng latLng = new LatLng(request.getLatitude(), request.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(request.getItemName());
                    if (request.getDescription() != null && request.getDescription().length() > 0) {
                        markerOptions.snippet(request.getDescription());
                    }

                    float[] hsv = new float[3];
                    Color.colorToHSV(getResources().getColor(R.color.colorPrimary), hsv);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));

                    //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    Marker marker = map.addMarker(markerOptions);
                    requestMarkers.add(marker);
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = 120; // offset from edges of the map in pixels
                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.moveCamera(cu);

                CircleOptions options = new CircleOptions();
                options.center(currLocationMarker.getPosition());
                //Radius in meters
                options.radius(currentRadius * 1609.344);
                options.strokeWidth(10);

                int zoomLevel = 11;
                if (options != null) {
                    double radius = options.getRadius();
                    //radius = 300 * 1609.344; /*for Ken's testing */
                    double scale = radius / 500;
                    zoomLevel = (int) Math.floor((16 - Math.log(scale) / Math.log(2)));
                }
                cu = CameraUpdateFactory.zoomTo(zoomLevel);
                map.moveCamera(cu);

            }
        }.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        String radiusString = (String) parent.getItemAtPosition(position);
        for (Map.Entry<Double, String> entry : radiusMap.entrySet()) {
            Double key = entry.getKey();
            String value = entry.getValue();
            if (value.equals(radiusString)) {
                currentRadius = key;
                // Get requests within that radius
                getRequests(key, false);
                break;
            }
        }

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e("map permission error: ", "unable to get user's current location, " + e.getMessage());
        }
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = map.addMarker(markerOptions);
            getRequests(.1, false);
            if (recList != null) {
                requestAdapter = new RequestAdapter(requests, this);
                recList.setAdapter(requestAdapter);
            }
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

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
        if (homeLocation) {
            return;
        }

        //place marker at current position
        //mGoogleMap.clear();
        updateMapFocus(new LatLng(location.getLatitude(), location.getLongitude()));

        //If you only need one location, unregister the listener
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        getRequests(.1, false);
        requestAdapter.swap(requests);
        //ft.show(mapFragment).commit();

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
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
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

    public void toggleView(String v) {
        if (v.equals("list")) {
            listView.setVisibility(View.VISIBLE);
            requestMapView.setVisibility(View.GONE);
        } else {
            requestMapView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    private void registerBroadcastReceiver() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NOTIFICATION_MESSAGE");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Snackbar snackbar = Snackbar.make(view.getRootView(), message, Snackbar.LENGTH_LONG);
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                    snackbar.getView().getRootView().getLayoutParams();

            params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 150);
            String type = intent.getStringExtra("type");
            View.OnClickListener mOnClickListener;
            Response response = null;
            Request request = null;
            boolean hasRequestResponseParams = type !=  null &&
                    (type.equals(NearbyMessagingService.NotificationType.response_update.toString())
                            || type.equals(NearbyMessagingService.NotificationType.offer_accepted.toString())
                            || type.equals(NearbyMessagingService.NotificationType.offer_closed.toString()));
            if (hasRequestResponseParams) {
                String responseJson = intent.getStringExtra("response");
                String requestJson = intent.getStringExtra("request");
                try {
                    response = new ObjectMapper().readValue(responseJson, Response.class);
                    request = new ObjectMapper().readValue(requestJson, Request.class);
                } catch (IOException e) {
                    Log.e("JSON ERROR", "**" + e.getMessage());
                }
            }

            if (type != null) {
                switch (type) {
                    case "response_update":
                        if (response.getId() != null) {
                            DialogFragment newFragment = null;
                            newFragment = ViewOfferDialogFragment.newInstance(response, request);
                            final DialogFragment frag = newFragment;
                            mOnClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    frag.show(getFragmentManager(), "dialog");
                                }
                            };
                            snackbar.setAction("view", mOnClickListener);
                        }
                        break;
                    default:
                        break;
                }
            }
            snackbar.getView().getRootView().setLayoutParams(params);
            snackbar.show();
        }
    };

    public void displayNoNewRequestSnackbar() {
        Snackbar snack = Snackbar.make(view.getRootView(), "Please add payment information to your account",
                Snackbar.LENGTH_LONG)
                .setAction("update account", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AccountFragment.updateAccountDialog = UpdateAccountDialogFragment.newInstance();
                        AccountFragment.updateAccountDialog.show(getFragmentManager(), "dialog");
                    }
                });
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snack.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);

        snack.getView().getRootView().setLayoutParams(params);
        snack.show();
    }

}
