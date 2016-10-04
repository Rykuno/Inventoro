package com.example.rykuno.inventoro.UI;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rykuno.inventoro.Data.InventoryContract;
import com.example.rykuno.inventoro.R;

import java.io.File;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    public static final int REQUEST_CODE = 100;
    public static final int REQUEST_PERMISSION = 200;
    private String filePath;
    private Uri mCurrentInventoryUri;
    private boolean mItemHasChanced = false;
    private TextView mTvItemName;
    private TextView mTvItemId;
    private TextView mTvItemPrice;
    private TextView mTvItemStock;
    private TextView mTvItemSold;
    private TextView mTvSupplierName;
    private TextView mTvSupplierEmail;
    private Button mEditInfo_button;
    private Button mSell_button;
    private Button mOrderMore_button;
    private Button mModifyStock_button;
    private ImageButton mItem_imageView;
    private boolean increaseBy;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanced = true;
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
            setTitle(getString(R.string.add_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.modify_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mTvItemId = (TextView) findViewById(R.id.tvItemId);
        mTvItemName = (TextView) findViewById(R.id.tvItemName);
        mTvItemPrice = (TextView) findViewById(R.id.tvItemPrice);
        mTvItemStock = (TextView) findViewById(R.id.tvItemStock);
        mTvItemSold = (TextView) findViewById(R.id.tvItemSold);
        mTvSupplierName = (TextView) findViewById(R.id.tvSupplier);
        mTvSupplierEmail = (TextView) findViewById(R.id.tvSupplierEmail);
        mItem_imageView = (ImageButton) findViewById(R.id.item_imageView);
        mEditInfo_button = (Button) findViewById(R.id.editInfo_button);
        mModifyStock_button = (Button) findViewById(R.id.modifyStock_button);
        mOrderMore_button = (Button) findViewById(R.id.orderMore_button);
        mSell_button = (Button) findViewById(R.id.sell_button);

        mEditInfo_button.setOnTouchListener(mOnTouchListener);
        mSell_button.setOnTouchListener(mOnTouchListener);
        mModifyStock_button.setOnTouchListener(mOnTouchListener);

        setupOnClickListeners();
        askForMediaPermission();
    }

    private void askForMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //User denied permission
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                Uri imageUri = data.getData();
                filePath = getImagePath(imageUri);
                mItem_imageView.setImageBitmap(mediaPathToBitmap(filePath));
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
        switch (item.getItemId()) {
            case R.id.action_save:
                if (TextUtils.isEmpty(mTvSupplierName.getText().toString().trim()) || TextUtils.isEmpty(mTvSupplierEmail.getText().toString().trim())
                        || TextUtils.isEmpty(mTvItemName.getText().toString().trim()) || TextUtils.isEmpty(mTvItemPrice.getText().toString().trim())
                        || TextUtils.isEmpty(mTvItemStock.getText().toString().trim()) || TextUtils.isEmpty(mTvItemPrice.getText().toString().trim())){
                    showConfirmDialog();
                    return false;
                }else {
                    saveItem();
                    finish();
                    return true;
                }
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mItemHasChanced) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

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
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanced) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentInventoryUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.delete_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.delete_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void saveItem() {
        String nameString = mTvItemName.getText().toString().trim();
        String supplierStirng = mTvSupplierName.getText().toString().trim();
        String supplierEmailString = mTvSupplierEmail.getText().toString().trim();
        String priceString = mTvItemPrice.getText().toString().trim();
        int stockInt = Integer.parseInt(mTvItemStock.getText().toString().trim());
        int soldInt = Integer.parseInt(mTvItemSold.getText().toString().trim());

            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierStirng);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmailString);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK, stockInt);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD, soldInt);
            values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PICTURE, filePath);
            if (mCurrentInventoryUri == null) {
                Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, R.string.error_updating,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, R.string.error_updating,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, R.string.item_updated,
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
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int idColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry._ID);
            int nameColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            int supplierColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER);
            int supplierEmailColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
            int stockColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_STOCK);
            int priceColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            int soldColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD);
            int pictureColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PICTURE);

            String name = data.getString(nameColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String suppierEmail = data.getString(supplierEmailColumnIndex);
            String price = data.getString(priceColumnIndex);
            String picture = data.getString(pictureColumnIndex);
            int sold = data.getInt(soldColumnIndex);
            int stock = data.getInt(stockColumnIndex);
            int id = data.getInt(idColumnIndex);

            mTvItemId.setText(getString(R.string.item_id) + String.valueOf(id));
            mTvItemName.setText(name);
            mTvSupplierName.setText(supplier);
            mTvSupplierEmail.setText(suppierEmail);
            mTvItemStock.setText(Integer.toString(stock));
            mTvItemSold.setText(Integer.toString(sold));
            mTvItemPrice.setText(formatPrice(price));

            if (picture != null)
                mItem_imageView.setImageBitmap(mediaPathToBitmap(picture));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTvItemName.setText(R.string.empty_text);
        mTvItemPrice.setText(R.string.empty_text);
        mTvItemSold.setText(R.string.empty_text);
        mTvItemStock.setText(R.string.empty_text);
        mTvSupplierName.setText(R.string.empty_text);
        mTvSupplierEmail.setText(R.string.empty_text);
    }

    public String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private Bitmap mediaPathToBitmap(String path) {
        File file = new File(path);
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        return bmp;
    }

    private void setupOnClickListeners() {

        mModifyStock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyStockDialog();
            }
        });

        mItem_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String path = pictureDirectory.getPath();
                Uri data = Uri.parse(path);

                //set data and type.
                intent.setDataAndType(data, "image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        mEditInfo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        mSell_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTvItemStock.getText().toString().trim().equals("") && !mTvItemSold.getText().toString().trim().equals(""))
                    showSellDialog();
                else
                    Toast.makeText(EditorActivity.this, R.string.fill_in_fields_stock_sold, Toast.LENGTH_SHORT).show();
            }
        });

        mOrderMore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: " + mTvSupplierEmail.getText().toString().trim())); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, mTvSupplierName.getText().toString().trim());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_order_request) + mTvItemName.getText().toString().trim());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void showModifyStockDialog() {
        View view = (LayoutInflater.from(EditorActivity.this)).inflate(R.layout.modify_stock_dialog, null);
        AlertDialog.Builder modifyStockDialog = new AlertDialog.Builder(EditorActivity.this);
        modifyStockDialog.setView(view);
        modifyStockDialog.setCancelable(true);
        final EditText modifyAmountBy = (EditText) view.findViewById(R.id.modifyStockBy_editText);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

        ArrayAdapter SpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_spinner_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(SpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.increaseBy))) {
                        increaseBy = true;
                    } else if (selection.equals(getString(R.string.decreaseBy))) {
                        increaseBy = false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                increaseBy = true;
            }
        });

        modifyStockDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!modifyAmountBy.getText().toString().trim().matches(getString(R.string.empty_text))) {
                    int originalStock = Integer.parseInt(mTvItemStock.getText().toString().trim());
                    int modifiedStock = Integer.parseInt(modifyAmountBy.getText().toString().trim());
                    if (increaseBy == true) {
                        mTvItemStock.setText(String.valueOf(originalStock + modifiedStock));
                    } else if (increaseBy == false && (originalStock - modifiedStock) >= 0) {
                        mTvItemStock.setText(String.valueOf(originalStock - modifiedStock));
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.insufficient_stock, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        modifyStockDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        Dialog dialog = modifyStockDialog.create();
        dialog.show();
    }

    private void showSellDialog(){
        View view = (LayoutInflater.from(EditorActivity.this)).inflate(R.layout.sell_dialog, null);
        AlertDialog.Builder sellDialog = new AlertDialog.Builder(EditorActivity.this);
        sellDialog.setView(view);
        sellDialog.setCancelable(false);

        final EditText mSellAmount_editText = (EditText) view.findViewById(R.id.sellAmount_editText);

        sellDialog.setPositiveButton(R.string.sell, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!mSellAmount_editText.getText().toString().trim().matches("")) {
                    int stock = Integer.parseInt(mTvItemStock.getText().toString().trim());
                    int sellAmount = Integer.parseInt(mSellAmount_editText.getText().toString().trim());
                    if ((stock - sellAmount) >= 0) {
                        int newSoldAmount = Integer.parseInt(mTvItemSold.getText().toString().trim()) + sellAmount;
                        mTvItemSold.setText(String.valueOf(newSoldAmount));
                        mTvItemStock.setText(String.valueOf(stock - sellAmount));
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.insufficient_stock, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        sellDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        Dialog dialog = sellDialog.create();
        dialog.show();
    }

    private void showEditDialog(){
        View view = (LayoutInflater.from(EditorActivity.this)).inflate(R.layout.edit_info_dialog, null);
        AlertDialog.Builder editDialog = new AlertDialog.Builder(EditorActivity.this);
        editDialog.setView(view);
        editDialog.setCancelable(false);

        final EditText mSupplierName_editText = (EditText) view.findViewById(R.id.supplierName_editText);
        final EditText mSupplierEmail_editText = (EditText) view.findViewById(R.id.supplierEmail_editText);
        final EditText mItemName_editText = (EditText) view.findViewById(R.id.itemName_editText);
        final EditText mItemPrice_editText = (EditText) view.findViewById(R.id.itemPrice_editText);
        final EditText mItemStock_editText = (EditText) view.findViewById(R.id.itemStock_editText);
        final EditText mItemSold_editText = (EditText) view.findViewById(R.id.itemSold_editText);

        if (mTvSupplierName.getText().toString() != null)
            mSupplierName_editText.setText(mTvSupplierName.getText().toString().trim());
        if (mTvSupplierEmail.getText().toString() != null)
            mSupplierEmail_editText.setText(mTvSupplierEmail.getText().toString().trim());
        if (mTvItemName.getText().toString() != null)
            mItemName_editText.setText(mTvItemName.getText().toString().trim());
        if (mTvItemPrice.getText().toString() != null)
            mItemPrice_editText.setText(mTvItemPrice.getText().toString().trim());
        if (mTvItemSold.getText().toString() != null)
            mItemSold_editText.setText(mTvItemSold.getText().toString().trim());
        if (mTvItemStock.getText().toString() != null)
            mItemStock_editText.setText(mTvItemStock.getText().toString().trim());

        //To take care of formatting issues
        if (mItemPrice_editText.getText().toString().trim().equals("$.00"))
            mItemPrice_editText.setText(R.string.empty_text);

        editDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    mTvSupplierName.setText(mSupplierName_editText.getText().toString().trim());
                    mTvSupplierEmail.setText(mSupplierEmail_editText.getText().toString().trim());
                    mTvItemName.setText(mItemName_editText.getText().toString().trim());
                    mTvItemStock.setText(mItemStock_editText.getText().toString().trim());
                    mTvItemSold.setText(mItemSold_editText.getText().toString().trim());

                if (mItemPrice_editText.getText().toString().trim().matches(getString(R.string.empty_text)))
                    mTvItemPrice.setText(R.string.empty_text);
                else
                    mTvItemPrice.setText(formatPrice(mItemPrice_editText.getText().toString().trim()));

            }
        });

        editDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        Dialog dialog = editDialog.create();
        dialog.show();
    }

    private void showConfirmDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.fill_in_all_fields).setPositiveButton(R.string.dialog_ok, dialogClickListener).show();
    }

    private String formatPrice(String price){
        if (!price.contains(".")){
            if (price.contains("$")){
                return price + ".00";
            }else {
                return "$" + price + ".00";
            }
        }else {
            if (price.contains("$")){
                return price;
            }else {
                return "$" + price;
            }
        }
    }

}
