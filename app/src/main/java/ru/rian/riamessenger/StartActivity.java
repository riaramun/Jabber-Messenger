package ru.rian.riamessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import io.fabric.sdk.android.Fabric;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.SysUtils;

public class StartActivity extends RiaBaseActivity {

    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.progress_bar)
    ProgressBarCircularIndeterminate progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        RiaBaseApplication.component().inject(this);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);

        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        final String password = userAppPreference.getPassStringKey();

        if (SysUtils.isMyServiceRunning(RiaXmppService.class, this)) {
            RiaEventBus.post(RiaServiceEvent.RiaEvent.TO_SIGN_IN);
        } else {
            Intent intent = new Intent(this, RiaXmppService.class);
            startService(intent);
        }
        launchNextActivity(TextUtils.isEmpty(password) ? false : true);
    }

    /*public void onEvent(AuthClientEvent event) {
        launchNextActivity(event.isAuth());
    }*/

    void launchNextActivity(boolean isAuth) {
        Class<?> cl = null;
        if (isAuth) {
            cl = ChatsActivity.class;
        } else {
            cl = LoginActivity.class;
        }
        Intent intent = new Intent(StartActivity.this, cl);
        startActivity(intent);
    }
}
