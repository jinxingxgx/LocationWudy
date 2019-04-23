package com.wudy.locationwudy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.wudy.locationwudy.adapter.LocationListAdapter;
import com.wudy.locationwudy.utils.MyApplication;
import com.wudy.locationwudy.R;
import com.wudy.locationwudy.bean.MapLocationBean;
import com.wudy.locationwudy.bean.MapLocationBeanDao;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xgx on 2019/4/12 for LocationWudy
 */
public class LocationListActivity extends AppCompatActivity {
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.listView)
    RecyclerView listView;
    LocationListAdapter mAdapter;
    private List<MapLocationBean> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        ButterKnife.bind(this);
        titlebar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        mAdapter = new LocationListAdapter(list);
        listView.setAdapter(mAdapter);
        listView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(LocationListActivity.this, LocationLineActivity.class);
                intent.putExtra("indexId", mAdapter.getItem(position).getIndexId());
                startActivity(intent);
            }
        });
        QueryBuilder<MapLocationBean> qb = MyApplication.getDaoInstant().getMapLocationBeanDao().queryBuilder();
        qb.where(MapLocationBeanDao.Properties.Address.isNotNull(), new WhereCondition.StringCondition("1=1 GROUP BY " +
                MapLocationBeanDao.Properties.IndexId.columnName));
        qb.orderDesc(MapLocationBeanDao.Properties.Time);
        list = qb.list();
        mAdapter.setNewData(list);
    }
}
