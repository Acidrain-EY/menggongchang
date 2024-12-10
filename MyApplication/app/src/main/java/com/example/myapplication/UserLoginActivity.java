package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etLoginInput, etPassword;
    private Button btnLogin;
    private RadioGroup radioGroupLoginMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // 初始化控件
        etLoginInput = findViewById(R.id.et_login_input);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        radioGroupLoginMethod = findViewById(R.id.radio_group_login_method);

        // 设置 RadioGroup 的监听器
        radioGroupLoginMethod.setOnCheckedChangeListener((group, checkedId) -> updateLoginHint(checkedId));

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> loginUser());
    }

    /**
     * 根据选中的登录方式更新输入框的提示
     */
    private void updateLoginHint(int checkedId) {
        if (checkedId == R.id.radio_user_id) {
            etLoginInput.setHint("请输入账号");
        } else if (checkedId == R.id.radio_email) {
            etLoginInput.setHint("请输入邮箱");
        } else if (checkedId == R.id.radio_phone) {
            etLoginInput.setHint("请输入手机号");
        } else {
            etLoginInput.setHint("请输入信息");
        }
    }

    /**
     * 登录用户逻辑
     */
    private void loginUser() {
        String input = etLoginInput.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写完整信息！", Toast.LENGTH_SHORT).show();
            return;
        }

        String columnName;
        int selectedId = radioGroupLoginMethod.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_user_id) {
            columnName = DatabaseHelper.COLUMN_USER_ID;
        } else if (selectedId == R.id.radio_email) {
            columnName = DatabaseHelper.COLUMN_EMAIL;
        } else if (selectedId == R.id.radio_phone) {
            columnName = DatabaseHelper.COLUMN_PHONE;
        } else {
            Toast.makeText(this, "请选择登录方式", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证用户信息
        validateUser(columnName, input, password);
    }

    /**
     * 验证用户信息
     */
    private void validateUser(String columnName, String input, String password) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                columnName + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                new String[]{input, password},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 获取用户信息
            String userId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
            String userName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));

            // 用户登录成功后
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userId", userId);  // 存储用户ID
            editor.putString("userName", userName);  // 存储用户姓名
            editor.putBoolean("isLoggedIn", true);  // 存储登录状态
            editor.putString("userType", "user");  // 存储用户类型为普通用户
            editor.commit();

            String userIda = sharedPreferences.getString("userId", "default");
            Log.d("调试", "userId: " + userIda);

            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            // 跳转到用户界面或其他逻辑
            Intent intent = new Intent(UserLoginActivity.this, UserMainActivity.class);
            startActivity(intent);
            cursor.close();
            finish();
        } else {
            Toast.makeText(this, "登录失败，账号或密码错误", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }
}

