package com.oneviewhealthcare.iottest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        float freq = 0;
        if (keyCode == KeyEvent.KEYCODE_A) {
            lightLed(RainbowHat.LED_RED, true);
            freq = 80;
        }
        else if (keyCode == KeyEvent.KEYCODE_B) {
            lightLed(RainbowHat.LED_GREEN, true);
            freq = 800;
        }
        else if (keyCode == KeyEvent.KEYCODE_C) {
            lightLed(RainbowHat.LED_BLUE, true);
            freq = 8000;
        }
        try {
            Speaker speaker = RainbowHat.openPiezo();
            speaker.play(freq);
            speaker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_A) {
            lightLed(RainbowHat.LED_RED, false);
            if (mTimer == null) {
                startTimer();
            }
            else {
                stopTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_B) {
            lightLed(RainbowHat.LED_GREEN, false);
            if (mInterval > MIN_INTERVAL) {
                mInterval = mInterval / 2;
                restartTimer();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_C) {
            lightLed(RainbowHat.LED_BLUE, false);
            if (mInterval < MAX_INTERVAL) {
                mInterval = mInterval * 2;
                restartTimer();
            }
        }
        try {
            Speaker speaker = RainbowHat.openPiezo();
            speaker.stop();
            speaker.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void lightLed(String ledId, boolean state) {
        try {
            Gpio led = RainbowHat.openLed(ledId);
            led.setValue(state);
            led.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void turnOffLeds() {
        try {
            Apa102 ledstrip = RainbowHat.openLedStrip();
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
        lightLed(RainbowHat.LED_RED, false);
        lightLed(RainbowHat.LED_GREEN, false);
        lightLed(RainbowHat.LED_BLUE, false);
    }

    private void knightRider() {
        if (mGoingUp) {
            mCurrentPos++;
            if (mCurrentPos == MAX_LEDS) {
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
            Apa102 ledstrip = RainbowHat.openLedStrip();
            ledstrip.setBrightness(15);
            int[] rainbow = new int[MAX_LEDS+1];
            for (int i = 0; i < rainbow.length; i++) {
                if (i == mCurrentPos) {
                    rainbow[i] = Color.HSVToColor(100, new float[]{1.0f, 1.0f, 1.0f});
                }
                else {
                    rainbow[i] = Color.HSVToColor(100, new float[]{0f, 0f, 0f});
                }
            }
            ledstrip.write(rainbow);
            // Close the device when done.
            ledstrip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
