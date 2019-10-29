package org.tensorflow.lite.examples.classification.ViewPager;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class LoopPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE=0.9f;
    @Override
    public void transformPage(@NonNull View page, float position) {
        Log.d("test", "transformPage: received pos:"+position);
        if(position<-1)
            position=-1;
        else if (position>1)
            position=1;

        float tempScale = position<0 ? 1+position:1-position;
        float scaleValue = MIN_SCALE + tempScale*0.1f;

       // Log.d("test", "transformPage: position:"+position+",tempScale:"+tempScale+",scaleValue"+scaleValue);
        page.setScaleX(scaleValue);
        page.setScaleY(scaleValue);
    }
}
