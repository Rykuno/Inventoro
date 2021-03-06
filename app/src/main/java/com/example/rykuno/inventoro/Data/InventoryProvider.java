package com.example.rykuno.inventoro.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rykuno on 9/29/16.
 */

public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private InventoryDbHelper mDbHelper;
    private static final int INVENTORY = 0;
    private static final int INVENTORY_ID = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(
                        InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
        String supplierEmail = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        String price = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        Integer stock = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK);
        Integer sold = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD);
        if (supplierName == null || supplierName.equals(""))
            throw new IllegalArgumentException("Supplier name required");
        if (supplierEmail == null || supplierEmail.equals(""))
            throw new IllegalArgumentException("Supplier email required");
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Item name required");
        if (price == null || name.equals(""))
            throw new IllegalArgumentException("Price required");
        if (stock == null)
            throw new IllegalArgumentException("Stock required");
        if (sold == null)
            throw new IllegalArgumentException("Sold amount required");

        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            Log.e(LOG_TAG, "ENTRY WORKED " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
        String supplierEmail = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        String price = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        Integer stock = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK);
        Integer sold = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD);

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER) && supplierName == null)
            throw new IllegalArgumentException("Supplier name required");
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL) && supplierEmail == null)
            throw new IllegalArgumentException("Supplier email required");
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME) && name == null)
            throw new IllegalArgumentException("Item name required");
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE) && price == null)
            throw new IllegalArgumentException("Price required");
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK) && stock == null)
            throw new IllegalArgumentException("Stock required");
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD) && sold == null)
            throw new IllegalArgumentException("Sold amount required");

        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
