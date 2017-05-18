package com.oneviewhealthcare.iottest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rportales on 19/12/2016.
 */
public class HomeActivity extends Activity {
    private static final int MAX_LEDS = 6;

    private static final int MIN_INTERVAL = 25;
    private static final int MAX_INTERVAL = 400;

    private Timer mTimer;
    private boolean mGoingUp = true;
    private int mCurrentPos = 0;
    private int mPreviousPos = 0;
    private long mInterval = 200;

    private ButtonInputDriver inputDriverA;
    private ButtonInputDriver inputDriverB;
    private ButtonInputDriver inputDriverC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            inputDriverA = new ButtonInputDriver(RainbowHat.BUTTON_A, RainbowHat.BUTTON_LOGIC_STATE, KeyEvent.KEYCODE_A);
            inputDriverA.register();
            inputDriverB = new ButtonInputDriver(RainbowHat.BUTTON_B, RainbowHat.BUTTON_LOGIC_STATE, KeyEvent.KEYCODE_B);
            inputDriverB.register();
            inputDriverC = new ButtonInputDriver(RainbowHat.BUTTON_C, RainbowHat.BUTTON_LOGIC_STATE, KeyEvent.KEYCODE_C);
            inputDriverC.register();

            displayText("KITT");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        turnOffLeds();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_A) {
            if (mTimer == null) {
                startTimer();
            }
            else {
                stopTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_B) {
            if (mInterval > MIN_INTERVAL) {
                mInterval = mInterval / 2;
                restartTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_C) {
            if (mInterval < MAX_INTERVAL) {
                mInterval = mInterval * 2;
                restartTimer();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void displayText(String text) throws IOException {
        AlphanumericDisplay segment = RainbowHat.openDisplay();
        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
        segment.display(text);
        segment.setEnabled(true);
        // Close the device when done.
        segment.close();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
        inputDriverA.unregister();
        inputDriverB.unregister();
        inputDriverC.unregister();
    }

    private void restartTimer() {
        stopTimer();
        startTimer();
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                knightRider();
            }
        }, mInterval, mInterval);
    }

    private void turnOffLeds() {
        try {
            ExtendedApa102 ledstrip = new ExtendedApa102(RainbowHat.BUS_LEDSTRIP, ExtendedApa102.Mode.BGR);
            ledstrip.setBrightness(16);
            int[] rainbow = new int[MAX_LEDS+1];
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(100, new float[]{0f, 0f, 0f});
            }
            ledstrip.write(rainbow);
            ledstrip.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void knightRider() {
        int previousPreviousPos = mPreviousPos;
        mPreviousPos = mCurrentPos;
        if (mGoingUp) {
            if (mCurrentPos == MAX_LEDS) {
                mGoingUp = false;
            }
            else {
                mCurrentPos++;
            }
        }
        else {
            if (mCurrentPos == 0) {
                mGoingUp = true;
            }
            else {
                mCurrentPos--;
            }
        }
        try {
            ExtendedApa102 ledstrip = new ExtendedApa102(RainbowHat.BUS_LEDSTRIP, ExtendedApa102.Mode.BGR);
            int[] brightness = new int[7];
            ledstrip.setBrightness(15);
            int[] rainbow = new int[MAX_LEDS+1];
            for (int i = 0; i < rainbow.length; i++) {
                if (i == mCurrentPos) {
                    brightness[i] = 15;
                    rainbow[i] = Color.HSVToColor(100, new float[]{1.0f, 1.0f, 1.0f});
                }
                else if (i == mPreviousPos){
                    brightness[i] = 7;
                    rainbow[i] = Color.HSVToColor(100, new float[]{1.0f, 1.0f, 1.0f});
                }
                else if (i == previousPreviousPos){
                    brightness[i] = 1;
                    rainbow[i] = Color.HSVToColor(100, new float[]{1.0f, 1.0f, 1.0f});
                }
                else {
                    rainbow[i] = Color.HSVToColor(100, new float[]{0f, 0f, 0f});
                }
            }
            ledstrip.setBrightness(brightness);
            ledstrip.write(rainbow);
            // Close the device when done.
            ledstrip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
