package layout;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        List<Double> radiusList = new ArrayList<Double>();
        radiusList.add(.1);
        radiusList.add(.25);
        radiusList.add(.5);
        radiusList.add(1D);
        radiusList.add(5D);
        radiusList.add(10D);

        // Creating adapter for spinner
        ArrayAdapter<Double> dataAdapter = new ArrayAdapter<Double>(context, android.R.layout.simple_spinner_item, radiusList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        noResults = (TextView) v.findViewById(R.id.no_results);
        noResults.setVisibility(View.GONE);
        return v;
    }

    public void showDialog(String itemId) {
        DialogFragment newFragment = NewOfferDialogFragment
                .newInstance(itemId);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void getRequests(final Double radius) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (latLng == null) {
                    return null;
                }
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests?radius=" + radius +
                            "&latitude=" + latLng.latitude + "&longitude=" + latLng.longitude +
                            "&includeMine=false&expired=false");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
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

                for (Request request : requests) {
                    LatLng latLng = new LatLng(request.getLatitude(), request.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(request.getItemName());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    Marker marker = map.addMarker(markerOptions);
                    requestMarkers.add(marker);
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = 120; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.moveCamera(cu);

            }
        }.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a radius from the spinner
        Double radius = (Double) parent.getItemAtPosition(position);
        // Get requests within that radius
        getRequests(radius);

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
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


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        void onFragmentInteraction(Uri uri);
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
