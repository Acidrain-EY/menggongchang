package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QueryActivity extends AppCompatActivity {

    private Spinner spinnerArea, spinnerPosition, spinnerItem;
    private Button btnSearch;
    private String selectedArea = "", selectedPosition = "", selectedItemId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        // 初始化控件
        spinnerArea = findViewById(R.id.spinner_area);
        spinnerPosition = findViewById(R.id.spinner_position);
        spinnerItem = findViewById(R.id.spinner_item);
        btnSearch = findViewById(R.id.btn_search);

        // 动态加载区域数据到第一个 Spinner
        setupAreaSpinner();

        // 查询按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedArea.isEmpty() || selectedPosition.isEmpty() || selectedItemId.isEmpty()) {
                    Toast.makeText(QueryActivity.this, "请完整选择所有信息！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 跳转到显示界面，并传递参数
                Intent intent = new Intent(QueryActivity.this, DisplayActivity.class);
                intent.putExtra("area", selectedArea);
                intent.putExtra("positionNumber", selectedPosition);
                intent.putExtra("itemId", selectedItemId);
                startActivity(intent);
            }
        });
    }

    private void setupAreaSpinner() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询数据库中的所有区域
        Cursor cursor = db.query(
                true,  // 去重
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_LOCATION_TYPE},  // 查询 location_type 列
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<String> areas = new ArrayList<>();
        areas.add("请选择区域"); // 添加默认选项

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String area = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_TYPE));
                areas.add(area);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();

        // 使用 ArrayAdapter 填充 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, areas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(adapter);

        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // 忽略默认选项
                    String newSelectedArea = areas.get(position);

                    // 避免重复加载
                    if (!newSelectedArea.equals(selectedArea)) {
                        selectedArea = newSelectedArea;
                        loadPositionsForArea(selectedArea); // 根据选中的区域加载位置
                    }
                } else {
                    selectedArea = "";
                    clearSpinner(spinnerPosition);
                    clearSpinner(spinnerItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedArea = "";
            }
        });
    }

    private void loadPositionsForArea(String area) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询数据库中区域对应的位置号
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_LOCATION_NUMBER},
                DatabaseHelper.COLUMN_LOCATION_TYPE + " = ?",
                new String[]{area},
                DatabaseHelper.COLUMN_LOCATION_NUMBER,
                null,
                null
        );

        List<String> positions = new ArrayList<>();
        positions.add("请选择位置"); // 添加默认选项

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String position = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_NUMBER));
                if (!positions.contains(position)) { // 避免重复
                    positions.add(position);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();

        // 更新位置 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);

        spinnerPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String newSelectedPosition = positions.get(position);

                    // 避免重复加载
                    if (!newSelectedPosition.equals(selectedPosition)) {
                        selectedPosition = newSelectedPosition;
                        loadItemsForPosition(selectedArea, selectedPosition);
                    }
                } else {
                    selectedPosition = "";
                    clearSpinner(spinnerItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPosition = "";
            }
        });
    }

    private void loadItemsForPosition(String area, String position) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询数据库中位置对应的物品
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_NAME},
                DatabaseHelper.COLUMN_LOCATION_TYPE + " = ? AND " +
                        DatabaseHelper.COLUMN_LOCATION_NUMBER + " = ?",
                new String[]{area, position},
                null,
                null,
                null
        );

        List<Item> items = new ArrayList<>();
        items.add(new Item("", "请选择物品"));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String itemId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID));
                String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                items.add(new Item(itemId, itemName));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();

        // 使用自定义适配器更新物品 Spinner
        ItemSpinnerAdapter adapter = new ItemSpinnerAdapter(this, items);
        spinnerItem.setAdapter(adapter);

        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = (Item) parent.getItemAtPosition(position);
                if (selectedItem != null && !selectedItem.getId().isEmpty()) {
                    selectedItemId = selectedItem.getId();
                } else {
                    selectedItemId = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedItemId = "";
            }
        });
    }

    private void clearSpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new String[]{"请选择"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
