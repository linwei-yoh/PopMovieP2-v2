package com.example.android.popmovie_fin.popsync;


import com.google.gson.Gson;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class MovieResponse {

    public class MovieInfoItem {
        public String id;
    }

    public class MovieTrailerItem {
        public String type;    //类型  预告片
        public String source;  //视频ID
        public String name;    //视频名称
    }

    public class MovieWebSiteItem {
        public List<MovieTrailerItem> youtube;

        public MovieWebSiteItem() {
            youtube = new ArrayList<>();
        }
    }

    public class MovieReviewItem {
        public String author;
        public String content;
    }

    public class MovieReviewList {
        List<MovieReviewItem> results;

        public MovieReviewList() {
            results = new ArrayList<>();
        }
    }

    public class MovieIdResponse {
        List<MovieInfoItem> results;

        public MovieIdResponse() {
            results = new ArrayList<MovieInfoItem>();
        }

        public List getMovieIdList(){
            List<String> list = new ArrayList<String>();

            if (null != results){
                for(int i=0;i<results.size();i++)
                    list.add(results.get(i).id);
            }
            return list;
        }
    }

    public class MovieDataResponse {
        public String id;
        public String poster_path;
        public String overview;
        public String release_date;
        public String title;
        public String runtime;
        public float popularity;
        public float vote_average;
        public MovieWebSiteItem trailers;
        public MovieReviewList reviews;
    }

    public static MovieIdResponse idList_parseJSON(Reader response) {
        Gson gson = new Gson();
        MovieIdResponse movieIdResponse = gson.fromJson(response, MovieIdResponse.class);
        return movieIdResponse;
    }

    public static MovieDataResponse movieData_parseJSON(Reader response) {
        Gson gson = new Gson();
        MovieDataResponse movieDataResponse = gson.fromJson(response, MovieDataResponse.class);
        return movieDataResponse;
    }


}
