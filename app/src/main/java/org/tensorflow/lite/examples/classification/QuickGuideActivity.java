package org.tensorflow.lite.examples.classification;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class QuickGuideActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private RadioGroup rg_indicate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_guide);
        int[] imageArray = {R.drawable.guide1, R.drawable.guide1, R.drawable.guide1};
        GuidePagerAdapter guidePagerAdapter = new GuidePagerAdapter(this, imageArray);
        ViewPager vp_guide = findViewById(R.id.vp_guide);
        vp_guide.setAdapter(guidePagerAdapter);
        vp_guide.setCurrentItem(0);
        vp_guide.addOnPageChangeListener(this);
        rg_indicate = findViewById(R.id.rg_indicate);
        for(int i=0;i<imageArray.length;i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            radioButton.setPadding(10, 10, 10, 10);
            rg_indicate.addView(radioButton);
        }
        ((RadioButton)rg_indicate.getChildAt(0)).setChecked(true);

    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ((RadioButton)rg_indicate.getChildAt(position)).setChecked(true);
    }
}