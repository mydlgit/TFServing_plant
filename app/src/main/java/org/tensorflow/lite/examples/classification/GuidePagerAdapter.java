package org.tensorflow.lite.examples.classification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class GuidePagerAdapter extends PagerAdapter {
    private ArrayList<View> mViewList = new ArrayList<>();

    public GuidePagerAdapter(Context context, int[] imageArray){
        LayoutInflater mInflater = LayoutInflater.from(context);

        for(int i=0;i<imageArray.length;i++){
            View view = mInflater.inflate(R.layout.quick_guide_page, null);
            ImageView iv_guide = view.findViewById(R.id.iv_guide);
            Button btn_start = view.findViewById(R.id.btn_start);
            iv_guide.setImageResource(imageArray[i]);
            if(i==imageArray.length-1){
                btn_start.setVisibility(View.VISIBLE);
                btn_start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        ((Activity)context).finish();
                    }
                });
            }
            mViewList.add(view);
        }
    }

    @Override
    public int getCount(){
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViewList.get(position));
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }
}
