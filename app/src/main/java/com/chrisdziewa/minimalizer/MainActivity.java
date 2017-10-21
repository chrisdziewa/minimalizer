package com.chrisdziewa.minimalizer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    // Navigation Drawer Variables
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mDrawerLinks;

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

        // Setup navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_list);
        mDrawerLinks = getResources().getStringArray(R.array.navigation_list);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this, R.layout.navigation_list_item, mDrawerLinks)
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 3) {
                    PreferenceFragment fragment = new SettingsFragment();
                    getFragmentManager()
                            .beginTransaction()
                            .add(fragment, "Preference Fragment")
                            .commit();
                }
            }
        });

        // End navigation bar setup
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportLoaderManager().initLoader(ITEM_LOADER_ID, null, this);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_add).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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
