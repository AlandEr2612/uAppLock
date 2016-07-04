package com.arrg.app.uapplock.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.arrg.app.uapplock.R;
import com.easyandroidanimations.library.Animation;
import com.easyandroidanimations.library.ShakeAnimation;
import com.norbsoft.typefacehelper.TypefaceHelper;
import com.shawnlin.preferencesmanager.PreferencesManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrg.app.uapplock.UAppLock.DURATIONS_OF_ANIMATIONS;

public class RequestPinFragment extends Fragment {

    @Bind(R.id.tvMessage)
    AppCompatTextView tvMessage;
    @Bind(R.id.indicatorDots)
    IndicatorDots indicatorDots;
    @Bind(R.id.pinLockView)
    PinLockView pinLockView;
    private String pin = "";
    private Vibrator vibrator;

    public RequestPinFragment() {
    }

    public static RequestPinFragment newInstance() {
        return new RequestPinFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_pin, container, false);
        ButterKnife.bind(this, view);
        TypefaceHelper.typeface(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pinLockView.attachIndicatorDots(indicatorDots);
        pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String userPin) {
                if (pin.length() == 0){
                    pin = userPin;

                    resetPin();
                    updateText(R.string.message_to_confirm_pin);
                } else {
                    if (pin.equals(userPin)) {
                        pinLockView.attachIndicatorDots(null);

                        updateText(R.string.correct_configuration_message_for_pin);

                        PreferencesManager.putString(getString(R.string.user_pin), pin);
                    } else {
                        new ShakeAnimation(indicatorDots).setNumOfShakes(2).setDuration(Animation.DURATION_SHORT).animate();

                        vibrator.vibrate(DURATIONS_OF_ANIMATIONS);

                        resetPin();
                        updateText(R.string.message_to_repeat_pattern);
                    }
                }
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btnResetPin)
    public void onClick() {
        pin = "";

        pinLockView.resetPinLockView();
        pinLockView.attachIndicatorDots(indicatorDots);

        PreferencesManager.putString(getString(R.string.user_pin), pin);

        updateText(R.string.message_to_request_pattern);
    }

    public void resetPin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pinLockView.resetPinLockView();
            }
        }, DURATIONS_OF_ANIMATIONS);
    }

    public void updateText(int text) {
        tvMessage.setText(text);
    }
}
