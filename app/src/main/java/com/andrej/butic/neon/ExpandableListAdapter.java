package com.andrej.butic.neon;

import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter
{
    private Context _context;
    private List<Venue> venue_header; // header titles
    private List<Venue> venue_shown;
    // child data in format of header title, child title
    String key = "";
    private String fbURL;
    private String mapURL;
    private String offer1;
    private String offer2;
    private String offer3;
    DBHandler dbHandler;


    public ExpandableListAdapter(Context context, List<Venue> allVenues,
                                 HashMap<String, HashMap<String, String>> allChildren)
    {
        _context = context;
        venue_header = allVenues;
        venue_shown = new ArrayList<>(allVenues);
        dbHandler = new DBHandler(_context, null, null, 1);

    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return venue_shown.get(groupPosition).getChild(key);
    }
    @Override
    public Object getGroup(int groupPosition) {
        return venue_shown == null ? null : venue_shown.get(groupPosition);
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getGroupCount() {
        return venue_shown == null ? 0 : venue_shown.size();
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        return venue_shown.get(groupPosition).getChildrenCount();
    }
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // Hide non-favorited Venues from view if 'Show Only Favorites' is selected in CitySearchActivity.
    public void filterFavorites() {
        venue_shown.clear();
        for (Venue v : venue_header) {
            if(dbHandler.checkIfFavorite(v.name)){
                v.isFavorite = true;
                venue_shown.add(v);
            }
            else{
                v.isFavorite = false;
            }
        }
        // If 'Show Only Favorites' is selected in CitySearchActivity and
        // the user has selected no favorites in this tab, notify them
        if(venue_shown.size()==0){
            Toast.makeText(_context, "You have no favorites here", Toast.LENGTH_SHORT).show();
        }

        notifyDataSetChanged();
    }

    // Put all the venues back into view when 'Show All Venues' is selected in CitySearchActivity
    public void repopulate(){
        venue_shown.clear();
        for (Venue v : venue_header){
            venue_shown.add(v);
        }
        notifyDataSetChanged();
    }

    // Filter ExpandableListView's children when one of the corresponding checkboxes is selected in CitySearchActivity
    public void childFilter(String childName, boolean filter) {

        for (Venue v : venue_shown) {
                v.childFilter(childName, filter);
        }

        notifyDataSetChanged();
    }


    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final Venue venue = (Venue) getGroup(groupPosition);
        String headerTitle = venue.name;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }


        final ToggleButton favorite_button = (ToggleButton) convertView
                .findViewById(R.id.favoriteButton);
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        // For each venue, check if it is already stored as a favorite with the dbHandler class,
        // in order to determine the state of the favorite_button.
        if(dbHandler.checkIfFavorite(headerTitle)){
            favorite_button.setChecked(true);
        }
        else{
            favorite_button.setChecked(false);
        }

        // Notify the user when a venue has been added or removed from their favorites. Use dbHandler and SQLite to store favorites.
        favorite_button.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                if(favorite_button.isChecked()){
                    Toast.makeText(_context, "Added to your favorites!", Toast.LENGTH_SHORT).show();
                    venue.isFavorite=true;
                    dbHandler.addVenue(venue);
                }
                else{
                    Toast.makeText(_context, "Removed from your favorites.", Toast.LENGTH_SHORT).show();
                    venue.isFavorite=false;
                    dbHandler.deleteVenue(venue.name);
                }

            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        switch(childPosition){
            case 0: key="Entry Price"; break;
            case 1: key="Opening Hours"; break;
            case 2: key="Cheapest Beer"; break;
            case 3: key="Cheapest Mixed Drink"; break;
            case 4: key="Special Offer 1"; break;
            case 5: key="Special Offer 2"; break;
            case 6: key="Special Offer 3"; break;
            case 7: key="Music"; break;
            case 8: key="Cocktails"; break;
            case 9:key="Draught Beer"; break;
            case 10:key="Map"; break;
            case 11:key="Facebook"; break;
        }
        String thing = (String) getChild(groupPosition, childPosition);
        //check for empty fields in the table. If found, increment childPosition to the next field


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        TextView txtListChild2 = (TextView) convertView
                .findViewById(R.id.lowerListItem);
        ImageButton facebook = (ImageButton) convertView
                .findViewById(R.id.button_facebook);
        ImageButton maps = (ImageButton) convertView
                .findViewById(R.id.button_maps);
        LinearLayout imageLayout = (LinearLayout) convertView
                .findViewById(R.id.imageLayout);
        imageLayout.setVisibility(View.GONE);
        txtListChild.setVisibility(View.GONE);
        txtListChild2.setVisibility(View.GONE);

        // Iterate through every venue shown and place list information accordingly.
        // If there is no corresponding 'key' in v.getShownChild, then that particular detail has been filtered out
        // and will not be shown in the View.
        for(Venue v : venue_shown){
            if(thing!=null) {
                switch (key) {
                    case "Entry Price":

                        if(v.getShownChild(key)!=null){
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                            txtListChild.setBackgroundResource(R.drawable.transparent_rec_mid);
                            txtListChild.setPadding(120, 60, 0, 0);
                        }
                        break;
                    case "Opening Hours":

                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Cheapest Beer":

                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Cheapest Mixed Drink":

                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;

                    // For Special Offers 1 to 3, make sure that they all fit under a single TextView.
                    case "Special Offer 1":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.GONE);
                            txtListChild2.setVisibility(View.GONE);
                            offer1 = thing;
                        }
                        break;
                    case "Special Offer 2":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.GONE);
                            txtListChild2.setVisibility(View.GONE);
                            offer2 = thing;
                        }
                        break;
                    case "Special Offer 3":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                            key = "Special Offers";
                            offer3 = thing;
                            if(!offer2.equals("null") && !offer3.equals("null")){
                                thing = offer1 + "\n" + offer2 + "\n" + offer3;
                            }else if (!offer2.equals("null") && offer3.equals("null")){
                                thing = offer1 + "\n" + offer2;
                            }else{
                                thing = offer1;
                            }
                            if (thing == "null") {
                                txtListChild.setVisibility(View.GONE);
                                txtListChild2.setVisibility(View.GONE);
                            }
                        }
                        break;
                    case "Music":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setBackgroundResource(R.drawable.transparent_rec_mid);
                            txtListChild.setPadding(120, 20, 0, 0);
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Cocktails":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setBackgroundResource(R.drawable.transparent_rec_mid);
                            txtListChild2.setGravity(Gravity.NO_GRAVITY);
                            txtListChild2.setPadding(140, 0, 0, 0);
                            txtListChild.setPadding(120, 20, 0, 0);
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Draught Beer":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setBackgroundResource(R.drawable.transparent_rec_mid);
                            txtListChild2.setGravity(Gravity.NO_GRAVITY);
                            txtListChild2.setPadding(140, 0, 0, 0);
                            txtListChild.setPadding(120, 20, 0, 0);
                            txtListChild.setVisibility(View.VISIBLE);
                            txtListChild2.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "Map":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.GONE);
                            txtListChild2.setVisibility(View.GONE);
                            imageLayout.setVisibility(View.VISIBLE);
                            mapURL = thing;
                            maps.setBackgroundResource(0);
                            facebook.setBackgroundResource(0);
                        }
                        break;
                    case "Facebook":
                        if(v.getShownChild(key)!=null) {
                            txtListChild.setVisibility(View.GONE);
                            txtListChild2.setVisibility(View.GONE);
                            imageLayout.setVisibility(View.GONE);
                            fbURL = thing;
                            break;
                        }
                }

                // These OnClickListeners will take the user to either the Venue's Facebook Page or Google Maps location.
                facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = fbURL;
                        Uri uri = Uri.parse(url);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                        _context.startActivity(browserIntent);
                    }
                });
                maps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = mapURL;
                        Uri uri = Uri.parse(url);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                        _context.startActivity(browserIntent);
                    }
                });

                txtListChild.setText(key + ":");
                txtListChild2.setText(thing);
        }}
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
