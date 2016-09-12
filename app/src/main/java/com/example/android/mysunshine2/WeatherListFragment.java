package com.example.android.mysunshine2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

/**
 * Created by Jeaun on 7/13/2016.
 */
public class WeatherListFragment extends Fragment{

    private ArrayAdapter<String> weatherAdapter;
    private static final String TAG = "WeatherListFragment";
    private View rootView;
    private final boolean useFake = false;
    private static final String MAX_DAYS = "16";

    public void onCreat(Bundle savedInsstanceState){
        super.onCreate(savedInsstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
           // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.weather_list, container, false);

        Log.i(TAG, "WeatherListFragment onCreatView is called --- 2 ");

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        /*
            String[] data = {
                    "Mon 6/23 - Sunny - 31/17",
                    "Tue 6/24 - Foggy - 21/8",
                    "Wed 6/25 - Cloudy - 22/17",
                    "Thurs 6/26 - Rainy - 18/11",
                    "Fri 6/27 - Foggy - 21/10",
                    "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                    "Sun 6/29 - Sunny - 20/7"
            };
             List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        */
        List<String> weekForecast = new ArrayList<String>();
         /* This ArrayAdapter to take TextView's layout and id */
        weatherAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        ListView listListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listListView.setAdapter(weatherAdapter);

        listListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View myView, int position, long id) {

                //toastMessage((String) adapterView.getItemAtPosition(position));
                showDetail((String) adapterView.getItemAtPosition(position));
            }
        });


        executeTask();

        return rootView;
    }

    public void onStart(){
        super.onStart();
        executeTask();

    }
    private void showDetail (String weatherInfo){

        Intent intent = new Intent(getActivity(), Detail.class);
        //if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
        intent.putExtra("info", weatherInfo);
            startActivity(intent);
        //}
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
        //menu.clear();
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

    private void executeTask(){
        //String queryInfo = "http://api.openweathermap.org/data/2.5/forecast/daily?q=98027&mode=json&units=metric&cnt=7";

        String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        String location = getSharedPreferenceLocation();
        String unit = findTempUnit(getSharedPreferenceTempUnit());
        String period = getSharedPreferencePeriod();

        //String key = MyOpenWeatherMapApiKey;

        String [] queryInfo = {"http", "api.openweathermap.org","data","2.5","forecast","daily",
                location,"json",unit,period,"a0b1dba4292d37df112985f4182f153b"
        };
        new FetchWeatherTask(getActivity(), weatherAdapter).execute(queryInfo);
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

    private String[] makeWeatherInfoRequest(String queryInfo[]){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String[] forecastJsonStrArray = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            /*String apiKey = "&APPID=a0b1dba4292d37df112985f4182f153b";
            URL url = new URL(queryInfo.concat(apiKey));
            Log.d(TAG,"Request URL1: "+queryInfo.concat(apiKey));
            */
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(queryInfo[0])
                    .authority(queryInfo[1])
                    .appendPath(queryInfo[2])
                    .appendPath(queryInfo[3])
                    .appendPath(queryInfo[4])
                    .appendPath(queryInfo[5])
                    .appendQueryParameter("q", queryInfo[6])
                    .appendQueryParameter("mode", queryInfo[7])
                    .appendQueryParameter("units", queryInfo[8])
                    .appendQueryParameter("cnt", queryInfo[9])
                    .appendQueryParameter("APPID", queryInfo[10]);

            String myUrl = builder.build().toString();
            int period = Integer.parseInt(queryInfo[9]);

            Log.d(TAG,"Request URL1: "+myUrl);

            URL url = new URL(myUrl);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                Log.d(TAG, "InputStream is null");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
                Log.d(TAG, line+"\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.d(TAG, "Buffer lenght is 0");
                return null;
            }
            //forecastJsonStr[0] = buffer.toString();

            forecastJsonStrArray = getWeatherDataFromJson(buffer.toString(), period);

        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception", e);
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("WeatherListFragment", "Error closing stream", e);
                }
            }
        }

        return forecastJsonStrArray;

    }
//
//    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        protected String[] doInBackground(String... queryInfo) {
//
//            return makeWeatherInfoRequest(queryInfo);
//        }
//
//        protected void onPostExecute(String[] weatherList) {
//
//            if (weatherList != null) {
//                weatherAdapter.clear();
//                for(String dayForecastStr : weatherList ) {
//                    weatherAdapter.add(dayForecastStr);
//                }
//            } else {
//                Log.d(TAG, "There is no info returned from Weather Server");
//            }
//        }
//    }

        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }


}
