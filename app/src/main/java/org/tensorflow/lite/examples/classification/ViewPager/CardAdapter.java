package org.tensorflow.lite.examples.classification.ViewPager;

import androidx.cardview.widget.CardView;

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR=8;
    float getBaseElevation();
    CardView getCardViewAt(int position);
    int getCount();
}
