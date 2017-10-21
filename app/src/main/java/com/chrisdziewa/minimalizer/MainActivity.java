package com.chrisdziewa.minimalizer;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";
    private static final String SHOW_ADD_BAR_KEY = "showAddBar";

    public static final String[] ITEMS_PROJECTION = {
            ItemEntry._ID,
            ItemEntry.COLUMN_NAME,
            ItemEntry.COLUMN_KEEP
    };
    private static final String INPUT_TEXT_KEY = "inputText";

    private ItemAdapter mItemAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mAddButtonBar;
    private EditText mAddItemEditText;
    private ImageButton mAddButton;
    private Boolean mShowAddBar;
    private int mPosition;

    private static final int ITEM_LOADER_ID = 300;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddButtonBar = (LinearLayout) findViewById(R.id.add_item_bar);
        mAddItemEditText = (EditText) findViewById(R.id.new_item_edit_text);
        if (savedInstanceState != null) {
            mShowAddBar = savedInstanceState.getBoolean(SHOW_ADD_BAR_KEY, false);
            mAddItemEditText.setText(savedInstanceState.getString(INPUT_TEXT_KEY));
        }
        else {
            mShowAddBar = false;
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mItemAdapter = new ItemAdapter(this);
        mRecyclerView.setAdapter(mItemAdapter);


        // https://stackoverflow.com/questions/19217582/implicit-submit-after-hitting-done-on-the-keyboard-at-the-last-edittext
        if (mShowAddBar) {
            mAddButtonBar.setVisibility(View.VISIBLE);
        } else {
            mAddButtonBar.setVisibility(View.GONE);
        }

        getSupportLoaderManager().initLoader(ITEM_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clutter_list, menu);
        if (mShowAddBar) {
            menu.findItem(R.id.action_add).setIcon(R.drawable.ic_clear_white);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:
                mShowAddBar = !mShowAddBar;

                if (mShowAddBar) {
                    mAddItemEditText.requestFocus();
                    mAddButtonBar.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_clear_white);
                    mAddButton = mAddButtonBar.findViewById(R.id.add_item_button);

                    // click from keyboard to add item thanks to user Hariharan
                    mAddItemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                            if (id == EditorInfo.IME_ACTION_DONE) {
                                mAddButton.performClick();
                                return true;
                            }

                            return false;
                        }
                    });

                    mAddButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String currentText = mAddItemEditText.getText().toString();
                            if (!TextUtils.isEmpty(currentText)) {
                                ContentValues values = new ContentValues();
                                values.put(ItemEntry.COLUMN_NAME, currentText);
                                getContentResolver().insert(ItemEntry.CONTENT_URI, values);
                                // Clear input
                                mAddItemEditText.setText("");
                            }
                        }
                    });
                } else {
                    item.setIcon(R.drawable.ic_add_white);
                    mAddButtonBar.setVisibility(View.GONE);

                    // Hide soft keyboard
                    // thanks to rmirabelle
                    // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view = getCurrentFocus();
                    if (view == null) {
                        view = new View(getApplicationContext());
                    }

                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SHOW_ADD_BAR_KEY, mShowAddBar);
        outState.putString(INPUT_TEXT_KEY, mAddItemEditText.getText().toString());
        Log.i(TAG, "onSaveInstanceState: " + mShowAddBar);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != ITEM_LOADER_ID) {
            throw new RuntimeException("Loader Not Implemented: " + id);
        }

        return new CursorLoader(getApplicationContext(),
                ItemEntry.CONTENT_URI,
                ITEMS_PROJECTION, null, null,
                ItemEntry._ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            mItemAdapter.swapCursor(data);

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemAdapter.swapCursor(null);
    }
}
