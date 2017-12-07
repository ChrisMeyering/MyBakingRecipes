package com.baking.chris.mybakingrecipes;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baking.chris.mybakingrecipes.data.Ingredient;
import com.baking.chris.mybakingrecipes.data.Recipe;
import com.baking.chris.mybakingrecipes.data.Step;
import com.baking.chris.mybakingrecipes.provider.RecipesContract;
import com.baking.chris.mybakingrecipes.ui.IngredientListAdapter;
import com.baking.chris.mybakingrecipes.ui.RecipeListFragment;
import com.baking.chris.mybakingrecipes.ui.StepListAdapter;

import com.baking.chris.mybakingrecipes.widget.GridWidgetService;
import com.baking.chris.mybakingrecipes.widget.RecipeIngredientsService;
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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements RecipeListFragment.OnRecipeClickListener,
        StepListAdapter.OnStepSelectedListener,
        ExoPlayer.EventListener
{
    public static final String TAG = MainActivity.class.getSimpleName();
    boolean isTablet;
    private Recipe recipe = null;
    private int currentStepId;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isTablet = getResources().getBoolean(R.bool.is_tablet_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            initializeMediaSession();
            if (recipe != null)
                initRecipeView();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        ActionBar actionBar = getSupportActionBar();
        if (!isTablet) {
            super.onBackPressed();
        } else if (findViewById(R.id.cl_step_layout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.cl_step_layout).setVisibility(View.INVISIBLE);
            releasePlayer();
            if (actionBar != null)
                actionBar.setSubtitle("");
        } else if (findViewById(R.id.sv_recipe_layout).getVisibility() == View.VISIBLE) {
            findViewById(R.id.sv_recipe_layout).setVisibility(View.INVISIBLE);
            recipe = null;
            if (actionBar != null)
                actionBar.setTitle(getString(R.string.app_name));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRecipeSelected(Recipe recipe) {
        getContentResolver().delete(RecipesContract.RecentEntry.CONTENT_URI, null, null);
        //int inserted = getContentResolver().bulkInsert(RecipesContract.RecentEntry.CONTENT_URI, recipe.getRecipeIngredientsContentValues());
        for (Ingredient ingredient : recipe.getIngredients()){
            getContentResolver().insert(RecipesContract.RecentEntry.CONTENT_URI, ingredient.toContentValues());
        }
        RecipeIngredientsService.startActionUpdateIngredients(this);
        if (!isTablet) {
            Bundle extra = new Bundle();
            extra.setClassLoader(Recipe.class.getClassLoader());
            extra.putParcelable(getString(R.string.RECIPE_KEY), recipe);
            Intent recipeIntent = new Intent(this, RecipeActivity.class);
            recipeIntent.putExtras(extra);
            startActivity(recipeIntent);
        } else {
            releasePlayer();
            findViewById(R.id.cl_step_layout).setVisibility(View.INVISIBLE);
            this.recipe = recipe;
            findViewById(R.id.intro_exo_player_view).setVisibility(View.GONE);
            findViewById(R.id.tv_introduction_title).setVisibility(View.GONE);
            findViewById(R.id.sv_recipe_layout).setVisibility(View.VISIBLE);
            initRecipeView();
        }
    }

    private void initRecipeView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(recipe.getName());
        }
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        RecyclerView rvIngredients = findViewById(R.id.rv_ingredients);
        rvIngredients.setNestedScrollingEnabled(false);
        rvIngredients.addItemDecoration(decoration);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(new IngredientListAdapter(this, recipe.getIngredients()));

        RecyclerView rvSteps = findViewById(R.id.rv_steps);
        rvSteps.setNestedScrollingEnabled(false);
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(new StepListAdapter(this, recipe.getSteps()));
    }

    @Override
    public void onStepSelected(int stepNumber) {
        findViewById(R.id.cl_step_layout).setVisibility(View.VISIBLE);
        currentStepId = stepNumber - 1;
        releasePlayer();
        updateStepView();
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
        updateStepView();
    }

    private void updateStepView(){
        Step step = recipe.getSteps().get(currentStepId);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("Step - " + step.getId());
        }
        if (currentStepId == 0){
            findViewById(R.id.btn_prev_step).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_prev_step).setVisibility(View.VISIBLE);
        }
        if (currentStepId == recipe.getSteps().size()-1) {
            findViewById(R.id.btn_next_step).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_next_step).setVisibility(View.VISIBLE);
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
                                Toast.makeText(MainActivity.this, "Error: Unable to load thumbnail.", Toast.LENGTH_LONG).show();
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
            Log.i(TAG, "onPlay");
            player.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            Log.i(TAG, "onPause");
            player.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            Log.i(TAG, "Skip to previous pressed");
                player.seekTo(0);
        }
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
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(recipe.getSteps().get(currentStepId).getVideoURL()),
                    new DefaultDataSourceFactory(this, userAgent), new DefaultExtractorsFactory(), null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(false);
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
/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recipes != null) {
            outState.putParcelableArrayList(getString(R.string.RECIPES_KEY), (ArrayList<Recipe>) recipes);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recipes = savedInstanceState.getParcelableArrayList(getString(R.string.RECIPES_KEY));
    }

    private void getRecipes() {
        mApiInterface = RetrofitClient.getClient().create(ApiInterface.class);

        Call<List<Recipe>> call = mApiInterface.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                recipes = response.body();
                Log.i(TAG, "Number of recipes = " + recipes.size());
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.i(TAG, t.toString());
            }
        });
    }*/
}
