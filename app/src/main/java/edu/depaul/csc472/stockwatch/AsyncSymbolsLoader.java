package edu.depaul.csc472.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class AsyncSymbolsLoader extends AsyncTask<String, Integer, String> {

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL =
            "https://api.iextrading.com/1.0/ref-data/symbols";


    AsyncSymbolsLoader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String,String> namesList = parseJSON(s);
        mainActivity.updateSymbols(namesList);
    }

    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            return null;
        }

        return sb.toString();
    }

    private HashMap<String,String> parseJSON(String s) {

        HashMap<String,String> stocksNamesList = new HashMap<String, String>();
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jCountry = (JSONObject) jObjMain.get(i);
                String symbol = jCountry.getString("symbol");
                String name = jCountry.getString("name");
                stocksNamesList.put(symbol,name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stocksNamesList;
    }
}
