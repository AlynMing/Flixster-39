package com.example.flixster;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flixster.databinding.ActivityMovieDetailsBinding;
import com.example.flixster.models.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends YouTubeBaseActivity {

    Movie movie;
    ImageView ivBackdrop;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    TextView tvGenre;
    YouTubePlayerView playerView;
    public static final String TAG = "MovieDetailsActivity";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovieDetailsBinding binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ivBackdrop = binding.ivPoster;
        tvTitle = binding.tvTitle;
        tvOverview = binding.tvOverview;
        rbVoteAverage = binding.rbVoteAverage;
        tvGenre = binding.tvGenre;
        playerView = binding.player;

        // Unwrap the movie passed in via intent, using its simple name as a key
        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s'", movie.getTitle()));

        // Set the main image
        String imageUrl;
        int placeholder;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageUrl = movie.getPosterPath();
            placeholder = R.drawable.flicks_movie_placeholder;
        } else {
            imageUrl = movie.getBackdropPath();
            placeholder = R.drawable.flicks_backdrop_placeholder;
        }
        int radius = 30;
        int margin = 5;
        Glide.with(context)
                .load(imageUrl)
                .placeholder(placeholder)
                .fitCenter()
                .transform(new RoundedCornersTransformation(radius, margin))
                .into(ivBackdrop);

        // Populate the text fields
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        tvGenre.setText(movie.getGenres());

        // Fill in the rating bar
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        // Initialize YouTube API playerView with API key stored in secrets.xml
        playerView.initialize(getString(R.string.youtube_api_key), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                final YouTubePlayer youTubePlayer, boolean b) {
                // Load the video in
                youTubePlayer.cueVideo(movie.getTrailerKey());
                // Make sure that when the video starts playing, it's in fullscreen mode
                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {
                        youTubePlayer.setFullscreen(true);
                    }
                    @Override
                    public void onPaused() { }
                    @Override
                    public void onStopped() { }
                    @Override
                    public void onBuffering(boolean b) { }
                    @Override
                    public void onSeekTo(int i) { }
                });
                // Stop video if not in fullscreen, otherwise play
                youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {

                    @Override
                    public void onFullscreen(boolean enteringFullscreen) {
                        if(!enteringFullscreen) {
                            youTubePlayer.pause();
                        } else {
                            youTubePlayer.play();
                        }
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "Error initializing YouTube player");
            }
        });
    }
}