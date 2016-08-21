package com.andrej.butic.neon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends Activity implements OnSeekBarChangeListener {

    // A generic String Array to populate the Spinner for picking a city to search in.
    String[] cities = {"Sarajevo", "Mostar", "Zenica", "Tuzla"};
    Spinner cityPicker;

    // Initialize SeekBar with variables for chosen distance and minimum distance.
    SeekBar sb;
    int kilometers;
    int minimumDistance = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the City and Distance tabs.
        final TabHost th = (TabHost) findViewById(R.id.searchMethod);
        th.setup();

        // Tab for searching by distance.
        TabHost.TabSpec distanceSearch = th.newTabSpec("distanceTag");
        distanceSearch.setContent(R.id.byDistance);
        distanceSearch.setIndicator("Search By Distance");
        th.addTab(distanceSearch);


        // Tab for searching by city.
        TabHost.TabSpec citySearch = th.newTabSpec("cityTag");
        citySearch.setContent(R.id.byCity);
        citySearch.setIndicator("Search By City");
        th.addTab(citySearch);

        // Make sure the initial tab selected displays the correct indicator
        th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);

        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("distanceTag")){
                    th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);
                    th.getTabWidget().getChildAt(1).setBackgroundResource(0);
                }else if(tabId.equals("cityTag")){
                    th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);
                    th.getTabWidget().getChildAt(0).setBackgroundResource(0);
                }
            }
        });

        // Change the color of the tab text to something more readable on a dark background.
        // Change the height of the indicator to blend in with the tab's border.
        for (int i = 0; i < th.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) th.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#d0d0e3"));
            th.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (80 * this.getResources().getDisplayMetrics().density);
        }

        // Set up a seek bar to decide on which distance to search from.
        sb = (SeekBar) findViewById(R.id.distanceBar);
        sb.setOnSeekBarChangeListener(this);

        //Make initial search distance 10km
        sb.setProgress(10);

        // Set up a spinner to pick a city. The city picked would determine the search results
        // if a corresponding remote MySQL database existed for it.
        // Since currently, such a database would contain only sample information, the results
        // shown will always be sample results from the 'sarajevo_venues' database.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, cities);
        cityPicker = (Spinner) findViewById(R.id.spinner);
        cityPicker.setAdapter(adapter);

        // Set up the search buttons.
        Button searchBtn1 = (Button) findViewById(R.id.searchBtn1);
        Button searchBtn2 = (Button) findViewById(R.id.searchBtn2);


        // Set Intents for both search buttons.
        searchBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent distanceIntent = new Intent(v.getContext(), DistanceSearchActivity.class);
                distanceIntent.putExtra("max_distance", kilometers);
                v.getContext().startActivity(distanceIntent);
            }
        });

        searchBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cityIntent = new Intent(v.getContext(), CitySearchActivity.class);
                v.getContext().startActivity(cityIntent);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Change the distance shown in kilometers in the distanceText TextView.
    private void changeDistance() {
        kilometers = sb.getProgress();
        TextView textView = (TextView) findViewById(R.id.distanceText);
        textView.setText(kilometers + " km");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        changeDistance();
        if (progress < minimumDistance) {
            // if seek bar value is lesser than min value then set min value to seek bar
            seekBar.setProgress(minimumDistance);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}
