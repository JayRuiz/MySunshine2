package com.example.android.mysunshine2;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private final String FORECASTFRAGMENT_TAG = "FFTAG";

    ///Jay save preference
    private String mLocation;
    private boolean mMetric;
    private String mPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOG_TAG, "MainActivity onCreate is called");
        mLocation = Utility.getPreferredLocation(this);
        mMetric = Utility.isMetric(this);
        mPeriod = Utility.getPreferredPeriod(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            Log.d(LOG_TAG, "savedInstanceState is null");
           getSupportFragmentManager().beginTransaction().add(R.id.container, new WeatherListFragment(), FORECASTFRAGMENT_TAG).commit();
        }
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

        this.setTitle(appName+": "+ location);

        //if(location !=null && !location.equals(mLocation) ){
        if(isRefreshRequired()) {
            WeatherListFragment ff = (WeatherListFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if (ff != null){
                ff.onLocationChanged();
            }
            mLocation = location;
        }


    }


}
