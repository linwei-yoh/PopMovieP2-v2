package com.example.android.popmovie_fin.popsync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.example.android.popmovie_fin.BuildConfig;
import com.example.android.popmovie_fin.MainActivity;
import com.example.android.popmovie_fin.popprovider.PopContract;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieCollect;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieReview;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieTrailer;
import com.example.android.popmovie_fin.popprovider.PopContract.MoviesStore;
import com.example.android.popmovie_fin.popsync.MovieResponse.MovieIdResponse;
import com.example.android.popmovie_fin.popsync.MovieResponse.MovieDataResponse;
import com.example.android.popmovie_fin.R;
import com.example.android.popmovie_fin.Utility;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class  PopSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PopSyncAdapter.class.getSimpleName();

    OkHttpClient client = new OkHttpClient(); //应该采用单例模式 。暂时这么处理先

    final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
    final String APPID_PARAM = "api_key";

    final String sMovieWithIdSel =
            MoviesStore.TABLE_NAME +
                    "." + MoviesStore.COLUMN_ID + " = ? ";

    final String sTrailerWithIdSel =
            MovieTrailer.TABLE_NAME +
                    "." + MovieTrailer.COLUMN_ID + " = ? ";

    final String sReviewWithIdSel =
            MovieReview.TABLE_NAME +
                    "." + MovieReview.COLUMN_ID + " = ? ";

    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    //1000ms * 60 * 60 * 24 = 1天的毫秒数
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int NOTIFICATION_ID = 17;

    public PopSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    private Request getRequestWithId(String id) {
        HttpUrl.Builder urlBuilder;
        String url;
        Request request;

        urlBuilder = HttpUrl.parse(MOVIE_BASE_URL).newBuilder();
        urlBuilder.addEncodedPathSegment(id);
        urlBuilder.addQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY);
        urlBuilder.addQueryParameter("append_to_response", "trailers,reviews");

        url = urlBuilder.build().toString();
        request = new Request.Builder().url(url).build();

        return request;
    }

    private Response getResponseSynchronousWithType(String type) {
        HttpUrl.Builder urlBuilder;
        String url;
        Request request;

        urlBuilder = HttpUrl.parse(MOVIE_BASE_URL).newBuilder();
        urlBuilder.addEncodedPathSegment(type);
        urlBuilder.addQueryParameter(APPID_PARAM, BuildConfig.MOVIE_API_KEY);
        url = urlBuilder.build().toString();
        request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            return  response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void deleteOldsrc(int julianday) {
        Time dayTime = new Time();
        getContext().getContentResolver().delete(PopContract.MoviesStore.CONTENT_URI,
                PopContract.MoviesStore.COLUMN_DATEMARK + " <= ?",
                new String[]{Long.toString(dayTime.setJulianDay(julianday - 1))});
    }



    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        MovieIdResponse movieIdResponse;
        Set<String> collist = new HashSet<>();
        Set<String> toplist = new HashSet<>();
        Set<String> poplist = new HashSet<>();
        Set<String> alllist = new HashSet<>();

        Response result;

        Time dayTime = new Time();
        dayTime.setToNow();
        int julianday = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        //查询TopMovie id 列表
        result = getResponseSynchronousWithType("top_rated");
        movieIdResponse = MovieResponse.idList_parseJSON(result.body().charStream());
        toplist.addAll(movieIdResponse.getMovieIdList());

        //查询popMovie id 列表
        result = getResponseSynchronousWithType("popular");
        movieIdResponse = MovieResponse.idList_parseJSON(result.body().charStream());
        poplist.addAll(movieIdResponse.getMovieIdList());

        //数据库获取收藏id列表
        Cursor cursor = getContext().getContentResolver().query(
                MovieCollect.CONTENT_URI,
                new String[]{MovieCollect.COLUMN_ID},
                null,
                null,
                null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                int id_index = cursor.getColumnIndex(MovieCollect.COLUMN_ID);
                collist.add(cursor.getString(id_index));
            }while (cursor.moveToNext());
            cursor.close();
        }

        alllist.addAll(collist);
        alllist.addAll(toplist);
        alllist.addAll(poplist);

        //遍历Set 更新DB中所有id在Set中的记录 修改时间和类别
        for (String id : alllist) {
            Cursor checkCursor = getContext().getContentResolver().query(
                    MoviesStore.CONTENT_URI,
                    null, sMovieWithIdSel, new String[]{id}, null);

            ContentValues values = new ContentValues();
            values.put(MoviesStore.COLUMN_DATEMARK, julianday);
            values.put(MoviesStore.COLUMN_TOP, toplist.contains(id) ? 1 : 0);
            values.put(MoviesStore.COLUMN_POP, poplist.contains(id) ? 1 : 0);

            //更新
            if (checkCursor.moveToFirst()) {
                getContext().getContentResolver().update(MoviesStore.CONTENT_URI,
                        values, sMovieWithIdSel, new String[]{id});
            }
            //search ths id one by one,so i can't bulkInsert it.
            else {
                values.put(MoviesStore.COLUMN_ID, id);
                getContext().getContentResolver().insert(MoviesStore.CONTENT_URI, values);
            }
            checkCursor.close();
        }

        //删除所有早期记录 级联删除预告片和评论数据
        getContext().getContentResolver().delete(MoviesStore.CONTENT_URI,
                MoviesStore.COLUMN_DATEMARK + " <= ?", new String[]{Long.toString(julianday - 1)});

        //更新现有列表的数据 通过异步
        for (String id : alllist) {
            client.newCall(getRequestWithId(id)).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    int trailerSize;
                    int reviewSize;
                    ContentValues cvDetail;
                    Vector<ContentValues> cVVector;
                    ContentValues itemVal;
                    ContentValues[] cvsTrailer;
                    ContentValues[] cvsReviews;

                    MovieDataResponse dataResponse =
                            MovieResponse.movieData_parseJSON(response.body().charStream());

                    //详细数据
                    cvDetail = Utility.objectToContentValues(dataResponse);
                    getContext().getContentResolver().update(PopContract.MoviesStore.CONTENT_URI,
                            cvDetail,
                            sMovieWithIdSel,
                            new String[]{dataResponse.id});

                    trailerSize = dataResponse.trailers.youtube.size();
                    reviewSize = dataResponse.reviews.results.size();

                    //预告片数据插入
                    cvsTrailer = new ContentValues[trailerSize];
                    cVVector = new Vector<>(trailerSize);
                    for (int i = 0; i < trailerSize; i++) {
                        itemVal = Utility.objectToContentValues(dataResponse.trailers.youtube.get(i));
                        itemVal.put(MovieTrailer.COLUMN_ID, dataResponse.id);
                        cVVector.add(itemVal);
                    }
                    cVVector.toArray(cvsTrailer);
                    getContext().getContentResolver().bulkInsert(
                            MovieTrailer.CONTENT_URI, cvsTrailer);

                    //评论数据插入
                    cvsReviews = new ContentValues[reviewSize];
                    cVVector = new Vector<>(reviewSize);
                    for (int i = 0; i < reviewSize; i++) {
                        itemVal = Utility.objectToContentValues(dataResponse.reviews.results.get(i));
                        itemVal.put(MovieReview.COLUMN_ID, dataResponse.id);
                        cVVector.add(itemVal);
                    }
                    cVVector.toArray(cvsReviews);
                    getContext().getContentResolver().bulkInsert(
                            MovieReview.CONTENT_URI, cvsReviews);
                }
            });
        }

        //通知
        notifyMovieMsg();
    }

    private void notifyMovieMsg() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String DisnotifIcationKey = context.getString(R.string.pref_enable_notifications_key);
        boolean ifdisnotification = prefs.getBoolean(DisnotifIcationKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (ifdisnotification) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                 mBuilder.setContentTitle("每日新电影")
                        .setContentText("通知功能测试");

                //带父界面的通知写法
//                Intent appIntent = new Intent(context, DetailActivity.class);
//                appIntent.setData(写入uri);
//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                stackBuilder.addParentStack(DetailActivity.class);
//                stackBuilder.addNextIntent(appIntent);

                Intent appIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(appIntent);

                PendingIntent pendingAppIntend = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingAppIntend);

                NotificationManager mNotificaationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificaationManager.notify(NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();

            }
        }
    }

    /********** SyncAdapter的配置和调用*********/
    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        //两个变量要求SyncAdapter强制执行
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}
