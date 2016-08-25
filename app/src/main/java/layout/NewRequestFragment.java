package layout;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import impulusecontrol.lend.AppUtils;
import impulusecontrol.lend.Category;
import impulusecontrol.lend.Constants;
import impulusecontrol.lend.PrefUtils;
import impulusecontrol.lend.R;
import impulusecontrol.lend.model.User;

/**
 * Created by kerrk on 8/23/16.
 */
public class NewRequestFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private User user;
    private Context context;
    Spinner typeSpinner;
    private OnFragmentInteractionListener mListener;
    private List<Category> categories;
    private List<String> categoryNames = new ArrayList<>();
    Spinner categorySpinner;
    Spinner rentalSpinner;

    public NewRequestFragment() {

    }

    public static NewRequestFragment newInstance() {
        NewRequestFragment fragment = new NewRequestFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        List<String> types = new ArrayList<>();
        types.add("item");
        types.add("service");
        View view = inflater.inflate(R.layout.fragment_new_request, container, false);
        ArrayAdapter<String> typeAdapter;
        typeSpinner= (Spinner) view.findViewById(R.id.request_type);
        typeAdapter= new ArrayAdapter<String>(context, R.layout.spinner_item, types);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(this);


        categorySpinner = (Spinner) view.findViewById(R.id.request_category);
        ArrayAdapter<String> categoryAdapter;
        //TODO: this is crazy...why can't I use the list above. must fix.
        List<String> c = new ArrayList<>();
        c.add("select a category");
        c.add("tools");
        categoryAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, c);
        categorySpinner.setAdapter(categoryAdapter);
        rentalSpinner = (Spinner) view.findViewById(R.id.rental_spinner);
        ArrayAdapter<String> rentBuyAdapter;
        List<String> rentBuyList = new ArrayList<>();
        rentBuyList.add("rent");
        rentBuyList.add("buy");
        rentBuyAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, rentBuyList);
        rentalSpinner.setAdapter(rentBuyAdapter);

        return view;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void getCategories() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/categories");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("x-auth-token", user.getAccessToken());
                    String output = AppUtils.getResponseContent(conn);
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                            categories = mapper.readValue(output, new TypeReference<List<Category>>() {});
                            categoryNames.add("select a category...");
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
        if(value.equals("service")) {
            rentalSpinner.setVisibility(View.GONE);
        } else {
            rentalSpinner.setVisibility(View.VISIBLE);
        }

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
