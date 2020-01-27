package edu.depaul.csc472.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class AsyncStockLoader extends AsyncTask<String, Integer, String> {

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL =
            "https://cloud.iexapis.com/stable/stock";
    private static final String TOKEN =
            "sk_7dc3416472364df0bb570b92d56579d6";
    private String symbol;


    AsyncStockLoader(MainActivity ma, String symbol) {
        mainActivity = ma;
        this.symbol = symbol;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String, Object> stockData = parseJSON(s);
        mainActivity.updateStock(stockData);
    }

    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(String.format("%s/%s/quote?token=%s", DATA_URL, symbol, TOKEN));
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

    private HashMap<String, Object> parseJSON(String s) {

        HashMap<String, Object> stockData = new HashMap<String, Object>();
        try {
            JSONObject stock = new JSONObject(s);
            stockData.put("symbol", this.symbol);
            stockData.put("name", stock.getString("companyName"));
            try {
                stockData.put("price", stock.getDouble("latestPrice"));
            } catch (Exception e){
                stockData.put("price", 0.0);
            }
            try {
                stockData.put("change", stock.getDouble("change"));
            } catch (Exception e){
                stockData.put("change", 0.0);
            }
            try {
                stockData.put("changePercent", stock.getDouble("changePercent"));
            } catch (Exception e){
                stockData.put("changePercent", 0.0);
            }




            return stockData;
        } catch (Exception e) {
            return stockData;
        }
//        return stockData;
    }
}
