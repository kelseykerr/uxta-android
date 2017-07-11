package layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;

import iuxta.uxta.AppUtils;
import iuxta.uxta.Constants;
import iuxta.uxta.MainActivity;
import iuxta.uxta.PrefUtils;
import iuxta.uxta.R;
import iuxta.uxta.SharedAsyncMethods;
import iuxta.uxta.model.Community;
import iuxta.uxta.model.User;

/**
 * Created by kelseykerr on 7/10/17.
 */

public class CommunityDetailFragment extends DialogFragment {

    private User user;
    private Context context;
    private static final String TAG = "CommunityDetailFragment";
    private Community community;
    private TextView communityName;
    private TextView communityDescription;
    private TextView communityAddress;
    private Button actionBtn;
    private View view;
    private RelativeLayout spinnerScreen;
    private static AccountFragment accountFragment;

    public static CommunityDetailFragment newInstance(Community community, AccountFragment accountFragment) {
        CommunityDetailFragment.accountFragment = accountFragment;
        CommunityDetailFragment fragment = new CommunityDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("COMMUNITY", community);
        fragment.setArguments(args);
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
        if (getArguments() != null) {
            community = (Community) getArguments().getSerializable("COMMUNITY");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_detail, container, false);
        ImageButton cancelBtn = (ImageButton) view.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        communityName = (TextView) view.findViewById(R.id.community_name);
        communityName.setText(community.getName());
        communityDescription = (TextView) view.findViewById(R.id.community_description);
        communityDescription.setText(community.getDescription());
        communityAddress = (TextView) view.findViewById(R.id.community_address);
        communityAddress.setText(community.getAddress());
        actionBtn = (Button) view.findViewById(R.id.action_btn);
        spinnerScreen = (RelativeLayout) view.findViewById(R.id.spinner_screen);
        if (user.getCommunityId() != null && user.getCommunityId().equals(community.getId())) {
            actionBtn.setBackgroundColor(actionBtn.getContext().getResources().getColor(R.color.colorAccent));
            actionBtn.setText("Leave Community");
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leaveCommunity(community.getId());
                }
            });
        } else {
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    joinCommunity(community.getId());
                }
            });
        }
        this.view = view;
        return view;

    }

    private void joinCommunity(final String communityId) {
        communityName.setVisibility(View.GONE);
        communityDescription.setVisibility(View.GONE);
        communityAddress.setVisibility(View.GONE);
        actionBtn.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/communities/" + communityId + "/users", "POST", user);
                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "POST /communities/{id}/users response code : " + responseCode);
                    if (responseCode != 200) {
                            String output = AppUtils.getResponseContent(conn);
                            errorMessage = output;
                            throw new IOException(errorMessage);

                    } else {
                        SharedAsyncMethods.getUserInfoFromServer(user, context);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR could not request to add user to community: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    if (accountFragment != null) {
                        accountFragment.requestedAccess(community.getName());
                    } else {
                        ((MainActivity) getActivity()).goToAccount("Your request to join join the community will be reviewed shortly.");
                    }
                    dismiss();
                } else {
                    communityName.setVisibility(View.VISIBLE);
                    communityDescription.setVisibility(View.VISIBLE);
                    communityAddress.setVisibility(View.VISIBLE);
                    actionBtn.setVisibility(View.VISIBLE);
                    spinnerScreen.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage != null ? errorMessage : "Could not request access at this time.", Constants.LONG_SNACK);
                    snackbar.show();
                }
            }
        }.execute();
    }

    private void leaveCommunity(final String communityId) {
        //TODO: show snack asking user to confirm
        communityName.setVisibility(View.GONE);
        communityDescription.setVisibility(View.GONE);
        communityAddress.setVisibility(View.GONE);
        actionBtn.setVisibility(View.GONE);
        spinnerScreen.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage;

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    HttpURLConnection conn = AppUtils.getHttpConnection("/communities/" + communityId + "/users", "DELETE", user);
                    Integer responseCode = conn.getResponseCode();
                    Log.i(TAG, "DELETE /communities/{id}/users response code : " + responseCode);
                    if (responseCode != 200) {
                        String output = AppUtils.getResponseContent(conn);
                        errorMessage = output;
                        throw new IOException(errorMessage);

                    } else {
                        SharedAsyncMethods.getUserInfoFromServer(user, context);
                    }
                    return responseCode;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR could not leave community: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                if (responseCode == 200) {
                    if (accountFragment != null) {
                        accountFragment.requestedRemoval(community.getName());
                    } else {
                        ((MainActivity) getActivity()).goToAccount("Your have been removed from the community.");
                    }
                    dismiss();
                } else {
                    communityName.setVisibility(View.VISIBLE);
                    communityDescription.setVisibility(View.VISIBLE);
                    communityAddress.setVisibility(View.VISIBLE);
                    actionBtn.setVisibility(View.VISIBLE);
                    spinnerScreen.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar
                            .make(view, errorMessage != null ? errorMessage : "Could not leave community at this time.", Constants.LONG_SNACK);
                    snackbar.show();
                }
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
}
