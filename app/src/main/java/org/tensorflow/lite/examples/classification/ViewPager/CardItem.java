package org.tensorflow.lite.examples.classification.ViewPager;

public class CardItem {
    private int mTextResource;//卡片的内容
    private int mTitleResorce;//卡片的标题

    public CardItem(int title,int text){
        mTextResource = text;
        mTitleResorce = title;
    }

    public int getText() {
        return mTextResource;
    }

    public int getTitle() {
        return mTitleResorce;
    }
}
