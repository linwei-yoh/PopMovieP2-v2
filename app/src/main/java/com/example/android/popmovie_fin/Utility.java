package com.example.android.popmovie_fin;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    public enum ImgSize{
        w185,w342,w500,w780,original;
    }

    public static String getPreferredOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.order_type_key),context.getString(R.string.order_val_popular));
    }

    //size
    public static String getImgPathiWithSize(String imgPath,ImgSize size){
        return "http://image.tmdb.org/t/p/" + size + imgPath;
    }

    public static String getImgPathiWithSize(String imgPath){
        return "http://image.tmdb.org/t/p/w185"  + imgPath;
    }

    //youtube
    public static Uri getTarilerUri(String path){
       return Uri.parse("http://youtube.com/watch?v=" + path);
    }

    //对不带List<Class>仅一层数据 的Json数据 进行转化 ,返回一份ContentValues
    public static ContentValues objectToContentValues(Object object) {
        ContentValues cv = new ContentValues();

        for (Field field : object.getClass().getFields()) {
            Object value = null;
            try {
                value = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (value instanceof Double || value instanceof Integer || value instanceof String || value instanceof Boolean
                    || value instanceof Long || value instanceof Float || value instanceof Short) {
                cv.put(field.getName(), value.toString());
            } else if (value instanceof Date) {
                cv.put(field.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)value));
            }
        }
        return cv;
    }
}

