/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.rian.riamessenger;

import android.app.Application;
import android.content.Context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.rian.riamessenger.di.AppComponent;
import ru.rian.riamessenger.di.AppSystemModule;

public class RiaApplication extends Application {
  private AppComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    buildComponentAndInject();
  }

  public void buildComponentAndInject() {
    component = DaggerComponentInitializer.init(this);
  }

  public static AppComponent component(Context context) {
    return ((RiaBaseApplication) context.getApplicationContext()).component;
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public final static class DaggerComponentInitializer {

    public static AppComponent init(RiaBaseApplication app) {
      return DaggerD2EComponent.builder()
              .systemServicesModule(new AppSystemModule(app))
              .build();
    }

  }
}
