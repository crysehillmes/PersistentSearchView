package org.cryse.widget.persistentsearch;

import android.app.Activity;
import android.content.Intent;

public abstract class VoiceRecognitionDelegate {
    public static final int DEFAULT_VOICE_REQUEST_CODE = 8185102;
    private int mVoiceRecognitionRequestCode;
    private Activity mActivity;

    public VoiceRecognitionDelegate(Activity activity) {
        this.mActivity = activity;
        this.mVoiceRecognitionRequestCode = DEFAULT_VOICE_REQUEST_CODE;
    }

    public VoiceRecognitionDelegate(Activity activity, int activityRequestCode) {
        this.mActivity = activity;
        this.mVoiceRecognitionRequestCode = activityRequestCode;
    }

    public void onStartVoiceRecognition() {
        if (mActivity != null) {
            Intent intent = buildVoiceRecognitionIntent();
            mActivity.startActivityForResult(intent, mVoiceRecognitionRequestCode);
        }
    }

    public Activity getActivity() {
        return mActivity;
    }

    public abstract Intent buildVoiceRecognitionIntent();

    public abstract boolean isVoiceRecognitionAvailable();
}
