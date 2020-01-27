package edu.depaul.csc472.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private ArrayList<Stock> aList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> aList, MainActivity mainActivity) {
        this.aList = aList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_item, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock selectedStock = aList.get(position);

        int c = Color.GREEN;
        String change = "";
        if (selectedStock.getChangePercent() < 0) {
            change = String.format("\u25BC %.2f (%.2f\uFF05)", selectedStock.getPriceChange(), selectedStock.getChangePercent()*100);
            holder.changeText.setText(change);
            c = Color.RED;
        } else {
            change = String.format("\u25B2 %.2f (%.2f\uFF05)", selectedStock.getPriceChange(), selectedStock.getChangePercent()*100);
            holder.changeText.setText(change);

        }
        holder.symbolText.setText(selectedStock.getSympol());
        holder.priceText.setText(String.format("%.2f", selectedStock.getPrice()));
        holder.nameText.setText(selectedStock.getName());

        // Color setting
        holder.symbolText.setTextColor(c);
        holder.priceText.setTextColor(c);
        holder.nameText.setTextColor(c);
        holder.changeText.setTextColor(c);

    }

    @Override
    public int getItemCount() {
        return this.aList.size();
    }
}
