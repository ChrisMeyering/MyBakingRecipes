package com.baking.chris.mybakingrecipes.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Recipe;
import com.baking.chris.mybakingrecipes.data.Step;
import com.baking.chris.mybakingrecipes.ui.IngredientListAdapter;
import com.baking.chris.mybakingrecipes.ui.StepListAdapter;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class RecipeActivity extends AppCompatActivity
        implements StepListAdapter.OnStepSelectedListener,
        ExoPlayer.EventListener
{
    public static final String TAG = RecipeActivity.class.getSimpleName();
    private Recipe recipe;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private int[] scrollPosition = null;
    private long playerPosition = 0;
    boolean playWhenReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent parentIntent = getIntent();
        if (parentIntent != null) {
            if (parentIntent.hasExtra(getString(R.string.RECIPE_KEY)))
                recipe = parentIntent.getParcelableExtra(getString(R.string.RECIPE_KEY));
        }

        ActionBar actionBar = getSupportActionBar();
        if (getResources().getBoolean(R.bool.is_landscape)) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            if (actionBar != null) {
                actionBar.hide();
            }
            setContentView(R.layout.activity_fullscreen_exo_player);
        } else {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(recipe.getName());
                actionBar.show();
            }
            setContentView(R.layout.activity_recipe);
            ((TextView)findViewById(R.id.test_recipe_name)).setText(recipe.getName());
        }
        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getIntArray(getString(R.string.SCROLL_VIEW_POSITION_KEY));
            final ScrollView parent = findViewById(R.id.sv_activity_recipe);
            if (scrollPosition != null && scrollPosition.length == 2 && parent != null) {
                parent.post(new Runnable() {
                    @Override
                    public void run() {parent.scrollTo(scrollPosition[0], scrollPosition[1]);
                    }
                });
            }
            playWhenReady = savedInstanceState.getBoolean(getString(R.string.VIDEO_PLAYER_PLAY_WHEN_READY_KEY));
            playerPosition = savedInstanceState.getLong(getString(R.string.VIDEO_PLAYER_POSITION_KEY));
        }

        initializeMediaSession();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ScrollView sv = findViewById(R.id.sv_activity_recipe);
        if (sv != null){
            outState.putIntArray(getString(R.string.SCROLL_VIEW_POSITION_KEY),
                new int[] {sv.getScrollX(), sv.getScrollY()});
        } else if (scrollPosition != null) {
            outState.putIntArray(getString(R.string.SCROLL_VIEW_POSITION_KEY), scrollPosition);
        }
        if (player != null) {
            outState.putLong(getString(R.string.VIDEO_PLAYER_POSITION_KEY), player.getCurrentPosition());
            outState.putBoolean(getString(R.string.VIDEO_PLAYER_PLAY_WHEN_READY_KEY), player.getPlayWhenReady());
        }
    }

    private void initializeMediaSession() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new MediaSessionCallbacks());
    }

    private class MediaSessionCallbacks extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            player.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            player.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            player.seekTo(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getBoolean(R.bool.is_landscape)) {
            initPlayer();
        } else
            initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void initView() {
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        RecyclerView rvIngredients = findViewById(R.id.rv_ingredients);
        rvIngredients.setNestedScrollingEnabled(false);
        rvIngredients.addItemDecoration(decoration);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(new IngredientListAdapter(this, recipe.getIngredients()));

        RecyclerView rvSteps = findViewById(R.id.rv_steps);
        rvSteps.setNestedScrollingEnabled(false);
        rvSteps.setBackground(getDrawable(R.drawable.round_background_light));
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(new StepListAdapter(this, recipe.getSteps().subList(1,recipe.getSteps().size())));

        initPlayer();
    }

    private void initPlayer() {
        if (player == null) {
            playerView = findViewById(R.id.exo_player_view);
            playerView.setVisibility(View.VISIBLE);
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            playerView.setPlayer(player);
            player.addListener(this);
            String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(recipe.getSteps().get(0).getVideoURL()),
                    new DefaultDataSourceFactory(this, userAgent), new DefaultExtractorsFactory(), null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(playWhenReady);
            playWhenReady = false;
            player.seekTo(playerPosition);
            playerPosition = 0;
            mediaSession.setActive(true);
        }
    }
    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (mediaSession != null)
            mediaSession.setActive(false);
    }

    @Override
    public void onStepSelected(int stepNumber) {
        Bundle extra = new Bundle();
        extra.putParcelableArrayList(getString(R.string.STEPS_KEY), (ArrayList<Step>) recipe.getSteps());
        extra.putInt(getString(R.string.STEP_NUMBER_KEY), stepNumber);
        extra.putString(getString(R.string.RECIPE_NAME_KEY), recipe.getName());
        Intent stepIntent = new Intent(this, StepActivity.class);
        stepIntent.putExtras(extra);
        startActivity(stepIntent);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    player.getCurrentPosition(), 1f);
        } else if((playbackState == ExoPlayer.STATE_READY)){
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    player.getCurrentPosition(), 1f);
        }
        Log.i(TAG, "player state changed");
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }
    }
}
