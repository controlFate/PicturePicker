package com.frank.lib_picturepicker.picturewatcher;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.bluemobi.dylan.photoview.library.PhotoView;

/**
 * Created by Frank on 2018/5/28.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public class PictureWatcherAdapter extends PagerAdapter {

    private List<PhotoView> mPhotoViews;

    PictureWatcherAdapter(List<PhotoView> children) {
        this.mPhotoViews = children;
    }

    /**
     * 获取子级布局的数量
     */
    @Override
    public int getCount() {
        return mPhotoViews.size();
    }

    /**
     * 判断某个 View 对象是否为当前被添加到 ViewPager 容器中的对象
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 实例化 ViewPager 容器中指定的 position 位置需要显示的 View 对象
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mPhotoViews.get(position);
        container.addView(view);
        return view;
    }

    /**
     * 在ViewPager中移除指定的 position 位置的 view 对象
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = mPhotoViews.get(position);
        container.removeView(view);
    }

}