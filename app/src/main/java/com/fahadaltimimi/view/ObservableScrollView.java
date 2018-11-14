package com.fahadaltimimi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Fahad on 2015-05-03.
 */
public class ObservableScrollView extends ScrollView {

    private Runnable scrollerTask;
    private int initialPosition;

    private int newCheck = 100;
    private static final String TAG = "ObservableScrollView";

    public interface OnScrollStoppedListener{
        void onScrollStopped();
        void onScrollChanged(ObservableScrollView v, int l, int t, int oldl, int oldt );
    }

    private OnScrollStoppedListener onScrollStoppedListener;

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scrollerTask = new Runnable() {

            public void run() {

                int newPosition = getScrollY();
                if(initialPosition - newPosition == 0){//has stopped

                    if(onScrollStoppedListener!=null){
                        onScrollStoppedListener.onScrollStopped();
                    }
                } else{
                    initialPosition = getScrollY();
                    ObservableScrollView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
    }

    public void setOnScrollStoppedListener(ObservableScrollView.OnScrollStoppedListener listener){
        onScrollStoppedListener = listener;
    }

    public void startScrollerTask(){
        initialPosition = getScrollY();
        ObservableScrollView.this.postDelayed(scrollerTask, newCheck);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        onScrollStoppedListener.onScrollChanged( this, l, t, oldl, oldt );
        super.onScrollChanged( l, t, oldl, oldt );
    }

}