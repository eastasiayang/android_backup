package com.yang.backup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yang.basic.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityAll extends AppCompatActivity {

    private final String TAG = ActivityAll.class.getSimpleName();
    private List<DataBaseManager.RecordsTable> infos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);

        infos = new ArrayList<>();
        ListView listview;
        listview = (ListView) findViewById(R.id.listview_all_record);
        List<Map<String, Object>> slist = new ArrayList<Map<String, Object>>();
        String result;
        result = DataBaseManager.getInstance(this).getFutureRecordList(Calendar.getInstance());
        try{
            JSONObject obj = new JSONObject(result);
            if (obj.has("data")) {
                JSONArray array = obj.optJSONArray("data");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.optJSONObject(i);
                    LogUtils.v(TAG, "jsonObject = " + jsonObject);
                    DataBaseManager.RecordsTable record = new DataBaseManager.RecordsTable();
                    record.coverJson(jsonObject.toString());

                    infos.add(record);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < infos.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img", infos.get(i).title);
            map.put("text", infos.get(i).start_time);
            slist.add(map);
        }
        SimpleAdapter simple = new SimpleAdapter(this, slist,
                R.layout.item_main_child, new String[] { "img", "text" }, new int[] {
                R.id.textview_item_main_child_title, R.id.textview_item_main_child_start_time });
        listview.setAdapter(simple);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                LogUtils.v(TAG, "position = " + position);

                Intent intent = new Intent(); // 建立 Intent
                intent.setClass(getApplicationContext(), ActivityExamine.class); // 设置活动
                startActivity(intent);
            }
        });
    }
}
