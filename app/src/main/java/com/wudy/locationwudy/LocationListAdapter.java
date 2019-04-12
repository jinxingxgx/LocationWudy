package com.wudy.locationwudy;

import com.allen.library.SuperTextView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.wudy.locationwudy.bean.MapLocationBean;

import java.util.List;

/**
 * Created by xgx on 2019/4/12 for LocationWudy
 */
public class LocationListAdapter extends BaseQuickAdapter<MapLocationBean, BaseViewHolder> {
    public static final int HEAD = 0;
    public static final int CONTENT = 1;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public LocationListAdapter(List<MapLocationBean> data) {
        super(R.layout.adpater_location, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MapLocationBean item) {
        SuperTextView stv = helper.getView(R.id.stv);
        stv.setLeftString("最后一次定位：" + item.getAddress());
        stv.setLeftBottomString("时间：" + item.getTime());
        stv.setLeftTopString("定位号：" + item.getIndexId());
    }
}
