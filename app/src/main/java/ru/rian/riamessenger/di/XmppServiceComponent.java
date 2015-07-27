package ru.rian.riamessenger.di;


import javax.inject.Singleton;

import dagger.Component;

/**
 * This class is in debug/ folder. You can use it to define injects or getters for dependencies only in debug variant
 */
@Singleton
@Component(modules = {XmppModule.class, AppSystemModule.class})
public interface XmppServiceComponent extends XmppServiceGraph{

}
