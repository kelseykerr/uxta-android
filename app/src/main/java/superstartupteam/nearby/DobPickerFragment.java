package superstartupteam.nearby;


import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.DatePicker;

public class DobPickerFragment extends DatePickerDialog implements DatePicker.OnDateChangedListener {

    private DatePickerDialog mDatePicker;

    public DobPickerFragment(Context context, int theme, OnDateSetListener callBack,
                             int year, int monthOfYear, int dayOfMonth) {
        super(context, theme,callBack, year, monthOfYear, dayOfMonth);
        mDatePicker = new DatePickerDialog(context,theme,callBack, year, monthOfYear, dayOfMonth) {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        };

        mDatePicker.getDatePicker().init(year, monthOfYear, dayOfMonth, this);

    }

    public DatePickerDialog getPicker(){

        return this.mDatePicker;
    }
}
