package com.example.android.popmovie_fin;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Detail_Trailer_Adapter  extends CursorAdapter {

    public Detail_Trailer_Adapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_trailers, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView TrailerName = (TextView)view.findViewById(R.id.trailerName);
        String name = cursor.getString(Detail_Fragment.COL_TRAILER_NAME);
        TrailerName.setText(name);
    }
}
