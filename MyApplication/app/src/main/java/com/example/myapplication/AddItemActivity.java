package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class AddItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private Spinner spinnerArea, spinnerPosition;
    private EditText editTextName, editTextQuantity, editTextCustomArea, editTextCustomPosition;
    private ImageView imageView;
    private Button btnAddImage, btnAddItem;

    private String selectedArea = "", selectedPosition = "";
    private Bitmap capturedImage = null;
    private Uri photoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 初始化控件
        spinnerArea = findViewById(R.id.spinner_area);
        spinnerPosition = findViewById(R.id.spinner_position);
        editTextName = findViewById(R.id.edit_text_name);
        editTextQuantity = findViewById(R.id.edit_text_quantity);
        editTextCustomArea = findViewById(R.id.edit_text_custom_area);
        editTextCustomPosition = findViewById(R.id.edit_text_custom_position);
        imageView = findViewById(R.id.iv_item_image);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnAddItem = findViewById(R.id.btn_add_item);

        // 加载区域数据到 Spinner
        loadAreaData();

        // 为 "添加图片" 按钮设置点击事件
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });

        // 为 "添加物品" 按钮设置点击事件
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToDatabase();
            }
        });
    }

    /**
     * 加载区域数据到 Spinner
     */
    private void loadAreaData() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                true,  // 去重
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_LOCATION_TYPE},
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<String> areas = new ArrayList<>();
        areas.add("请选择区域"); // 默认选项
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String area = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_TYPE));
                areas.add(area);
            } while (cursor.moveToNext());
            cursor.close();
        }

        areas.add("自定义输入"); // 增加自定义输入选项
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, areas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(adapter);

        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == areas.size() - 1) { // 自定义输入选项
                    editTextCustomArea.setVisibility(View.VISIBLE);
                    selectedArea = "";
                } else {
                    editTextCustomArea.setVisibility(View.GONE);
                    selectedArea = areas.get(position);
                    loadPositionData(selectedArea);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedArea = "";
            }
        });
    }

    /**
     * 加载位置数据到 Spinner
     */
    private void loadPositionData(String area) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                true,  // 去重
                DatabaseHelper.TABLE_ITEMS,
                new String[]{DatabaseHelper.COLUMN_LOCATION_NUMBER},
                DatabaseHelper.COLUMN_LOCATION_TYPE + " = ?",
                new String[]{area},
                null,
                null,
                null,
                null
        );

        List<String> positions = new ArrayList<>();
        positions.add("请选择位置"); // 默认选项
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String position = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION_NUMBER));
                positions.add(position);
            } while (cursor.moveToNext());
            cursor.close();
        }

        positions.add("自定义输入"); // 增加自定义输入选项
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);

        spinnerPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == positions.size() - 1) { // 自定义输入选项
                    editTextCustomPosition.setVisibility(View.VISIBLE);
                    selectedPosition = "";
                } else {
                    editTextCustomPosition.setVisibility(View.GONE);
                    selectedPosition = positions.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPosition = "";
            }
        });
    }

    /**
     * 添加物品到数据库
     */
    private void addItemToDatabase() {
        String name = editTextName.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();
        String finalArea = editTextCustomArea.getVisibility() == View.VISIBLE
                ? editTextCustomArea.getText().toString().trim()
                : selectedArea;
        String finalPosition = editTextCustomPosition.getVisibility() == View.VISIBLE
                ? editTextCustomPosition.getText().toString().trim()
                : selectedPosition;

        if (finalArea.isEmpty() || finalPosition.isEmpty() || name.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "请填写完整信息！", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        String itemId = generateRandomId();

        byte[] qrCodeBytes = generateQRCode(itemId);
        if (qrCodeBytes == null) {
            Toast.makeText(this, "二维码生成失败，请重试！", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ITEM_ID, itemId);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_LOCATION_TYPE, finalArea);
        values.put(DatabaseHelper.COLUMN_LOCATION_NUMBER, finalPosition);

        if (capturedImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            capturedImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            values.put(DatabaseHelper.COLUMN_IMAGE_BLOB, imageBytes);
        }

        long newRowId = db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "物品添加成功！ID: " + itemId, Toast.LENGTH_SHORT).show();

            // 跳转到二维码显示界面
            Intent intent = new Intent(AddItemActivity.this, QRCodeDisplayActivity.class);

            // 确保二维码字节数组非空
            if (qrCodeBytes != null && qrCodeBytes.length > 0) {
                Log.d("QRCode", "准备传递二维码字节数组，大小: " + qrCodeBytes.length);
                intent.putExtra("qrCodeBytes", qrCodeBytes); // 传递字节数组
            } else {
                Log.e("QRCode", "二维码字节数组为空，无法传递");
            }

            intent.putExtra("itemId", itemId);
            intent.putExtra("qrCode", qrCodeBytes);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "物品添加失败！", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    /**
     * 打开摄像头拍照
     */
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                capturedImage = bitmap;
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "拍照未完成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 生成 8 位随机 ID
     */
    private String generateRandomId() {
        Random random = new Random();
        int id = random.nextInt(89999999) + 10000000;
        return String.valueOf(id);
    }

    private byte[] generateQRCode(String id) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(id, BarcodeFormat.QR_CODE, 400, 400);

            // 将 Bitmap 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            // 调试日志
            Log.d("QRCode", "生成二维码字节数组成功，大小: " + qrCodeBytes.length);
            return qrCodeBytes;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("QRCode", "二维码生成失败");
            return null;
        }
    }
}
