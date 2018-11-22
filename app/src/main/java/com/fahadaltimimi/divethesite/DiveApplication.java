package com.fahadaltimimi.divethesite;

import android.app.Application;

public class DiveApplication extends Application {

  public static boolean isActivityVisible() {
    return activityVisible;
  }  

  public static void activityResumed() {
    activityVisible = true;
  }

  public static void activityPaused() {
    activityVisible = false;
  }

  private static boolean activityVisible;
}
