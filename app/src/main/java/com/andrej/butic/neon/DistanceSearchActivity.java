package com.andrej.butic.neon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DistanceSearchActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    // Internet connectivity and location variables
    private LocationManager lm;
    private LocationListener ll;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private boolean venueInfoReceived = false;
    private boolean connectionCheck;
    private GoogleMap gMap;
    private LatLng currentLocation;

    // Variables for venue marker information
    private String markerTitle;
    private String address;
    private Context context = this;
    private List<String> venueInfo = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private ArrayAdapter<String> dateAdapter;
    private Marker m;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private int maxDistance;

    // Initialize Buttons on map
    private ToggleButton bar_btn;
    private ToggleButton club_btn;
    private Button facebook_btn;
    private String facebook_url;

    private boolean showBars = true;
    private boolean showClubs = true;
    private String closed = "Closed on this day";

    // Animations for facebook_btn
    private Animation slideIn;
    private Animation slideOut;

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

    double longitude;
    double latitude;

    // JSONArray to store parsed data from remote database
    JSONArray jsonarr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distance_search);

        //Assign the map.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Check if an internet connection is present
        connectionCheck = isConnected(context);

        //Acquire calendar information from phone.
        getDates();

        //Set up the Facebook button and its animations.
        facebook_btn = (Button) findViewById(R.id.fb_btn);
        slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        facebook_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent distanceIntent = new Intent(facebookIntent(context.getPackageManager(), facebook_url));
                startActivity(distanceIntent);
            }
        });


        // Set up both ToggleButtons, which when clicked, will determine whether the map will show
        // only markers for bars, clubs, both, or neither.
        bar_btn = (ToggleButton)findViewById(R.id.bar_btn);
        club_btn = (ToggleButton)findViewById(R.id.club_btn);
        bar_btn.setChecked(true);
        club_btn.setChecked(true);

        // Set OnClickListeners for both ToggleButtons. If a button is clicked and there is no internet
        // connection present, inform the user that the map markers cannot be changed.
        bar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bar_btn.isChecked()){
                    showBars=true;
                    if(connectionCheck){
                        new PlaceMarkers().execute();
                    }else{
                        Toast toast = Toast.makeText(context, "Please reconnect to the internet to refresh map markers", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }else{
                    showBars=false;
                    if(connectionCheck){
                        new PlaceMarkers().execute();
                    }else{
                        Toast toast = Toast.makeText(context, "Please reconnect to the internet to refresh map markers", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });
        club_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(club_btn.isChecked()){
                    showClubs=true;
                    if(connectionCheck){
                        new PlaceMarkers().execute();
                    }else{
                        Toast toast = Toast.makeText(context, "Please reconnect to the internet to refresh map markers", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }else{
                    showClubs=false;
                    if(connectionCheck){
                        new PlaceMarkers().execute();
                    }else{
                        Toast toast = Toast.makeText(context, "Please reconnect to the internet to refresh map markers", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });

        // Get the maximum distance that user wants to search from their current location. Selected in previous activity.
        Bundle extras = getIntent().getExtras();
        maxDistance = extras.getInt("max_distance");

        // Set up location listener and manager for determining user's location
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }

    // Activity checks if device is connected to the internet. Keeps AsyncTask from crashing app.
    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean connected = false;
        if (info != null && info.isConnectedOrConnecting()) {
            connected = true;
        }
        return connected;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        enableMyLocation();
    }

    /* Once Location Permission has been granted, find the user's location by LatLng
     * And set appropriate Map functions
     */
    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            Toast.makeText(DistanceSearchActivity.this, "You don't have Permission to access Maps", Toast.LENGTH_SHORT).show();
        } else if (gMap != null) {
            // Access to the location has been granted to the app.

            // Find user's location
            gMap.setMyLocationEnabled(true);
            gMap.setPadding(0,160,0,0);
            lm.requestLocationUpdates("gps", 2000, 10, ll);
            Location location = getLastKnownLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currentLocation=latLng;

            //Set different functions for OnClickListeners and InfoWindows in gMap
            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    markerTitle = marker.getTitle();
                    m = marker;
                    venueInfoReceived = false;
                    // Animate facebook_btn to come into view
                    if(facebook_btn.getVisibility()==View.GONE) {
                        facebook_btn.setVisibility(View.VISIBLE);
                        facebook_btn.startAnimation(slideIn);
                    }
                    return false;
                }
            });

            // When the map is clicked, any marker's info window will be hidden, so set 'm' to null and remove facebook_btn
            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    m=null;
                    // Animate facebook_btn to slide out of view
                    facebook_btn.startAnimation(slideOut);
                    facebook_btn.setVisibility(View.GONE);
                }
            });

            gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {

                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Initialize all the Info Window Views, set their content, and set 'm' to the current marker.
                    final View v = getLayoutInflater().inflate(R.layout.info_window, null);
                    m = marker;
                    TextView venueName_tv = (TextView) v.findViewById(R.id.venue_name);
                    TextView venueType_tv = (TextView) v.findViewById(R.id.venue_type);
                    ListView venueInfo_lv = (ListView) v.findViewById(R.id.venue_info);
                    arrayAdapter = new ArrayAdapter<String>(context, R.layout.distance_list_item, venueInfo );
                    venueInfo_lv.setAdapter(arrayAdapter);
                    venueName_tv.setText(marker.getTitle());
                    venueType_tv.setText(address);
                    if(m.getSnippet().equals(closed)){
                        venueType_tv.setText(closed);
                    }

                    if(!venueInfoReceived) {
                        /* If the information for this venue has not been yet collected from the database
                         * Then first check if an internet connection is still present to prevent a crash
                         * And run GetMarkerInfo if it is present
                         */
                        connectionCheck = isConnected(context);
                        if(connectionCheck){
                            new GetMarkerInfo(new ListCompletedListener() {
                                @Override
                                public void onListCompleted(List list) {
                                    // If a marker is selected, reopen the info window to display new information
                                    if (m != null && m.isInfoWindowShown()) {
                                        m.showInfoWindow();
                                    }
                                }
                            }).execute();
                        } else {
                            // Connection to the internet is not present
                            Toast toast = Toast.makeText(context, "You are disconnected from the internet. Venue information shown might not be correctly updated", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                    return v;
                }
            });

            // Zoom to user's location
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            gMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /* Method for determining user's last location in order to track them on the map */
    private Location getLastKnownLocation() {
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            Toast.makeText(DistanceSearchActivity.this, "You don't have Permission to access Maps", Toast.LENGTH_SHORT).show();
        } else if (gMap != null) {
        for (String provider : providers) {
            Location l = lm.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        }
        return bestLocation;

    }

    /* Displays a dialog with error message explaining that the location permission is missing */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    /* Calculate how far away a certain venue is from the user
     * Used with every venue collected from the remote database 'locations'
     */
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    /* Populate the date picker and set some variables accordingly */
    private void getDates() {
        Calendar calendar = Calendar.getInstance();

        today = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        tomorrow = calendar.getTime();

        calendar.add(Calendar.DATE, 1);
        day3 = calendar.getTime();
        String dan3 = new SimpleDateFormat("EEEE MMM-dd").format(day3);

        calendar.add(Calendar.DATE, 1);
        day4 = calendar.getTime();
        String dan4 = new SimpleDateFormat("EEEE MMM-dd").format(day4);

        calendar.add(Calendar.DATE, 1);
        day5 = calendar.getTime();
        String dan5 = new SimpleDateFormat("EEEE MMM-dd").format(day5);

        calendar.add(Calendar.DATE, 1);
        day6 = calendar.getTime();
        String dan6 = new SimpleDateFormat("EEEE MMM-dd").format(day6);

        calendar.add(Calendar.DATE, 1);
        day7 = calendar.getTime();
        String dan7 = new SimpleDateFormat("EEEE MMM-dd").format(day7);

        String[] days = {"Today", "Tomorrow", dan3, dan4, dan5, dan6, dan7};

        dateAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, days);
        datePicker = (Spinner) findViewById(R.id.date_spinner);
        datePicker.setAdapter(dateAdapter);
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
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 1:
                dayOfWeek = new SimpleDateFormat("EEE").format(tomorrow);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 2:
                dayOfWeek = new SimpleDateFormat("EEE").format(day3);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 3:
                dayOfWeek = new SimpleDateFormat("EEE").format(day4);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 4:
                dayOfWeek = new SimpleDateFormat("EEE").format(day5);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 5:
                dayOfWeek = new SimpleDateFormat("EEE").format(day6);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
            case 6:
                dayOfWeek = new SimpleDateFormat("EEE").format(day7);
                venueInfoReceived=false;
                notifyIfDisconnected();
                if(m!=null && m.isInfoWindowShown()){
                    m.showInfoWindow();
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* Short method to run an AsyncTask if a connection is present.
     * Will instead notify the user that the app needs a connection if one is not present.
     */
    private void notifyIfDisconnected(){
        connectionCheck = isConnected(context);
        if (connectionCheck){
            new PlaceMarkers().execute();
        }
        else{
            Toast toast = Toast.makeText(context, "You require an internet connection " +
                    "to display venue locations and information.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /* ASyncTask to connect to 'locations' table and determine placement of map markers */
    private class PlaceMarkers extends AsyncTask<String, Void, JSONArray>{

        @Override
        protected JSONArray doInBackground(String... params) {

            // Parse JSON array from URL
            return JParser.getJSONFromUrl("http://neonappservice.com/neonservice/sarajevo/locations.php");
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            jsonarr=jsonArray;
            try {
                if(markerList.isEmpty()){
                    /* If there are no markers on the map, connect to the 'locations' database
                     * and fill up the map using latitude and longitude. Also make sure that each
                     * marker contains basic info relating to the venue it is pointing to */
                    for (int i = 0; i < jsonarr.length(); i++){
                        JSONObject c = jsonarr.getJSONObject(i);
                        /*Calculate distance between all the markers loaded
                        * And place only the ones which are less than the
                        * maxDistance value, which was passed along from the previous activity*/
                        if(CalculationByDistance(currentLocation, new LatLng(
                                c.getDouble("lat"),
                                c.getDouble("lng")))
                                <maxDistance)
                        {
                                if (c.getString("Type").equals("club")&& c.get(dayOfWeek).equals("1")) {
                                    markerList.add(gMap.addMarker(new MarkerOptions().position(new LatLng(c.getDouble("lat"), c.getDouble("lng"))).title(c.getString("Name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.club_marker)).snippet("")));
                                }else if(c.getString("Type").equals("club")&& c.getString(dayOfWeek).equals("0")){
                                    markerList.add(gMap.addMarker((new MarkerOptions().position(new LatLng(c.getDouble("lat"), c.getDouble("lng"))).title(c.getString("Name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.club_marker_off)).snippet(closed))));
                                }else if (c.getString("Type").equals("bar")&& c.getString(dayOfWeek).equals("1")) {
                                    markerList.add(gMap.addMarker((new MarkerOptions().position(new LatLng(c.getDouble("lat"), c.getDouble("lng"))).title(c.getString("Name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.bar_marker)).snippet(""))));
                                }else if(c.getString("Type").equals("bar")&& c.getString(dayOfWeek).equals("0")){
                                    markerList.add(gMap.addMarker((new MarkerOptions().position(new LatLng(c.getDouble("lat"), c.getDouble("lng"))).title(c.getString("Name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.bar_marker_off)).snippet(closed))));
                                }

                        }
                    }
                }else{
                    /* If the map already has markers placed on it, run this operation.
                     * Connect to the 'locations' table again and compare the results with the contents of markerList.
                     * If the new dayOfWeek for each venue has changed values between 0 and 1, change the icon appropriately.
                     *
                     * If this ASyncTask was called because either bar_btn or club_btn were clicked, then this operation
                     * will also filter out appropriate markers using .setVisible()
                     */
                    for (int i = 0; i < jsonarr.length(); i++){
                        JSONObject c = jsonarr.getJSONObject(i);
                        for(Marker m : markerList){
                            if (m.getTitle().equals(c.getString("Name")) && c.getString("Type").equals("club") && showClubs){
                                if(c.getString(dayOfWeek).equals("0")) {
                                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.club_marker_off));
                                    m.setSnippet(closed);
                                    m.setVisible(true);
                                }else if(c.getString(dayOfWeek).equals("1")) {
                                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.club_marker));
                                    m.setSnippet("");
                                    m.setVisible(true);
                                }
                            }else if(m.getTitle().equals(c.getString("Name")) && c.getString("Type").equals("club") && !showClubs){
                                    m.setVisible(false);
                            }else if(m.getTitle().equals(c.getString("Name")) && c.getString("Type").equals("bar") && showBars){
                                if(c.getString(dayOfWeek).equals("0")) {
                                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bar_marker_off));
                                    m.setSnippet(closed);
                                    m.setVisible(true);
                                }else if(c.getString(dayOfWeek).equals("1")) {
                                    m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bar_marker));
                                    m.setSnippet("");
                                    m.setVisible(true);
                                }
                            }else if (m.getTitle().equals(c.getString("Name")) && c.getString("Type").equals("bar") && !showBars){
                                m.setVisible(false);
                            }
                        }
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* Check relevant table for quick info on the current venue marker. Display a few details
     * in a ListView within the marker's InfoWindow. Less comprehensive than CitySearchActivity,
     * but easily digestible.
     */
    private class GetMarkerInfo extends AsyncTask<String, Void, JSONArray>{

        //Initialize the ListCompletedListener interface to notify the InfoWindow when the app has collected details from the database
        private ListCompletedListener listener;
        public GetMarkerInfo(ListCompletedListener listener){
            this.listener=listener;
        }
        @Override
        protected JSONArray doInBackground(String... params) {

            // Parse JSON array from URL
            return JParser.getJSONFromUrl("http://neonappservice.com/neonservice/sarajevo/venues_" + dayOfWeek + ".php");
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            jsonarr=jsonArray;

            try {
                venueInfo = new ArrayList<String>();
                for (int i = 0; i < jsonarr.length(); i++){
                    JSONObject c = jsonarr.getJSONObject(i);
                    if(c.getString("Name").equals(markerTitle)){
                        venueInfo.add(c.getString("Entry Price") + " entry");
                        venueInfo.add(c.getString("Cheapest Beer"));
                        venueInfo.add(c.getString("Cheapest Mixed Drink"));
                        //If the venue has no special offers, don't add those fields to the info window
                        if(!c.getString("Special Offer 1").equals("null")) {
                            venueInfo.add(c.getString("Special Offer 1"));
                        }
                        if(!c.getString("Special Offer 2").equals("null")) {
                            venueInfo.add(c.getString("Special Offer 2"));
                        }
                        if(!c.getString("Special Offer 3").equals("null")) {
                            venueInfo.add(c.getString("Special Offer 3"));
                        }
                        address=c.getString("Address");
                        facebook_url=c.getString("Facebook");
                    }
                }


            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            //Notify InfoWindow of ASyncTask completion.
            venueInfoReceived = true;
            listener.onListCompleted(venueInfo);
            //Reset listener to null
            listener = null;

        }
    }

    /* Dynamic Url depending on which marker was clicked, will take the user to the relevant Facebook page */
    public static Intent facebookIntent(PackageManager pm, String url) {
        Uri uri;
        try {
            pm.getPackageInfo("com.facebook.katana", 0);
            uri = Uri.parse("fb://facewebmodal/f?href=" + url);
        } catch (PackageManager.NameNotFoundException e) {
            uri = Uri.parse(url);
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }
}
