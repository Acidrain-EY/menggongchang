package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;

public class UserMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        TextView tvGreeting = findViewById(R.id.tv_greeting);
        Button btnLogout = findViewById(R.id.btn_logout);

        // 获取当前登录用户信息
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "用户");

        String userId = sharedPreferences.getString("userId", "default");
        Log.d("SharedPrefs Debug", "Logged in user: " + userId + ", " + userName);
        // 显示用户名
        tvGreeting.setText("你好！" + userName);

        // 登出按钮点击事件
        btnLogout.setOnClickListener(v -> {
            // 清除登录状态
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // 返回主界面
            Intent intent = new Intent(UserMainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 结束当前活动
        });

        // 初始化按钮
        Button btnQueryItem = findViewById(R.id.btn_user_query_item);
        Button btnQuerySingle = findViewById(R.id.btn_user_query_single);

        // 设置查询物品按钮点击事件
        btnQueryItem.setOnClickListener(v -> {
            Intent intent = new Intent(UserMainActivity.this, QueryActivity.class);
            startActivity(intent);
        });

        // 设置查询单个物品按钮点击事件
        btnQuerySingle.setOnClickListener(v -> {
            Intent intent = new Intent(UserMainActivity.this, SearchItemByIdActivity.class);
            startActivity(intent);
        });
    }
}
