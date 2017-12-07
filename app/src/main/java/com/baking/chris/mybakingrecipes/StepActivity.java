package com.baking.chris.mybakingrecipes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baking.chris.mybakingrecipes.data.Step;
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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StepActivity extends AppCompatActivity implements ExoPlayer.EventListener {
    public static final String TAG = StepActivity.class.getSimpleName();
    private String recipeName;
    private int currentStepId;
    private List<Step> steps;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);
        if (savedInstanceState != null) {

        }
        Intent parentIntent = getIntent();
        if (parentIntent != null) {
            currentStepId = parentIntent.getIntExtra(getString(R.string.STEP_NUMBER_KEY), 1);
            steps = parentIntent.getParcelableArrayListExtra(getString(R.string.STEPS_KEY));
            recipeName = parentIntent.getStringExtra(getString(R.string.RECIPE_NAME_KEY));
        }
        initializeMediaSession();
        updateView();
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

    private void updateView(){
        Step step = steps.get(currentStepId);
        if (currentStepId == 0){
            findViewById(R.id.btn_prev_step).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_prev_step).setVisibility(View.VISIBLE);
        }
        if (currentStepId == steps.size()-1) {
            findViewById(R.id.btn_next_step).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_next_step).setVisibility(View.VISIBLE);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(recipeName + " - Step " + String.valueOf(step.getId()));
        }

        String stepThumb = step.getThumbnailURL();
        if (stepThumb != null && stepThumb.trim().length() > 0) {
            String thumbType = stepThumb.substring(stepThumb.lastIndexOf('.')+1);
            if (thumbType.equals("mp4")) {
                step.setVideoURL(stepThumb);
                step.setThumbnailURL(null);
            } else {
                final ImageView ivStepThumb = findViewById(R.id.iv_step_thumbnail);
                ivStepThumb.setVisibility(View.VISIBLE);
                Picasso.with(this).load(Uri.parse(stepThumb))
                        .placeholder(R.drawable.recipe_placeholder)
                        .error(R.drawable.error)
                        .into(ivStepThumb, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(StepActivity.this, "Error: Unable to load thumbnail.", Toast.LENGTH_LONG).show();
                                ivStepThumb.setVisibility(View.GONE);
                            }
                        });
            }
        }
        String stepVideoURL = step.getVideoURL();
        if (stepVideoURL != null && stepVideoURL.trim().length() > 0) {
            initPlayer();
        } else {
            findViewById(R.id.step_exo_player_view).setVisibility(View.GONE);
        }
        TextView tvStepDescription = findViewById(R.id.tv_step_description);
        tvStepDescription.setText(step.getDescription());


    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }


    private void initPlayer() {
        if (player == null) {
            playerView = findViewById(R.id.step_exo_player_view);
            playerView.setVisibility(View.VISIBLE);
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            playerView.setPlayer(player);
            player.addListener(this);
            String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(steps.get(currentStepId).getVideoURL()),
                    new DefaultDataSourceFactory(this, userAgent), new DefaultExtractorsFactory(), null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(false);
            mediaSession.setActive(true);
        }
    }

    public void onStepChanged(View view) {
        switch (view.getId()) {
            case (R.id.btn_next_step):
                currentStepId++;
                break;
            case (R.id.btn_prev_step):
                currentStepId--;
                break;
        }
        releasePlayer();
        updateView();
    }

    private void releasePlayer() {
        if (player != null) {
            findViewById(R.id.step_exo_player_view).setVisibility(View.GONE);
            player.release();
            player = null;
        }
        if (mediaSession != null)
            mediaSession.setActive(false);
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
}
