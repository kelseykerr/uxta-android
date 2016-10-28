package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ImageButton;
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
public class RequestDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {
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
    private EditText itemName;
    private EditText description;
    private View view;
    public static Request request;

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
        itemName = (EditText) view.findViewById(R.id.request_name);
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
        rentalSpinner.setSelection(1);

        requestBtn = (Button) view.findViewById(R.id.create_request_button);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (request != null) {
                    updateRequest();
                } else {
                    createRequest(v);
                }
            }
        });

        closeRequestBtn = (Button) view.findViewById(R.id.close_request_button);
        if (request == null) {
            closeRequestBtn.setVisibility(View.GONE);
        } else {
            closeRequestBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    request.setExpireDate(new Date());
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

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri url, String nextFragment, int fragmentPostProcessingRequest);
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

    private Request createNewRequestObject() {
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
        newRequest.setLatitude(PrefUtils.latLng.latitude);
        newRequest.setLongitude(PrefUtils.latLng.longitude);
        newRequest.setLocation(null);
        return newRequest;
    }

    private void updateRequest() {
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
                    conn.setRequestProperty("Content-Type", "application/json");

                    updateRequestObject();
                    ObjectMapper mapper = new ObjectMapper();
                    // we don't need to update the location or user info
                    Request r = new Request();
                    r = request;
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
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not update request: " + e.getMessage());
                }
                return responseCode;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    dismiss();
                    ((MainActivity) getActivity()).goToHistory("successfully updated request");
                }
            }
        }.execute();
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
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
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
