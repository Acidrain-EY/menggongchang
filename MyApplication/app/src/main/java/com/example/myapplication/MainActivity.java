package com.example.myapplication;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime;
    private Handler timeHandler;
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tv_time);

        // 初始化 Handler 和 Runnable
        timeHandler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                timeHandler.postDelayed(this, 1000); // 每秒更新一次
            }
        };

        // 开始更新时间
        timeHandler.post(timeRunnable);

        // 初始化数据库
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 插入样例数据（如果尚未插入）
        if (!isSampleDataInserted(db)) {
            insertSampleData(db);
        }

        db.close();

        // 检查登录状态
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        String userName = sharedPreferences.getString("userName", "用户");

        String userId = sharedPreferences.getString("userId", "default");
        Log.d("tiaoshiSharedPrefs Debug", "Logged in user: " + userId + ", " + userName);

        if (isLoggedIn) {
            String userType = sharedPreferences.getString("userType", "user");

            if (userType.equals("admin")) {
                // 已登录且是管理员，跳转到管理员界面
                Intent adminIntent = new Intent(MainActivity.this, AdminMainActivity.class);
                startActivity(adminIntent);
            } else {
                // 已登录且是普通用户，跳转到用户界面
                Intent userIntent = new Intent(MainActivity.this, UserMainActivity.class);
                startActivity(userIntent);
            }

            finish(); // 结束当前活动
            return;
        }

        // 初始化按钮
        Button btnUserLogin = findViewById(R.id.btn_user_login);
        Button btnUserRegister = findViewById(R.id.btn_user_register);
        Button btnAdminLogin = findViewById(R.id.btn_admin_login);

        // 设置用户登陆界面按钮点击事件
        btnUserLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(intent);
        });

        // 设置管理员登录按钮点击事件
        btnAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // 设置用户注册按钮点击事件
        btnUserRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserRegisterActivity.class);
            startActivity(intent);
        });

        if (btnUserLogin == null) {
            Log.e("MainActivity", "btnUserLogin is null");
        }

    }

    /**
     * 插入样例数据到数据库
     */
    private void insertSampleData(SQLiteDatabase db) {
        String[][] sampleData = {
                {"12345672", "电阻", "100", "0", "电子工作台", "抽屉1"},
                {"12345679", "电容", "50", "1", "电子工作台", "抽屉2"},
                {"12345680", "晶体管", "75", "0", "电子工作台", "抽屉3"},
                {"12345681", "二极管", "200", "0", "电子工作台", "抽屉4"},
                {"22345678", "IC芯片", "30", "1", "元器件储藏柜", "柜子1"},
                {"22345679", "继电器", "20", "0", "元器件储藏柜", "柜子2"},
                {"22345680", "电感", "80", "0", "元器件储藏柜", "柜子3"},
                {"22345681", "电位器", "40", "1", "元器件储藏柜", "柜子4"},
                {"22345682", "电源模块", "15", "0", "元器件储藏柜", "柜子5"},
                {"22345683", "逻辑门", "25", "0", "元器件储藏柜", "柜子6"}
        };

        for (String[] data : sampleData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_ITEM_ID, data[0]);
            values.put(DatabaseHelper.COLUMN_NAME, data[1]);
            values.put(DatabaseHelper.COLUMN_QUANTITY, Integer.parseInt(data[2]));
            values.put(DatabaseHelper.COLUMN_IS_BORROWED, Integer.parseInt(data[3]));
            values.put(DatabaseHelper.COLUMN_LOCATION_TYPE, data[4]);
            values.put(DatabaseHelper.COLUMN_LOCATION_NUMBER, data[5]);

            // 根据物品名称获取对应的图片字节数组
            byte[] imageBytes = getImageForItem(data[1]);
            values.put(DatabaseHelper.COLUMN_IMAGE_BLOB, imageBytes);

            long newRowId = db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
            if (newRowId == -1) {
                System.out.println("数据插入失败，ID: " + data[0]);
            }
        }
    }

    /**
     * 检查样例数据是否已经插入
     */
    private boolean isSampleDataInserted(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_ITEMS,
                    new String[]{DatabaseHelper.COLUMN_ITEM_ID},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据物品类型返回对应的图片字节数组
     */
    private byte[] getImageForItem(String itemName) {
        int imageResId;
        switch (itemName) {
            case "电阻":
                imageResId = R.drawable.resistor_image;
                break;
            case "电容":
                imageResId = R.drawable.capacitor_image;
                break;
            case "晶体管":
                imageResId = R.drawable.transistor_image;
                break;
            case "二极管":
                imageResId = R.drawable.diode_image;
                break;
            case "IC芯片":
                imageResId = R.drawable.ic_chip_image;
                break;
            case "继电器":
                imageResId = R.drawable.relay_image;
                break;
            case "电感":
                imageResId = R.drawable.inductor_image;
                break;
            case "电位器":
                imageResId = R.drawable.potentiometer_image;
                break;
            case "电源模块":
                imageResId = R.drawable.power_module_image;
                break;
            case "逻辑门":
                imageResId = R.drawable.logic_gate_image;
                break;
            default:
                imageResId = R.drawable.default_image; // 默认图片
                break;
        }

        // 从资源加载图片并调整为 1:1 比例和最大尺寸
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), imageResId);
        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 200, 200);

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // 80% 压缩质量
        return outputStream.toByteArray();
    }

    /**
     * 将 Bitmap 调整为 1:1 比例，并指定最大宽度和高度
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 计算裁剪起点和裁剪区域
        int newWidth = Math.min(width, height);
        int newHeight = newWidth;
        int cropStartX = (width - newWidth) / 2;
        int cropStartY = (height - newHeight) / 2;

        // 裁剪为正方形
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, cropStartX, cropStartY, newWidth, newHeight);

        // 调整为指定尺寸
        return Bitmap.createScaledBitmap(croppedBitmap, maxWidth, maxHeight, true);
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvTime.setText(currentTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止时间更新
        if (timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
}
