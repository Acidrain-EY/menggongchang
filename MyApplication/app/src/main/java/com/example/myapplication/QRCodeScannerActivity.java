package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class QRCodeScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// 启动二维码扫描，使用自定义的竖屏扫描界面
        new IntentIntegrator(this)
                .setCaptureActivity(MyCaptureActivity.class)  // 使用自定义活动
                .setOrientationLocked(false)  // 不锁定方向
                .setPrompt("对准二维码以扫描")  // 提示文字
                .setBeepEnabled(true)  // 成功播放提示音
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 获取扫描结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // 扫描成功，解析二维码内容
                String scannedId = result.getContents();
                validateScannedId(scannedId);
            } else {
                Toast.makeText(this, "未扫描到任何内容", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 验证扫描结果是否有效
     */
    private void validateScannedId(String scannedId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ITEMS,
                null,
                DatabaseHelper.COLUMN_ITEM_ID + " = ?",
                new String[]{scannedId},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // 匹配成功，跳转到 DisplayActivity 显示物品详情
            Intent intent = new Intent(QRCodeScannerActivity.this, DisplayActivity.class);
            intent.putExtra("itemId", scannedId);

            // 获取其他相关信息（区域和位置）
            String area = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_TYPE));
            String positionNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_NUMBER));

            intent.putExtra("area", area);
            intent.putExtra("positionNumber", positionNumber);
            startActivity(intent);
            cursor.close();
            finish();
        } else {
            Toast.makeText(this, "未找到对应的物品", Toast.LENGTH_SHORT).show();
            if (cursor != null) {
                cursor.close();
            }
            finish();
        }

        db.close();
    }
}

