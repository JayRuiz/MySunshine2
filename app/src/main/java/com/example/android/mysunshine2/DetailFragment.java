package com.example.android.mysunshine2;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private View detailFragment;

    private ShareActionProvider myShareActionProvider;
    private static final String hasTag = "#SunshineApp";

    private static final String TAG_DETAIL_F = "DetailFragment";

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

        String weatherInfoDF = getActivity().getIntent().getStringExtra("info");

        updateText(weatherInfoDF);

        return detailFragment;
    }

    public void onActivityCreated(Bundle savedInstanceStage){
        super.onActivityCreated(savedInstanceStage);
        //updateText();

    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.menu_detail, menu);

        //Get reference for SahreActionProvider and set the intent to share

        MenuItem shareItem = menu.findItem(R.id.action_share);
        myShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT, getWeatherFromTextField()+hasTag);
        myShareActionProvider.setShareIntent(myShareIntent);
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


        TextView detailText = (TextView) detailFragment.findViewById(R.id.detail_wetherInfo_textview);
        //String info = ((Detail)getActivity()).weatherInfoD;

        Log.d(TAG_DETAIL_F, "UpdateText is called: "+ wInfo);

        detailText.setText(wInfo);

    }
}
