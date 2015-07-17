package ru.rian.riamessenger.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;


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
    public static void styleSearchView(@NonNull SearchView searchView, @NonNull Context context) {
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            //searchPlate.setBackground(context.getResources().getDrawable(R.drawable.search_view_bg));
            // Set the search plate color to white
            int linlayId = context.getResources().getIdentifier("android:id/search_plate", null, null);
            View view = searchView.findViewById(linlayId);
           // view.setBackgroundResource(R.drawable.search_view_bg);

            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                // searchText.setTextColor(Color.WHITE);
                // searchText.setHintTextColor(Color.WHITE);
                //searchText.setHint(context.getResources().getString(R.string.discovery_search_hint));
            }
        }
        searchView.setFocusable(true);
        searchView.setIconifiedByDefault(true);
        //searchView.onActionViewExpanded();
    }
}
