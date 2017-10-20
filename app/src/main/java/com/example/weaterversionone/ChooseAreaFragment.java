package com.example.weaterversionone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weaterversionone.db.City;
import com.example.weaterversionone.db.County;
import com.example.weaterversionone.db.Province;
import com.example.weaterversionone.util.HttpUtil;
import com.example.weaterversionone.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/11 0011.
 */

public class ChooseAreaFragment extends Fragment {

    private ProgressDialog progressDialog;
    private int LEVEL_PROVINCE = 0;
    private int LEVEL_CITY = 1;
    private int LEVEL_COUNTY = 2;
    private int currentLevel = LEVEL_PROVINCE;
    private Button backButton;
    private TextView titleView;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private boolean result = false;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        backButton = view.findViewById(R.id.back_button);
        titleView = view.findViewById(R.id.title_text);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        //showProgressDialog();
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounty();
                }else if (currentLevel == LEVEL_COUNTY) {
                    selectedCounty = countyList.get(i);
                    if (getActivity() instanceof MainActivity) {
                        MainActivity activity = (MainActivity) getActivity();
                        Intent intent = new Intent(activity, WeatherActivity.class);
                        Log.d("weatherid", selectedCounty.getWeatherId());
                        intent.putExtra("weatherId", selectedCounty.getWeatherId());
                        startActivity(intent);
                        activity.finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.mDrawerLayout.closeDrawers();
                        activity.addWeatherView(selectedCounty.getWeatherId());
                    }


                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                }else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    public void queryProvince() {
        showProgressDialog();

        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            closeProgressDialog();
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    public void queryCity() {
        showProgressDialog();
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            closeProgressDialog();
            titleView.setText(selectedProvince.getProvinceName());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + String.valueOf(selectedProvince.getProvinceCode());
            queryFromServer(address, "city");
        }
    }

    public void queryCounty() {
        showProgressDialog();
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getCityCode()))
                .find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            closeProgressDialog();
            titleView.setText(selectedCity.getCityName());
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTY;
        } else {
            String address = "http://guolin.tech/api/china/"
                    + String.valueOf(selectedProvince.getProvinceCode())
                    + "/" + String.valueOf(selectedCity.getCityCode());
            queryFromServer(address, "county");
        }
    }

    public void queryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mResponse = response.body().string();
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(mResponse);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(mResponse, selectedProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(mResponse, selectedCity.getCityCode());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }


    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载..");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
