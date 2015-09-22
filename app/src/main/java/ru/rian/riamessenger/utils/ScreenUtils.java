package ru.rian.riamessenger.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import ru.rian.riamessenger.R;


/**
 * Created by Roman on 16.04.2015.
 */
public class ScreenUtils {

    static public void hideKeyboard(Context aContext) {
        // Check if no view has focus:
        View view = ((Activity) aContext).getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) aContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void styleSearchView(@NonNull SearchView searchView, String title_to_search, @NonNull Context context) {
        searchView.setQuery("", true);
        searchView.setQueryHint(context.getString(R.string.search_hint));
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
            }
            searchPlate.setBackgroundResource(R.drawable.search_view_bg);
        }

        if (TextUtils.isEmpty(title_to_search)) {
            searchView.setIconified(true);
        } else {
            searchView.setIconified(false);
            searchView.setQuery(title_to_search, true);
        }
    }
}
