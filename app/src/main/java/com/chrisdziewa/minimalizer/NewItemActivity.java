package com.chrisdziewa.minimalizer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

public class NewItemActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        mEditText = (EditText) findViewById(R.id.new_item_edit_text);
        mAddButton = (Button) findViewById(R.id.add_button);

        // Add item to database when add button is clicked.
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = mEditText.getText().toString();

                if (TextUtils.isEmpty(itemName)) {
                    Toast.makeText(NewItemActivity.this, "Item name can't be blank", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(ItemEntry.COLUMN_NAME, itemName);

                Uri resultUri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);

                long id = ContentUris.parseId(resultUri);
                if (id < 0) {
                    Toast.makeText(NewItemActivity.this, "Item could not be inserted.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mEditText.setText("");
                Toast.makeText(NewItemActivity.this, "Item was successfully added", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
