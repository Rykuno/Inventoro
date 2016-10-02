package com.example.rykuno.inventoro.UI;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rykuno.inventoro.Data.InventoryContract;
import com.example.rykuno.inventoro.R;

import java.io.File;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_PET_LOADER = 0;
    public static final int REQUEST_CODE = 20;
    public static final int REQUEST_PERMISSION =200;
    Uri imageUri;
    String filePath;
    private Uri mCurrentInventoryUri;
    private EditText mName_EditText;
    private EditText mSupplier_EditText;
    private EditText mStock_EditText;
    private EditText mPrice_EditText;
    private EditText mSold_EditText;
    private EditText mProviderEmail_EditText;
    private Button mAddImageButton;
    private boolean mItemHasChanced = false;
    private ImageView mImageView;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanced=true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        if (mCurrentInventoryUri == null) {
            setTitle("Add an Item");
            invalidateOptionsMenu();
        }else{
            setTitle("Modify Item");
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        mName_EditText = (EditText) findViewById(R.id.edit_item_name);
        mSupplier_EditText = (EditText) findViewById(R.id.edit_item_provider);
        mPrice_EditText = (EditText) findViewById(R.id.edit_item_price);
        mStock_EditText = (EditText) findViewById(R.id.edit_item_quantity);
        mSold_EditText = (EditText) findViewById(R.id.edit_item_sold);
        mProviderEmail_EditText = (EditText) findViewById(R.id.edit_item_provider_email);
        mAddImageButton = (Button) findViewById(R.id.addImage_button);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mName_EditText.setOnTouchListener(mOnTouchListener);
        mSupplier_EditText.setOnTouchListener(mOnTouchListener);
        mSold_EditText.setOnTouchListener(mOnTouchListener);
        mStock_EditText.setOnTouchListener(mOnTouchListener);
        mPrice_EditText.setOnTouchListener(mOnTouchListener);
        mProviderEmail_EditText.setOnTouchListener(mOnTouchListener);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);

                //whre do we want to find the data?
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String path = pictureDirectory.getPath();
                Uri data = Uri.parse(path);

                //set data and type.
                intent.setDataAndType(data, "image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }


    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK){
            //if everything processed successfully
            if (requestCode == REQUEST_CODE){
                imageUri = data.getData();
                    filePath = getImagePath(imageUri);
                File file = new File(filePath);
                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());


                mImageView.setImageBitmap(bmp);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveItem();
                finish();
            return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mItemHasChanced){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanced) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Delete Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Delete Successful",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


    private void saveItem() {
        String nameString = mName_EditText.getText().toString().trim();
        String supplierStirng = mSupplier_EditText.getText().toString().trim();
        String supplierEmailString = mProviderEmail_EditText.getText().toString().trim();
        String priceString = mPrice_EditText.getText().toString().trim();
        int stockInt = Integer.parseInt(mStock_EditText.getText().toString().trim());
        int soldInt = Integer.parseInt(mSold_EditText.getText().toString().trim());

        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierStirng) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(mSold_EditText.getText().toString().trim())
                && TextUtils.isEmpty(mStock_EditText.getText().toString().trim())) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierStirng);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmailString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK, stockInt);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD, soldInt);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PICTURE, filePath);
        if (mCurrentInventoryUri ==null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error updating",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            }
        }else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, "Error Updating",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Item Updated",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PICTURE
        };

        return new CursorLoader(this, mCurrentInventoryUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1){
            return;
        }

        if (data.moveToFirst()){
            int nameColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            int supplierColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
            int supplierEmailColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
            int stockColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK);
            int priceColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            int soldColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD);
            int pictureColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PICTURE);

            String name = data.getString(nameColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String supplierEmail = data.getString(supplierEmailColumnIndex);
            int stock = data.getInt(stockColumnIndex);
            String price = data.getString(priceColumnIndex);
            int sold = data.getInt(soldColumnIndex);
            String picture = data.getString(pictureColumnIndex);

            File file = new File(picture);
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());


            mName_EditText.setText(name);
            mSupplier_EditText.setText(supplier);
            mProviderEmail_EditText.setText(supplierEmail);
            mStock_EditText.setText(Integer.toString(stock));
            mPrice_EditText.setText(price);
            mSold_EditText.setText(Integer.toString(sold));
            mImageView.setImageBitmap(bmp);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mName_EditText.setText("");
        mPrice_EditText.setText("");
        mProviderEmail_EditText.setText("");
        mSupplier_EditText.setText("");
        mStock_EditText.setText("");
        mPrice_EditText.setText("");
    }

    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
    
    private Bitmap MediaPathToBitmap(String path){
        File file = new File(path);
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        return bmp;
    }
}
