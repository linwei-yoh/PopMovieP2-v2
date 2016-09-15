package com.example.android.popmovie_fin;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MovieListAdapter extends CursorAdapter {

    public MovieListAdapter(Context context,Cursor c,int flag){
        super(context,c,flag);
    }

    static class ViewHolder {
        @BindView(R.id.Poster_Path)
        ImageView posterImg;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_movie, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String baseUrl = Utility.getImgPathiWithSize(
                cursor.getString(Main_Fragment.COL_MOVIE_IMG)
                , Utility.ImgSize.w185);

        Picasso.with(context)
                .load(baseUrl)
                .fit()
                .into(viewHolder.posterImg);
    }

}
