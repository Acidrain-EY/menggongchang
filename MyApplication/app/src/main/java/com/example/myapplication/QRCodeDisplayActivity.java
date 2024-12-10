package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QRCodeDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_display);

        ImageView ivQRCode = findViewById(R.id.iv_qr_code);
        TextView tvItemId = findViewById(R.id.tv_item_id);
        Button btnConfirm = findViewById(R.id.btn_confirm);

        // 获取传递的参数
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");
        byte[] qrCodeBytes = intent.getByteArrayExtra("qrCodeBytes");

        if (itemId != null) {
            tvItemId.setText("物品 ID: " + itemId);
        }

        // 验证是否接收到字节数组
        if (qrCodeBytes != null && qrCodeBytes.length > 0) {
            Log.d("QRCode", "接收到二维码字节数组，大小: " + qrCodeBytes.length);
            Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(qrCodeBytes, 0, qrCodeBytes.length);
            if (qrCodeBitmap != null) {
                ivQRCode.setImageBitmap(qrCodeBitmap);
            } else {
                Log.e("QRCode", "二维码字节数组解码失败");
                Toast.makeText(this, "二维码加载失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("QRCode", "未接收到二维码字节数组");
            Toast.makeText(this, "未收到二维码数据！", Toast.LENGTH_SHORT).show();
        }

        btnConfirm.setOnClickListener(v -> {
            Intent mainIntent = new Intent(QRCodeDisplayActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }

}

