package com.andrej.butic.neon;

import java.util.HashMap;

/* Model class for storing Venue information which was retrieved from a remote database */
public class Venue {
    public String name;
    public boolean isFavorite = false;

    // HashMap containing all the children obtained from remote database.
    private HashMap<String, String> children;

    //HashMap containing a collection of booleans to tell which children are to be filtered out.
    private HashMap<String, Boolean> childrenFiltered;

    //Hashmap containing only children which have not been filtered out.
    private HashMap<String, String> childrenShown;

    public Venue(String name, HashMap<String, String> details)
    {
        this.name = name;
        children = details;
        childrenFiltered = new HashMap<String, Boolean>();
        for (String child : details.keySet()) {
            childrenFiltered.put(child, true);
        }
        childrenShown = new HashMap<>(details);

    }

    public int getChildrenCount() {
        return children == null ? 0 : children.size();
    }

    public String getShownChild(String key) { return childrenShown.get(key); }

    /* The following method will return ALL of the children contained in the venue,
     * However 'getShownChild' above will be called by the ExpandableListAdapter to return the
     * correct list of filtered 'children' and compare it to the 'children' Map to determine which
     * children to show and which to hide.
     */
    public String getChild(String key) {
        return children.get(key);
    }

    /* Populate 'childrenShown' with all the children that have not been filtered out in CitySearchActivity */
    public void childFilter(String key, boolean filter) {

        childrenShown.clear();
        childrenFiltered.put(key, filter);
        for (String k : children.keySet()){
            if(childrenFiltered.get(k)){
                childrenShown.put(k, children.get(k));
            }
        }

    }

}
