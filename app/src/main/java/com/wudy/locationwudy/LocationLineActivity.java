package com.wudy.locationwudy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.wudy.locationwudy.bean.MapLocationBean;
import com.wudy.locationwudy.bean.MapLocationBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 此demo用来展示如何结合定位SDK实现室内定位，并使用MyLocationOverlay绘制定位位置
 */
public class LocationLineActivity extends Activity {

    // 定位相关

    MapView mMapView;
    BaiduMap mBaiduMap;
    static Context mContext;
    // UI相关

    private Marker mMarkerA;
    private Marker mMarkerB;
    private Polyline mPolyline;
    private InfoWindow mInfoWindow;

    @Override
    @AfterPermissionGranted(1)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout layout = new RelativeLayout(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainview = inflater.inflate(R.layout.activity_location_line, null);
        layout.addView(mainview);
        // 地图初始化
        mMapView = (MapView) mainview.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 定位初始化
        setContentView(layout);
        coordinateConvert();
    }

    List<LatLng> latLngs = new ArrayList<LatLng>();
    BitmapDescriptor startBD = BitmapDescriptorFactory
            .fromResource(R.drawable.ic_me_history_startpoint);
    BitmapDescriptor finishBD = BitmapDescriptorFactory
            .fromResource(R.drawable.ic_me_history_finishpoint);

    /**
     * 讲google地图的wgs84坐标转化为百度地图坐标
     */
    private void coordinateConvert() {
        String indexId = getIntent().getStringExtra("indexId");
        QueryBuilder<MapLocationBean> qb = MyApplication.getDaoInstant().getMapLocationBeanDao().queryBuilder();
        if (StringUtils.isEmpty(indexId)) {
            qb.where(MapLocationBeanDao.Properties.IndexId.isNull());
        } else {
            qb.where(MapLocationBeanDao.Properties.IndexId.eq(indexId));
        }
        qb.orderDesc(MapLocationBeanDao.Properties.Time);
        List<MapLocationBean> mapLocations = qb.list();
        if (mapLocations != null) {
            for (int i = 0; i < mapLocations.size(); i++) {
                LatLng sourceLatLng = new LatLng(mapLocations.get(i).getLatitude(), mapLocations.get(i).getLongitude());
                latLngs.add(sourceLatLng);
            }
        }
        if (latLngs.size() < 2) {
            ToastUtils.showShort("记录的点位太少");
            finish();
        }
        MarkerOptions oStart = new MarkerOptions();//地图标记覆盖物参数配置类 
        oStart.position(latLngs.get(0));//覆盖物位置点，第一个点为起点
        oStart.icon(startBD);//设置覆盖物图片
        oStart.zIndex(1);//设置覆盖物Index
        mMarkerA = (Marker) (mBaiduMap.addOverlay(oStart)); //在地图上添加此图层
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLngs.get(0)).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        //添加终点图层
        MarkerOptions oFinish = new MarkerOptions().position(latLngs.get(latLngs.size() - 1)).icon(finishBD).zIndex(2);
        mMarkerB = (Marker) (mBaiduMap.addOverlay(oFinish));
        OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(latLngs);
        mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
        mPolyline.setZIndex(3);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {

                if (marker.getZIndex() == mMarkerA.getZIndex()) {//如果是起始点图层
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText("起点");
                    textView.setTextColor(Color.BLACK);
                    textView.setGravity(Gravity.CENTER);

                    //设置信息窗口点击回调
                    InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                            Toast.makeText(getApplicationContext(), "这里是起点", Toast.LENGTH_SHORT).show();
                            mBaiduMap.hideInfoWindow();//隐藏信息窗口
                        }
                    };
                    LatLng latLng = marker.getPosition();//信息窗口显示的位置点
                    /**
                     * 通过传入的 bitmap descriptor 构造一个 InfoWindow
                     * bd - 展示的bitmap
                     position - InfoWindow显示的位置点
                     yOffset - 信息窗口会与图层图标重叠，设置Y轴偏移量可以解决
                     listener - 点击监听者
                     */
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(textView), latLng, -47, listener);
                    mBaiduMap.showInfoWindow(mInfoWindow);//显示信息窗口

                } else if (marker.getZIndex() == mMarkerB.getZIndex()) {//如果是终点图层
                    Button button = new Button(getApplicationContext());
                    button.setText("终点");
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "这里是终点", Toast.LENGTH_SHORT).show();
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    LatLng latLng = marker.getPosition();
                    /**
                     * 通过传入的 view 构造一个 InfoWindow, 此时只是利用该view生成一个Bitmap绘制在地图中，监听事件由自己实现。
                     view - 展示的 view
                     position - 显示的地理位置
                     yOffset - Y轴偏移量
                     */
                    mInfoWindow = new InfoWindow(button, latLng, -47);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
        });

        mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                if (polyline.getZIndex() == mPolyline.getZIndex()) {
                    Toast.makeText(getApplicationContext(), "点数：" + polyline.getPoints().size() + ",width:" + polyline.getWidth(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        // 关闭定位图层
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

}
