package layout;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import superstartupteam.nearby.Constants;
import superstartupteam.nearby.MainActivity;
import superstartupteam.nearby.PrefUtils;
import superstartupteam.nearby.R;
import superstartupteam.nearby.model.Category;
import superstartupteam.nearby.model.Request;
import superstartupteam.nearby.model.Response;
import superstartupteam.nearby.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewOfferDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewOfferDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewOfferDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener{
    private String requestId;
    private User user;
    private Context context;
    private OnFragmentInteractionListener mListener;
    private Spinner offerTypeSpinner;
    private Button submitOfferBtn;
    private EditText offerPrice;
    private List<String> offerTypes = new ArrayList<>();




    public NewOfferDialogFragment() {
        // Required empty public constructor
    }

    public static NewOfferDialogFragment newInstance(String requestId) {
        NewOfferDialogFragment fragment = new NewOfferDialogFragment();
        Bundle args = new Bundle();
        args.putString("REQUEST_ID", requestId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        user = PrefUtils.getCurrentUser(context);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestId = getArguments().getString("REQUEST_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_offer_dialog, container, false);

        offerTypes.add("flat");
        offerTypes.add("per hour");
        offerTypes.add("per day");
        ArrayAdapter<String> offerTypeAdapter;
        offerTypeSpinner = (Spinner) view.findViewById(R.id.offer_type);
        offerTypeAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, offerTypes);
        offerTypeSpinner.setAdapter(offerTypeAdapter);
        offerTypeSpinner.setOnItemSelectedListener(this);

        submitOfferBtn = (Button) view.findViewById(R.id.submit_offer_button);
        submitOfferBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createOffer(v);
            }
        });

        offerPrice = (EditText) view.findViewById(R.id.offer_price);

        TextView cancelText = (TextView) view.findViewById(R.id.cancel_offer);
        cancelText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                dismiss();
                return false;
            }
        });
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

    private void createOffer(View v) {
        // AsyncTask<Params, Progress, Result>
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer responseCode = null;
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/requests/" + requestId + "/responses");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(Constants.AUTH_HEADER, user.getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");

                    Response newResponse = createNewResponseObject();
                    ObjectMapper mapper = new ObjectMapper();
                    String responseJson = mapper.writeValueAsString(newResponse);
                    Log.i("response json: ", responseJson);
                    byte[] outputInBytes = responseJson.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                    responseCode = conn.getResponseCode();
                    Log.i("POST /responses", "Response Code : " + responseCode);
                    if (responseCode != 201) {
                        throw new IOException(conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    Log.e("ERROR ", "Could not create offer: " + e.getMessage());
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private Response createNewResponseObject() {
        Response response = new Response();
        response.setRequestId(requestId);
        response.setSellerId(user.getId());
        double offer = Double.parseDouble(offerPrice.getText().toString());
        response.setOfferPrice(offer);
        String offerType = offerTypeSpinner.getSelectedItem().toString();
        response.setPriceType(offerType);
        return response;
    }
}
