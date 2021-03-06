package com.greata.greatasmartcam;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class AddDeviceActivity extends AppCompatActivity {

    public static final String WIFI = "wifi";
    List<String> list;
    ArrayAdapter<String> adapter;
    private AddFragment f0, f2, f3;
    private FragmentManager fManager;
    private ImageView lightView;
    private ActionBar actionBar;
    private EditText editSSID, editPwd;
    private TextView f2WifiTv, f2PwdTv, f2ModelTv;
    private List<String> ssidList;
    private Spinner sp;
    private ListPopupWindow listPopupWindow;
    private CheckBox pwdCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("根據提示新增攝影機 ");
        f0 = AddFragment.newInstance(R.layout.fragment_a);
        f2 = AddFragment.newInstance(R.layout.fragment_two);
        f3 = AddFragment.newInstance(R.layout.fragment_three);
        fManager = getFragmentManager();
        fManager.beginTransaction().add(R.id.frameFragment, f0).add(R.id.frameFragment, f2).add(R.id.frameFragment, f3).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (lightView == null) {
            lightView = f0.getView().findViewById(R.id.light_view);
            AnimationDrawable animationDrawable = (AnimationDrawable) lightView.getDrawable();
            animationDrawable.start();
        }
        if (sp == null || editSSID == null) {
            sp = f2.getView().findViewById(R.id.spinner);
            editSSID = f2.getView().findViewById(R.id.edit_ssid);
            Drawable dwRight4 = getResources().getDrawable(R.drawable.add_744pxpng);
            dwRight4.setBounds(0, 0, 40, 40);
            Drawable dwLeftC4 = DrawableCompat.wrap(dwRight4);
            DrawableCompat.setTint(dwLeftC4, getResources().getColor(android.R.color.darker_gray));
            editSSID.setCompoundDrawables(null, null, dwLeftC4, null);
            (new WifiTask()).execute();
        }

        if (editPwd == null) {
            editPwd = f2.getView().findViewById(R.id.edit_pwd);
            f2WifiTv = f2.getView().findViewById(R.id.f2_wifi_tv);
            Drawable dwLeft = getResources().getDrawable(R.drawable.wifi_164);
            dwLeft.setBounds(0, 0, 40, 30);
            Drawable dwLeftC = DrawableCompat.wrap(dwLeft);
            DrawableCompat.setTint(dwLeftC, getResources().getColor(R.color.colorPrimary));
            f2WifiTv.setCompoundDrawables(dwLeftC, null, null, null);
            f2PwdTv = f2.getView().findViewById(R.id.f2_pwd);
            Drawable dwLeft2 = getResources().getDrawable(R.drawable.key_lock);
            dwLeft2.setBounds(0, 0, 40, 40);
            Drawable dwLeftC2 = DrawableCompat.wrap(dwLeft2);
            DrawableCompat.setTint(dwLeftC2, getResources().getColor(R.color.colorPrimary));
            f2PwdTv.setCompoundDrawables(dwLeftC2, null, null, null);
            f2ModelTv = f2.getView().findViewById(R.id.f2_model);
            Drawable dwLeft3 = getResources().getDrawable(R.drawable.web_cam2_128px);
            dwLeft3.setBounds(0, 0, 40, 40);
            Drawable dwLeftC3 = DrawableCompat.wrap(dwLeft3);
            DrawableCompat.setTint(dwLeftC3, getResources().getColor(R.color.colorPrimary));
            f2ModelTv.setCompoundDrawables(dwLeftC3, null, null, null);
        }
        if (pwdCheckBox == null) {
            pwdCheckBox = f2.getView().findViewById(R.id.show_pwd_checkbox);
            pwdCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        //如果选中，显示密码
                        editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        //否则隐藏密码
                        editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }

                }
            });
        }
        fManager.beginTransaction().hide(f2).hide(f3).commit();
    }

    public void onHelpClick(View v) {
        Intent intent = new Intent(AddDeviceActivity.this, HelpActivity.class);
        startActivity(intent);
        AddDeviceActivity.this.finish();
    }

    public void onClick(View v) {
        Toast toast;
        switch (v.getId()) {
            case R.id.button_next_0:
                fManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).hide(f0).show(f2).addToBackStack(null).commit();
                if (!NetWorkUtils.isWifiConnected(AddDeviceActivity.this)) {
                    final AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(AddDeviceActivity.this);
                    normalDialog.setMessage("請將手機與即將配對攝像機的WiFi網絡連接");
                    normalDialog.setPositiveButton("設定WiFi ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                            AddDeviceActivity.this.finish();
                        }
                    });
                    normalDialog.setNegativeButton("取消", null);
                    normalDialog.show();
                }
                break;
            case R.id.button_next_2:
                if (editSSID.getText().toString().equals("")) {
                    toast = Toast.makeText(AddDeviceActivity.this, "請輸入" + getResources().getString(R.string.ssid_connect_text), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                if (!NetWorkUtils.isWifiConnected(this)) {
                    toast = Toast.makeText(AddDeviceActivity.this, R.string.con_phone_net, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //execute the task
                        Toast toast = Toast.makeText(AddDeviceActivity.this, R.string.unmatch_device, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }, 800);

                //fManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).hide(f2).show(f3).addToBackStack(null).commit();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private class WifiTask extends AsyncTask<String, Integer, String> {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            Log.i(WIFI, "onPreExecute() called");

        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            Log.i(WIFI, "doInBackground(Params... params) called");
            try {
                if (list == null) {
                    list = new ArrayList<String>();
                    list.add("鴻優視 720P");
                    list.add("鴻優視 1080P");
                    list.add("鴻優視 2S 720P");
                    list.add("鴻優視 2S 1080P");
                    list.add("鴻優視 Cloud 720P");
                    list.add("鴻優視 Cloud 1080P");
                    adapter = new ArrayAdapter<String>(AddDeviceActivity.this, android.R.layout.simple_spinner_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
                }
                if (ssidList == null || ssidList.isEmpty()) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    ssidList = new ArrayList<String>();
                    List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
                    if (wifiConfigurationList != null) {
                        for (int i = 0; i < wifiConfigurationList.size(); i++) {
                            Log.d(WIFI, "" + wifiConfigurationList.get(i).SSID);
                            ssidList.add(wifiConfigurationList.get(i).SSID.replaceAll("\"", ""));
                        }
                        Log.d(WIFI, "doInBackground: EDITSSID=NULL?");
                        Log.d(WIFI, "doInBackground: EDITSSID!=NULL");
                    }
                }
            } catch (Exception e) {
                Log.e(WIFI, e.getMessage());
            }
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
            Log.i(WIFI, "onProgressUpdate(Progress... progresses) called");
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            Log.i(WIFI, "onPostExecute(Result result) called");

            sp.setAdapter(adapter);

            editSSID.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getX() >= (editSSID.getWidth() - editSSID
                                .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if (!NetWorkUtils.isWifiConnected(AddDeviceActivity.this)) {
                                Toast toast = Toast.makeText(AddDeviceActivity.this, R.string.con_phone_net, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return true;
                            }
                            (new WifiTask()).execute();
                            listPopupWindow = new ListPopupWindow(AddDeviceActivity.this);
                            listPopupWindow.setAdapter(new ArrayAdapter<String>(AddDeviceActivity.this, android.R.layout.simple_list_item_1, ssidList));
                            listPopupWindow.setAnchorView(editSSID);
                            listPopupWindow.setHeight(500);
                            listPopupWindow.setModal(true);
                            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    editSSID.setText(ssidList.get(i));
                                    listPopupWindow.dismiss();
                                }
                            });
                            listPopupWindow.show();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
            Log.i(WIFI, "onCancelled() called");

        }
    }
}
