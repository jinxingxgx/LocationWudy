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
import com.blankj.utilcode.util.StringUtils;
import com.wudy.locationwudy.bean.DaoMaster;
import com.wudy.locationwudy.bean.DaoSession;
import com.wudy.locationwudy.bean.MapLocationBean;
import com.wudy.locationwudy.bean.MapLocationBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
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
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
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
                indexid = UUID.randomUUID().toString();
            }
        });
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
        QueryBuilder<MapLocationBean> qb = MyApplication.getDaoInstant().getMapLocationBeanDao().queryBuilder();
        qb.where(MapLocationBeanDao.Properties.Address.isNotNull(), new WhereCondition.StringCondition("GROUP BY indexId"));
        qb.orderDesc(MapLocationBeanDao.Properties.Time);
        List<MapLocationBean> mapLocations = qb.list();
        if (mapLocations != null) {
            for (int i = 0; i < mapLocations.size(); i++) {
                LatLng sourceLatLng = new LatLng(mapLocations.get(i).getLatitude(), mapLocations.get(i).getLongitude());
                latLngs.add(sourceLatLng);
            }
        }
        MarkerOptions oStart = new MarkerOptions();//地图标记覆盖物参数配置类 
        oStart.position(latLngs.get(0));//覆盖物位置点，第一个点为起点
        oStart.icon(startBD);//设置覆盖物图片
        oStart.zIndex(1);//设置覆盖物Index
        mMarkerA = (Marker) (mBaiduMap.addOverlay(oStart)); //在地图上添加此图层

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

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner extends BDAbstractLocationListener {

        private String lastFloor = null;

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || StringUtils.isEmpty(location.getAddrStr()) || !isStart) {
                return;
            }

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

            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            location.getCity();
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


            //轨迹查询
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
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
