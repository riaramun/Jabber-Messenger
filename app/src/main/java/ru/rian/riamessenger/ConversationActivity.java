package ru.rian.riamessenger;

import android.os.Bundle;

import butterknife.ButterKnife;
import ru.rian.riamessenger.common.RiaBaseActivity;

/**
 * Created by Roman on 7/21/2015.
 */
public class ConversationActivity extends RiaBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);

    }


    @Override
    protected void authenticated(boolean isAuthenticated) {

    }
}