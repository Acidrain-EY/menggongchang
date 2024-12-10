package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "Warehouse.db";
    private static final int DATABASE_VERSION = 2; // 升级版本号

    // 表名
    public static final String TABLE_ITEMS = "Items";

    // 表字段
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_IS_BORROWED = "is_borrowed";
    public static final String COLUMN_LOCATION_TYPE = "location_type";
    public static final String COLUMN_LOCATION_NUMBER = "location_number";
    public static final String COLUMN_IMAGE_BLOB = "image_blob"; // 新增字段

    // 创建表的SQL语句
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ITEM_ID + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    COLUMN_IS_BORROWED + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_LOCATION_TYPE + " TEXT NOT NULL, " +
                    COLUMN_LOCATION_NUMBER + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_BLOB + " BLOB" + // 新增字段
                    ");";

    // 表名
    public static final String TABLE_USERS = "Users";

    // 用户表字段
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PASSWORD = "password";

    // 创建用户表 SQL
    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_PHONE + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL" +
                    ");";

    // 借出归还记录表名
    public static final String TABLE_BORROW_RETURN = "BorrowReturn";

    // 借出归还记录表字段
    public static final String COLUMN_BORROW_RETURN_ID = "id";
    public static final String COLUMN_BORROW_ITEM_ID = "item_id";
    public static final String COLUMN_BORROW_ITEM_NAME = "item_name";
    public static final String COLUMN_USER_ID_BORROW = "user_id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_USER_PHONE = "user_phone";
    public static final String COLUMN_USER_EMAIL = "user_email";
    public static final String COLUMN_BORROW_TIME = "borrow_time";
    public static final String COLUMN_EXPECTED_DAYS = "expected_days";
    public static final String COLUMN_IS_BORROWED_FLAG = "is_borrowed"; // 1: 借出，0: 归还

    // 创建借出归还记录表 SQL
    private static final String TABLE_CREATE_BORROW_RETURN =
            "CREATE TABLE " + TABLE_BORROW_RETURN + " (" +
                    COLUMN_BORROW_RETURN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BORROW_ITEM_ID + " TEXT NOT NULL, " +
                    COLUMN_BORROW_ITEM_NAME + " TEXT NOT NULL, " +
                    COLUMN_USER_ID_BORROW + " TEXT NOT NULL, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_USER_PHONE + " TEXT NOT NULL, " +
                    COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_BORROW_TIME + " TEXT NOT NULL, " +
                    COLUMN_EXPECTED_DAYS + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_IS_BORROWED_FLAG + " INTEGER NOT NULL DEFAULT 1" + // 默认为借出
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // 创建物品表
        db.execSQL(TABLE_CREATE_USERS); // 创建用户表
        db.execSQL(TABLE_CREATE_BORROW_RETURN); // 创建借出归还表
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 如果旧版本小于2，先升级到新版本，创建借出归还表
            db.execSQL(TABLE_CREATE_BORROW_RETURN);
        }

        // 其他表的升级操作
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

}
