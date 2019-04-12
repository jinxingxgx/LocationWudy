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

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import com.baidu.mapapi.utils.CoordinateConverter;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.wudy.locationwudy.bean.DaoMaster;
import com.wudy.locationwudy.bean.DaoSession;
import com.wudy.locationwudy.bean.MapLocationBean;
import com.wudy.locationwudy.bean.MapLocationBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 此demo用来展示如何结合定位SDK实现室内定位，并使用MyLocationOverlay绘制定位位置
 */
public class IndoorLocationActivity extends Activity {

    // 定位相关
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    MapView mMapView;
    BaiduMap mBaiduMap;

    StripListView stripListView;
    BaseStripAdapter mFloorListAdapter;
    MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;
    static Context mContext;
    // UI相关

    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private LocationService locationService;
    private Button startButton;
    private Marker mMarkerA;
    private Marker mMarkerB;
    private Polyline mPolyline;
    private InfoWindow mInfoWindow;
    private boolean isStart = false;
    private String indexid;
    private Button lineButton;
    private boolean isLine = false;
    private Button ipButton;

    @Override
    @AfterPermissionGranted(1)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout layout = new RelativeLayout(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainview = inflater.inflate(R.layout.activity_location, null);
        layout.addView(mainview);

        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
            EasyPermissions.requestPermissions(this, "需要访问定位权限", 1, perms);
        }
        requestLocButton = (Button) mainview.findViewById(R.id.button1);
        startButton = (Button) mainview.findViewById(R.id.button2);
        lineButton = (Button) mainview.findViewById(R.id.button3);
        ipButton = (Button) mainview.findViewById(R.id.button4);
        mCurrentMode = LocationMode.FOLLOWING;
        requestLocButton.setText("跟随");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true,
                                mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        // 地图初始化
        mMapView = (MapView) mainview.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 开启室内图
        mBaiduMap.setIndoorEnable(true);
        // 定位初始化
        locationService = new LocationService(this);
        locationService.registerListener(myListener);
        locationService.start();
        stripListView = new StripListView(this);
        layout.addView(stripListView);
        setContentView(layout);
        mFloorListAdapter = new BaseStripAdapter(IndoorLocationActivity.this);

        mBaiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b == false || mapBaseIndoorMapInfo == null) {
                    stripListView.setVisibility(View.INVISIBLE);

                    return;
                }

                mFloorListAdapter.setmFloorList(mapBaseIndoorMapInfo.getFloors());
                stripListView.setVisibility(View.VISIBLE);
                stripListView.setStripAdapter(mFloorListAdapter);
                mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
            }
        });
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = !isStart;
                if (isStart) {
                    startButton.setText("停止记录");
                } else {
                    startButton.setText("开始记录");
                }
                indexid = UUID.randomUUID().toString();
            }
        });
        lineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndoorLocationActivity.this, LocationListActivity.class);
                startActivity(intent);

            }
        });
        ipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OkGo.<String>get("http://pv.sohu.com/cityjson?ie=utf-8")
                        .execute(new JsonCallback<String>() {
                            @Override
                            public void onError(com.lzy.okgo.model.Response<String> response) {
                                super.onError(response);
                                ToastUtils.showShort(response.getException().getMessage());
                            }

                            @Override
                            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                                try {
                                    int start = response.body().indexOf("{");
                                    int end = response.body().indexOf("}");
                                    String json = response.body().substring(start, end + 1);
                                    final MobileIpBean mobileIpBean = JSON.parseObject(json, MobileIpBean.class);
                                    if (mobileIpBean != null) {
                                        OkGo.<IpBean>post("http://api.map.baidu.com/location/ip")
                                                .params("ak", "F454f8a5efe5e577997931cc01de3974")
                                                .params("ip", mobileIpBean.getCip())
                                                .execute(new JsonCallback<IpBean>() {
                                                    @Override
                                                    public void onSuccess(com.lzy.okgo.model.Response<IpBean> response) {
                                                        IpBean bean = response.body();
                                                        if (bean != null) {
                                                            ToastUtils.showShort("当前ip地址为(正常)：" + bean.getAddress() + "（" + mobileIpBean.getCip() + ")");
                                                        } else {
                                                            ToastUtils.showShort(response.message());

                                                        }
                                                    }
                                                });
                                    }
                                } catch (Exception e) {

                                }

                            }
                        });


            }
        });
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner extends BDAbstractLocationListener {

        private String lastFloor = null;

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || StringUtils.isEmpty(location.getAddrStr())) {
                return;
            }
            //轨迹查询

            String bid = location.getBuildingID();
            if (bid != null && mMapBaseIndoorMapInfo != null) {
                Log.i("indoor", "bid = " + bid + " mid = " + mMapBaseIndoorMapInfo.getID());
                if (bid.equals(mMapBaseIndoorMapInfo.getID())) {// 校验是否满足室内定位模式开启条件
                    // Log.i("indoor","bid = mMapBaseIndoorMapInfo.getID()");
                    String floor = location.getFloor().toUpperCase();// 楼层
                    Log.i("indoor", "floor = " + floor + " position = " + mFloorListAdapter.getPosition(floor));
                    Log.i("indoor", "radius = " + location.getRadius() + " type = " + location.getNetworkLocationType());

                    boolean needUpdateFloor = true;
                    if (lastFloor == null) {
                        lastFloor = floor;
                    } else {
                        if (lastFloor.equals(floor)) {
                            needUpdateFloor = false;
                        } else {
                            lastFloor = floor;
                        }
                    }
                    if (needUpdateFloor) {// 切换楼层
                        mBaiduMap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
                        mFloorListAdapter.setSelectedPostion(mFloorListAdapter.getPosition(floor));
                        mFloorListAdapter.notifyDataSetInvalidated();
                    }

                }
            }
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (!isStart) {
                return;
            }
            try {
                MapLocationBean bean = new MapLocationBean();
                bean.setAccuracy(location.getRadius());
                bean.setDirection(100);
                bean.setLatitude(location.getLatitude());
                bean.setIndexId(indexid);
                bean.setLongitude(location.getLongitude());
                if (location.getPoiList() != null && location.getPoiList().size() > 0) {
                    bean.setAddress(location.getPoiList().get(0).getName());
                } else {
                    bean.setAddress(location.getAddrStr());
                }
                bean.setCity(location.getCity());
                bean.setSatellitesNum(location.getSatelliteNumber());
                bean.setTime(location.getTime());
                MyApplication.getDaoInstant().getMapLocationBeanDao().insertInTx(bean);
            } catch (Exception e) {

            }


        }

        public void onReceivePoi(BDLocation poiLocation) {
        }

        public void onConnectHotSpotMessage(String s, int i) {
        }
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
        locationService.stop();
        locationService.unregisterListener(myListener);
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

}
