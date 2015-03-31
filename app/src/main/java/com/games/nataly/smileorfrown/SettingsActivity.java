package com.games.nataly.smileorfrown;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class SettingsActivity
        extends ActionBarActivity
        implements OnClickListener {

    public final static String SAVED_PREFS = "com.games.nataly.smileorfrown.SAVED_PREFS";
    String TAG = "SmileOrFrown";

    Button button_vibrate;
    Button button_sound;
    Button button_reset;
    Button button_back;

    Boolean vibrateEnabled = true;
    Boolean soundEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Settings started");
        super.onStart();
        setContentView(R.layout.activity_settings);

        button_vibrate = (Button) findViewById(R.id.button_vibrate);
        button_sound = (Button) findViewById(R.id.button_sound);
        button_reset = (Button) findViewById(R.id.button_reset);
        button_back = (Button) findViewById(R.id.button_back);

        // load prefs
        SharedPreferences prefs = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
        vibrateEnabled = prefs.getBoolean("vibrate_enabled", true);
        soundEnabled = prefs.getBoolean("sound_enabled", true);
        if (vibrateEnabled) {
            button_vibrate.setText(getString(R.string.vibrate_button_on));
        }
        else {
            button_vibrate.setText(getString(R.string.vibrate_button_off));
        }

        if (soundEnabled) {
            button_sound.setText(getString(R.string.sound_button_on));
        }
        else {
            button_sound.setText(getString(R.string.sound_button_off));
        }

        button_vibrate.setOnClickListener(this);
        button_sound.setOnClickListener(this);
        button_reset.setOnClickListener(this);
        button_back.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Settings stopped");
        super.onStop();
    }

    public void toggleVibrate() {
        if (vibrateEnabled) {
            vibrateEnabled = false;
            button_vibrate.setText(getString(R.string.vibrate_button_off));
        }
        else {
            vibrateEnabled = true;
            button_vibrate.setText(getString(R.string.vibrate_button_on));
        }
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("vibrate_enabled", vibrateEnabled);
        editor.apply();
    }

    public void toggleSound() {
        if (soundEnabled) {
            soundEnabled = false;
            button_sound.setText(getString(R.string.sound_button_off));
        }
        else {
            soundEnabled = true;
            button_sound.setText(getString(R.string.sound_button_on));
        }
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("sound_enabled", soundEnabled);
        editor.apply();
    }

    public void resetScore() {
        // TODO - add "Are you sure?" dialog
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt("high_score", 0);
        editor.putInt("last_score", 0);
        // should we reset the times played as well?
        editor.putInt("times_played", 0);
        editor.apply();
    }

    public void returnToMain() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_vibrate:
                toggleVibrate();
                break;
            case R.id.button_sound:
                toggleSound();
                break;
            case R.id.button_reset:
                resetScore();
                break;
            case R.id.button_back:
                returnToMain();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
