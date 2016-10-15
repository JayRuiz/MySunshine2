package com.example.android.mysunshine2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;

import com.example.android.mysunshine2.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements  LoaderManager.LoaderCallbacks <Cursor> {


    private ShareActionProvider myShareActionProvider;
    private static final String hasTag = "#SunshineApp";

    private static final String TAG_DETAIL_F = "DetailFragment";


    private String mForcast;

    private static final int DETAIL_FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIN_SPEED =6;
    static final int COL_WEATHER_PRESSURE =7;
    static final int COL_WEATHER_DEGREE = 8;
    static final int COL_WEATHER_CONDITION_ID=9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
    }

    public void onCreat(Bundle savedInsstanceState){
        super.onCreate(savedInsstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView ;
    }

    public void onActivityCreated(Bundle savedInstanceStage){
        super.onActivityCreated(savedInstanceStage);
        getLoaderManager().initLoader(DETAIL_FORECAST_LOADER, null, this);
        //updateText();

    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.menu_detail, menu);

        //Get reference for SahreActionProvider and set the intent to share

        MenuItem shareItem = menu.findItem(R.id.action_share);
        myShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        myShareActionProvider.setShareIntent(createShareForecaseIntent());
    }

    private Intent createShareForecaseIntent(){

        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT, mForcast+hasTag);
        return myShareIntent;

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        Log.d(TAG_DETAIL_F, "Menu is selected id: " + item.getItemId());

        switch (item.getItemId()) {
            case R.id.action_settings_detail:
                openSetting();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSetting(){

        Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(settingIntent);
    }

     public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();
        if(intent == null){
            return null;
        }

             return new CursorLoader(getActivity(), intent.getData(), FORECAST_COLUMNS, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //weatherAdapter.swapCursor(data);
        if(!data.moveToFirst()){ return;}

//
//        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
//        String weatherDescription = data.getString(COL_WEATHER_DESC);
//        boolean isMetric = Utility.isMetric(getActivity());
//
//        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
//        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
//        mForcast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low );
//
//        TextView detailTextView = (TextView)getView().findViewById(R.id.detail_wetherInfo_textview);
//        detailTextView.setText(mForcast);

        Context detailContext = getActivity();
        boolean isMetric = Utility.isMetric(detailContext);

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        String dayName = Utility.getDayName(detailContext, data.getLong(COL_WEATHER_DATE));
        mFriendlyDateView.setText(dayName);
        String monthDay = Utility.getFormattedMonthDay(detailContext, data.getLong(COL_WEATHER_DATE));
        mDateView.setText(monthDay);

        String description =data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(description);

        String high = Utility.formatTemperature(detailContext, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        mHighTempView.setText(high);
        String low = Utility.formatTemperature(detailContext, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mLowTempView.setText(low);

        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
        float wind =  data.getFloat(COL_WEATHER_WIN_SPEED);
        float degree = data.getFloat(COL_WEATHER_DEGREE);
        mWindView.setText(Utility.getFormattedWind(detailContext, wind, degree));

        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        mForcast = String.format("%s - %s - %s/%s", monthDay, description, high, low );

        if(myShareActionProvider!=null){
            myShareActionProvider.setShareIntent(createShareForecaseIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
