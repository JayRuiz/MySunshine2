package com.example.android.mysunshine2;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.mysunshine2.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements WeatherListFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    //private final String FORECASTFRAGMENT_TAG = "FFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    ///Jay save preference
    private String mLocation;
    private boolean mMetric;
    private String mPeriod;

    private boolean mTwoPane;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "MainActivity onCreate is called");
        mLocation = Utility.getPreferredLocation(this);
        mMetric = Utility.isMetric(this);
        mPeriod = Utility.getPreferredPeriod(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        if(savedInstanceState == null){
            Log.d(LOG_TAG, "savedInstanceState is null");
           getSupportFragmentManager().beginTransaction().add(R.id.container, new WeatherListFragment(), FORECASTFRAGMENT_TAG).commit();
        }
        */

        if(findViewById(R.id.weather_detail_container)!=null){

            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else{
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        //Jay 11/09/2016 let weatherlistfragment know if this will be TodayLayout or normal

        WeatherListFragment forecastFragment = (WeatherListFragment)getSupportFragmentManager().findFragmentById(R.id.WeatherFragment);
        forecastFragment.setUseTodayLayout(!mTwoPane);

        // jay 02/20/2017
        SunshineSyncAdapter.initializeSyncAdapter(this);

    }

    private boolean isRefreshRequired(){

        //Jay setting: location, metric, period
        String location = Utility.getPreferredLocation(this);
        if(location!=null && !location.equals(mLocation)){
            return true;
        }

        boolean metric = Utility.isMetric(this);
        if(mMetric != metric){
            return true;
        }

        String period = Utility.getPreferredPeriod(this);
        return period != null && !mPeriod.equals(period);

    }


    protected void onResume(){
        super.onResume();

        Log.d(LOG_TAG, "onResume is called");

        String location = Utility.getPreferredLocation(this);

        String appName = this.getString(R.string.app_name);

        //this.setTitle(appName+": "+ location);

        //if(location !=null && !location.equals(mLocation) ){
        if(isRefreshRequired()) {
            WeatherListFragment ff = (WeatherListFragment)getSupportFragmentManager().findFragmentById(R.id.WeatherFragment);
            if (ff != null){
                ff.onLocationChanged();
            }

            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null){
                df.onLocationChanged(location);
            }
            mLocation = location;
        }


    }

    public void onItemSelected(Uri contentUri){
        if(mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG).commit();

        }
        else {
            Intent intent = new Intent(this, Detail.class).setData(contentUri);
            startActivity(intent);
        }
    }


}
