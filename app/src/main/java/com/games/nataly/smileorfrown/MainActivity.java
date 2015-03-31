package com.games.nataly.smileorfrown;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.Random;

public class MainActivity
        extends ActionBarActivity
        implements OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public final static String SAVED_PREFS = "com.games.nataly.smileorfrown.SAVED_PREFS";
    String TAG = "SmileOrFrown";

    TextView lastScoreText;
    TextView highScoreText;
    Button button_start;
    Button button_settings;
    Button button_quit;
    Button button_google_play;

    Integer highScore = 0;
    Integer receivedLastScore = 0;

    ///////////////////////////////////////
    // Start - Code copied from example //
    /////////////////////////////////////

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // request codes we use when invoking an external activity
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    /////////////////////////////////////
    // End - Code copied from example //
    ///////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "App created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "App started");
        super.onStart();
        setContentView(R.layout.activity_main);

        highScoreText = (TextView) findViewById(R.id.highScore);
        lastScoreText = (TextView) findViewById(R.id.lastScore);

        button_start = (Button) findViewById(R.id.button_start);
        button_settings = (Button) findViewById(R.id.button_settings);
        button_quit = (Button) findViewById(R.id.button_quit);
        button_google_play = (Button) findViewById(R.id.button_google_play);

        // load prefs
        SharedPreferences prefs = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);
        receivedLastScore = prefs.getInt("last_score", 0);

        if (isSignedIn()) {
            button_google_play.setText(getString(R.string.google_play_button_logout));
        }
        else {
            button_google_play.setText(getString(R.string.google_play_button_login));
        }

        lastScoreText.setText(getString(R.string.last_score) + receivedLastScore);
        highScoreText.setText(getString(R.string.high_score) + highScore);

        if (prefs.getBoolean("userWantsSignIn", true)) {
            Log.d(TAG, "onStart(): connecting");
            mGoogleApiClient.connect();
        }

        button_start.setOnClickListener(this);
        button_settings.setOnClickListener(this);
        button_quit.setOnClickListener(this);
        button_google_play.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "App stopped");
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    ///////////////////////////////////////
    // Start - Code copied from example //
    /////////////////////////////////////

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    void unlockAchievement(int achievementId, String fallbackString) {
        if (isSignedIn()) {
            Games.Achievements.unlock(mGoogleApiClient, getString(achievementId));
        } else {
            Toast.makeText(this, getString(R.string.achievement) + ": " + fallbackString,
                    Toast.LENGTH_LONG).show();
        }
    }

    void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(this);
            return;
        }
        if (mOutbox.mTryAchievement) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_try));
            mOutbox.mTryAchievement = false;
        }
        if (mOutbox.mScore100) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score100));
            mOutbox.mScore100 = false;
        }
        if (mOutbox.mScore200) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score200));
            mOutbox.mScore200 = false;
        }
        if (mOutbox.mScore500) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score500));
            mOutbox.mScore500 = false;
        }
        if (mOutbox.mScore800) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score800));
            mOutbox.mScore800 = false;
        }
        if (mOutbox.mScore1000) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score1000));
            mOutbox.mScore1000 = false;
        }
        if (mOutbox.mScore1500) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score1500));
            mOutbox.mScore1500 = false;
        }
        if (mOutbox.mScore2000) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score2000));
            mOutbox.mScore2000 = false;
        }
        if (mOutbox.mScore2500) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score2500));
            mOutbox.mScore2500 = false;
        }
        if (mOutbox.mScore3000) {
            Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_score3000));
            mOutbox.mScore3000 = false;
        }

        if (mOutbox.mHighScore >= 0) {
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard),
                    mOutbox.mHighScore);
            mOutbox.mHighScore = -1;
        }
        mOutbox.saveLocal(this);
    }

    void updateLeaderboards(int finalScore) {
        if (mOutbox.mHighScore < finalScore) {
            mOutbox.mHighScore = finalScore;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // Show sign-out button on main menu
        button_google_play.setText(R.string.google_play_button_logout);

        // show a welcome greeting (google style)

        /*
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        // display greeting with name (google style)
        Log.d(TAG, "Hello, " + displayName);
        */

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Sign-in failed, so show sign-in button on main menu
        button_google_play.setText(R.string.google_play_button_login);
        // display a sign-in failed message (google style)
    }

    public void onSignInButtonClicked() {
        // start the sign-in flow
        mSignInClicked = true;
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("userWantsSignIn", true);
        editor.apply();
        mGoogleApiClient.connect();
    }

    public void onSignOutButtonClicked() {
        mSignInClicked = false;
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("userWantsSignIn", false);
        editor.apply();

        Games.signOut(mGoogleApiClient);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        // show sign out button
        button_google_play.setText(R.string.google_play_button_login);
        // display a goodbye message (google style)
    }

    /////////////////////////////////////
    // End - Code copied from example //
    ///////////////////////////////////

    class AccomplishmentsOutbox {
        boolean mTryAchievement = false;
        boolean mScore100 = false;
        boolean mScore200 = false;
        boolean mScore500 = false;
        boolean mScore800 = false;
        boolean mScore1000 = false;
        boolean mScore1500 = false;
        boolean mScore2000 = false;
        boolean mScore2500 = false;
        boolean mScore3000 = false;
        int mHighScore = -1;

        boolean isEmpty() {
            return !mTryAchievement && !mScore100 && !mScore200 &&
                    !mScore500 && !mScore800 && !mScore1000 &&
                    !mScore1500 && !mScore2000 && !mScore2500 &&
                    !mScore3000 && mHighScore < 0;
        }

        public void saveLocal(Context ctx) {
            SharedPreferences.Editor editor = ctx.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
            editor.putBoolean("mTryAchievement", mTryAchievement);
            editor.putBoolean("mScore100", mScore100);
            editor.putBoolean("mScore200", mScore200);
            editor.putBoolean("mScore500", mScore500);
            editor.putBoolean("mScore800", mScore800);
            editor.putBoolean("mScore1000", mScore1000);
            editor.putBoolean("mScore1500", mScore1500);
            editor.putBoolean("mScore2000", mScore2000);
            editor.putBoolean("mScore2500", mScore2500);
            editor.putBoolean("mScore3000", mScore3000);
            editor.putInt("mHighScore", mHighScore);
            editor.apply();
        }

        public void loadLocal(Context ctx) {
            SharedPreferences prefs = ctx.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
            mTryAchievement = prefs.getBoolean("mTryAchievement", false);
            mScore100 = prefs.getBoolean("mScore100", false);
            mScore200 = prefs.getBoolean("mScore200", false);
            mScore500 = prefs.getBoolean("mScore500", false);
            mScore800 = prefs.getBoolean("mScore800", false);
            mScore1000 = prefs.getBoolean("mScore1000", false);
            mScore1500 = prefs.getBoolean("mScore1500", false);
            mScore2000 = prefs.getBoolean("mScore2000", false);
            mScore2500 = prefs.getBoolean("mScore2500", false);
            mScore3000 = prefs.getBoolean("mScore3000", false);
            mHighScore = prefs.getInt("mHighScore", -1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ///////////////////////////////////////
        // Start - Code copied from example //
        /////////////////////////////////////

        // back from login resolving
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }

        /////////////////////////////////////
        // End - Code copied from example //
        ///////////////////////////////////

        // back from game
        else if (requestCode == 1) {
            SharedPreferences prefs = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
            highScore = prefs.getInt("high_score", 0);
            receivedLastScore = prefs.getInt("last_score", 0);
            lastScoreText.setText(getString(R.string.last_score) + receivedLastScore);
            highScoreText.setText(getString(R.string.high_score) + highScore);

            // SYNC SCORE TO GOOGLE
            /*
            if (isSignedIn()) {

            }
            */
        }

        // back from settings
        else if (requestCode == 2) {
            // check high score in case of reset
            SharedPreferences prefs = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
            highScore = prefs.getInt("high_score", 0);
            receivedLastScore = prefs.getInt("last_score", 0);
            lastScoreText.setText(getString(R.string.last_score) + receivedLastScore);
            highScoreText.setText(getString(R.string.high_score) + highScore);
            // do something with music playing? (if user changed the setting)
        }
    }

    public void startNewGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivityForResult(intent, 1);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                startNewGame();
                break;
            case R.id.button_settings:
                openSettings();
                break;
            case R.id.button_quit:
                Log.d(TAG, "Running finish() on MainActivity");
                finish();
                break;
            case R.id.button_google_play:
                if (isSignedIn()) {
                    onSignOutButtonClicked();
                }
                else {
                    onSignInButtonClicked();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}