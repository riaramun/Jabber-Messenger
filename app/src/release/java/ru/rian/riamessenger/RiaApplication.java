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
import ru.rian.riamessenger.di.D2EComponent;
import ru.rian.riamessenger.di.SystemServicesModule;

public class RiaApplication extends Application {
  private D2EComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    buildComponentAndInject();
  }

  public void buildComponentAndInject() {
    component = DaggerComponentInitializer.init(this);
  }

  public static D2EComponent component(Context context) {
    return ((RiaBaseApplication) context.getApplicationContext()).component;
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public final static class DaggerComponentInitializer {

    public static D2EComponent init(RiaBaseApplication app) {
      return DaggerD2EComponent.builder()
              .systemServicesModule(new SystemServicesModule(app))
              .build();
    }

  }
}
