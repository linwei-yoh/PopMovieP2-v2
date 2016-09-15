package com.example.android.popmovie_fin.popprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.popmovie_fin.popprovider.PopContract.MovieCollect;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieReview;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieTrailer;
import com.example.android.popmovie_fin.popprovider.PopContract.MoviesStore;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PopDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_TOP = 110;
    static final int MOVIE_WITH_POP = 120;
    static final int MOVIE_WITH_FAV = 130;
    static final int MOVIE_WITH_ID = 101;

    static final int TRAILER = 200;
    static final int TRAILER_WITH_ID = 201;

    static final int REVIEW = 300;
    static final int REVIEW_WITH_ID = 301;

    static final int COLLECT = 400;

    private static final String sMovieStoreWithIdSel =
            MoviesStore.TABLE_NAME + "." + MoviesStore.COLUMN_ID + " = ? ";
    private static final String sMovieStoreWithTopSel =
            MoviesStore.TABLE_NAME + "." + MoviesStore.COLUMN_TOP + " = ? ";
    private static final String sMovieStoreWithpopSel =
            MoviesStore.TABLE_NAME + "." + MoviesStore.COLUMN_POP + " = ? ";

    private static final String sMovieTrailerWithIdSel =
            MovieTrailer.TABLE_NAME + "." + MovieTrailer.COLUMN_ID + " = ? ";

    private static final String sMovieReviewWithIdSel =
            MovieReview.TABLE_NAME + "." + MovieReview.COLUMN_ID + " = ? ";

    private static final SQLiteQueryBuilder sMovieWithFavoritegQueryBuilder;

    static {
        sMovieWithFavoritegQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sMovieWithFavoritegQueryBuilder.setTables(
                MoviesStore.TABLE_NAME + " INNER JOIN " +
                        MovieCollect.TABLE_NAME +
                        " ON " + MoviesStore.TABLE_NAME +
                        "." + MoviesStore.COLUMN_ID +
                        " = " + MovieCollect.TABLE_NAME +
                        "." + MovieCollect.COLUMN_ID);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PopContract.MOVIE_PATH, MOVIE);
        matcher.addURI(authority, PopContract.MOVIE_PATH + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, PopContract.MOVIE_PATH + "/top", MOVIE_WITH_TOP);
        matcher.addURI(authority, PopContract.MOVIE_PATH + "/pop", MOVIE_WITH_POP);
        matcher.addURI(authority, PopContract.MOVIE_PATH + "/fav", MOVIE_WITH_FAV);

        matcher.addURI(authority, PopContract.TRAILER_PATH, TRAILER);
        matcher.addURI(authority, PopContract.TRAILER_PATH + "/#", TRAILER_WITH_ID);

        matcher.addURI(authority, PopContract.REVIEW_PATH, REVIEW);
        matcher.addURI(authority, PopContract.REVIEW_PATH + "/#", REVIEW_WITH_ID);

        matcher.addURI(authority, PopContract.COLLECT_PATH, COLLECT);
        return matcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesStore.CONTENT_TYPE;
            case MOVIE_WITH_TOP:
                return MoviesStore.CONTENT_TYPE;
            case MOVIE_WITH_POP:
                return MoviesStore.CONTENT_TYPE;
            case MOVIE_WITH_FAV:
                return MoviesStore.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MoviesStore.CONTENT_ITEM_TYPE;
            case TRAILER:
                return MovieTrailer.CONTENT_TYPE;
            case TRAILER_WITH_ID:
                return MovieTrailer.CONTENT_TYPE;
            case REVIEW:
                return MovieReview.CONTENT_TYPE;
            case REVIEW_WITH_ID:
                return MovieReview.CONTENT_TYPE;
            case COLLECT:
                return MovieCollect.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PopDbHelper(getContext());
        return false;
    }

    private Cursor getMoviebyId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesStore.getMovieIdFromUri(uri);
        Cursor resutl = mOpenHelper.getReadableDatabase().query(
                MoviesStore.TABLE_NAME,
                projection,
                sMovieStoreWithIdSel,
                new String[]{movieId},
                null,
                null,
                sortOrder
        );
        return resutl;
    }

    private Cursor getTopMovies(String[] projection, String sortOrder) {
        Cursor resutl = mOpenHelper.getReadableDatabase().query(
                MoviesStore.TABLE_NAME,
                projection,
                sMovieStoreWithTopSel,
                new String[]{"1"},
                null,
                null,
                sortOrder);
        return resutl;
    }

    private Cursor getPopMovies(String[] projection, String sortOrder) {
        Cursor resutl = mOpenHelper.getReadableDatabase().query(
                MoviesStore.TABLE_NAME,
                projection,
                sMovieStoreWithpopSel,
                new String[]{"1"},
                null,
                null,
                sortOrder);
        return resutl;
    }

    private Cursor getFavoriteMovies(String[] projection, String sortOrder) {
        return sMovieWithFavoritegQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
    }

    private Cursor getTrailerbyId(Uri uri, String[] projection, String sortOrde) {
        String id = MovieTrailer.getTrailerIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                MovieTrailer.TABLE_NAME,
                projection,
                sMovieTrailerWithIdSel,
                new String[]{id},
                null,
                null,
                sortOrde);
    }

    private Cursor getReviewbyId(Uri uri, String[] projection, String sortOrde) {
        String id = MovieReview.getReviewIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                MovieReview.TABLE_NAME,
                projection,
                sMovieReviewWithIdSel,
                new String[]{id},
                null,
                null,
                sortOrde);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                retCursor = db.query(
                        MoviesStore.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MOVIE_WITH_ID:
                retCursor = getMoviebyId(uri, projection, sortOrder);
                break;
            case MOVIE_WITH_TOP:
                retCursor = getTopMovies(projection, sortOrder);
                break;
            case MOVIE_WITH_POP:
                retCursor = getPopMovies(projection, sortOrder);
                break;
            case MOVIE_WITH_FAV:
                retCursor = getFavoriteMovies(projection,sortOrder);
                break;
            case TRAILER:
                retCursor = db.query(
                        MovieTrailer.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TRAILER_WITH_ID:
                retCursor = getTrailerbyId(uri, projection, sortOrder);
                break;
            case REVIEW:
                retCursor = db.query(
                        MovieReview.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case REVIEW_WITH_ID:
                retCursor = getReviewbyId(uri, projection, sortOrder);
                break;
            case COLLECT:
                retCursor = db.query(
                        MovieCollect.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        Uri returnUri;

        switch (match) {
            case MOVIE:
                _id = db.insert(MoviesStore.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = null; //不提供插入后的当前数据URI地址
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case COLLECT:
                _id = db.insert(MovieCollect.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = null;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case TRAILER:
                _id = db.insert(MovieTrailer.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = null;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case REVIEW:
                _id = db.insert(MovieReview.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = null;
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(MoviesStore.TABLE_NAME, selection, selectionArgs);
                break;
            case COLLECT:
                rowsDeleted = db.delete(MovieCollect.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(MovieTrailer.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(MovieReview.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private int dbbulkInsert(String tablename, SQLiteDatabase db, ContentValues[] values, Uri uri) {
        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tablename, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return returnCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return dbbulkInsert(MoviesStore.TABLE_NAME, db, values, uri);
            case COLLECT:
                return dbbulkInsert(MovieCollect.TABLE_NAME, db, values, uri);
            case TRAILER:
                return dbbulkInsert(MovieTrailer.TABLE_NAME, db, values, uri);
            case REVIEW:
                return dbbulkInsert(MovieReview.TABLE_NAME, db, values, uri);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MoviesStore.TABLE_NAME, values, selection, selectionArgs);
                break;
            case COLLECT:
                rowsUpdated = db.update(MovieCollect.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MovieTrailer.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MovieReview.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
