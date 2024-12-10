package com.example.myapplication;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;  // 引入Log类
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BorrowActivity extends AppCompatActivity {

    private TextView tvItemName, tvItemId;
    private EditText etExpectedDays;
    private Button btnSubmitBorrow;

    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);

        // 获取界面元素
        tvItemName = findViewById(R.id.tv_item_name);
        tvItemId = findViewById(R.id.tv_item_id);
        etExpectedDays = findViewById(R.id.et_expected_days);
        btnSubmitBorrow = findViewById(R.id.btn_submit_borrow);

        // 获取物品ID
        itemId = getIntent().getStringExtra("itemId");

        // 显示物品信息
        getItemInfo(itemId);

        // 判断当前是否为管理员登录，并根据登录类型修改按钮文字
        if (isAdminLoggedIn()) {
            btnSubmitBorrow.setText("维修");
        } else {
            btnSubmitBorrow.setText("借出");
        }

        // 提交按钮点击事件
        btnSubmitBorrow.setOnClickListener(v -> submitItemAction());
    }

    // 判断是否为管理员登录
    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", "user"); // 默认为普通用户
        return "admin".equals(userType);  // 如果是管理员，则返回true
    }

    // 获取物品信息并显示
    private void getItemInfo(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_NAME},
                DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                new String[]{itemId},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            tvItemName.setText("物品名称: " + itemName);
            tvItemId.setText("物品ID: " + itemId);
            cursor.close();
        }
        db.close();
    }

    // 提交借出/维修操作
    private void submitItemAction() {
        // 获取预计使用天数
        String useDays = etExpectedDays.getText().toString();
        if (useDays.isEmpty()) {
            Toast.makeText(this, "请输入预计使用天数", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前登录用户信息
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String userType = sharedPreferences.getString("userType", "");
        String userName = ("");
        String userPhone = ("");
        String userEmail = ("");

        // 管理员信息（硬编码）
        String adminName = "管理员";
        String adminPhone = "1234567890";
        String adminEmail = "admin@example.com";

        // 对于管理员，可以直接使用硬编码的管理员信息
        if ("admin".equals(userType)) {
            userId = "admin";
            userName = adminName;
            userPhone = adminPhone;
            userEmail = adminEmail;
        } else {
            // 对于普通用户，查询数据库获取个人信息
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PHONE, DatabaseHelper.COLUMN_EMAIL},
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{userId},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                userName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                userPhone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
                userEmail = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));
                cursor.close();
            }
        }

        // 判断当前是否为管理员，管理员进行维修，普通用户进行借出
        if (isAdminLoggedIn()) {
            // 这里存储管理员的借出（维修）信息
            Log.d("BorrowActivity", "管理员操作：维修物品，物品ID：" + itemId);
            recordItemAction(userId, userName, userPhone, userEmail, useDays, true);
        } else {
            // 存储用户的借出信息
            Log.d("BorrowActivity", "普通用户操作：借出物品，物品ID：" + itemId);
            recordItemAction(userId, userName, userPhone, userEmail, useDays, false);
        }

        // 更新物品状态为已借出
        updateItemStatus(itemId);

        // 跳转回物品显示界面
        finish();
    }

    // 存储借出/维修信息
    private void recordItemAction(String userId, String userName, String userPhone, String userEmail, String useDays, boolean isAdminAction) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 使用 SimpleDateFormat 格式化当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String borrowTimeFormatted = sdf.format(new Date());  // 获取当前时间并格式化

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BORROW_ITEM_ID, itemId);
        values.put(DatabaseHelper.COLUMN_BORROW_ITEM_NAME, tvItemName.getText().toString()); // 显示的物品名称
        values.put(DatabaseHelper.COLUMN_USER_ID_BORROW, userId);
        values.put(DatabaseHelper.COLUMN_USER_NAME, userName);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, userPhone);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, userEmail);
        values.put(DatabaseHelper.COLUMN_BORROW_TIME, borrowTimeFormatted); // 存储格式化后的时间
        values.put(DatabaseHelper.COLUMN_EXPECTED_DAYS, Integer.parseInt(useDays));
        values.put(DatabaseHelper.COLUMN_IS_BORROWED_FLAG, 1); // 更新为借出状态

        long result = db.insert(DatabaseHelper.TABLE_BORROW_RETURN, null, values);
        db.close();

        if (result != -1) {
            Log.d("BorrowActivity", "借出/维修记录插入成功，记录ID：" + result);
        } else {
            Log.e("BorrowActivity", "借出/维修记录插入失败");
        }
    }


    // 更新物品状态为借出
    private void updateItemStatus(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_BORROWED, 1); // 更新为借出状态
        int rowsAffected = db.update(DatabaseHelper.TABLE_ITEMS, values, DatabaseHelper.COLUMN_ITEM_ID + " = ?", new String[]{itemId});
        db.close();

        if (rowsAffected > 0) {
            Log.d("BorrowActivity", "物品状态更新成功，物品ID：" + itemId);
        } else {
            Log.e("BorrowActivity", "物品状态更新失败，物品ID：" + itemId);
        }
    }
}
