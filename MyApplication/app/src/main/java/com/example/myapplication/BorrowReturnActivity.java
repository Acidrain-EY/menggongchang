package com.example.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BorrowReturnActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;  // 用于显示借出/归还记录的 ExpandableListView
    private ArrayList<BorrowReturnRecord> borrowReturnRecords = new ArrayList<>(); // 存储借出归还记录的集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_return);

        // 初始化 ExpandableListView
        expandableListView = findViewById(R.id.expandableListView);

        // 获取传递过来的物品ID
        String itemId = getIntent().getStringExtra("itemId");
        if (itemId != null) {
            // 查询并显示借出/归还记录
            loadBorrowReturnData(itemId);
        } else {
            Toast.makeText(this, "物品ID不能为空！", Toast.LENGTH_SHORT).show();
        }
    }

    // 查询借出归还记录并更新界面
    private void loadBorrowReturnData(String itemId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询借出归还记录
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BORROW_RETURN, // 借出归还表
                new String[]{
                        DatabaseHelper.COLUMN_BORROW_ITEM_NAME,
                        DatabaseHelper.COLUMN_USER_NAME,
                        DatabaseHelper.COLUMN_USER_PHONE,
                        DatabaseHelper.COLUMN_USER_EMAIL,
                        DatabaseHelper.COLUMN_BORROW_TIME,
                        DatabaseHelper.COLUMN_EXPECTED_DAYS,
                        DatabaseHelper.COLUMN_IS_BORROWED_FLAG
                },
                DatabaseHelper.COLUMN_BORROW_ITEM_ID + " = ?",
                new String[]{itemId},
                null,
                null,
                DatabaseHelper.COLUMN_BORROW_TIME + " DESC" // 按照借出时间降序排列
        );

        // 清空现有记录
        borrowReturnRecords.clear();

        // 遍历游标，填充借出归还记录
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BORROW_ITEM_NAME));
                String borrowerName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
                String borrowerPhone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE));
                String borrowerEmail = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL));
                String borrowTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BORROW_TIME));
                int expectedDays = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPECTED_DAYS));
                int isBorrowedFlag = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BORROWED_FLAG));

                // 创建 BorrowReturnRecord 对象
                BorrowReturnRecord record = new BorrowReturnRecord(
                        itemName, borrowerName, borrowerPhone, borrowerEmail,
                        borrowTime, expectedDays, isBorrowedFlag
                );

                // 将该记录添加到列表中
                borrowReturnRecords.add(record);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Toast.makeText(this, "没有找到相关借出/归还记录！", Toast.LENGTH_SHORT).show();
        }

        db.close();

        // 创建并设置适配器
        BorrowReturnExpandableAdapter adapter = new BorrowReturnExpandableAdapter(this, borrowReturnRecords);
        expandableListView.setAdapter(adapter);
    }
}
