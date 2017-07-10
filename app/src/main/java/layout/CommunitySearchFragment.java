package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import iuxta.uxta.AppUtils;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.model.Community;
import iuxta.uxta.model.User;

/**
 * Created by kelseykerr on 7/9/17.
 */

public class CommunitySearchFragment extends DialogFragment implements AdapterView.OnItemClickListener{

    private ListView communitiesListView;
    private CommunityAdapter adapter;
    private EditText searchText;
    List<Community> communityList;
    private User user;
    private Context context;
    private static AccountFragment accountFragment;
    private static final String TAG = "CommunitySearchFragment";


    public static CommunitySearchFragment newInstance(AccountFragment accountFragment) {
        CommunitySearchFragment.accountFragment = accountFragment;
        CommunitySearchFragment fragment = new CommunitySearchFragment();
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
        super.onCreate(savedInstanceState);
        user = PrefUtils.getCurrentUser(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_search, container, false);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        communityList = new ArrayList<>();
        Community c = new Community();
        c.setName("");
        communityList.add(c);
        communitiesListView = (ListView) view.findViewById(R.id.community_list_view);
        searchText = (EditText) view.findViewById(R.id.community_search);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                if (cs.toString().length() > 3) {
                    getCommunities(cs.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        adapter = new CommunityAdapter(context, R.layout.community_list_item, communityList);
        communitiesListView.setAdapter(adapter);
        communitiesListView.setOnItemClickListener(this);
        getCommunities(null);
        return view;
    }

    private void getCommunities(final String searchTerm) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    String path = "/communities" + (searchTerm != null && !searchTerm.isEmpty() ? "?searchTerm=" + searchTerm : "");
                    HttpURLConnection conn = AppUtils.getHttpConnection(path, "GET", user);
                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "PUT /price response code : " + responseCode);
                    if (responseCode != 200) {
                        throw new IOException(conn.getResponseMessage());
                    }
                    String output = AppUtils.getResponseContent(conn);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        List<Community> pojos = mapper.readValue(output, new TypeReference<List<Community>>() {});
                        communityList = pojos;
                        Log.i(TAG, communityList.size() + " - communities returned");
                    } catch (IOException e) {
                        Log.e(TAG, "error converting string to community list: " + e.getMessage());
                        throw new IOException(e);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR could not confirm price: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                adapter.updateList(communityList);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
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


    protected void onAttachToContext(Context context) {
        this.context = context;
    }


    public void onItemClick(AdapterView parent, View v, int position, long id) {
        Community c = communityList.get(position);
        CommunityDetailFragment communityDetailFragment = CommunityDetailFragment.newInstance(c, accountFragment);
        communityDetailFragment.show(getFragmentManager(), "dialog");
    }


    public static class CommunityAdapter extends ArrayAdapter<Community> {

        List<Community> communityList = new ArrayList<>();

        public CommunityAdapter(Context context, int textViewResourceId, List<Community> objects) {
            super(context, textViewResourceId, objects);
            communityList = objects;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        public void updateList(List<Community> communities) {
            this.communityList = communities;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Community c = communityList.get(position);
            View view = inflater.inflate(R.layout.community_list_item, null);
            TextView textView = (TextView) view.findViewById(R.id.community_name);
            textView.setText(c.getName());
            TextView descriptionText = (TextView) view.findViewById(R.id.community_description);
            descriptionText.setText(c.getDescription());
            return view;

        }

    }

}
