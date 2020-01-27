package edu.depaul.csc472.stockwatch;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    TextView symbolText;
    TextView priceText;
    TextView changeText;
    TextView nameText;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        this.symbolText = itemView.findViewById(R.id.symbolText);
        this.priceText = itemView.findViewById(R.id.priceText);
        this.changeText = itemView.findViewById(R.id.changeText);
        this.nameText = itemView.findViewById(R.id.nameText);
    }
}
