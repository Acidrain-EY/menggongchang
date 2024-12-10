package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // 初始化控件
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        Button btnQueryItem = findViewById(R.id.btn_query_item);
        Button btnAddItem = findViewById(R.id.btn_add_item);
        Button btnQuerySingleItem = findViewById(R.id.btn_query_single_item);
        Button btnLogout = findViewById(R.id.btn_logout);

        // 获取当前登录管理员信息
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String adminName = sharedPreferences.getString("userName", "管理员");

        // 显示管理员名称
        tvGreeting.setText("你好，" + adminName + "！");

        // 查询物品按钮
        btnQueryItem.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, QueryActivity.class);
            startActivity(intent);
        });

        // 添加物品按钮
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        // 查询单个物品按钮
        btnQuerySingleItem.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, SearchItemByIdActivity.class);
            startActivity(intent);
        });

        // 登出按钮
        btnLogout.setOnClickListener(v -> {
            // 清除登录状态
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // 返回主界面
            Intent intent = new Intent(AdminMainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
