package ru.rian.riamessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoButton;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.RiaEventBus;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.request.RiaServiceEvent;
import ru.rian.riamessenger.riaevents.response.XmppErrorEvent;
import ru.rian.riamessenger.utils.NetworkStateManager;
import ru.rian.riamessenger.utils.ScreenUtils;
import ru.rian.riamessenger.utils.SysUtils;

public class LoginActivity extends RiaBaseActivity {



    @Inject
    public UserAppPreference userAppPreference;

    @Bind(R.id.enter_button)
    RobotoButton enterButton;

    @Bind(R.id.name_edit_text)
    EditText nameEditText;

    @Bind(R.id.login_edit_text)
    EditText loginEditText;

    @Bind(R.id.password_edit_text)
    EditText passwordEditText;

    @Bind(R.id.progress_bar)
    ProgressBarCircularIndeterminate progressBar;

    @OnTextChanged({R.id.login_edit_text, R.id.password_edit_text, R.id.name_edit_text})
    void onTextChanged(CharSequence text) {
        changeOkButtonVisibility();
    }

    @OnEditorAction(R.id.password_edit_text)
    boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == 101 || actionId == EditorInfo.IME_ACTION_DONE) {
            requestLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.enter_button)
    public void onClick() {
        requestLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RiaBaseApplication.component().inject(this);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        passwordEditText.setImeActionLabel(getString(R.string.Sign_in), EditorInfo.IME_ACTION_DONE);

        String login = userAppPreference.getLoginStringKey();
        String pass = userAppPreference.getPassStringKey();
        String name = userAppPreference.getFirstSecondName();

        nameEditText.setText(name);
        loginEditText.setText(login);
        passwordEditText.setText(pass);

        // loginEditText.setText(RiaConstants.XMPP_LOGIN);
        // passwordEditText.setText(RiaConstants.XMPP_PASS);
    }
    /*@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        RiaBaseApplication.component().inject(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }*/
    @Override
    public void onStart() {
        super.onStart();
        String token = userAppPreference.getUserStringKey();
        String login = userAppPreference.getLoginStringKey();
        String pass = userAppPreference.getPassStringKey();
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(login) && !TextUtils.isEmpty(pass)) {
            // requestLogin();
        } else {
            loginEditText.requestFocus();
        }
    }

    void requestLogin() {
        ScreenUtils.hideKeyboard(this);
        if(NetworkStateManager.isNetworkAvailable(this)) {
            if (progressBar.getVisibility() != View.VISIBLE && enterButton.isEnabled()) {
                progressBar.setVisibility(View.VISIBLE);

                userAppPreference.setFirstSecondName(nameEditText.getText().toString().trim());
                userAppPreference.setLoginStringKey(loginEditText.getText().toString().trim());
                userAppPreference.setPassStringKey(passwordEditText.getText().toString());


                if (SysUtils.isMyServiceRunning(RiaXmppService.class, this)) {
                    RiaEventBus.post(RiaServiceEvent.RiaEvent.TO_SIGN_IN);
                } else {
                    Intent intent = new Intent(this, RiaXmppService.class);
                    startService(intent);
                }
            }
        } else {
            showAppMsgInView(LoginActivity.this, getString(R.string.connectionFailure));
        }

    }

    boolean IsFieldNotEmpty(EditText aEditText) {
        String editText = aEditText.getText().toString().trim();
        return !TextUtils.isEmpty(editText);
    }

    void changeOkButtonVisibility() {
        boolean allFieldsNotEmpty = IsFieldNotEmpty(loginEditText) & IsFieldNotEmpty(nameEditText) &
                IsFieldNotEmpty(passwordEditText);
        enterButton.setEnabled(allFieldsNotEmpty);
    }


    public void onEvent(final XmppErrorEvent xmppErrorEvent) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (xmppErrorEvent.state) {
                    case EAuthenticated:
                    case EDbUpdated:
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(LoginActivity.this, ChatsActivity.class);
                        startActivity(intent);
                        break;
                    case EAuthenticationFailed:
                        progressBar.setVisibility(View.INVISIBLE);
                        showAppMsgInView(LoginActivity.this, getString(R.string.authorizationFailure));
                        break;
                    default:
                        LoginActivity.super.onEvent(xmppErrorEvent);
                        break;
                }
            }
        });
    }
}
