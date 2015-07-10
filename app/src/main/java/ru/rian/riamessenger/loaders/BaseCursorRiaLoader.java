/*
 * Copyright (c) 2014. We Communicate INC.
 * All rights reserved.
 */

package ru.rian.riamessenger.loaders;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Vladimir Molodkin on 03/12/14.
 * fisher3421@gmail.com
 */
public abstract class BaseCursorRiaLoader<T> extends AsyncTaskLoader<BaseCursorRiaLoader.LoaderResult<T>> {

    protected ForceLoadContentObserver mContentObserver;
    private   LoaderResult<T>          mData;

    public BaseCursorRiaLoader(Context context) {
        super(context);
        mContentObserver = new ForceLoadContentObserver();
    }

    @Override
    public void onCanceled(LoaderResult<T> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    @Override
    public final LoaderResult<T> loadInBackground() {
        LoaderResult<T> loaderResult = new LoaderResult<T>();
        try {
            loaderResult.result = load();
        } catch (Exception e) {
            loaderResult.exception = e;
        }
        return loaderResult;
    }

    protected abstract T load() throws Exception;

    @Override
    public void deliverResult(LoaderResult<T> data) {
        if (isReset()) {
            releaseResources(data);
            return;
        }
        LoaderResult oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    public static class LoaderResult<T> {
        public T         result;
        public Exception exception;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }
    }


    protected abstract void releaseResources(LoaderResult<T> data);


}
