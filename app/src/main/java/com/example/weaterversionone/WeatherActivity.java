package com.example.weaterversionone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weaterversionone.json.Weather;
import com.example.weaterversionone.util.HttpUtil;
import com.example.weaterversionone.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.weaterversionone.util.Utility.handleWeatherResponse;

/**
 * Created by Administrator on 2017/10/17 0017.
 */

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView weatherCond1, weatherCond2;
    private TextView tmpRange1, tmpRange2;

    private TextView airBrf, cwBrf, fluBrf, sportBrf;
    private TextView airTxt, cwTxt, fluTxt, sportTxt;

    public String weatherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolBar = (Toolbar) findViewById(R.id.tool_bar);
        weatherCond1 = (TextView) findViewById(R.id.date_cond1);
        weatherCond2 = (TextView) findViewById(R.id.date_cond2);
        tmpRange1 = (TextView) findViewById(R.id.tmp_range1);
        tmpRange2 = (TextView) findViewById(R.id.tmp_range2);

        airBrf = (TextView) findViewById(R.id.air_brf);
        airTxt = (TextView) findViewById(R.id.air_txt);
        cwBrf = (TextView) findViewById(R.id.cw_brf);
        cwTxt = (TextView) findViewById(R.id.cw_txt);
        fluBrf = (TextView) findViewById(R.id.flu_brf);
        fluTxt = (TextView) findViewById(R.id.flu_txt);
        sportBrf = (TextView) findViewById(R.id.sport_brf);
        sportTxt = (TextView) findViewById(R.id.sport_txt);
        mToolBar.setTitle("");
        setSupportActionBar(mToolBar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        weatherId = getIntent().getStringExtra("weatherId");
        Log.d("weatherid2", " " +weatherId);
        if (weatherId != null) {
            requestWeather(weatherId);
        } else {
            SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
            String weatherInfo = pre.getString("weather_info", null);
            if (weatherInfo != null) {
                Weather weather = Utility.handleWeatherResponse(weatherInfo);
                showWeather(weather);
            } else {
                Toast.makeText(this, "请求天气失败", Toast.LENGTH_SHORT).show();
            }
        }

        /*SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherInfo = pre.getString("weather_info", null);
        if (weatherInfo !=null) {
            Weather weather = Utility.handleWeatherResponse(weatherInfo);
            showWeather(weather);
        } else {
            weatherId = getIntent().getStringExtra("weatherId");
            if (weatherId != null) {
                requestWeather(weatherId);
            } else {
                Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            } else
                mDrawerLayout.closeDrawers();
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestWeather(String weatherId) {
        Log.d("test1" , "weatherId" + weatherId);
        String url = "https://free-api.heweather.com/v5/weather?city="
                +weatherId+"&key=13c7da944e8f45098b4e3d3edae261f3";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "请求天气失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mResponse = response.body().string();

                Weather mWeather = handleWeatherResponse(mResponse);
                if (mWeather !=null) {
                    SharedPreferences.Editor editor = PreferenceManager.
                            getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("weather_info", mResponse);
                    editor.apply();
                    Log.d("weatherid", " " + mWeather.basic.city);
                    showWeather(mWeather);
                }
                else {
                    WeatherActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "天气请求失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    public void showWeather(final Weather weather) {

        WeatherActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("weatherID", "" +weather.basic.city);
                mToolBar.setTitle(weather.basic.city + "    " + weather.now.cond.txt + "  " + weather.now.tmp);
                weatherCond1.setText(weather.foreCastList.get(1).date + " " +weather.foreCastList.get(1).cond.txt_d);
                tmpRange1.setText(weather.foreCastList.get(1).tmp.min + "~" + weather.foreCastList.get(1).tmp.max + "℃");
                weatherCond2.setText(weather.foreCastList.get(2).date + " " +weather.foreCastList.get(2).cond.txt_d);
                tmpRange2.setText(weather.foreCastList.get(2).tmp.min + "~" + weather.foreCastList.get(2).tmp.max + "℃");
                airBrf.setText(" " +weather.suggestion.air.brf);
                airTxt.setText(weather.suggestion.air.txt);
                cwBrf.setText(" " +weather.suggestion.cw.brf);
                cwTxt.setText(weather.suggestion.cw.txt);
                fluBrf.setText(" " +weather.suggestion.flu.brf);
                fluTxt.setText(weather.suggestion.flu.txt);
                sportBrf.setText(" " +weather.suggestion.sport.brf);
                sportTxt.setText(weather.suggestion.sport.txt);

            }
        });
    }

    public void addWeatherView() {

    }
}
