package com.example.myapplication;

public class BorrowReturnRecord {
    private String itemName;
    private String borrowerName;
    private String borrowerPhone;
    private String borrowerEmail;
    private String borrowTime;  // 只有借出时间
    private int expectedDays;
    private int status; // 1 表示借出，0 表示归还

    // 构造函数
    public BorrowReturnRecord(String itemName, String borrowerName, String borrowerPhone,
                              String borrowerEmail, String borrowTime, int expectedDays, int status) {
        this.itemName = itemName;
        this.borrowerName = borrowerName;
        this.borrowerPhone = borrowerPhone;
        this.borrowerEmail = borrowerEmail;
        this.borrowTime = borrowTime;
        this.expectedDays = expectedDays;
        this.status = status;
    }

    // Getter 和 Setter 方法
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    public void setBorrowerPhone(String borrowerPhone) {
        this.borrowerPhone = borrowerPhone;
    }

    public String getBorrowerEmail() {
        return borrowerEmail;
    }

    public void setBorrowerEmail(String borrowerEmail) {
        this.borrowerEmail = borrowerEmail;
    }

    public String getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(String borrowTime) {
        this.borrowTime = borrowTime;
    }

    public int getExpectedDays() {
        return expectedDays;
    }

    public void setExpectedDays(int expectedDays) {
        this.expectedDays = expectedDays;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
