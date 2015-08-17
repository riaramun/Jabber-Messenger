/*
 * Copyright (c) 2014. We Communicate INC.
 * All rights reserved.
 */

package ru.rian.riamessenger.loaders.base;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Vladimir Molodkin on 03/12/14.
 * fisher3421@gmail.com
 */
public abstract class CursorRiaLoader extends BaseCursorRiaLoader<Cursor> {

    private Uri mSubscription;
    private Context mContext;

    public CursorRiaLoader(Context context) {
        super(context);
        setUpdateThrottle(1000);
        mContext = context;
    }

    @Override
    protected final Cursor load() throws Exception {
        Cursor cursor = loadCursor();
        if (mSubscription != null) {
            cursor.registerContentObserver(mContentObserver);
            cursor.setNotificationUri(mContext.getContentResolver(), mSubscription);
        }
        return cursor;
    }

    @Override
    protected void releaseResources(LoaderResult<Cursor> data) {
        if (data != null && data.result != null && !data.result.isClosed()) {
            data.result.close();
        }
    }

    protected abstract Cursor loadCursor() throws Exception;

    public void setSubscription(Uri subscription) {
        mSubscription = subscription;
    }
}
