package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    // 固定的管理员账号和密码
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // 初始化控件
        EditText etUsername = findViewById(R.id.et_admin_username);
        EditText etPassword = findViewById(R.id.et_admin_password);
        Button btnLogin = findViewById(R.id.btn_admin_login);

        // 设置登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 验证账号和密码
            if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                // 获取管理员信息
                String adminId = "admin";
                String adminName = "管理大大";

                // 管理员登录成功后
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userId", adminId);  // 存储管理员ID
                editor.putString("userName", adminName);  // 存储管理员姓名
                editor.putBoolean("isLoggedIn", true);  // 存储登录状态
                editor.putString("userType", "admin");  // 存储用户类型为管理员
                editor.commit();

                String userId = sharedPreferences.getString("userId", "default");
                Log.d("调试", "userId: " + userId);

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

                // 跳转到管理员界面
                Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                startActivity(intent);
                finish(); // 结束登录界面
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
