package ru.rian.riamessenger;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.fragments.ContactsAddNewRoomFragment;


public class AddNewRoomActivity extends RiaBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_add_new_room);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ContactsAddNewRoomFragment contactsAddNewRoomFragment = new ContactsAddNewRoomFragment();
        contactsAddNewRoomFragment.setArguments(getIntent().getExtras());
        fragmentTransaction.replace(R.id.container, contactsAddNewRoomFragment, BaseTabFragment.CONTACTS_FRAGMENT_TAG).commit();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            /*case android.R.id.home:
                finish();
                //NavUtils.navigateUpFromSameTask(this);
                return true;*/
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

}
