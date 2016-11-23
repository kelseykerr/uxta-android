package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 11/23/16.
 */

public class FiltersDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private User user;
    private Context context;
    private View view;
    private Double currentRadius;
    private Boolean homeLocation;
    private String sortBy;
    private Spinner radiusSpinner;
    private Spinner locationSpinner;
    private Spinner sortBySpinner;
    private UpdateAccountDialogFragment.OnFragmentInteractionListener mListener;
    private Map<Double, String> radiusMap = new HashMap<Double, String>();


    public static FiltersDialogFragment newInstance() {
        FiltersDialogFragment fragment = new FiltersDialogFragment();
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters_dialog, container, false);
        currentRadius = HomeFragment.currentRadius;
        homeLocation = HomeFragment.homeLocation;
        sortBy = HomeFragment.sortBy;
        locationSpinner = (Spinner) view.findViewById(R.id.location_spinner);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.locationItems));
        locationAdapter.setDropDownViewResource(R.layout.spinner_item);
        locationSpinner.setAdapter(locationAdapter);
        if (user.getHomeLongitude() != null && user.getHomeLatitude() != null) {
            locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (selectedItem.equals("current location")) {
                        homeLocation = false;
                    } else {
                        homeLocation = true;
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            locationSpinner.setVisibility(View.VISIBLE);
            if (homeLocation == null || !homeLocation) {
                locationSpinner.setSelection(1);
            } else {
                locationSpinner.setSelection(2);
            }
        } else {
            locationSpinner.setVisibility(View.GONE);
        }


        // Spinner element
        radiusSpinner = (Spinner) view.findViewById(R.id.radius_spinner);

        // Spinner click listener
        radiusSpinner.setOnItemSelectedListener(this);

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
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.simple_spinner_item, radiusList);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);

        // attaching data adapter to spinner
        radiusSpinner.setAdapter(dataAdapter);

        // set radius spinner to current radius
        if (currentRadius != null) {
            String selectedRadius = radiusMap.get(currentRadius);
            for (int i = 0; i < radiusList.size(); i++) {
                if (radiusList.get(i).equals(selectedRadius)) {
                    radiusSpinner.setSelection(i+1);
                    break;
                }
            }
        }

        sortBySpinner = (Spinner) view.findViewById(R.id.sort_by_spinner);
        final ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.sortByItems));
        sortByAdapter.setDropDownViewResource(R.layout.spinner_item);
        sortBySpinner.setAdapter(sortByAdapter);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                sortBy = selectedItem;
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set sort by spinner to current sort by
        if (sortBy != null) {
            String [] sortItems = getResources().getStringArray(R.array.sortByItems);
            for (int i = 0; i < sortItems.length; i++) {
                if (sortItems[i].equals(sortBy)) {
                    sortBySpinner.setSelection(i+1);
                    break;
                }
            }
        } else {
            sortBySpinner.setSelection(1);
        }

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.close_filters);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        Button submitBtn = (Button) view.findViewById(R.id.filter_button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HomeFragment.currentRadius = currentRadius;
                HomeFragment.homeLocation = homeLocation;
                HomeFragment.sortBy = sortBy;
                String nextFragment = " ";
                Uri url = null;
                mListener.onFragmentInteraction(url, nextFragment, Constants.FPPR_SUBMIT_FILTERS);
                dismiss();
            }
        });

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
        if (context instanceof UpdateAccountDialogFragment.OnFragmentInteractionListener) {
            mListener = (UpdateAccountDialogFragment.OnFragmentInteractionListener) context;
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
        mListener = (UpdateAccountDialogFragment.OnFragmentInteractionListener) activity;

    }

    protected void onAttachToContext(Context context) {
        this.context = context;
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
                break;
            }
        }

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
        void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
    }

}
