package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

import iuxta.nearby.AppUtils;
import iuxta.nearby.Constants;
import iuxta.nearby.MainActivity;
import iuxta.nearby.PrefUtils;
import iuxta.nearby.R;
import iuxta.nearby.RequestAdapter;
import iuxta.nearby.model.Request;
import iuxta.nearby.model.User;
import iuxta.nearby.service.RequestNotificationService;


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
    public static User user;
    private List<Request> requests = new ArrayList<>();
    private RecyclerView recList;
    private RequestAdapter requestAdapter;
    private List<Marker> requestMarkers = new ArrayList<>();
    private TextView noResults; //map
    private TextView noResultsList;
    private ScrollView listView;
    private RelativeLayout requestMapView;
    private CameraUpdate cu;
    private Map<Double, String> radiusMap = new HashMap<Double, String>();
    private View view;
    public static Boolean homeLocation = false;
    public static Double currentRadius = 10.0;
    public static String sortBy;
    private Location currentLocation;
    public static String searchTerm;


    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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

        recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        noResults = (TextView) v.findViewById(R.id.no_results);
        noResults.setVisibility(View.GONE);

        noResultsList = (TextView) v.findViewById(R.id.no_results_list);
        noResultsList.setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRequests(currentRadius);
    }

    private void setMarkerClick() {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                boolean goodMerchantStatus = user.getCanRespond();
                if (user.getStripeManagedAccountId() != null && goodMerchantStatus) {
                    try {
                        for (Request r : requests) {
                            if (r.getLatitude().equals(marker.getPosition().latitude) &&
                                    r.getLongitude().equals(marker.getPosition().longitude) &&
                                    marker.getTitle().equals(r.getItemName())) {
                                showDialog(r.getId(), r.getRental());
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
                    /*if (user.getStripeManagedAccountId() != null &&
                            user.getMerchantStatus().toString().toLowerCase().equals("pending")) {
                        title = "Your merchant account is pending, please try again later";
                    }*/
                    title = "Please link your bank account to your profile";
                    Snackbar snack = Snackbar.make(view.getRootView(), title,
                            Constants.LONG_SNACK);
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
                                AccountFragment.paymentDialogFragment = PaymentDialogFragment.newInstance();
                                AccountFragment.paymentDialogFragment.show(getFragmentManager(), "dialog");
                            }
                        });
                    }
                    snack.getView().getRootView().setLayoutParams(params);
                    snack.show();
                }

            }
        });
    }

    public void showDialog(String itemId, Boolean isRental) {
        if (!MainActivity.areLocationServicesOn()) {
            ((MainActivity) getActivity()).showNoLocationServicesSnack(view);
            return;
        }
        DialogFragment newFragment = NewOfferDialogFragment.newInstance(itemId, isRental);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void showNoConnectionSnackbar() {
        Snackbar snackbar = Snackbar.make(view.getRootView(), R.string.noNetworkConnection,
                Constants.LONG_SNACK)
                .setAction("open settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                snackbar.getView().getRootView().getLayoutParams();

        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + 150);
        snackbar.getView().getRootView().setLayoutParams(params);
        snackbar.show();
    }


    public void getRequests(final Double radius) {
        if (!MainActivity.isNetworkConnected()) {
            showNoConnectionSnackbar();
            return;
        }
        if (!isAdded()) {
            return;
        }
        else if (user == null) {
            return;
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                if (latLng == null) {
                    return null;
                }
                try {
                    Double r = radius != null ? radius : currentRadius;
                    String urlString = Constants.NEARBY_API_PATH + "/requests?radius=" + r +
                            "&latitude=" + (homeLocation ? user.getHomeLatitude() : latLng.latitude)
                            + "&longitude=" + (homeLocation ? user.getHomeLongitude() : latLng.longitude) +
                            "&includeMine=false&expired=false";
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        urlString += ("&searchTerm=" + searchTerm);
                    }
                    if (sortBy != null && !sortBy.isEmpty() && !sortBy.equals("best match")) {
                        urlString += ("&sortBy=" + sortBy);
                    }
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty(Constants.METHOD_HEADER, user.getAuthMethod());
                    String output = AppUtils.getResponseContent(conn);
                    int responseCode = conn.getResponseCode();
                    Log.i("GET /requests", "Response Code : " + responseCode);
                    //Nearby is not available in this location
                    if (responseCode == 403) {
                        return responseCode;
                    }
                    try {
                        requests = AppUtils.jsonStringToRequestList(output);
                    } catch (IOException e) {
                        Log.e("Error", output);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not get requests: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                //Nearby not in this location yet
                if (responseCode != null && responseCode == 403) {
                    if (requestMarkers != null) {
                        //remove old markers
                        for (Marker m : requestMarkers) {
                            m.remove();
                        }
                    }
                    requestMarkers.clear();
                    noResults.setText("Nearby is not yet available here");
                    noResults.setVisibility(View.VISIBLE);
                } else {
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
                        noResults.setText("no results found");
                        noResults.setVisibility(View.VISIBLE);
                        noResultsList.setVisibility(View.VISIBLE);
                    } else {
                        noResults.setVisibility(View.GONE);
                        noResultsList.setVisibility(View.GONE);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(currLocationMarker.getPosition());
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
                            Marker marker = map.addMarker(markerOptions);
                            requestMarkers.add(marker);
                            builder.include(marker.getPosition());
                        }
                    }
                }
                if (latLng != null && !homeLocation) {
                    PrefUtils.setLatLng(latLng);
                    updateZoom(null, latLng);
                } else if (homeLocation != null && homeLocation && user.getHomeLongitude() != null
                        && user.getHomeLatitude() != null) {
                    LatLng home = new LatLng(user.getHomeLatitude(), user.getHomeLongitude());
                    updateZoom(null, home);
                }
            }
        }.execute();
    }

    private void updateZoom(Marker marker, LatLng latLng) {
        if (currentRadius == null) {
            currentRadius = 1.0;
        }
        CircleOptions options = new CircleOptions();
        options.center(marker != null ? marker.getPosition() : latLng);
        //Radius in meters
        options.radius(currentRadius * 1609.344);
        options.strokeWidth(10);

        double radius = options.getRadius();
        double scale = radius / 500;
        int zoomLevel = (int) Math.floor((16 - Math.log(scale) / Math.log(2)));

        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(zoomLevel)
                .target(marker != null ? marker.getPosition() : latLng).build();
        cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cu);
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
                getRequests(key);
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
        map.getUiSettings().setMapToolbarEnabled(false);

        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
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
            getRequests(1.0);
            if (recList != null) {
                requestAdapter = new RequestAdapter(requests, this);
                recList.setAdapter(requestAdapter);
            }
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

        //place marker at current position
        //mGoogleMap.clear();
        updateMapFocus(new LatLng(location.getLatitude(), location.getLongitude()));

        //If you only need one location, unregister the listener
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        getRequests(1.0);
        if (requestAdapter != null) {
            requestAdapter.swap(requests);
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

    public void displayUpdateAccountSnackbar() {
        Snackbar snack = Snackbar.make(view.getRootView(), "Please finish filling out your account info",
                Constants.LONG_SNACK)
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

    public void displayNoNewRequestSnackbar() {
        Snackbar snack = Snackbar.make(view.getRootView(), "Please add payment information to your account",
                Snackbar.LENGTH_LONG)
                .setAction("update account", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AccountFragment.paymentDialogFragment = PaymentDialogFragment.newInstance();
                        AccountFragment.paymentDialogFragment.show(getFragmentManager(), "dialog");
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
