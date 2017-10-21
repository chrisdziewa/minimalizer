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

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

/**
 * Created by Chris on 8/31/2017.
 */

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ClutterHolder> {

    private static final String TAG = "ItemAdapter";

    private Cursor mCursor;
    private Context mContext;

    public ItemAdapter(@NonNull Context context) {
        mContext = context;
        swapCursor(null);
    }

    void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ClutterHolder holder, int position) {
        mCursor.moveToPosition(position);

        int idIndex = mCursor.getColumnIndex(ItemEntry._ID);
        int nameIndex = mCursor.getColumnIndex(ItemEntry.COLUMN_NAME);
        int keptIndex = mCursor.getColumnIndex(ItemEntry.COLUMN_KEEP);

        final long _id = mCursor.getLong(idIndex);
        String itemName = mCursor.getString(nameIndex);
        boolean isKept = mCursor.getInt(keptIndex) == 1;

        holder.itemName.setText(itemName);
        holder.itemCheckBox.setChecked(isKept);
        final boolean checked = holder.itemCheckBox.isChecked();

        holder.itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                // flip checked
                values.put(ItemEntry.COLUMN_KEEP, (checked ? 0 : 1));
                Log.i(TAG, "onClick: id = " + _id);
                Uri uri = Uri.parse(ItemEntry.CONTENT_URI + "/" + String.valueOf(_id));
                int rowsAffected = mContext.getContentResolver().update(uri, values, ItemEntry._ID + " = ?", new String[]{String.valueOf(_id)});
                Log.i(TAG, "rowsAffected = " + rowsAffected);
            }
        });
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

    class ClutterHolder extends RecyclerView.ViewHolder {

        final TextView itemName;
        final CheckBox itemCheckBox;

        public ClutterHolder(View itemView) {
            super(itemView);

            // Get the viewholder components
            itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemCheckBox = (CheckBox) itemView.findViewById(R.id.keep_checkbox);
        }
    }
}
