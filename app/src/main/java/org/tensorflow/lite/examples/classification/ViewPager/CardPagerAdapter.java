package org.tensorflow.lite.examples.classification.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import org.tensorflow.lite.examples.classification.R;

import java.util.ArrayList;
import java.util.List;

//自定义卡片页面适配器
public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    private final static String TAG = "CardPagerAdapter";

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;

    public CardPagerAdapter(){
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item){
        mViews.add(null);
        mData.add(item);
    }
    public float getBaseElevation(){
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入
        //根据上下文，寻找子项card的xml布局文件（adapter），并且实例化，false表示只让我们在父布局中声明的layout属性生效，
        //但不会为这个view添加父布局
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter,container,false);
        container.addView(view);

        //根据position，获取当前的CardItem,再对与其对应的view中的控件进行赋值
        bind(mData.get(position), view);

        //获取adapter.xml布局文件下的CardView控件
        CardView cardView = view.findViewById(R.id.cardView);

        if(mBaseElevation==0){
            mBaseElevation=cardView.getCardElevation();
        }
        cardView.setMaxCardElevation(mBaseElevation*MAX_ELEVATION_FACTOR);
        mViews.set(position,cardView);

        Log.d(TAG, "instantiateItem: "+position);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
        mViews.set(position,null);
    }

    //获取每个CardItem 中的的内容，并赋值给view中相应的控件
    public void bind(CardItem item, View view){
        TextView title_textView = view.findViewById(R.id.titleTextView);
        TextView content_textView = view.findViewById(R.id.contentTextView);
        title_textView.setText(item.getTitle());
        content_textView.setText(item.getText());
    }
}
