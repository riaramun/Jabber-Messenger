package ru.rian.riamessenger;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.utils.ScreenUtils;


public class AddNewRoomActivity extends RiaBaseActivity {

    DialogFragment searchFilterDialog;
    MenuItem addNewsLineMenuItem;
    MenuItem filterMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_add_new_room);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*@Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_new_line, menu);

       /* addNewsLineMenuItem = menu.findItem(R.id.action_add_news_line);

        final EditText addNewLineEditView = (EditText) menu.findItem(R.id.add_new_line_edit).getActionView();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        addNewLineEditView.setMinWidth(displayMetrics.widthPixels - getResources().getDimensionPixelSize(R.dimen.action_btn_width) );

        addNewLineEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (addNewLineEditView.getText().length() >= 3) {
                    addNewsLineMenuItem.getActionView().findViewById(R.id.add_news_line_icon_text_view).setEnabled(true);
                } else {
                    addNewsLineMenuItem.getActionView().findViewById(R.id.add_news_line_icon_text_view).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        MenuItemCompat.getActionView(addNewsLineMenuItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenUtils.hideKeyboard(AddNewRoomActivity.this);
                if (addNewLineEditView.getText().length() >= 3) {

                }

            }
        });

        addNewLineEditView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ScreenUtils.hideKeyboard(AddNewRoomActivity.this);
                }
                return true;
            }
        });*/

      /*  addNewsLineMenuItem = menu.findItem(R.id.action_add_news_line);
        final EditText addNewLineEditView = (EditText) menu.findItem(R.id.add_new_line_edit).getActionView();
       // final EditText addNewLineEditView = (EditText) linearLayout.findViewById(R.id.edit_query);
       */
        return true;
    }
}
