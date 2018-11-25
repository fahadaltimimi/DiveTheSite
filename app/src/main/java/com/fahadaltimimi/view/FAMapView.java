package com.fahadaltimimi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Custom Map View
 */
public class FAMapView extends MapView {

    public FAMapView(Context context) {
        super(context);
    }

    public FAMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FAMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public FAMapView(Context context, GoogleMapOptions googleMapOptions) {
        super(context, googleMapOptions);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /**
         * Request all parents to relinquish the touch events
         */
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
