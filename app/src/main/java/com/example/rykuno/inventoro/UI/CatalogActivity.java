package com.example.rykuno.inventoro.UI;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.rykuno.inventoro.Adapters.InventoryCursorAdapter;
import com.example.rykuno.inventoro.Data.InventoryContract;
import com.example.rykuno.inventoro.R;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int INVENTORY_LOADER = 0;
    InventoryCursorAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        mAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mAdapter);
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all:
                deleteAllData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
    }

    private void insertItem() {
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, "Soup");
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUMMARY, "Tomato");
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK, 5);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, 13);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD, 3);

        Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUMMARY,};
        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI,
                projection, null, null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
