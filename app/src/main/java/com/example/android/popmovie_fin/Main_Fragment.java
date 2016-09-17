package com.example.android.popmovie_fin;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popmovie_fin.popprovider.PopContract.MoviesStore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Main_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MovieImg_LOADER = 0;
    private Parcelable mState;
    private static final String SELECTED_KEY = "selected_position";

    private static final String LOG_TAG = Main_Fragment.class.getSimpleName();

    private MovieListAdapter mMovieListAdapter;

    private static final String[] MOVIE_COLUMNS = {
            MoviesStore.TABLE_NAME + "." + MoviesStore._ID,
            MoviesStore.TABLE_NAME + "." + MoviesStore.COLUMN_ID,
            MoviesStore.COLUMN_PATH
    };

    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_IMG = 2;

    public Main_Fragment() {
    }

    public interface Callback {
        boolean IsTwoPane();

        void onItemSelected(String mMovie_Id);
    }

    void onOrderChanged() {
        getLoaderManager().restartLoader(MovieImg_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MovieImg_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @BindView(R.id.MovieTable)
    GridView tableList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, root);

        if (((Callback) getActivity()).IsTwoPane())
            tableList.setNumColumns(3);
        else
            tableList.setNumColumns(2);


        mMovieListAdapter = new MovieListAdapter(getContext(), null, 0);
        tableList.setAdapter(mMovieListAdapter);

        tableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null)
                    ((Callback) getActivity()).onItemSelected(cursor.getString(COL_MOVIE_ID));
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mState = savedInstanceState.getParcelable(SELECTED_KEY);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mState = tableList.onSaveInstanceState();
        outState.putParcelable(SELECTED_KEY, mState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder;
        String orderType = Utility.getPreferredOrder(getContext());
        Uri uri;

        if (orderType.equals(getString(R.string.order_val_popular))) {
            sortOrder = MoviesStore.COLUMN_POPULAR + " DESC";
            uri = MoviesStore.buildMovieUriwithPop();
        } else if (orderType.equals(getString(R.string.order_val_toprated))) {
            sortOrder = MoviesStore.COLUMN_VOTE + " DESC";
            uri = MoviesStore.buildMovieUriwithTop();
        } else {
            sortOrder = null;
            uri = MoviesStore.buildMovieUriwithFav();
        }

        return new CursorLoader(getActivity(),
                uri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieListAdapter.swapCursor(data);
        if (mState != null)
            tableList.onRestoreInstanceState(mState);
        else if (mMovieListAdapter.getCount() > 0  && ((Callback) getActivity()).IsTwoPane())
            tableList.performItemClick(null, 0, 0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieListAdapter.swapCursor(null);
    }
}
