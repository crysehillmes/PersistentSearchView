package android.support.v7.app;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewCompat;

public class SupportDrawerArrowDrawable extends DrawerArrowDrawable {

    private final Activity mActivity;

    public SupportDrawerArrowDrawable(Activity activity, Context themedContext) {
        super(themedContext);
        mActivity = activity;
    }

    public void setPosition(float position) {
        if (position == 1f) {
            setVerticalMirror(true);
        } else if (position == 0f) {
            setVerticalMirror(false);
        }
        super.setProgress(position);
    }

    @Override
    boolean isLayoutRtl() {
        return ViewCompat.getLayoutDirection(mActivity.getWindow().getDecorView())
                == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public float getPosition() {
        return super.getProgress();
    }
}