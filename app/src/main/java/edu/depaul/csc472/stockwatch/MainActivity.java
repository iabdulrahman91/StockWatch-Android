package edu.depaul.csc472.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.MovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    String url = "https://www.marketwatch.com/investing/stock/";

    private ArrayList<Stock> stockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper; // The SwipeRefreshLayout

    // app vars
    HashMap<String, String> symbolsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load the data
        symbolsList = new HashMap<String, String>();
        new AsyncSymbolsLoader(this).execute();


        // do read
        doRead();

    }

    @Override
    protected void onPause() {
        super.onPause();
        doWrite();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (isConnected()) {
                    if (symbolsList.isEmpty()) {
                        new AsyncSymbolsLoader(this).execute();
                    }
                    showAddDialog();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("No Network Connection");
                    builder.setMessage("Stocks Cannot Be Added Without A Network Connection.");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Create an edittext and set it to be the builder's view
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);

        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // pass to function
                String s = et.getText().toString().trim().toUpperCase();
                if (!s.isEmpty()) searchStock(s);
            }
        });
        builder.setNegativeButton("Cancel", null);


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void searchStock(final String symbol) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (String s : symbolsList.keySet()) {
            if (s.toUpperCase().indexOf(symbol.toUpperCase()) == 0) {
                result.put(s, symbolsList.get(s));
            }
        }
        switch (result.size()) {
            case 0:
                // not found dialog
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("Symbol Not Found: " + symbol);
                builder1.setMessage("Data for stock symbol");
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                break;
            case 1:
                // add it
//                Toast.makeText(this, "One found", Toast.LENGTH_SHORT).show();
                addStock(symbol);
                break;
            default:
                // show list dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                ArrayList<String> array = new ArrayList<String>();
                for (String k : result.keySet()) {
                    array.add(k + " - " + result.get(k));
                }
                builder.setTitle("Make a selection");
                final CharSequence[] sArray = array.toArray(new CharSequence[0]);
                builder.setItems(sArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        addStock(sArray[which].toString());

                    }
                });

                builder.setNegativeButton("Nevermind", null);
                AlertDialog dialog = builder.create();
                dialog.show();
        }

    }

    private void addStock(String s) {
        String[] ss = s.split(" - ");
        String symbol = ss[0].trim();
        String name = (ss.length > 1) ? ss[1] : "";
        for (Stock stock : stockList) {
            if (stock.getSympol().equalsIgnoreCase(symbol)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.baseline_warning_24);
                builder.setTitle("Duplicate Stock");
                builder.setMessage(String.format("Stock Symbol %s is already displayed.", symbol));
                AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        stockList.add(new Stock(symbol, name, 0.0, 0.0, 0.0));
        Collections.sort(stockList);
        new AsyncStockLoader(this, symbol).execute();
        doWrite();
    }

    private void doRefresh() {
        stockList.clear();
        stockAdapter.notifyDataSetChanged();
        doRead();
        swiper.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock s = stockList.get(pos);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(this.url + s.getSympol()));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock s = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // Code goes here
                        stockList.remove(s);
                        stockAdapter.notifyDataSetChanged();
                        doWrite();

                    }

                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // Code goes here

                    }

                });
        builder.setIcon(android.R.drawable.ic_menu_delete);
        builder.setTitle("Delete Stock");
        builder.setMessage(String.format("Delete Stock Symbol %s?", s.getSympol()));
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    public void updateSymbols(HashMap<String, String> result) {
        this.symbolsList = result;
    }

    public void updateStock(HashMap<String, Object> stockData) {
        for (Stock s : this.stockList) {
            if (s.getSympol().equalsIgnoreCase((String) stockData.get("symbol"))) {
                s.setName((String) stockData.get("name"));
                s.setPrice((Double) stockData.get("price"));
                s.setPriceChange((Double) stockData.get("change"));
                s.setChangePercent((Double) stockData.get("changePercent"));
                break;
            }
        }
        stockAdapter.notifyDataSetChanged();
    }

    public void doWrite() {

        JSONArray jsonArray = new JSONArray();

        for (Stock n : stockList) {
            try {
                JSONObject noteJSON = new JSONObject();
                noteJSON.put("symbol", n.getSympol());
                noteJSON.put("name", n.getName());
                jsonArray.put(noteJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String jsonText = jsonArray.toString();


        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            openFileOutput("mydata.txt", Context.MODE_PRIVATE)
                    );

            outputStreamWriter.write(jsonText);
            outputStreamWriter.close();
//            Toast.makeText(this, "File write success!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
//            Toast.makeText(this, "File write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void doRead() {

        stockList.clear();
        try {
            InputStream inputStream = openFileInput("mydata.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                String jsonText = stringBuilder.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String symbol = jsonObject.getString("symbol");
                        String name = jsonObject.getString("name");
                        Stock n = new Stock(symbol, name, 0.0, 0.0, 0.0);
                        stockList.add(n);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "NEW JSON created", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
        }

        Collections.sort(stockList);
        if(isConnected()){
            for (Stock s : stockList) {
                new AsyncStockLoader(this, s.getSympol()).execute();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection.");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
}
