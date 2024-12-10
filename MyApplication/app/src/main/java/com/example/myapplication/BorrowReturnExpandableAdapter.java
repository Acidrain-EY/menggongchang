package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BorrowReturnExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<BorrowReturnRecord> borrowReturnRecords;

    public BorrowReturnExpandableAdapter(Context context, ArrayList<BorrowReturnRecord> borrowReturnRecords) {
        this.context = context;
        this.borrowReturnRecords = borrowReturnRecords;
    }

    @Override
    public int getGroupCount() {
        return borrowReturnRecords.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1; // 每组只有一个子项
    }

    @Override
    public Object getGroup(int groupPosition) {
        return borrowReturnRecords.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return borrowReturnRecords.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }

        BorrowReturnRecord record = (BorrowReturnRecord) getGroup(groupPosition);

        TextView itemNameTextView = convertView.findViewById(R.id.itemName);
        TextView borrowerNameTextView = convertView.findViewById(R.id.borrowerName);

        itemNameTextView.setText(record.getItemName());
        borrowerNameTextView.setText("借用人: " + record.getBorrowerName());

        // 设置颜色：借出状态为绿色，归还状态为蓝色
        if (record.getStatus() == 1) {
            // 借出状态
            itemNameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBorrowed)); // 绿色
            borrowerNameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBorrowed)); // 绿色
        } else {
            // 归还状态
            itemNameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorReturned)); // 蓝色
            borrowerNameTextView.setTextColor(ContextCompat.getColor(context, R.color.colorReturned)); // 蓝色
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        // 获取对应的 BorrowReturnRecord 对象
        BorrowReturnRecord record = (BorrowReturnRecord) getChild(groupPosition, childPosition);

        TextView borrowerPhoneTextView = convertView.findViewById(R.id.borrowerPhone);
        TextView borrowerEmailTextView = convertView.findViewById(R.id.borrowerEmail);
        TextView borrowTimeTextView = convertView.findViewById(R.id.borrowTime);
        TextView expectedDaysTextView = convertView.findViewById(R.id.expectedDays);
        TextView statusTextView = convertView.findViewById(R.id.status);

        // 填充视图
        borrowerPhoneTextView.setText("电话: " + record.getBorrowerPhone());
        borrowerEmailTextView.setText("邮箱: " + record.getBorrowerEmail());

        if (record.getStatus() == 1) { // 借出
            borrowTimeTextView.setText("借出时间: " + record.getBorrowTime());
            expectedDaysTextView.setText("预计归还天数: " + record.getExpectedDays());
            statusTextView.setText("状态: 借出");

            // 设置颜色：借出状态为绿色
            //borrowTimeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBorrowed));
            //expectedDaysTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBorrowed));
            statusTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBorrowed));
        } else { // 归还
            borrowTimeTextView.setText("归还时间: " + record.getBorrowTime());
            expectedDaysTextView.setText("预计归还天数: " + record.getExpectedDays());
            statusTextView.setText("状态: 归还");

            // 设置颜色：归还状态为蓝色
            //borrowTimeTextView.setTextColor(ContextCompat.getColor(context, R.color.colorReturned));
            //expectedDaysTextView.setTextColor(ContextCompat.getColor(context, R.color.colorReturned));
            statusTextView.setTextColor(ContextCompat.getColor(context, R.color.colorReturned));
        }

        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

