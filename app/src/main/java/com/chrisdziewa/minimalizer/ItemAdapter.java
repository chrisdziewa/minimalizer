package com.chrisdziewa.minimalizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;
import com.chrisdziewa.minimalizer.data.ItemCursorWrapper;

import java.util.ArrayList;

/**
 * Created by Chris on 8/31/2017.
 */

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ClutterHolder> {

    private static final String TAG = "ItemAdapter";

    private final static float KEEP_RATIO = (float) 1 / 4;

    private Cursor mCursor;
    private Context mContext;
    private ArrayList<Item> mPositionList;

    public ItemAdapter(@NonNull Context context) {
        mContext = context;
        swapCursor(null);
        mPositionList = new ArrayList<>();
    }

    void swapCursor(Cursor newCursor) {
        if (newCursor == null) {
            return;
        }

        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;
        mPositionList = new ArrayList<>(mCursor.getCount());
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ClutterHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Thanks to user Pli for help with only adding indices that aren't already there
        // https://stackoverflow.com/questions/2131802/java-arraylist-how-can-i-check-if-an-index-exists
        try {
            mPositionList.get(position);
        } catch (IndexOutOfBoundsException e) {
            mPositionList.add(position, new ItemCursorWrapper(mCursor).getItem());
        }

        Item item = mPositionList.get(position);

        holder.itemName.setText(item.getName());
        holder.itemCheckBox.setChecked(item.isKept());
        holder.item = item;
        //TODO Needs to be updated in object because every time view is recycled, a number is added to checked count

//        holder.itemCheckBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int position = holder.getAdapterPosition();
//                Item item = mPositionList.get(position);
//                long id = item.getId();
//
//                // check item to see if it is being kept or not
//                item.setKept(!item.isKept());
//
//                // Make sure we haven't surpassed our limit
//                if (item.isKept() &&
//                        (getCheckedCount() / getItemCount()) > KEEP_RATIO) {
//                    holder.itemCheckBox.setChecked(false);
//                    return;
//                } else if (!item.isKept()) {
//                    holder.itemCheckBox.setChecked(false);
//                } else {
//                    holder.itemCheckBox.setChecked(true);
//                }
//
//                ContentValues values = new ContentValues();
//                // Change the checkbox value in the database
//                values.put(ItemEntry.COLUMN_KEEP, item.isKept() ? 1 : 0);
//                // Update database if changed
//                Uri uri = Uri.parse(ItemEntry.CONTENT_URI + "/" + String.valueOf(id));
//                int rowsAffected = mContext.getContentResolver().update(uri, values, ItemEntry._ID + " = ?", new String[]{String.valueOf(id)});
//            }
//    });

}

    public int getCheckedCount() {
        int checkedCount = 0;
        for (Item item : mPositionList) {
            if (item.isKept()) {
                checkedCount += 1;
            }
        }

        return checkedCount;
    }

    @Override
    public ClutterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.clutter_list_item, parent, false);

        ClutterHolder holder = new ClutterHolder(view);

        return holder;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

class ClutterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    final TextView itemName;
    final CheckBox itemCheckBox;
    Item item;

    public ClutterHolder(View itemView) {
        super(itemView);

        // Get the viewholder components
        itemName = (TextView) itemView.findViewById(R.id.item_name);
        itemCheckBox = (CheckBox) itemView.findViewById(R.id.keep_checkbox);
        itemCheckBox.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // check item to see if it is being kept or not
        item.setKept(!item.isKept());

        Log.i(TAG, "======================================");
        Log.i(TAG, "getCheckedCount: " + getCheckedCount());
        Log.i(TAG, "cursor count: " + getItemCount());
        Log.i(TAG, "ratio = " + (getCheckedCount() / getItemCount()));
        Log.i(TAG, "KEEP_RATIO: " + KEEP_RATIO);

        // Make sure we haven't surpassed our limit
        if (item.isKept() &&
                ((float) getCheckedCount() / getItemCount()) > KEEP_RATIO) {
            itemCheckBox.setChecked(false);
            Toast.makeText(mContext, "Only a quarter of the items on your list may be kept", Toast.LENGTH_SHORT).show();
            return;
        } else if (!item.isKept()) {
            itemCheckBox.setChecked(false);
        } else {
            itemCheckBox.setChecked(true);
        }

        ContentValues values = new ContentValues();
        // Change the checkbox value in the database
        values.put(ItemEntry.COLUMN_KEEP, item.isKept() ? 1 : 0);
        // Update database if changed
        Uri uri = Uri.parse(ItemEntry.CONTENT_URI + "/" + String.valueOf(item.getId()));
        int rowsAffected = mContext.getContentResolver().update(uri, values, ItemEntry._ID + " = ?", new String[]{String.valueOf(item.getId())});
    }
}
}
