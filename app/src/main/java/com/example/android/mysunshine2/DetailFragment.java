package com.example.android.mysunshine2;

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

    private View detailFragment;

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
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;

    public DetailFragment() {
    }

    public void onCreat(Bundle savedInsstanceState){
        super.onCreate(savedInsstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        detailFragment = inflater.inflate(R.layout.fragment_detail, container, false);

        //
        // String weatherInfoDF = getActivity().getIntent().getStringExtra("info");
//
//        String weatherInfoDF = null;
//        Intent intent = getActivity().getIntent();
//
//        if (intent != null) {
//            weatherInfoDF = intent.getDataString();
//        }
//
//        updateText(weatherInfoDF);

        return detailFragment;
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
        myShareIntent.putExtra(Intent.EXTRA_TEXT, getWeatherFromTextField()+hasTag);
        return myShareIntent;

    }

    private String getWeatherFromTextField(){

        TextView view = (TextView)detailFragment.findViewById(R.id.detail_wetherInfo_textview);
        String weatherInfo = (String)view.getText();

        return weatherInfo;
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

    private void updateText(String wInfo){

        if(wInfo != null) {
            TextView detailText = (TextView) detailFragment.findViewById(R.id.detail_wetherInfo_textview);
            //String info = ((Detail)getActivity()).weatherInfoD;

            Log.d(TAG_DETAIL_F, "UpdateText is called: " + wInfo);

            detailText.setText(wInfo);
        }

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

        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mForcast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low );

        TextView detailTextView = (TextView)getView().findViewById(R.id.detail_wetherInfo_textview);
        detailTextView.setText(mForcast);

        if(myShareActionProvider!=null){
            myShareActionProvider.setShareIntent(createShareForecaseIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
