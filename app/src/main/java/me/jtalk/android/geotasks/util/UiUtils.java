package me.jtalk.android.geotasks.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class UiUtils {

    public static void inflateLayoutWithId(Activity owner, int parentViewId, int viewToInflate, int index) {
        ViewGroup parentView = (ViewGroup) owner.findViewById(parentViewId);
        View mapView = owner.getLayoutInflater().inflate(viewToInflate, parentView, false);
        parentView.addView(mapView, index);
    }

}
