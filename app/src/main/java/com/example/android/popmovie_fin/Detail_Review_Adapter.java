package com.example.android.popmovie_fin;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class Detail_Review_Adapter extends CursorAdapter {

    public Detail_Review_Adapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_reviews, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView R_Author = (TextView)view.findViewById(R.id.Review_Author);
        TextView R_Context = (TextView) view.findViewById(R.id.Review_Content);
        String authname = cursor.getString(Detail_Fragment.COL_REVIEW_AUTHOR);
        R_Author.setText(authname + ":");
        R_Context.setText(cursor.getString(Detail_Fragment.COL_REVIEW_CONTENT));
    }

}
