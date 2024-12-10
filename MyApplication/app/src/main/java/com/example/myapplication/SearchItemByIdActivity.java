package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SearchItemByIdActivity extends AppCompatActivity {

    private EditText editTextItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_item_by_id);

        // 初始化控件
        editTextItemId = findViewById(R.id.edit_text_item_id);
        Button btnSearch = findViewById(R.id.btn_search);

        // 设置按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemId = editTextItemId.getText().toString().trim();
                if (itemId.isEmpty()) {
                    Toast.makeText(SearchItemByIdActivity.this, "请输入物品 ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 跳转到 DisplayActivity，并传递物品 ID
                Intent intent = new Intent(SearchItemByIdActivity.this, DisplayActivity.class);
                intent.putExtra("itemId", itemId); // 仅传递 itemId
                startActivity(intent);
            }
        });

        Button btnScanQR = findViewById(R.id.btn_scan_qr);
        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(SearchItemByIdActivity.this, QRCodeScannerActivity.class);
            startActivity(intent);
        });

    }
}

