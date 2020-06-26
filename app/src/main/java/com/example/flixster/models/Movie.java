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
    String relBackdropPath;
    String relPosterPath;
    String backdropSize;
    String posterSize;
    String backdropPath;
    String posterPath;
    String title;
    String overview;
    Double voteAverage;
    Integer id;
    Integer[] genreIDs;
    String[] genres;
    String trailerKey;
    public static final String DEFAULT_TRAILER_KEY = "dQw4w9WgXcQ";
    public static final String DEFAULT_SIZE = "original";
    public static final int MAX_GENRES = 2;
    public static final String TAG = "Movie";

    // No argument, empty constructor, required for Parceler
    public Movie() {}

    public Movie(Context context, JSONObject jsonObject) throws JSONException {
        // Get all data from JSON objects
        relBackdropPath = jsonObject.getString("backdrop_path");
        relPosterPath = jsonObject.getString("poster_path");
        title = jsonObject.getString("title");
        overview = jsonObject.getString("overview");
        voteAverage = jsonObject.getDouble("vote_average");
        id = jsonObject.getInt("id");

        genreIDs = new Integer[]{0, 0};
        JSONArray jsonArray = jsonObject.getJSONArray("genre_ids");
        for(int i = 0; i < Math.min(jsonArray.length(), MAX_GENRES); i++) {
            genreIDs[i] = (Integer) jsonArray.get(i);
        }
        genres = new String[]{"", ""};
        setGenres(context);

        trailerKey = DEFAULT_TRAILER_KEY;
        setTrailerKey(context);

        backdropSize = DEFAULT_SIZE;
        posterSize = DEFAULT_SIZE;
        setSizes(context);
    }

    /** Creates a Movie object for each JSONObject and returns an array of all of them. */
    public static List<Movie> fromJsonArray(Context context, JSONArray movieJsonArray) throws JSONException {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < movieJsonArray.length(); i++) {
            movies.add(new Movie(context, movieJsonArray.getJSONObject((i))));
        }
        return movies;
    }

    public String getBackdropPath() {
        return String.format("https://image.tmdb.org/t/p/%s/%s", backdropSize, relBackdropPath);
    }

    public String getPosterPath() {
        System.out.println(String.format("https://image.tmdb.org/t/p/%s/%s", posterSize , relPosterPath));
        return String.format("https://image.tmdb.org/t/p/%s/%s", posterSize , relPosterPath);
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() { return overview; }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getId() { return id; }

    public String getGenres() {
        if (!genres[1].equals("")) {
            return genres[0] + ", " + genres[1];
        } else if (!genres[0].equals("")) {
            return genres[0];
        } else {
            return "";
        }
    }

    public String getTrailerKey() { return trailerKey; }

    /** Gets/sets the key for the trailer on YouTube using The Movie Database API */
    public void setTrailerKey(final Context context) {
        @SuppressLint("DefaultLocale")
        String movieUrl = String.format("https://api.themoviedb.org/3/movie/%d/videos?api_key=%s",
                this.id,
                context.getString(R.string.movies_api_key));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(movieUrl,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG,"onSuccess");
                        JSONObject jsonObject = json.jsonObject;
                        try {
                            JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
                            Log.i(TAG, "result: " + result);
                            trailerKey = result.getString("key");
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

    /** Gets/sets the sizes for the poster and backdrop using The Movie Database API*/
    private void setSizes(Context context) {
        String configurationUrl = String.format("https://api.themoviedb.org/3/configuration?api_key=%s",
                context.getString(R.string.movies_api_key));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(configurationUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess");
                JSONObject jsonObject;
                try {
                    jsonObject = json.jsonObject.getJSONObject("images");
                    JSONArray backdropSizes = jsonObject.getJSONArray("backdrop_sizes");
                    backdropSize = backdropSizes.getString(backdropSizes.length() - 2);
                    JSONArray posterSizes = jsonObject.getJSONArray("poster_sizes");
                    posterSize = posterSizes.getString(posterSizes.length() - 2);
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

    /** Gets/sets the genres for the Movie using The Movie Database API */
    private void setGenres(Context context) {
        String genreURL = String.format("https://api.themoviedb.org/3/genre/movie/list?api_key=%s",
                context.getString(R.string.movies_api_key));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(genreURL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess");
                JSONArray genreArray;
                try {
                    genreArray = json.jsonObject.getJSONArray("genres");
                    // Only get the top two genres
                    for (int i = 0; i < Math.min(genreIDs.length, MAX_GENRES); i++) {
                        for (int j = 0; j < genreArray.length(); j++ ) {
                            JSONObject genreObject = genreArray.getJSONObject(j);
                            int genreID;
                            try {
                                genreID = (int) genreObject.get("id");
                            } catch (NullPointerException e) {
                                Log.e(TAG, "Genre does not exist", e);
                                continue;
                            }
                            if (genreIDs[i] == genreID) {
                                genres[i] = (String) genreObject.get("name");
                            }
                        }
                    }
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
