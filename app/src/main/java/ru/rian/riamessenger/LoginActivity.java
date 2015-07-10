package ru.rian.riamessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import ru.rian.riamessenger.common.RiaBaseActivity;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.riaevents.client.AuthClientEvent;
import ru.rian.riamessenger.riaevents.service.RiaServiceEvent;

public class LoginActivity extends RiaBaseActivity {


    @Inject
    UserAppPreference userAppPreference;

    @Bind(R.id.login_edit_text)
    EditText loginEditText;

    @Bind(R.id.password_edit_text)
    EditText passwordEditText;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    @OnTextChanged({R.id.login_edit_text, R.id.password_edit_text})
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

        progressBar.setVisibility(View.GONE);
        passwordEditText.setImeActionLabel(getString(R.string.enter), EditorInfo.IME_ACTION_DONE);

        String login = userAppPreference.getLoginStringKey();
        String pass = userAppPreference.getPassStringKey();


        loginEditText.setText(login);
        passwordEditText.setText(pass);

        loginEditText.setText(RiaConstants.XMPP_LOGIN);
        passwordEditText.setText(RiaConstants.XMPP_PASS);
    }

    public void onEvent(AuthClientEvent eventName) {
        progressBar.setVisibility(View.GONE);
        if (eventName.isAuth()) {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String token = userAppPreference.getTokenStringKey();
        String login = userAppPreference.getLoginStringKey();
        String pass = userAppPreference.getPassStringKey();
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(login) && !TextUtils.isEmpty(pass)) {
            // requestLogin();
        } else {
            loginEditText.requestFocus();
        }
    }

    private void requestLogin() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);

            userAppPreference.setLoginStringKey(loginEditText.getText().toString());
            userAppPreference.setPassStringKey(passwordEditText.getText().toString());
            EventBus.getDefault().post(new RiaServiceEvent(RiaServiceEvent.RiaEvent.SIGN_IN));
        }
    }

    protected MenuItem mActionOkMenuItem;

    protected boolean IsFieldNotEmpty(EditText aEditText) {
        return !aEditText.getText().toString().isEmpty();
    }

    protected void changeOkButtonVisibility() {
        boolean allFieldsNotEmpty = IsFieldNotEmpty(loginEditText) &
                IsFieldNotEmpty(passwordEditText);
        if (mActionOkMenuItem != null) {
            mActionOkMenuItem.setVisible(allFieldsNotEmpty);
        }
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_login, menu);
        mActionOkMenuItem = menu.findItem(R.id.action_ok);
        mActionOkMenuItem.setVisible(false);
        MenuItemCompat.getActionView(mActionOkMenuItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLogin();
            }
        });
    }*/
}
