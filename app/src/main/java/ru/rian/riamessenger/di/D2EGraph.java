package ru.rian.riamessenger.di;


import ru.rian.riamessenger.ContactsActivity;
import ru.rian.riamessenger.LoginActivity;
import ru.rian.riamessenger.StartActivity;
import ru.rian.riamessenger.fragments.BaseTabFragment;
import ru.rian.riamessenger.RiaApplication;
import ru.rian.riamessenger.loaders.base.BaseRiaLoader;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.D2ECollectionUtils;
import ru.rian.riamessenger.xmpp.SmackRosterListener;

public interface D2EGraph {

    void inject(RiaApplication app);
    void inject(LoginActivity loginActivity);
    void inject(StartActivity startActivity);
    void inject(ContactsActivity contactsActivity);
    void inject(BaseTabFragment fragment);

    void inject(UserAppPreference userAppPreference);
    void inject(SmackRosterListener listener);

    void inject(BaseRiaLoader loader);
    void inject(RiaXmppService riaXmppService);

    //void inject(XmppConnectionService xmppConnectionService);

    D2ECollectionUtils getD2EStringUtils();




}
