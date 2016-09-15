package com.example.android.popmovie_fin.popprovider;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class PopContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.popmovie_fin.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static String MOVIE_PATH = "movie";
    public static String COLLECT_PATH = "favorite";
    public static String TRAILER_PATH = "trailer";
    public static String REVIEW_PATH = "review";


    //PATH_PARAM : COLLECT_TABLE_NAME / Movie_TABLE_NAME
    public static Uri getContentUri(String PATH_PARAM) {
        return BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARAM).build();
    }

    public static String getContentType(String PATH_PARAM) {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARAM;
    }

    public static String getContentItemType(String PATH_PARAM) {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARAM;
    }

    //Popular/TopRated Movie store
    public static class MoviesStore implements BaseColumns {
        public static final String TABLE_NAME = "MovieTable";

        public static final Uri CONTENT_URI = getContentUri(MOVIE_PATH);
        public static final String CONTENT_TYPE = getContentType(MOVIE_PATH);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(MOVIE_PATH);

        public final static String COLUMN_ID = "id";
        public final static String COLUMN_PATH = "poster_path";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_OVERVIEW = "overview";
        public final static String COLUMN_VOTE = "vote_average";
        public final static String COLUMN_POPULAR = "popularity";
        public final static String COLUMN_RELEASE = "release_date";
        public final static String COLUMN_RUNTIME = "runtime";

        public final static String COLUMN_IMG = "img";

        //type
        public final static String COLUMN_TOP = "istop";
        public final static String COLUMN_POP = "ispop";

        public final static String COLUMN_DATEMARK = "tag";

        public static Uri buildMovieUriwithId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildMovieUriwithTop() {
            return CONTENT_URI.buildUpon().appendPath("top").build();
        }

        public static Uri buildMovieUriwithPop() {
            return CONTENT_URI.buildUpon().appendPath("pop").build();
        }

        public static Uri buildMovieUriwithFav() {
            return CONTENT_URI.buildUpon().appendPath("fav").build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            long test = ContentUris.parseId(uri);
            String test2 = uri.getPathSegments().get(1);
            return Long.toString(test);
        }
    }

    //收藏电影列表
    public static class MovieCollect implements BaseColumns {
        public static final String TABLE_NAME = "CollectTable";

        public static final Uri CONTENT_URI = getContentUri(COLLECT_PATH);
        public static final String CONTENT_TYPE = getContentType(COLLECT_PATH);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(COLLECT_PATH);

        public final static String COLUMN_ID = "id";
    }

    //预览视频地址
    public static class MovieTrailer implements BaseColumns {
        public static final String TABLE_NAME = "TrailerTable";

        public static final Uri CONTENT_URI = getContentUri(TRAILER_PATH);
        public static final String CONTENT_TYPE = getContentType(TRAILER_PATH);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(TRAILER_PATH);

        public final static String COLUMN_ID = "id";
        public final static String COLUMN_URL = "source"; //youtube 视频地址
        public final static String COLUMN_NAME = "name"; //预告片名称
        public final static String COLUMN_TYPE = "type"; //视频类型

        public static Uri buildTrailerUriWithId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getTrailerIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    //电影评论
    public static class MovieReview implements BaseColumns {
        public static final String TABLE_NAME = "ReviewTable";

        public static final Uri CONTENT_URI = getContentUri(REVIEW_PATH);
        public static final String CONTENT_TYPE = getContentType(REVIEW_PATH);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(REVIEW_PATH);

        public final static String COLUMN_ID = "id";

        public final static String COLUMN_REVIEW_AUTHOR = "author"; //评论发布者
        public final static String COLUMN_REVIEW_CONTENT = "content";//评论内容

        public static Uri buildReviewUriWithId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getReviewIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
