package com.oneviewhealthcare.iottest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rportales on 19/12/2016.
 */
public class HomeActivity extends Activity {
    private static final int MIN_INTERVAL = 25;
    private static final int MAX_INTERVAL = 400;

    private Timer mTimer;
    private boolean mGoingUp = true;
    private int mCurrentPos = 0;
    private long mInterval = 200;

    private ButtonInputDriver mInputDriverA;
    private ButtonInputDriver mInputDriverB;
    private ButtonInputDriver mInputDriverC;

    private Gpio mRedLed;
    private Gpio mGreenLed;
    private Gpio mBlueLed;
    private AlphanumericDisplay mAlphanumericDisplay;
    private Apa102 ledstrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mRedLed = RainbowHat.openLedRed();
            mGreenLed = RainbowHat.openLedGreen();
            mBlueLed = RainbowHat.openLedBlue();

            ledstrip = RainbowHat.openLedStrip();

            mAlphanumericDisplay = RainbowHat.openDisplay();

            displayText("KITT");

            mInputDriverA = RainbowHat.createButtonAInputDriver(KeyEvent.KEYCODE_A);
            mInputDriverA.register();
            mInputDriverB = RainbowHat.createButtonBInputDriver(KeyEvent.KEYCODE_B);
            mInputDriverB.register();
            mInputDriverC = RainbowHat.createButtonCInputDriver(KeyEvent.KEYCODE_C);
            mInputDriverC.register();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        turnOffLeds();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        float freq = 0;
        if (keyCode == KeyEvent.KEYCODE_A) {
            lightLed(mRedLed, true);
        }
        else if (keyCode == KeyEvent.KEYCODE_B) {
            lightLed(mGreenLed, true);
        }
        else if (keyCode == KeyEvent.KEYCODE_C) {
            lightLed(mBlueLed, true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_A) {
            lightLed(mRedLed, false);
            if (mTimer == null) {
                startTimer();
            }
            else {
                stopTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_B) {
            lightLed(mGreenLed, false);
            if (mInterval > MIN_INTERVAL) {
                mInterval = mInterval / 2;
                restartTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_C) {
            lightLed(mBlueLed, false);
            if (mInterval < MAX_INTERVAL) {
                mInterval = mInterval * 2;
                restartTimer();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void displayText(String text) throws IOException {
        mAlphanumericDisplay.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
        mAlphanumericDisplay.display(text);
        mAlphanumericDisplay.setEnabled(true);
        // Close the device when done.
        mAlphanumericDisplay.close();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
        turnOffLeds();

        mInputDriverA.unregister();
        mInputDriverB.unregister();
        mInputDriverC.unregister();

        try {
            ledstrip.close();
            mAlphanumericDisplay.close();
            mRedLed.close();
            mGreenLed.close();
            mBlueLed.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void lightLed(Gpio led, boolean state) {
        try {
            led.setValue(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void turnOffLeds() {
        try {
            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(100, new float[]{0f, 0f, 0f});
            }
            ledstrip.write(rainbow);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        lightLed(mRedLed, false);
        lightLed(mGreenLed, false);
        lightLed(mBlueLed, false);
    }

    private void knightRider() {
        if (mGoingUp) {
            mCurrentPos++;
            if (mCurrentPos == RainbowHat.LEDSTRIP_LENGTH - 1) {
                mGoingUp = false;
            }
        }
        else {
            mCurrentPos--;
            if (mCurrentPos == 0) {
                mGoingUp = true;
            }
        }
        try {
            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            for (int i = 0; i < rainbow.length; i++) {
                if (i == mCurrentPos) {
                    rainbow[i] = Color.HSVToColor(100, new float[]{1.0f, 1.0f, 1.0f});
                }
                else {
                    rainbow[i] = Color.HSVToColor(100, new float[]{0f, 0f, 0f});
                }
            }
            ledstrip.setBrightness(31);
            ledstrip.setDirection(Apa102.Direction.NORMAL);
            ledstrip.write(rainbow);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
