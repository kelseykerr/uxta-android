package layout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import iuxta.uxta.R;
import iuxta.uxta.ScannerActivity;

/**
 * Created by kerrk on 9/30/16.
 */
public class EnterCodeFragment extends Fragment {

    private EditText codeInput;
    private Button submitCodeBtn;
    private static final String TAG = "EnterCodeFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_code, container, false);
        codeInput = (EditText) view.findViewById(R.id.exchange_code_input);
        submitCodeBtn = (Button) view.findViewById(R.id.submit_code_button);
        submitCodeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String code = codeInput.getText().toString();
                Log.i(TAG, "input code was: " + code);
                ((ScannerActivity)getActivity()).verifyCode(code);
            }
        });
        return view;
    }

}
