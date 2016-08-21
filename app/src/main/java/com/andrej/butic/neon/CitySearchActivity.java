package com.andrej.butic.neon;

import android.app.Activity;
import android.content.Context;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CitySearchActivity extends Activity implements AdapterView.OnItemSelectedListener {

    // Variables for setting up date spinner and keeping selected information
    Spinner datePicker;
    String dayOfWeek;

    Date today;
    Date tomorrow;
    Date day3;
    Date day4;
    Date day5;
    Date day6;
    Date day7;


    // String variables to determine URL path for app
    String location= "http://neonappservice.com/neonservice/";
    String city = "sarajevo/";
    String type = "bar"; //default venue type is 'bar' because it is the first tab in the activity.

    // Variables for Expandable List Adapter
    HashMap<String, HashMap<String, String>> barDetails;
    ExpandableListAdapter barAdapter;
    ExpandableListView barListView;
    ExpandableListAdapter clubAdapter;
    ExpandableListView clubListView;
    List<Venue> barName;
    Context context = this;
    JSONArray jsonarr = null;
    boolean connectionCheck;

    // View variables
    Button button_openDrawer;
    Button button_showFilters;
    ToggleButton button_showFavorites;
    LinearLayout filterList;
    DrawerLayout settingsLayout;
    CheckBox chkEntryPrice;
    CheckBox chkOpeningHours;
    CheckBox chkCheapestBeer;
    CheckBox chkCheapestMixedDrink;
    CheckBox chkMusic;
    CheckBox chkCocktails;
    CheckBox chkDraught;

    // Boolean to determine if Navigation Drawer's search filters are visible
    boolean boolFiltersShown = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_search);

        //Initialize Navigation Drawer's content
        settingsLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        button_showFilters = (Button) findViewById(R.id.button_showSearchFilters);
        button_showFavorites = (ToggleButton) findViewById(R.id.button_showOnlyFavorites);
        button_openDrawer = (Button) findViewById(R.id.search_filters_button);
        filterList = (LinearLayout) findViewById(R.id.searchFilters);
        chkEntryPrice = (CheckBox) findViewById(R.id.chkEntryPrice);
        chkOpeningHours = (CheckBox) findViewById(R.id.chkOpeningHours);
        chkCheapestBeer = (CheckBox) findViewById(R.id.chkCheapestBeer);
        chkCheapestMixedDrink = (CheckBox) findViewById(R.id.chkCheapestMix);
        chkMusic = (CheckBox) findViewById(R.id.chkMusic);
        chkCocktails = (CheckBox) findViewById(R.id.chkCocktails);
        chkDraught = (CheckBox) findViewById(R.id.chkDraught);

        //Make sure all search filters are on by default
        chkEntryPrice.setChecked(true);
        chkOpeningHours.setChecked(true);
        chkCheapestBeer.setChecked(true);
        chkCheapestMixedDrink.setChecked(true);
        chkMusic.setChecked(true);
        chkCocktails.setChecked(true);
        chkDraught.setChecked(true);

        // Populate the top spinner to display dates up to 7 days in advance
        getDates();

        // Assign the ExpandableListViews
        barListView = (ExpandableListView) findViewById(R.id.barView);
        clubListView = (ExpandableListView) findViewById(R.id.clubView);

        // Set up the City and Distance tabs
        final TabHost th = (TabHost) findViewById(R.id.venueType);
        th.setup();

        // Tab for searching bars
        TabHost.TabSpec bars = th.newTabSpec("bar");
        bars.setContent(R.id.barTab);
        bars.setIndicator("Bars");
        th.addTab(bars);

        // Tab for searching clubs
        TabHost.TabSpec clubs = th.newTabSpec("club");
        clubs.setContent(R.id.clubTab);
        clubs.setIndicator("Clubs");
        th.addTab(clubs);

        // Make sure the initial tab selected displays the correct indicator
        th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);

        // Change the color of the tab text to something more readable on a dark background.
        for (int i = 0; i < th.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) th.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#d0d0e3"));
            th.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (80 * this.getResources().getDisplayMetrics().density);
        }

        /* On tab change update 'type' so the AsyncTask will know which venues to filter out of the list.
         * Also run a check on what filters have been disabled or enabled.
         * notifyIfDisconnected() will run an internet connection check and then either fill up a new ExpandableListView
         * or ask the user to find an internet connection.
         */
        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int i = th.getCurrentTab();
                if(tabId.equals("bar")) {
                    type = "bar";
                    th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);
                    th.getTabWidget().getChildAt(1).setBackgroundResource(0);
                    barListView.setAdapter((BaseExpandableListAdapter) null);
                    clubListView.setAdapter((BaseExpandableListAdapter) null);
                    notifyIfDisconnected();
                    filterCheck(barAdapter, clubAdapter);
                }else if(tabId.equals("club")){
                    type = "club";
                    th.getCurrentTabView().setBackgroundResource(R.drawable.indicator);
                    th.getTabWidget().getChildAt(0).setBackgroundResource(0);
                    barListView.setAdapter((BaseExpandableListAdapter)null);
                    clubListView.setAdapter((BaseExpandableListAdapter)null);
                    notifyIfDisconnected();
                    filterCheck(barAdapter, clubAdapter);

                }

            }
        });

        button_openDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsLayout.openDrawer(Gravity.LEFT);
            }
        });

        filterList.setVisibility(View.VISIBLE);
        // onClickListeners for Navigation Drawer's buttons
        button_showFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(!boolFiltersShown)
                {
                    filterList.setVisibility(View.VISIBLE);
                    boolFiltersShown = true;
                }
                else
                {
                    filterList.setVisibility(View.GONE);
                    boolFiltersShown = false;
                }
            }
        });
        button_showFavorites.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                if(button_showFavorites.isChecked()){
                    barAdapter.filterFavorites();
                    clubAdapter.filterFavorites();
                }
                else{
                    barAdapter.repopulate();
                    clubAdapter.repopulate();
                }
            }
        });


        // Set onCheckedChangeListener for each of the filter checkboxes
        chkEntryPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Entry Price", isChecked);
                clubAdapter.childFilter("Entry Price", isChecked);
            }
        });
        chkOpeningHours.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Opening Hours", isChecked);
                clubAdapter.childFilter("Opening Hours", isChecked);
            }
        });
        chkCheapestBeer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Cheapest Beer", isChecked);
                clubAdapter.childFilter("Cheapest Beer", isChecked);
            }
        });
        chkCheapestMixedDrink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Cheapest Mixed Drink", isChecked);
                clubAdapter.childFilter("Cheapest Mixed Drink", isChecked);
            }
        });
        chkCocktails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Cocktails", isChecked);
                clubAdapter.childFilter("Cocktails", isChecked);
            }
        });
        chkDraught.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Draught Beer", isChecked);
                clubAdapter.childFilter("Draught Beer", isChecked);
            }
        });
        chkMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                barAdapter.childFilter("Music", isChecked);
                clubAdapter.childFilter("Music", isChecked);
            }
        });

    }

    /* Called at the end of NetConnect() to check if any of the filter checkboxes have been unchecked. */
    public void filterCheck(ExpandableListAdapter barAdapter, ExpandableListAdapter clubAdapter){

                barAdapter.childFilter("Entry Price", chkEntryPrice.isChecked());
                clubAdapter.childFilter("Entry Price", chkEntryPrice.isChecked());

                barAdapter.childFilter("Opening Hours", chkOpeningHours.isChecked());
                clubAdapter.childFilter("Opening Hours", chkOpeningHours.isChecked());

                barAdapter.childFilter("Cheapest Beer", chkCheapestBeer.isChecked());
                clubAdapter.childFilter("Cheapest Beer", chkCheapestBeer.isChecked());

                barAdapter.childFilter("Cheapest Mixed Drink", chkCheapestMixedDrink.isChecked());
                clubAdapter.childFilter("Cheapest Mixed Drink", chkCheapestMixedDrink.isChecked());

                barAdapter.childFilter("Music", chkMusic.isChecked());
                clubAdapter.childFilter("Music", chkMusic.isChecked());

                barAdapter.childFilter("Cocktails", chkCocktails.isChecked());
                clubAdapter.childFilter("Cocktails", chkCocktails.isChecked());

                barAdapter.childFilter("Draught Beer", chkDraught.isChecked());
                clubAdapter.childFilter("Draught Beer", chkDraught.isChecked());

    }

    /* Checks if device is connected to the internet. Keeps ASyncTask from crashing app. */
    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean connected = false;
        if (info != null && info.isConnectedOrConnecting()) {
            connected = true;
        }
        return connected;
    }

    /* Short method to run an AsyncTask if a connection is present.
     * Will instead notify the user that the app needs a connection if one is not present.
     */
    private void notifyIfDisconnected(){
        connectionCheck = isConnected(context);
        if (connectionCheck){
            new NetConnect().execute();
        }
        else{
            Toast toast = Toast.makeText(context, "You are disconnected from the internet. Unable to show venue information.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /* Populate the date picker and set some variables accordingly */
    private void getDates() {
        Calendar calendar = Calendar.getInstance();

        today = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        tomorrow = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        day3 = calendar.getTime();
        String d3 = new SimpleDateFormat("EEEE MMM-dd").format(day3);

        calendar.add(Calendar.DATE, 1);
        day4 = calendar.getTime();
        String d4 = new SimpleDateFormat("EEEE MMM-dd").format(day4);

        calendar.add(Calendar.DATE, 1);
        day5 = calendar.getTime();
        String d5 = new SimpleDateFormat("EEEE MMM-dd").format(day5);

        calendar.add(Calendar.DATE, 1);
        day6 = calendar.getTime();
        String d6 = new SimpleDateFormat("EEEE MMM-dd").format(day6);

        calendar.add(Calendar.DATE, 1);
        day7 = calendar.getTime();
        String d7 = new SimpleDateFormat("EEEE MMM-dd").format(day7);

        String[] days = {"Today", "Tomorrow", d3, d4, d5, d6, d7};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, days);
        datePicker = (Spinner) findViewById(R.id.spinner);
        datePicker.setAdapter(adapter);
        datePicker.setOnItemSelectedListener(this);
    }

    /* Build a URL using previously set variables and connect to the database via ASyncTask
     * Reconnect to the database and find new data every time a new date is picked.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int picked = datePicker.getSelectedItemPosition();
        switch(picked) {
            case 0:
                dayOfWeek = new SimpleDateFormat("EEE").format(today);
                notifyIfDisconnected();
                break;
            case 1:
                dayOfWeek = new SimpleDateFormat("EEE").format(tomorrow);
                notifyIfDisconnected();
                break;
            case 2:
                dayOfWeek = new SimpleDateFormat("EEE").format(day3);
                notifyIfDisconnected();
                break;
            case 3:
                dayOfWeek = new SimpleDateFormat("EEE").format(day4);
                notifyIfDisconnected();
                break;
            case 4:
                dayOfWeek = new SimpleDateFormat("EEE").format(day5);
                notifyIfDisconnected();
                break;
            case 5:
                dayOfWeek = new SimpleDateFormat("EEE").format(day6);
                notifyIfDisconnected();
                break;
            case 6:
                dayOfWeek = new SimpleDateFormat("EEE").format(day7);
                notifyIfDisconnected();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* Populate the expandable list view with data from an online database */
    private class NetConnect extends AsyncTask<String, Void, JSONArray> {

        String url = location + city + "venues_" + dayOfWeek + ".php";
        @Override
        protected JSONArray doInBackground(String... args) {

            // Parse JSON array from URL
            return JParser.getJSONFromUrl(url);
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            try {
                // Getting JSON Array from URL, then initializing children and groups for expListView
                jsonarr = json;
                barName = new ArrayList<Venue>();
                barDetails = new HashMap<String, HashMap<String, String>>();
                int t = 0;
                String venueType = type;
                for (int i = 0; i < jsonarr.length(); i++) {
                    JSONObject c = jsonarr.getJSONObject(i);
                    // Storing  JSON item in a Variable
                    if(c.getString("Type").equals(venueType)) {
                        String name = c.getString("Name");
                        HashMap<String, String> detailList = new HashMap<>();

                        detailList.put("Entry Price", c.getString("Entry Price"));
                        detailList.put("Opening Hours", c.getString("Opening Hours"));
                        detailList.put("Cheapest Beer", c.getString("Cheapest Beer"));
                        detailList.put("Cheapest Mixed Drink", c.getString("Cheapest Mixed Drink"));
                        detailList.put("Special Offer 1", c.getString("Special Offer 1"));
                        detailList.put("Special Offer 2", c.getString("Special Offer 2"));
                        detailList.put("Special Offer 3", c.getString("Special Offer 3"));
                        detailList.put("Music", c.getString("Music"));
                        detailList.put("Cocktails", c.getString("Cocktails"));
                        detailList.put("Draught Beer", c.getString("Draught Beer"));
                        detailList.put("Map", c.getString("Map"));
                        detailList.put("Facebook", c.getString("Facebook"));
                        Venue venue = new Venue(name, detailList);

                        barName.add(venue);

                        barDetails.put(barName.get(t).name, detailList);
                        Log.d("Type is", c.getString("Type"));

                        t++;
                    }
                }

                //Setting list adapters.
                barAdapter = new ExpandableListAdapter(context, barName, barDetails);
                clubAdapter = new ExpandableListAdapter(context, barName, barDetails);

                //Calls filterCheck() to make sure child results get filtered automatically according to previous choice.
                filterCheck(barAdapter, clubAdapter);

                //Assign adapters to ExpandableListViews.
                barListView.setAdapter(barAdapter);
                clubListView.setAdapter(clubAdapter);

                //If 'Show Only Favorites' button is still selected, results will be automatically filtered with this statement.
                if(button_showFavorites.isChecked()){
                    barAdapter.filterFavorites();
                    clubAdapter.filterFavorites();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }
}
