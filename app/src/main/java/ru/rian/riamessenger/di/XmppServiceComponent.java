package ru.rian.riamessenger.di;


import javax.inject.Singleton;

import dagger.Component;
import ru.rian.riamessenger.common.RiaBaseActivity;

/**
 * This class is in debug/ folder. You can use it to define injects or getters for dependencies only in debug variant
 */
@Singleton
@Component(modules = {XmppModule.class, SystemServicesModule.class})
public interface XmppServiceComponent extends XmppServiceGraph{

}
