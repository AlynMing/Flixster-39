package com.example.flixster.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

@Parcel
public class Movie {

    // fields must be public for Parceler
    String backdropPath;
    String posterPath;
    String title;
    String overview;
    Double voteAverage;
    Integer id;
    String trailerKey;
    public static final String DEFAULT_TRAILER_KEY = "dQw4w9WgXcQ";
    public static final String TAG = "Movie";

    // No argument, empty constructor, required for Parceler
    public Movie() {}

    public Movie(Context context, JSONObject jsonObject) throws JSONException {
        backdropPath = jsonObject.getString("backdrop_path");
        posterPath = jsonObject.getString("poster_path");
        title = jsonObject.getString("title");
        overview = jsonObject.getString("overview");
        voteAverage = jsonObject.getDouble("vote_average");
        id = jsonObject.getInt("id");
        trailerKey = DEFAULT_TRAILER_KEY;
        setTrailerKey(context);
    }

    public static List<Movie> fromJsonArray(Context context, JSONArray movieJsonArray) throws JSONException {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < movieJsonArray.length(); i++) {
            movies.add(new Movie(context, movieJsonArray.getJSONObject((i))));
        }
        return movies;
    }

    public String getBackdropPath() {
        return String.format("https://image.tmdb.org/t/p/w342/%s", backdropPath);
    }

    public String getPosterPath() {
        return String.format("https://image.tmdb.org/t/p/w342/%s", posterPath);
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() { return overview; }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getId() { return id; }

    public String getTrailerKey() { return trailerKey; }

    public void setTrailerKey(final Context context) {
        @SuppressLint("DefaultLocale")
        String movieUrl = String.format("https://api.themoviedb.org/3/movie/%d/videos?api_key=%s",
                this.id,
                context.getString(R.string.movies_api_key));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(movieUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray array = (JSONArray) jsonObject.get("results");
                    //if (array.length() > 0) {
                        JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
                        Log.i(TAG, "result: " + result);
                        trailerKey = result.getString("key");

                    //}
                } catch (JSONException e) {
                    Log.e(TAG, "Hit json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });
    }

}
