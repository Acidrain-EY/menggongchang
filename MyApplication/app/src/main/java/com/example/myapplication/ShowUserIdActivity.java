package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowUserIdActivity extends AppCompatActivity {

    private TextView tvUserId;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_id);

        // 初始化控件
        tvUserId = findViewById(R.id.tv_user_id);
        btnConfirm = findViewById(R.id.btn_confirm);

        // 获取传递的用户 ID
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        // 显示用户 ID
        tvUserId.setText("您的用户 ID 是：" + userId);

        // 确认按钮点击事件
        btnConfirm.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ShowUserIdActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }
}
