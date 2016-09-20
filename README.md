# Neon
--------------------------------------------------------------------------------------------------------------------------------------
IMPORTANT: The Hostgator server containing the database for this app is no longer active, meaning that the app's functionality is currently limited.

Instead I would recommend that you view a demo of the full app in action here: https://youtu.be/lZJJGKLDIBM
--------------------------------------------------------------------------------------------------------------------------------------
How to use Neon

The application allows for two search methods, simplified as much as possible for user friendliness. Both search methods are separated by a TabHost. The first search method is the one more likely to be used. The user selects a distance radius from their current position within which they want to find venues. They are then directed to a MapView displaying markers for all venues within the chosen distance. The markers are distinguishable by the letters 'B' and 'C', standing for 'Bars' and 'Clubs' respectively, so the user knows what sort of venue they are looking at. Another type of 'greyed out' marker is also shown for venues which are closed on that day. The reason for this is that the user will know there is a venue present there which they might want to enter on a different day, therefore still providing advertising for the venue in question.

When any of the markers are clicked, the app will connect to a remote database and display venue information to the user in an info window, including basic details such as opening hours, entry and drinks prices.

Several other options are available on the MapView, such as filtering out Bars or Clubs, connecting to the venue's Facebook page, or changing the date for which the user wishes to explore the venues.

The second search method lets the user pick a city instead of a distance and shows a larger number of venue details, displayed in an ExpandableListView. This search method is customizable in two ways. The user can mark certain venues as 'favorites' and filter out non-favorited venues, and they can choose which details they want to be shown, ie: If they don't care about beer prices they can remove "Cheapest Beer" from the search results. These filters are contained in a DrawerLayout on the left side of the activity, and favorites can be marked using the star-shaped button to the right of every venue name.
