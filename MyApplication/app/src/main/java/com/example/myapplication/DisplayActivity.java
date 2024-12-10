package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DisplayActivity extends AppCompatActivity {

    private TextView tvDataDisplay;
    private ImageView ivItemImage;
    private Button btnBorrow;  // 新增借出按钮
    private Button btnShowBorrowReturn;
    private Button btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        // 初始化视图
        tvDataDisplay = findViewById(R.id.tv_data_display);
        ivItemImage = findViewById(R.id.iv_item_image); // 获取 ImageView
        btnBorrow = findViewById(R.id.btn_borrow);  // 获取借出按钮
        btnReturn = findViewById(R.id.btn_return);
        btnShowBorrowReturn = findViewById(R.id.btn_show_borrow_return);

        // 获取传递的参数
        Intent intent = getIntent();
        String area = intent.getStringExtra("area");
        String positionNumber = intent.getStringExtra("positionNumber");
        String itemId = intent.getStringExtra("itemId");

        // 检查传递的参数，分别处理不同的查询方式
        if (itemId != null && area == null && positionNumber == null) {
            // 通过 itemId 单独查询物品
            displayItemById(itemId);
        } else if (area != null && positionNumber != null && itemId != null) {
            // 通过区域、位置和 itemId 查询物品
            displayItemByLocationAndId(area, positionNumber, itemId);
        } else {
            Toast.makeText(this, "查询参数错误！", Toast.LENGTH_SHORT).show();
            finish(); // 返回上一界面
        }

        // 获取删除按钮
        Button deleteButton = findViewById(R.id.delete_button);

// 判断当前是否为管理员登录
        if (isAdminLoggedIn()) {
            btnBorrow.setText("维修");  // 如果是管理员，显示维修按钮
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            btnBorrow.setText("借出");  // 否则显示借出按钮
            deleteButton.setVisibility(View.GONE);
        }

// 设置删除按钮点击事件
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItemFromDatabase(itemId);
            }
        });




        // 归还按钮点击事件
        btnReturn.setOnClickListener(v -> {
            String itemIdNow = getIntent().getStringExtra("itemId");
            if (itemIdNow != null) {
                returnItem(itemIdNow);
            }
        });

        // 点击借出按钮的事件处理
        btnBorrow.setOnClickListener(v -> {
            String itemId_now = getIntent().getStringExtra("itemId");
            if (itemId_now != null) {
                // 检查物品状态，是否可借出
                checkItemStatusAndNavigate(itemId_now);
            }
        });

        btnShowBorrowReturn.setOnClickListener(v -> {
            String itemId_a = getIntent().getStringExtra("itemId");
            if (itemId_a != null) {
                // 跳转到显示借出归还信息的页面
                Intent intent_a = new Intent(DisplayActivity.this, BorrowReturnActivity.class);
                intent_a.putExtra("itemId", itemId_a);  // 传递物品ID
                startActivity(intent_a);
            }
        });
    }

    private void deleteItemFromDatabase(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 删除指定ID的物品
        int rowsDeleted = db.delete(
                DatabaseHelper.TABLE_ITEMS,
                DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                new String[]{itemId}
        );

        if (rowsDeleted > 0) {
            Toast.makeText(this, "物品已删除", Toast.LENGTH_SHORT).show();

            // 删除成功后，返回管理员页面
            Intent intent = new Intent(DisplayActivity.this, AdminMainActivity.class);
            startActivity(intent);
            finish();  // 结束当前的 DisplayActivity
        } else {
            Toast.makeText(this, "删除失败，物品未找到", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 每次回到界面时重新加载物品信息
        String itemId = getIntent().getStringExtra("itemId");
        if (itemId != null) {
            displayItemById(itemId); // 根据 itemId 刷新物品信息
        }
    }

    private void returnItem(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. 查询最近的借出记录
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BORROW_RETURN,
                new String[]{
                        DatabaseHelper.COLUMN_USER_ID_BORROW,
                        DatabaseHelper.COLUMN_BORROW_TIME,
                        DatabaseHelper.COLUMN_IS_BORROWED_FLAG,
                        DatabaseHelper.COLUMN_BORROW_ITEM_NAME,
                        DatabaseHelper.COLUMN_EXPECTED_DAYS,
                        DatabaseHelper.COLUMN_USER_NAME,
                        DatabaseHelper.COLUMN_USER_PHONE,
                        DatabaseHelper.COLUMN_USER_EMAIL,
                        DatabaseHelper.COLUMN_BORROW_ITEM_ID
                },
                DatabaseHelper.COLUMN_BORROW_ITEM_ID + " = ?", // 查询该物品ID的所有借出记录
                new String[]{itemId},
                null, null,
                DatabaseHelper.COLUMN_BORROW_TIME + " DESC", "1" // 获取时间最近的借出记录
        );

        // 如果查询到结果（有借出记录）
        if (cursor != null && cursor.moveToFirst()) {
            // 获取查询结果的各个字段值
            String borrowerId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID_BORROW));
            String borrowTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BORROW_TIME));
            String expectedDays = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPECTED_DAYS));
            String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BORROW_ITEM_NAME));
            String borrowerName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
            String borrowerPhone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE));
            String borrowerEmail = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL));
            int isBorrowedFlag = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BORROWED_FLAG)); // 借出状态

            // 获取当前登录用户信息
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String loggedInUserId = sharedPreferences.getString("userId", "default");

            Log.d("jiechuSharedPrefs Debug", "Logged in user: " + loggedInUserId);

            // 判断当前登录用户是否是借用者，并且确认物品的借出状态为借出（isBorrowedFlag == 1）
            if (borrowerId.equals(loggedInUserId) && isBorrowedFlag == 1) {
                // 获取当前时间作为归还时间
                String returnTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                // 4. 插入一条新的归还记录
                ContentValues returnValues = new ContentValues();
                returnValues.put(DatabaseHelper.COLUMN_BORROW_ITEM_ID, itemId); // 物品ID
                returnValues.put(DatabaseHelper.COLUMN_BORROW_ITEM_NAME, itemName); // 物品名称
                returnValues.put(DatabaseHelper.COLUMN_USER_ID_BORROW, borrowerId); // 借用者ID
                returnValues.put(DatabaseHelper.COLUMN_USER_NAME, borrowerName); // 借用者姓名
                returnValues.put(DatabaseHelper.COLUMN_USER_PHONE, borrowerPhone); // 借用者电话
                returnValues.put(DatabaseHelper.COLUMN_USER_EMAIL, borrowerEmail); // 借用者邮箱
                returnValues.put(DatabaseHelper.COLUMN_BORROW_TIME, returnTime); // 归还时间
                returnValues.put(DatabaseHelper.COLUMN_EXPECTED_DAYS, expectedDays); // 预计天数
                returnValues.put(DatabaseHelper.COLUMN_IS_BORROWED_FLAG, 0); // 归还状态（0表示已归还）

                // 插入归还记录
                long result = db.insert(DatabaseHelper.TABLE_BORROW_RETURN, null, returnValues);

                if (result != -1) {
                    // 5. 更新物品状态为未借出
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(DatabaseHelper.COLUMN_IS_BORROWED_FLAG, 0);  // 设置物品状态为未借出
                    db.update(DatabaseHelper.TABLE_ITEMS, updateValues, DatabaseHelper.COLUMN_ITEM_ID + " = ?", new String[]{itemId});

                    Toast.makeText(this, "物品已成功归还！", Toast.LENGTH_SHORT).show();

                    // 6. 刷新界面
                    displayItemById(itemId);  // 刷新物品信息
                } else {
                    Toast.makeText(this, "归还记录插入失败！", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 物品未借出或当前用户不是借用者
                Toast.makeText(this, "该物品未借出或您无权限归还！", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        } else {
            // 如果没有找到借出记录，说明物品未借出，无法归还
            Toast.makeText(this, "物品未借出，无法归还！", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }


    // 判断是否为管理员登录
    private boolean isAdminLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = sharedPreferences.getString("userType", "");  // 获取用户类型
        return "admin".equals(userType);  // 如果是管理员，则返回true
    }

    private void checkItemStatusAndNavigate(String itemId) {
        // 获取当前物品状态
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询物品状态
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_IS_BORROWED},
                DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                new String[]{itemId},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int isBorrowed = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BORROWED));
            if (isBorrowed == 0) {
                // 物品未被借出，可以进行借出操作
                navigateToBorrowActivity(itemId);
            } else {
                Toast.makeText(this, "物品已借出，无法再次借出", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        db.close();
    }

    private void navigateToBorrowActivity(String itemId) {
        Intent intent = new Intent(DisplayActivity.this, BorrowActivity.class);
        intent.putExtra("itemId", itemId); // 传递物品ID
        startActivity(intent);
    }

    /**
     * 根据区域、位置和 ID 查询物品数据
     */
    private void displayItemByLocationAndId(String area, String positionNumber, String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            // 查询指定物品
            cursor = db.query(
                    DatabaseHelper.TABLE_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_LOCATION_TYPE + " = ? AND " +
                            DatabaseHelper.COLUMN_LOCATION_NUMBER + " = ? AND " +
                            DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                    new String[]{area, positionNumber, itemId},
                    null,
                    null,
                    null
            );

            handleCursorResult(cursor, area, positionNumber, itemId);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * 根据 itemId 单独查询物品数据
     */
    private void displayItemById(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            // 查询指定物品
            cursor = db.query(
                    DatabaseHelper.TABLE_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                    new String[]{itemId},
                    null,
                    null,
                    null
            );

            handleCursorResult(cursor, null, null, itemId);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * 处理查询结果并显示物品信息
     */
    private void handleCursorResult(Cursor cursor, String area, String positionNumber, String itemId) {
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY));
            int isBorrowed = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BORROWED));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_BLOB));
            String locationArea = area != null ? area : cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_TYPE));
            String locationPosition = positionNumber != null ? positionNumber : cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_NUMBER));

            String result = "物品ID: " + itemId + "\n" +
                    "名称: " + name + "\n" +
                    "数量: " + quantity + "\n" +
                    "是否借出: " + (isBorrowed == 1 ? "是" : "否") + "\n" +
                    "区域: " + locationArea + "\n" +
                    "位置: " + locationPosition;

            tvDataDisplay.setText(result);

            // 如果有图片数据，则显示图片
            if (imageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                ivItemImage.setImageBitmap(bitmap);
            } else {
                // 如果没有图片，显示默认图片
                ivItemImage.setImageResource(R.drawable.default_image);
            }
        } else {
            tvDataDisplay.setText("未找到对应物品！");
            ivItemImage.setImageResource(R.drawable.default_image); // 显示默认图片
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DisplayActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // 结束当前活动
    }
}

