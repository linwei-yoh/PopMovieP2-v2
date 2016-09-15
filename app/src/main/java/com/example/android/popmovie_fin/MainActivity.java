package com.example.android.popmovie_fin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.popmovie_fin.popsync.PopSyncAdapter;

/*
1.图片大小w185可能需要根据屏幕大小修改
2.检查播放图标的大小
3
*/

public class MainActivity extends AppCompatActivity implements Main_Fragment.Callback{

    private boolean mTwoPane;
    private static final String DETAIL_FLAG = "DFTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private String mOrderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrderType = Utility.getPreferredOrder(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) != null){
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new Detail_Fragment(), DETAIL_FLAG)
                        .commit();
            }
        }
        else
            mTwoPane = false;

        PopSyncAdapter.initializeSyncAdapter(this);
        Log.d(LOG_TAG,"主活动启动,SyncAdapter初始化");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String orderType = Utility.getPreferredOrder( this );
        // update the location in our second pane using the fragment manager
        if (orderType != null && !orderType.equals(mOrderType)) {
            Main_Fragment main_fragment = (Main_Fragment)getSupportFragmentManager().findFragmentById(R.id.pop_fragment);
            if ( null != main_fragment )
                main_fragment.onOrderChanged();

            mOrderType = orderType;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_setting) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean IsTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onItemSelected(String mMovie_Id) {
        if (mTwoPane) {
            //how can i click the first item in the movielist after onLoadFinished?
            //this approach has some flaw when configChanges
            Detail_Fragment fragment = (Detail_Fragment)getSupportFragmentManager().findFragmentByTag(DETAIL_FLAG);
            fragment.resetId(mMovie_Id);

//            Bundle args = new Bundle();
//            args.putString(Detail_Fragment.DETAIL_KEY, mMovie_Id);
//
//            Detail_Fragment fragment = new Detail_Fragment();
//            fragment.setArguments(args);
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.movie_detail_container, fragment, DETAIL_FLAG)
//                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(Detail_Fragment.DETAIL_KEY, mMovie_Id);
            startActivity(intent);
        }
    }
}
