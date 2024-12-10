package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Button btnRegister, btnTogglePasswordVisibility;
    private TextView tvPasswordStrength;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // 初始化控件
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);
        btnTogglePasswordVisibility = findViewById(R.id.btn_toggle_password_visibility); // 密码可见性按钮
        btnRegister = findViewById(R.id.btn_register);

        // 监听密码输入变化
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 设置密码可见性按钮点击事件
        btnTogglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());

        // 设置注册按钮点击事件
        btnRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * 切换密码可见性
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 隐藏密码
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePasswordVisibility.setText("👁");
        } else {
            // 显示密码
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            btnTogglePasswordVisibility.setText("🙈");
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length()); // 确保光标在末尾
    }

    /**
     * 根据密码强度更新 UI
     */
    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setVisibility(View.GONE);
            return;
        }

        tvPasswordStrength.setVisibility(View.VISIBLE);

        if (password.length() < 6) {
            tvPasswordStrength.setText("弱：密码长度应不少于6位");
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (!password.matches(".*[A-Z].*")) {
            tvPasswordStrength.setText("中：添加一个大写字母");
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else if (!password.matches(".*\\d.*")) {
            tvPasswordStrength.setText("中：添加一个数字");
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else if (password.length() < 10) {
            tvPasswordStrength.setText("中：密码长度可进一步增加");
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvPasswordStrength.setText("强：密码强度高");
            tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }


    /**
     * 注册用户逻辑
     */
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写完整信息！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhone(phone)) {
            Toast.makeText(this, "请输入11位有效的手机号！", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = generateRandomId();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);

        long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "注册成功！用户ID: " + userId, Toast.LENGTH_SHORT).show();

            // 跳转到显示用户 ID 的界面
            Intent intent = new Intent(UserRegisterActivity.this, ShowUserIdActivity.class);
            intent.putExtra("userId", userId); // 传递用户 ID
            startActivity(intent);
            finish(); // 返回上一界面
        } else {
            Toast.makeText(this, "注册失败，请重试！", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$"); // 国内 11 位手机号码验证
    }


    /**
     * 生成随机 6 位 ID
     */
    private String generateRandomId() {
        Random random = new Random();
        int id = random.nextInt(899999) + 100000; // 保证是 6 位数字
        return String.valueOf(id);
    }
}
