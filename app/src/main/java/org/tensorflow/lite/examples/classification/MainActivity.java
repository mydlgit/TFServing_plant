package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.tensorflow.lite.examples.classification.ViewPager.CardItem;
import org.tensorflow.lite.examples.classification.ViewPager.CardPagerAdapter;
import org.tensorflow.lite.examples.classification.ViewPager.LoopPageTransformer;
import org.tensorflow.lite.examples.classification.ViewPager.ShadowTransformer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ViewPager mViewPager;
    private CardPagerAdapter cardPagerAdapter;
    private ShadowTransformer shadowTransformer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager =  findViewById(R.id.viewPager);
        cardPagerAdapter = new CardPagerAdapter();
        cardPagerAdapter.addCardItem(new CardItem(R.string.title_1, R.string.text_1));
        cardPagerAdapter.addCardItem(new CardItem(R.string.title_2,R.string.text_1));
        cardPagerAdapter.addCardItem(new CardItem(R.string.title_3,R.string.text_1));
        cardPagerAdapter.addCardItem(new CardItem(R.string.title_4, R.string.text_1));

        shadowTransformer = new ShadowTransformer(mViewPager,cardPagerAdapter);
        shadowTransformer.enableScaling(true);

//        transformer = new LoopPageTransformer();

        mViewPager.setAdapter(cardPagerAdapter);
        mViewPager.setPageTransformer(false, shadowTransformer);
        mViewPager.setOffscreenPageLimit(3); //默认加载界面个数


        Button local = findViewById(R.id.local);
        Button online = findViewById(R.id.online);

        local.setOnClickListener(this);
        online.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.local:
                Intent intent = new Intent(MainActivity.this,ClassifierActivity.class);
                startActivity(intent);
                break;
            case R.id.online:
                Intent intent2 = new Intent(MainActivity.this, OnlineInferenceActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }
}
