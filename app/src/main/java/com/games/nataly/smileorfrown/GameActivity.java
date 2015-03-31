package com.games.nataly.smileorfrown;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.SharedPreferences;

import java.util.Random;

public class GameActivity
        extends ActionBarActivity
        implements AnimationListener, OnClickListener {

    public final static String SAVED_PREFS = "com.games.nataly.smileorfrown.SAVED_PREFS";
    String TAG = "SmileOrFrown";
    private Handler handler;

    private Runnable runPlayerMissedClick;
    private Runnable runStartNextTurn;
    private Runnable runStartFirstTurn;
    private Runnable runGetReady;

    Integer score;
    Integer strikes;
    Integer highScore;
    Integer timesPlayed;

    Integer TIME_TO_PAUSE; // 0.8 second
    Integer TIME_TO_SHOW; // 3 seconds
    Integer GRACE_TIME; // 1 second
    Integer TIME_TO_VIBRATE_ON_STRIKE; // 0.4 seconds
    Integer difficulty; // difficulty level 1 out of 5

    Integer MAX_STRIKES;

    // face image
    ImageView faceImage;
    // text displays for score, difficulty/level, strikes
    TextView scoreView;
    TextView difView;
    TextView strikeView;
    // special text display located in the screen center
    TextView gameNoteText;
    // buttons
    Button button_good;
    Button button_bad;

    FaceType displayedFace;
    Boolean buttonsActive;

    Vibrator vib;
    Animation zoomIn, zoomOut;
    Animation zoomInReady, zoomOutReady;
    Animation zoomInStrike, zoomOutStrike;
    Animation zoomInOver, zoomOutOver;

    public enum FaceType { GOOD, BAD, NONE }
    public enum TextColor { STRIKE_COLOR, REGULAR_COLOR }
    public enum StrikeType { STRIKE, MISS }
    public enum DisplayTextType { GAME_OVER, GET_READY, STRIKE }

    public enum FacePic {
        SMILE_RED,
        SMILE_GREEN,
        FROWN_RED,
        FROWN_GREEN;

        public static FacePic getRandomFacePic() {
            FacePic[] facePics = FacePic.values();
            Random random = new Random();
            return facePics[random.nextInt(facePics.length)];
        }
    }

    Boolean vibrateEnabled = true;
    Boolean soundEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Game created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onStart() {

        Log.d(TAG, "Game started");
        super.onStart();
        setContentView(R.layout.activity_game);

        // open prefs
        SharedPreferences prefs = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE);
        vibrateEnabled = prefs.getBoolean("vibrate_enabled", true);
        soundEnabled = prefs.getBoolean("sound_enabled", true);
        highScore = prefs.getInt("high_score", 0);
        timesPlayed = prefs.getInt("times_played", 0);
        Log.d(TAG, "TIMES PLAYED SO FAR: " + timesPlayed.toString());

        score = 0;
        strikes = 0;
        displayedFace = FaceType.NONE;

        difficulty = 1;
        TIME_TO_PAUSE = 400;
        TIME_TO_SHOW = 2300;
        GRACE_TIME = 1000;
        TIME_TO_VIBRATE_ON_STRIKE = 400;

        if (BuildConfig.DEBUG) {
            MAX_STRIKES = 10;
        }
        else {
            MAX_STRIKES = 3;
        }

        scoreView = (TextView) findViewById(R.id.scoreView);
        difView = (TextView) findViewById(R.id.difView);
        strikeView = (TextView) findViewById(R.id.strikeView);
        faceImage = (ImageView) findViewById(R.id.imageView);
        gameNoteText = (TextView) findViewById(R.id.gameNoteText);

        button_good = (Button) findViewById(R.id.button_good);
        button_bad = (Button) findViewById(R.id.button_bad);

        // setup vibrator
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // setup animations
        // zoom in
        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        // specific
        zoomInStrike = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomInReady = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomInOver = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        // zoom out
        zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        // specific
        zoomOutStrike = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        zoomOutReady = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        zoomOutOver = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        // set listeners for animations
        zoomInStrike.setAnimationListener(this);
        zoomInReady.setAnimationListener(this);
        zoomInOver.setAnimationListener(this);
        zoomOutStrike.setAnimationListener(this);
        zoomOutReady.setAnimationListener(this);
        zoomOutOver.setAnimationListener(this);

        String s = String.format(getString(R.string.game_strikes_format), strikes, MAX_STRIKES);
        strikeView.setText(s);
        //strikeView.setText("Strikes: " + strikes + "/" + MAX_STRIKES);
        difView.setText(getString(R.string.game_level) + difficulty);
        scoreView.setText(getString(R.string.game_score) + score);

        buttonsActive = false;
        button_good.setOnClickListener(this);
        button_bad.setOnClickListener(this);

        handler = new Handler();

        runPlayerMissedClick = new Runnable() {
            @Override
            public void run() {
                // played missed
                endOfTurn(FaceType.NONE);
            }
        };

        runStartNextTurn = new Runnable() {
            @Override
            public void run() {
                startNextTurn(false);
            }
        };

        runStartFirstTurn = new Runnable() {
            @Override
            public void run() {
                startNextTurn(true);
            }
        };

        runGetReady = new Runnable() {
            @Override
            public void run() {
                // display "Get Ready!"
                String s = getString(R.string.get_ready);
                displayGameText(DisplayTextType.GET_READY, s, TextColor.REGULAR_COLOR);
                // after the animation the next turn will start
            }
        };

        // Start the game loop
        handler.postDelayed(runGetReady, GRACE_TIME);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Game stopped");
        super.onStop();
        handler.removeCallbacks(runPlayerMissedClick);
        handler.removeCallbacks(runStartNextTurn);
        handler.removeCallbacks(runStartFirstTurn);
        handler.removeCallbacks(runGetReady);
        saveHighScore();
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Game destroyed");
        handler.removeCallbacks(runPlayerMissedClick);
        handler.removeCallbacks(runStartNextTurn);
        handler.removeCallbacks(runStartFirstTurn);
        handler.removeCallbacks(runGetReady);
        saveHighScore();
        super.onDestroy();
    }

    public void displayGameText(DisplayTextType dispType, String newText, TextColor newColor) {
        // find the color
        int newColorNum = 0xf4000238; // default
        if (newColor == TextColor.REGULAR_COLOR) {
            newColorNum = 0xf4000238; // dark blue
        }
        else if (newColor == TextColor.STRIKE_COLOR) {
            newColorNum = 0xffe60000; // red
        }
        // setup the text
        gameNoteText.setText(newText);
        gameNoteText.setTextColor(newColorNum);
        // hide the face
        faceImage.setVisibility(View.INVISIBLE);
        // show the text
        gameNoteText.setVisibility(View.VISIBLE);

        if (dispType == DisplayTextType.GAME_OVER) {
            gameNoteText.startAnimation(zoomInOver);
        }
        else if (dispType == DisplayTextType.GET_READY) {
            gameNoteText.startAnimation(zoomInReady);
        }
        else if (dispType == DisplayTextType.STRIKE) {
            gameNoteText.startAnimation(zoomInStrike);
        }
        // after the animation finishes a callback is called (onAnimationEnd)
    }

    public void addStrike() {
        strikes = strikes + 1;
        String s = String.format(getString(R.string.game_strikes_format), strikes, MAX_STRIKES);
        strikeView.setText(s);
        //strikeView.setText("Strikes: " + strikes + "/" + MAX_STRIKES);
    }

    public void createStrikeEffect(StrikeType type) {
        // vibrate
        if (vib.hasVibrator() && vibrateEnabled) {
            // time should be same as TIME_TO_PAUSE?
            vib.vibrate(TIME_TO_VIBRATE_ON_STRIKE);
        }

        // play a sound?
        if (soundEnabled) {
            // do stuff
        }

        // prepare text
        String dispText = getString(R.string.strike); // default, shouldn't happen
        if (type == StrikeType.STRIKE) {
            dispText = getString(R.string.strike);
        }
        else if (type == StrikeType.MISS) {
            dispText = getString(R.string.miss);
        }

        // display and animate
        displayGameText(DisplayTextType.STRIKE, dispText, TextColor.STRIKE_COLOR);
    }

    public void createScoreEffect() {
        // play a sound?

        // check if reached higher level and indicate that?

        // ***MAYBE*** display "+10"

        // reset timer
        checkToResetTurn();
    }

    public void checkDif() {
        // initial times are PAUSE=400, SHOW=2300
        if (difficulty == 1) {
            if (score >= 50) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 300;
                TIME_TO_SHOW = 2000;
            }
        }
        else if (difficulty == 2) {
            if (score >= 110) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 300;
                TIME_TO_SHOW = 1500;
            }
        }
        else if (difficulty == 3) {
            if (score >= 230) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 300;
                TIME_TO_SHOW = 1200;
            }
        }
        else if (difficulty == 4) {
            if (score >= 360) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 200;
                TIME_TO_SHOW = 1000;
            }
        }
        else if (difficulty == 5) {
            if (score >= 500) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 200;
                TIME_TO_SHOW = 800;
            }
        }
        else if (difficulty == 6) {
            if (score >= 800) {
                difficulty = difficulty + 1;
                TIME_TO_PAUSE = 100;
                TIME_TO_SHOW = 700;
            }
        }
        else if (difficulty == 7) {
            if (score >= 1100) {
                // max difficulty
                // enter a special mode?
            }
        }
        //difficulty = difficulty + 1;
        //TIME_TO_PAUSE = TIME_TO_PAUSE - 100;
        //TIME_TO_SHOW = TIME_TO_SHOW - 500;
        difView.setText(getString(R.string.game_level) + difficulty);
    }

    public void incScore() {
        score = score + 10;
        scoreView.setText(getString(R.string.game_score) + score);
        saveHighScore();
    }

    public void selectNextFaceAndDisplay() {
        FacePic fp = null;

        // game levels
        if ((difficulty == 1) || (difficulty == 2)) {
            fp = setFaceMostBiasedGameTwoColor();
        }
        else if ((difficulty == 3) || (difficulty == 4)) {
            fp = setFaceMoreBiasedGameTwoColor();
        }
        else if (difficulty == 5) {
            fp = setFaceReverseBiasedGameTwoColor();
        }
        else if (difficulty == 6) {
            fp = setFaceNormalGameTwoColor();
        }
        else if (difficulty == 7) {
            fp = setFaceReverseBiasedGameTwoColor();
        }

        // default safety
        if (fp == null) {
            fp = FacePic.SMILE_GREEN;
        }

        switch (fp) {
            case SMILE_RED:
                faceImage.setImageResource(R.drawable.smile_red);
                displayedFace = FaceType.GOOD;
                break;
            case SMILE_GREEN:
                faceImage.setImageResource(R.drawable.smile_green);
                displayedFace = FaceType.GOOD;
                break;
            case FROWN_RED:
                faceImage.setImageResource(R.drawable.frown_red);
                displayedFace = FaceType.BAD;
                break;
            case FROWN_GREEN:
                faceImage.setImageResource(R.drawable.frown_green);
                displayedFace = FaceType.BAD;
                break;
            default:
                // should not happen
                faceImage.setImageResource(R.drawable.blank);
                displayedFace = FaceType.NONE;
                break;
        }
        // enable buttons
        buttonsActive = true;
        // show the face
        faceImage.setVisibility(View.VISIBLE);
    }

    public FacePic setFaceNormalGame() {
        Random random = new Random();
        return FacePic.getRandomFacePic();
    }

    public FacePic setFaceMostBiasedGameTwoColor() {
        Random random = new Random();
        Integer num = random.nextInt(1000) + 1;
        FacePic f = null;
        if ((num >= 1) && (num <= 500)) {
            f = FacePic.SMILE_GREEN;
        }
        else if ((num >= 501) && (num <= 1000)) {
            f = FacePic.FROWN_RED;
        }
        return f;
    }

    public FacePic setFaceMoreBiasedGameTwoColor() {
        Random random = new Random();
        Integer num = random.nextInt(1000) + 1;
        FacePic f = null;
        if ((num >= 1) && (num <= 480)) {
            f = FacePic.SMILE_GREEN;
        }
        else if ((num >= 481) && (num <= 500)) {
            f = FacePic.SMILE_RED;
        }
        else if ((num >= 501) && (num <= 980)) {
            f = FacePic.FROWN_RED;
        }
        else if ((num >= 981) && (num <= 1000)) {
            f = FacePic.FROWN_GREEN;
        }
        return f;
    }

    public FacePic setFaceNormalGameTwoColor() {
        Random random = new Random();
        Integer num = random.nextInt(1000) + 1;
        FacePic f = null;
        if ((num >= 1) && (num <= 250)) {
            f = FacePic.SMILE_GREEN;
        }
        else if ((num >= 251) && (num <= 500)) {
            f = FacePic.SMILE_RED;
        }
        else if ((num >= 501) && (num <= 750)) {
            f = FacePic.FROWN_RED;
        }
        else if ((num >= 751) && (num <= 1000)) {
            f = FacePic.FROWN_GREEN;
        }
        return f;
    }

    public FacePic setFaceReverseBiasedGameTwoColor() {
        Random random = new Random();
        Integer num = random.nextInt(1000) + 1;
        FacePic f = null;
        if ((num >= 1) && (num <= 480)) {
            f = FacePic.SMILE_RED;
        }
        else if ((num >= 481) && (num <= 500)) {
            f = FacePic.SMILE_GREEN;
        }
        else if ((num >= 501) && (num <= 980)) {
            f = FacePic.FROWN_GREEN;
        }
        else if ((num >= 981) && (num <= 1000)) {
            f = FacePic.FROWN_RED;
        }
        return f;
    }

    public void startNextTurn(Boolean isFirstTurn) {
        // hide text in case it's visible ("Get Ready!" or "Strike!" or "Miss!")
        gameNoteText.setVisibility(View.INVISIBLE);
        // set the current game level
        checkDif();
        // choose the next face and show it
        selectNextFaceAndDisplay();
        // set timer for player to click
        handler.postDelayed(runPlayerMissedClick, TIME_TO_SHOW);
    }

    public void gameOver() {
        // safety - stop the game loop
        handler.removeCallbacks(runPlayerMissedClick);
        handler.removeCallbacks(runStartNextTurn);
        handler.removeCallbacks(runStartFirstTurn);
        handler.removeCallbacks(runGetReady);
        // save the high score and last score
        saveHighScore();
        // increase number of times played
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt("times_played", timesPlayed + 1);
        editor.apply();
        // return to menu
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void saveHighScore() {
        SharedPreferences.Editor editor = this.getSharedPreferences(SAVED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt("last_score", score);
        if (score > highScore) {
            highScore = score;
            editor.putInt("high_score", highScore);
        }
        editor.apply();
    }

    public void handleUserClick (FaceType choice) {
        endOfTurn(choice);
    }

    public void endOfTurn (FaceType clickedFace) {
        // remove if the player clicked, and just in case remove anyway
        handler.removeCallbacks(runPlayerMissedClick);
        buttonsActive = false;
        faceImage.setImageResource(R.drawable.blank);

        // user missed the click, time ran out
        if (clickedFace == FaceType.NONE) {
            addStrike();
            createStrikeEffect(StrikeType.MISS);
        }
        else {
            // if user chose the correct button
            if (clickedFace == displayedFace) {
                displayedFace = FaceType.NONE;
                incScore();
                createScoreEffect();
            }
            else {
                displayedFace = FaceType.NONE;
                addStrike();
                createStrikeEffect(StrikeType.STRIKE);
            }
        }
    }

    public void checkToResetTurn() {
        if (strikes >= MAX_STRIKES) {
            // display "Game Over!"
            String s = getString(R.string.game_over);
            displayGameText(DisplayTextType.GAME_OVER, s, TextColor.REGULAR_COLOR);
            // after the text animates gameOver() will be called by onAnimationEnd
        }
        else {
            // start the next turn after the interval
            handler.postDelayed(runStartNextTurn, TIME_TO_PAUSE);
        }
    }

    // on click listener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_good:
                if (buttonsActive) {
                    handleUserClick(FaceType.GOOD);
                }
                break;
            case R.id.button_bad:
                if (buttonsActive) {
                    handleUserClick(FaceType.BAD);
                }
                break;
        }
    }

    // animation listeners
    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation
        // check for fade in animation
        if (animation == zoomInStrike) {
            gameNoteText.startAnimation(zoomOutStrike);
        }
        else if (animation == zoomOutStrike) {
            gameNoteText.setVisibility(View.INVISIBLE);
            // reset timer
            checkToResetTurn();
        }
        else if (animation == zoomInReady) {
            gameNoteText.startAnimation(zoomOutReady);
        }
        else if (animation == zoomOutReady) {
            gameNoteText.setVisibility(View.INVISIBLE);
            // start first turn
            handler.postDelayed(runStartFirstTurn, TIME_TO_PAUSE);
        }
        else if (animation == zoomInOver) {
            gameNoteText.startAnimation(zoomOutOver);
        }
        else if (animation == zoomOutOver) {
            gameNoteText.setVisibility(View.INVISIBLE);
            if (score > highScore) {
                // congratulate the player for their new high score
                //animateNewHighScore();
            }
            // game over
            gameOver();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Animation is repeating
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Animation started
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
