package ru.rian.riamessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.common.utils.SysUtils;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.services.RiaXmppService;

public class StartActivity extends RiaBaseActivity {

    @Inject
    UserAppPreference userAppPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);
        setContentView(R.layout.activity_start);
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        final String username = userAppPreference.getLoginStringKey();
        final String password = userAppPreference.getPassStringKey();

        if (SysUtils.isMyServiceRunning(RiaXmppService.class, this)) {
            RiaEventBus.post(RiaServiceEvent.RiaEvent.SIGN_IN);
        } else {
            Intent intent = new Intent(this, RiaXmppService.class);
            startService(intent);
        }
        launchNextActivity(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) ? false : true);
    }

    /*public void onEvent(AuthClientEvent event) {
        launchNextActivity(event.isAuth());
    }*/

    void launchNextActivity(boolean isAuth) {
        Class<?> cl = null;
        if (isAuth) {
            cl = ContactsActivity.class;
        } else {
            cl = LoginActivity.class;
        }
        Intent intent = new Intent(StartActivity.this, cl);
        startActivity(intent);
    }

    @Override
    protected void authenticated(boolean isAuthenticated) {
       //nothing to do, since in this case the method launchNextActivity starts ContactsActivity
    }
}
