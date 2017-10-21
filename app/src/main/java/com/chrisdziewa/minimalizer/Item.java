package com.chrisdziewa.minimalizer;

/**
 * Created by Chris on 8/31/2017.
 */

public class Item {
    private String mName;
    private boolean mKept;

    public Item(String name, boolean keep) {
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
}
