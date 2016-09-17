package com.example.android.popmovie_fin;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popmovie_fin.popprovider.PopContract.MovieCollect;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieReview;
import com.example.android.popmovie_fin.popprovider.PopContract.MovieTrailer;
import com.example.android.popmovie_fin.popprovider.PopContract.MoviesStore;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Detail_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_KEY = "MovieID";
    private String MovieID;

    private static final String MOVIE_SHARE_HASHTAG = " #PopMovieApp";

    private String mMovieInfo;

    private boolean buttonSta;
    private QueryHandler mQueryHandler;
    private static final int DELETE_TOKEN = 1;
    private static final int INSERT_TOKEN = 2;
    private static final int QUERY_TOKEN = 3;

    private Detail_Trailer_Adapter mDetail_trailer_adapter;
    private Detail_Review_Adapter mDetail_review_adapter;

    private static final int DETAIL_LOADER = 1;
    private static final int TRAILER_LOADER = 2;
    private static final int REVIEW_LOADER = 3;

    private static final String[] DETAIL_COLUMNS = {
            MoviesStore.TABLE_NAME + "." + MoviesStore._ID,
            MoviesStore.COLUMN_PATH,
            MoviesStore.COLUMN_TITLE,
            MoviesStore.COLUMN_RELEASE,
            MoviesStore.COLUMN_VOTE,
            MoviesStore.COLUMN_RUNTIME,
            MoviesStore.COLUMN_OVERVIEW
    };

    static final int COL_DETAIL_IMG = 1;
    static final int COL_DETAIL_TITLE = 2;
    static final int COL_DETAIL_DATA = 3;
    static final int COL_DETAIL_VOTE = 4;
    static final int COL_DETAIL_RUNTIME = 5;
    static final int COL_DETAIL_OVERVIEW = 6;

    private static final String[] TRAILER_COLUMNS = {
            MovieTrailer.TABLE_NAME + "." + MovieTrailer._ID,
            MovieTrailer.COLUMN_URL,
            MovieTrailer.COLUMN_NAME
    };

    static final int COL_TRAILER_URL = 1;
    static final int COL_TRAILER_NAME = 2;

    private static final String[] REVIEW_COLUMNS = {
            MovieReview.TABLE_NAME + "." + MovieReview._ID,
            MovieReview.COLUMN_REVIEW_AUTHOR,
            MovieReview.COLUMN_REVIEW_CONTENT
    };

    static final int COL_REVIEW_AUTHOR = 1;
    static final int COL_REVIEW_CONTENT = 2;

    @BindView(R.id.movieTitel)
    TextView title;
    @BindView(R.id.movieYear)
    TextView year;
    @BindView(R.id.movieRunTime)
    TextView runTime;
    @BindView(R.id.movieRating)
    TextView rating;
    @BindView(R.id.movieOverView)
    TextView oView;
    @BindView(R.id.TrailerList)
    UnScrollListView view_Trailer;
    @BindView(R.id.ReviewList)
    UnScrollListView view_Reviews;

    @BindView(R.id.moviePoster)
    ImageView posterImg;
    @BindView(R.id.movieFavorite)
    Button markCol;

    @OnClick(R.id.movieFavorite)
    public void btnClick() {
        buttonSta = !buttonSta;

        if (buttonSta) {
            ContentValues values = new ContentValues();
            values.put(MovieCollect.COLUMN_ID, MovieID);
            mQueryHandler.startInsert(INSERT_TOKEN, null, MovieCollect.CONTENT_URI, values);
        } else {
            final String sMovieCollectWithIdSel =
                    MovieCollect.TABLE_NAME + "." + MovieCollect.COLUMN_ID + " = ? ";
            mQueryHandler.startDelete(DELETE_TOKEN, null, MovieCollect.CONTENT_URI, sMovieCollectWithIdSel, new String[]{MovieID});
        }
    }


    private void setFavBtnSta(boolean btnSta) {
        if (btnSta) {
            markCol.setText("已收藏!");
            markCol.setBackgroundColor(getResources().getColor(R.color.buttonDown));
        } else {
            markCol.setText("加入收藏");
            markCol.setBackgroundColor(getResources().getColor(R.color.buttonUp));
        }
    }

    private void initFavButtonSta() {
        if (MovieID == null) return;
        String sCollectSel = MovieCollect.TABLE_NAME + "."
                + MovieCollect.COLUMN_ID + " = ? ";
        mQueryHandler.startQuery(QUERY_TOKEN, null,
                MovieCollect.CONTENT_URI, null, sCollectSel, new String[]{MovieID}, null);
    }

    public Detail_Fragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mQueryHandler = new QueryHandler(getContext().getContentResolver());

        if (arguments != null) {
            MovieID = arguments.getString(DETAIL_KEY);
            initFavButtonSta();
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);

        //预告片列表;
        mDetail_trailer_adapter = new Detail_Trailer_Adapter(getContext(), null, 0);
        view_Trailer.setAdapter(mDetail_trailer_adapter);
        view_Trailer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Utility.getTarilerUri(cursor.getString(COL_TRAILER_URL)));

                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Log.d("OpenUriErr", "no receiving apps installed!");
                    }
                }
            }
        });

        //评论列表
        mDetail_review_adapter = new Detail_Review_Adapter(getContext(), null, 0);
        view_Reviews.setAdapter(mDetail_review_adapter);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ShareActionProvider mShareActionProvider;
        inflater.inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mMovieInfo != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieInfo + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    public void resetId(String newID) {
        if(MovieID == null) {
            MovieID = newID;
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            getLoaderManager().initLoader(TRAILER_LOADER, null, this);
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        }
        else{
            MovieID = newID;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            getLoaderManager().restartLoader(TRAILER_LOADER, null, this);
            getLoaderManager().restartLoader(REVIEW_LOADER, null, this);
        }
        initFavButtonSta();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(MovieID != null) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            getLoaderManager().initLoader(TRAILER_LOADER, null, this);
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri mUri;
        switch (id) {
            case DETAIL_LOADER:
                mUri = MoviesStore.buildMovieUriwithId(MovieID);
                return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS,
                        null, null, null);
            case TRAILER_LOADER:
                mUri = MovieTrailer.buildTrailerUriWithId(MovieID);
                return new CursorLoader(getActivity(), mUri, TRAILER_COLUMNS,
                        null, null, null);
            case REVIEW_LOADER:
                mUri = MovieReview.buildReviewUriWithId(MovieID);
                return new CursorLoader(getActivity(), mUri, REVIEW_COLUMNS,
                        null, null, null);
            default:
                return null;
        }
    }

    private void onUpdateDetail(Cursor data) {
        if (data != null && data.moveToFirst()) {

            String titleVal = data.getString(COL_DETAIL_TITLE);
            String data_year = data.getString(COL_DETAIL_DATA);
            String movielength = data.getString(COL_DETAIL_RUNTIME) + "min";
            DecimalFormat decimalFormat = new DecimalFormat(".0");
            String vote = decimalFormat.format(data.getFloat(COL_DETAIL_VOTE)) + "/10";
            String overView = data.getString(COL_DETAIL_OVERVIEW);

            String imgPath = data.getString(COL_DETAIL_IMG);
            String imgUrl = Utility.getImgPathiWithSize(imgPath, Utility.ImgSize.w185);

            title.setText(titleVal);
            year.setText(data_year);
            runTime.setText(movielength);
            rating.setText(vote);
            oView.setText(overView);

            Picasso.with(getContext())
                    .load(imgUrl)
                    .into(posterImg);

            mMovieInfo = String.format("%s 真是个好电影！", titleVal);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null != data) {
            int id = loader.getId();
            switch (id) {
                case DETAIL_LOADER:
                    onUpdateDetail(data);
                    break;
                case TRAILER_LOADER:
                    mDetail_trailer_adapter.swapCursor(data);
                    break;
                case REVIEW_LOADER:
                    mDetail_review_adapter.swapCursor(data);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        switch (id) {
            case TRAILER_LOADER:
                mDetail_trailer_adapter.swapCursor(null);
                break;
            case REVIEW_LOADER:
                mDetail_review_adapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);

            if (cursor.moveToFirst())
                buttonSta = true;
            else
                buttonSta = false;
            setFavBtnSta(buttonSta);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            buttonSta = false;
            setFavBtnSta(buttonSta);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            buttonSta = true;
            setFavBtnSta(buttonSta);
        }
    }
}
