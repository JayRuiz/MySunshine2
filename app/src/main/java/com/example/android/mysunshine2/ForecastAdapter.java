package com.example.android.mysunshine2;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.mysunshine2.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private int count = 0;

    private final int VIEW_TYPE_COUNT = 2;
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY= 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    /**
     * Prepare the weather high/lows for presentation.
     */
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(getActivity(),high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
//        return highLowStr;
//    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(WeatherListFragment.COL_WEATHER_MAX_TEMP),
//                cursor.getDouble(WeatherListFragment.COL_WEATHER_MIN_TEMP)
//                );
//
//        return Utility.formatDate(cursor.getLong(WeatherListFragment.COL_WEATHER_DATE))+
//                " - " + cursor.getString(WeatherListFragment.COL_WEATHER_DESC)+
//                " - " + highAndLow;
//    }

    public int getItemViewType(int position){
        return (position ==0)? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public int getViewTypeCount(){
        return VIEW_TYPE_COUNT;

    }
    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Log.d(LOG_TAG,"newView is called");

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if(viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        }else {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Log.d(LOG_TAG,"bindView is called: ");

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());

        int weatherId = cursor.getInt(WeatherListFragment.COL_WEATHER_CONDITION_ID);

        //Jay if viewtype is today, we have to use Art
        // if viewtype is future, we have to use ic
        if(viewType == VIEW_TYPE_TODAY) {
            viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        }else {
            viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }


        long dateInMillis = cursor.getLong(WeatherListFragment.COL_WEATHER_DATE);
        //TextView dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context,dateInMillis));

        String description = cursor.getString(WeatherListFragment.COL_WEATHER_DESC);
        //TextView descView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        viewHolder.descriptionView.setText(description);

        boolean isMetric = Utility.isMetric(context);

        double high = cursor.getDouble(WeatherListFragment.COL_WEATHER_MAX_TEMP);
        //TextView highText = (TextView)view.findViewById(R.id.list_item_high_textview);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high, isMetric));

        double low = cursor.getDouble(WeatherListFragment.COL_WEATHER_MIN_TEMP);
        //TextView lowText = (TextView)view.findViewById(R.id.list_item_low_textview);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));

    }


    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}