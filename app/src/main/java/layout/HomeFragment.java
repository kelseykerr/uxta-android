package layout;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import impulusecontrol.lend.Category;
import impulusecontrol.lend.R;
import impulusecontrol.lend.Request;
import impulusecontrol.lend.RequestAdapter;

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
        View v =  inflater.inflate(R.layout.fragment_home, container, false);
        mapFragment = (MapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        fm = this.getChildFragmentManager();
        ft = fm.beginTransaction();
        mapFragment.getMapAsync(this);

        // Spinner element
        Spinner spinner = (Spinner) v.findViewById(R.id.radius_spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> radiusList = new ArrayList<String>();
        radiusList.add(".1 miles");
        radiusList.add(".25 miles");
        radiusList.add(".5 miles");
        radiusList.add("1 mile");
        radiusList.add("5 miles");
        radiusList.add("10 miles");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, radiusList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        RecyclerView recList = (RecyclerView) v.findViewById(R.id.request_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        //TODO: fetch requests from server
        List<Request> requests = new ArrayList<>();
        Category cat = new Category();
        cat.setId("1");
        cat.setName("dummy category");
        Request r1 = new Request();
        r1.setId("1");
        r1.setItemName("Item 1");
        r1.setCategory(cat);
        r1.setPostDate(new Date());
        //r1.setExpireDate(new Date() + );
        r1.setDescription("a dummy request");
        Request r2 = new Request();
        r2.setId("2");
        r2.setItemName("Item 2");
        r2.setCategory(cat);
        r2.setPostDate(new Date());
        //r1.setExpireDate(new Date() + );
        r2.setDescription("a dummy request");
        Request r3 = new Request();
        r3.setId("3");
        r3.setItemName("Item 3");
        r3.setCategory(cat);
        r3.setPostDate(new Date());
        //r1.setExpireDate(new Date() + );
        r3.setDescription("a dummy request");
        requests.add(r1);
        requests.add(r2);
        requests.add(r3);

        RequestAdapter ca = new RequestAdapter(requests);
        recList.setAdapter(ca);

        return v;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
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
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = map.addMarker(markerOptions);
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
        Toast.makeText(context,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context,"onConnectionFailed",Toast.LENGTH_SHORT).show();
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
}
