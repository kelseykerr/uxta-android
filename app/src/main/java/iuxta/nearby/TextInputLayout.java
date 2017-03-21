package iuxta.nearby;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by kerrk on 11/19/16.
 */

public class TextInputLayout extends android.support.design.widget.TextInputLayout{


    public TextInputLayout(Context context) {
        super(context);
    }

    public TextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setError(@Nullable CharSequence error) {
        super.setError(error);
        if ((getEditText() != null && getEditText().getBackground() != null) &&
                (Build.VERSION.SDK_INT == 22 || Build.VERSION.SDK_INT == 21)) {
            Drawable drawable = getEditText().getBackground().getConstantState().newDrawable();
            getEditText().setBackgroundDrawable(drawable);
        }
    }
}
