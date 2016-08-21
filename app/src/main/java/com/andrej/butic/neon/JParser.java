package com.andrej.butic.neon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

/* General class for parsing remote database data into JSON. Used by CitySearchActivity and DistanceSearchActivity */
public class JParser {


    public JParser() {
    }
    public static JSONArray getJSONFromUrl(String url) {

        HttpURLConnection connection = null;
        JSONArray jsonarr = null;
        BufferedReader reader = null;

        // Making HTTP request
        try{
            URL c = new URL(url);

            connection = (HttpURLConnection) c.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line="";
            while ((line = reader.readLine()) !=null){
                builder.append(line + "\n");
            }
            try {
                jsonarr = new JSONArray(builder.toString());
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
            return jsonarr;
        }catch(MalformedURLException e){
            e.printStackTrace();
            System.out.println("Invalid URL.");
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Failed to read from database.");
        }finally{
            if(connection!=null) {
                connection.disconnect();
            }
            try {
                if(reader!=null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}