package com.example.android.mysunshine2;

//import android.app.Fragment;
//import android.app.LoaderManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.android.mysunshine2.data.WeatherContract;

//import com.example.android.mysunshine2.service.SunshineService;
import com.example.android.mysunshine2.sync.SunshineSyncAdapter;


/**
 * Created by Jeaun on 7/13/2016.
 */
public class WeatherListFragment extends Fragment implements LoaderManager.LoaderCallbacks <Cursor> {


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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    //private ArrayAdapter<String> weatherAdapter;
    private ForecastAdapter weatherAdapter;

    private static final String TAG = "WeatherListFragment";
    private View rootView;
    private final boolean useFake = false;
    private static final String MAX_DAYS = "16";
    private static final int FORECAST_LOADER = 0;

    private ListView mListView;

    private boolean mUseTodayLayout = true;

    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {

        /**
         * DetailFragmentCallback for when an item has been selected.
        */

        public void onItemSelected(Uri dateUri);
    }

    public void onCreat(Bundle savedInsstanceState){
        super.onCreate(savedInsstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
           // Inflate the layout for this fragment

        Log.i(TAG, "WeatherListFragment onCreatView is called --- 2 ");

        setHasOptionsMenu(true);

        weatherAdapter = new ForecastAdapter(getActivity(), null, 0);

        rootView = inflater.inflate(R.layout.weather_list, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(weatherAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());

                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)));

                }
                // Step 1: Jay - Save position when selected
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.

        // Step 2-1:  Jay Read position from Bundle when re-creatView after rotation.
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            
        }

        // Jay 11/09/2016 added to make plane UI for the two pane of table UI
        weatherAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }

    // Jay 11/09/2016 added for two panes of table UI. This is called by MainActivity
    // when it is created and when it judges if this is two pane UI or not
    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if(weatherAdapter!=null){
            weatherAdapter.setUseTodayLayout(mUseTodayLayout);
        }

    }
    public void onSaveInstanceState(Bundle outState){

        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        // Step 2-2: Save position before ratating

        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void onStart(){
        super.onStart();
        executeTask();

    }


    private void toastMessage(String weatherInfo) {
        Context context = getActivity();
        CharSequence text = weatherInfo;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }



    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.forecastfragment, menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        Log.d(TAG, "Menu is selected id: " + item.getItemId());

        switch (item.getItemId()) {
            case R.id.action_refresh:
                executeTask();
                return true;
            case R.id.action_settings_main:
                openSetting();
                return true;
            case R.id.action_open_map:
                openMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onLocationChanged(){

        executeTask();
        //getLoaderManager().
        Log.d(TAG, "onLocationChanged() is called. Need to reset the list view");


        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void executeTask(){
        //String queryInfo = "http://api.openweathermap.org/data/2.5/forecast/daily?q=98027&mode=json&units=metric&cnt=7";

        /*Jay 02/20/2017 Removed by adding SyncAdapter.
        String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        String location = getSharedPreferenceLocation();
        String unit = findTempUnit(getSharedPreferenceTempUnit());
        String period = getSharedPreferencePeriod();

        //String key = MyOpenWeatherMapApiKey;

        String [] queryInfo = {"http", "api.openweathermap.org","data","2.5","forecast","daily",
                location,"json",unit,period,"a0b1dba4292d37df112985f4182f153b"
        };
        */
        // Jay: change by ForecastAdapter
        //new FetchWeatherTask(getActivity(), weatherAdapter).execute(queryInfo);

        /* Jay 11/09/2016 change code for the SunshineSerivice
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(queryInfo);
        */

        startSunshineService();

    }

    private void startSunshineService(){


//        Intent intent = new Intent(getActivity(), SunshineService.class);
//        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,  queryInfo );
//        getActivity().startService(intent);
//
        // Jay need to add alarm 11/15/2016
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, queryInfo);
//
//        //Wrap in a pending intent which only fire once
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//
//        Log.d(TAG, "Kick the Alarm as 5 sec" );
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, pi);

        //Jay 02/14/2017
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private String findTempUnit(String tempUnit){
        if(tempUnit.equals("1"))
            return "imperial";
        else if(tempUnit.equals("2"))
            return "metric";
        else
            return "imperial";

    }

    private String getSharedPreferencePeriod(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String period = sharedPref.getString(getString(R.string.pref_period_key), getString(R.string.pref_period_default));
        Log.d(TAG, "Period preference: "+ period);

        if(Integer.parseInt(period) > 16) {
            toastMessage(MAX_DAYS +" Days are maximum!!");
            period = new String(MAX_DAYS);
        }

        return period;
    }

    private String getSharedPreferenceTempUnit(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unit = sharedPref.getString(getString(R.string.pref_temp_key), getString(R.string.pref_temp_default));
        Log.d(TAG, "Temperature preference: "+ unit);

        return unit;
    }
    private String getSharedPreferenceLocation(){

        //SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Log.d(TAG, "Location preference: "+ location);

        return location;
    }
    private void openSetting(){

        Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void openMap(){

        String location = getSharedPreferenceLocation();

        Intent geoIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0.0?q="+location));
        startActivity(geoIntent);
    }


     // Jay: added for Loader
    public void onActivityCreated(Bundle savedInsstanceState){
        super.onActivityCreated(savedInsstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE+ " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished is called");
        weatherAdapter.swapCursor(data);
        // If we don't need to restart the loader, and there's a desired position to restore
        // to, do so now.
        // Step 3: Jay restore the saved position when loading is finished.
        if(mPosition != ListView.INVALID_POSITION){
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset is called");
        weatherAdapter.swapCursor(null);
    }
}
