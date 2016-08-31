package layout;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import superstartupteam.nearby.AppUtils;
import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Category;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.User;

/**
 * Created by kerrk on 8/23/16.
 */
public class NewRequestDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private User user;
    private Context context;
    Spinner typeSpinner;
    private OnFragmentInteractionListener mListener;
    private List<Category> categories;
    private List<String> categoryNames = new ArrayList<>();
    Spinner categorySpinner;
    Spinner rentalSpinner;
    Button requestBtn;
    EditText itemName;
    EditText description;
    View view;

    public NewRequestDialogFragment() {

    }

    public static NewRequestDialogFragment newInstance() {
        NewRequestDialogFragment fragment = new NewRequestDialogFragment();
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
        typeSpinner = (Spinner) view.findViewById(R.id.request_type);
        typeAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, types);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(this);

        itemName = (EditText) view.findViewById(R.id.request_name);
        description = (EditText) view.findViewById(R.id.request_description);

        categorySpinner = (Spinner) view.findViewById(R.id.request_category);
        ArrayAdapter<String> categoryAdapter;
        //TODO: this is crazy...why can't I use the list above. must fix.
        List<String> c = new ArrayList<>();
        c.add(Constants.SELECT_CATEGORY_STRING);
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

        requestBtn = (Button) view.findViewById(R.id.create_request_button);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createRequest(v);
            }
        });

        TextView cancelText = (TextView) view.findViewById(R.id.cancel_request);
        cancelText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                dismiss();
                return false;
            }
        });
        Rect clickableArea = new Rect();
        cancelText.getHitRect(clickableArea);
        // Extend the touch area of
        // the ImageButton beyond its bounds
        // on the right and bottom.
        clickableArea.left += 200;
        clickableArea.bottom += 200;
        clickableArea.top += 200;
        clickableArea.right += 200;
        TouchDelegate touchDelegate = new TouchDelegate(clickableArea,
                cancelText);
        // Sets the TouchDelegate on the parent view, such that touches
        // within the touch delegate bounds are routed to the child.
        if (View.class.isInstance(cancelText.getParent())) {
            ((View) cancelText.getParent()).setTouchDelegate(touchDelegate);
        }
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
        void onFragmentInteraction(Uri uri);
    }

    private Request createNewRequestObject() {
        Request newRequest = new Request();
        newRequest.setItemName(itemName.getText().toString());
        newRequest.setDescription(description.getText().toString());
        if (typeSpinner.getSelectedItem().toString().equals("item")) {
            newRequest.setType(Request.Type.item);
            newRequest.setRental(rentalSpinner.getSelectedItem().toString().equals("rent"));
        } else {
            newRequest.setType(Request.Type.service);
        }
        if (!categorySpinner.getSelectedItem().toString().equals(Constants.SELECT_CATEGORY_STRING)) {
            String cat = categorySpinner.getSelectedItem().toString();
            for (Category c : categories) {
                if (c.getName().equals(cat)) {
                    newRequest.setCategory(c);
                }
            }
        }
        newRequest.setPostDate(new Date());
        newRequest.setLatitude(PrefUtils.latLng.latitude);
        newRequest.setLongitude(PrefUtils.latLng.longitude);
        newRequest.setLocation(null);
        return newRequest;
    }

    private void createRequest(View v) {
        // AsyncTask<Params, Progress, Result>
        new AsyncTask<Void, Void, Integer>() {
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
                    conn.setRequestProperty("Content-Type", "application/json");

                    Request newRequest = createNewRequestObject();
                    ObjectMapper mapper = new ObjectMapper();
                    String requestJson = mapper.writeValueAsString(newRequest);
                    byte[] outputInBytes = requestJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("POST /api/requests", "Response Code : " + responseCode);
                    if (responseCode != 201) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not create new request: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 201) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory();
                }
            }
        }.execute();
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
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
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

}