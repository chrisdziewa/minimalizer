package com.chrisdziewa.minimalizer;

/**
 * Created by Chris on 8/31/2017.
 */

public class Item {
    private long mId;
    private String mName;
    private boolean mKept;

    public Item(long id, String name, boolean keep) {
        mId = id;
        mName = name;
        mKept = keep;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isKept() {
        return mKept;
    }

    public void setKept(boolean kept) {
        mKept = kept;
    }

    public long getId() {
        return mId;
    }
}
