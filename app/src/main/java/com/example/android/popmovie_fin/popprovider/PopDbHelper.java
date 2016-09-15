package com.example.android.popmovie_fin.popprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popmovie_fin.popprovider.PopContract.MoviesStore;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieCollect;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieReview;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieTrailer;


public class PopDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public PopDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private String SQL_CreateMovieTable(){

         String SQL_CREATE_TABLE = "CREATE TABLE " + MoviesStore.TABLE_NAME + " (" +

                 MoviesStore._ID + " INTEGER PRIMARY KEY," +

                 MoviesStore.COLUMN_ID + " TEXT UNIQUE NOT NULL , " +
                 MoviesStore.COLUMN_PATH + " TEXT, " +

                 MoviesStore.COLUMN_TITLE + " TEXT, " +
                 MoviesStore.COLUMN_OVERVIEW  + " TEXT, " +
                 MoviesStore.COLUMN_VOTE + " REAL, " +
                 MoviesStore.COLUMN_POPULAR + " REAL, " +
                 MoviesStore.COLUMN_RUNTIME  + " TEXT, " +
                 MoviesStore.COLUMN_RELEASE + " INTEGER, " +
                 MoviesStore.COLUMN_TOP + " INTEGER, " +
                 MoviesStore.COLUMN_POP + " INTEGER, " +

                 MoviesStore.COLUMN_IMG + " BLOB, " +

                 MoviesStore.COLUMN_DATEMARK + " INTEGER NOT NULL " + " );";

        return SQL_CREATE_TABLE;
    }

    private String SQL_CreateCollectMovieTable(){
        String SQL_CREATE_TABLE = "CREATE TABLE " + MovieCollect.TABLE_NAME + " (" +
                MovieCollect._ID + " INTEGER PRIMARY KEY," +
                MovieCollect.COLUMN_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE" + " );";
        return SQL_CREATE_TABLE;
    }

    private String SQL_CreateTrailerTable(){
        String SQL_CREATE_TABLE = "CREATE TABLE " + MovieTrailer.TABLE_NAME + " (" +

                MovieTrailer._ID + " INTEGER PRIMARY KEY," +
                MovieTrailer.COLUMN_ID + " TEXT NOT NULL, " +
                MovieTrailer.COLUMN_URL + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, " +
                MovieTrailer.COLUMN_NAME + " TEXT DEFAULT 'Trailer', " +
                MovieTrailer.COLUMN_TYPE + " TEXT, " +

                " FOREIGN KEY (" + MovieTrailer.COLUMN_ID + ") REFERENCES " +
                MoviesStore.TABLE_NAME + " (" + MoviesStore.COLUMN_ID + ") ON DELETE CASCADE);";

        return SQL_CREATE_TABLE;
    }

    private String SQL_CreateReviewTable(){
        String SQL_CREATE_TABLE = "CREATE TABLE " + MovieReview.TABLE_NAME + " (" +

                MovieReview._ID + " INTEGER PRIMARY KEY," +
                MovieReview.COLUMN_ID + " TEXT NOT NULL, " +
                MovieReview.COLUMN_REVIEW_AUTHOR + " TEXT DEFAULT 'anonymity', " +
                MovieReview.COLUMN_REVIEW_CONTENT + " TEXT DEFAULT 'none', " +

                " FOREIGN KEY (" + MovieReview.COLUMN_ID + ") REFERENCES " +
                MoviesStore.TABLE_NAME + " (" + MoviesStore.COLUMN_ID + ") ON DELETE CASCADE);";

        return SQL_CREATE_TABLE;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON"); //手动开启外键支持

        sqLiteDatabase.execSQL(SQL_CreateMovieTable());
        sqLiteDatabase.execSQL(SQL_CreateCollectMovieTable());
        sqLiteDatabase.execSQL(SQL_CreateTrailerTable());
        sqLiteDatabase.execSQL(SQL_CreateReviewTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesStore.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieCollect.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieTrailer.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieReview.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
