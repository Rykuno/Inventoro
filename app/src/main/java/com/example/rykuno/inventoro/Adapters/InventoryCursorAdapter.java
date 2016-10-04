package com.example.rykuno.inventoro.Adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.rykuno.inventoro.Data.InventoryContract;
import com.example.rykuno.inventoro.R;

/**
 * Created by rykuno on 9/29/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    Context mContext;
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.name_textView);
        TextView tvProvider = (TextView) view.findViewById(R.id.provider_textView);
        TextView tvPrice = (TextView) view.findViewById(R.id.price_textView);
        TextView tvStock = (TextView) view.findViewById(R.id.stock_textView);
        final Button sellButton = (Button) view.findViewById(R.id.sell_button);
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));

        final String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        final String provider = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER));
        final String providerEmail = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL));
        final String price = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));
        final int stock = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK));
        final int sold = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD));

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newStock;
                int newSold;

                if (stock>0) {
                    newStock = stock - 1;
                    newSold = sold + 1;
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK, newStock);
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD, newSold);
                    Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                    mContext.getContentResolver().update(uri, values, null, null);
                } else {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto: "+ providerEmail)); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, provider);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Order Request: " + name);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    }
                }
            }
        });

        tvName.setText(name);
        tvProvider.setText(provider);

        if (!price.contains(".")){
            if (price.contains("$")){
                tvPrice.setText(price + ".00");
            }else {
                tvPrice.setText("$" + price + ".00");
            }
        }else {
            if (price.contains("$")){
                tvPrice.setText(price);
            }else {
                tvPrice.setText("$" + price);
            }
        }

        if (stock==0){
            tvStock.setText("Out of stock");
            tvStock.setTextColor(Color.RED);
            sellButton.setText("Order");
            sellButton.getBackground().setColorFilter(0xffffffff, PorterDuff.Mode.MULTIPLY);
        }else{
            tvStock.setText("Stock: "+ stock);
            tvStock.setTextColor(Color.GRAY);
            sellButton.setText("SELL");
            sellButton.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        }
    }
}
