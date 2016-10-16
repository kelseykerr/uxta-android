package layout;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.Circle;
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
import superstartupteam.nearby.model.User;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mapFragment = (MapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        mapFragment.getMapAsync(this);
        user = PrefUtils.getCurrentUser(context);
        listView = (ScrollView) v.findViewById(R.id.list_view);
        listView.setVisibility(View.GONE);
        requestMapView = (RelativeLayout) v.findViewById(R.id.map_view);

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

        // Drop down layout style - list view with radio button
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        noResults = (TextView) v.findViewById(R.id.no_results);
        noResults.setVisibility(View.GONE);
        getRequests(currentRadius);
        return v;
    }

    private void setMarkerClick() {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
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

            }
        });
    }

    public void showDialog(String itemId) {
        DialogFragment newFragment = NewOfferDialogFragment.newInstance(itemId);
        newFragment.show(getFragmentManager(), "dialog");
    }



    public void getRequests(final Double radius) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (latLng == null) {
                    return null;
                }
                try {
                    Double r = radius != null ? radius : currentRadius;
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests?radius=" + r +
                            "&latitude=" + latLng.latitude + "&longitude=" + latLng.longitude +
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
                    if (latLng != null) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                        PrefUtils.setLatLng(latLng);
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
                if (options != null)
                {
                    double radius = options.getRadius();
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
            getRequests(.1);
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

        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Log.i("Location", "longitude: " + latLng.longitude + " latitude: " + latLng.latitude);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = map.addMarker(markerOptions);

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(15).build();

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        //If you only need one location, unregister the listener
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        getRequests(.1);
        requestAdapter.swap(requests);
        //ft.show(mapFragment).commit();

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
        void onFragmentInteraction(Uri uri, String nextFragment);
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
}
