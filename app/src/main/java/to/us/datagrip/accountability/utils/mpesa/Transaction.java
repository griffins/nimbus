package to.us.datagrip.accountability.utils.mpesa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import to.us.datagrip.accountability.utils.Misc;

public class Transaction {
    private double amount = -1, balance = 0;
    private Date transactionDate;
    private String referenceNo, otherAccountNo, otherName, text;
    private int type = -1;
    private boolean selected;

    public Transaction() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getOtherAccountNo() {
        return otherAccountNo;
    }

    public void setOtherAccountNo(String otherAccountNo) {
        this.otherAccountNo = otherAccountNo;
    }

    public String getOtherName() {
        return otherName;
    }

    public String getOtherName(boolean title) {
        if (title) {
            return Misc.title(otherName);
        }
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isInvalid() {
        return (getType() < 0 && getBalance() < 0);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", balance=" + balance +
                ", transactionDate=" + transactionDate +
                ", referenceNo='" + referenceNo + '\'' + "\n\t" +
                ", otherAccountNo='" + otherAccountNo + '\'' +
                ", otherName='" + otherName + '\'' +
                ", type=" + getTypeText() +
                '}';
    }

    public boolean isSelected() {
        return selected;
    }

    public String getDate() {
        if (getTransactionDate() == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
        return format.format(getTransactionDate());
    }

    public String getWeekDay() {
        if (getTransactionDate() == null) {
            return "\u2639";
        }
        SimpleDateFormat format = new SimpleDateFormat("E", Locale.getDefault());
        return format.format(getTransactionDate());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTypeText() {
        switch (getType()) {
            case 6:
                return "Bought airtime";
            case 7:
                return "Bought airtime for";
            case 4:
            case 5:
                return "Paid to";
            case 3:
                return "Sent to";
            case 2:
                return "Received from";
            default:
                return "Unknown";
        }
    }

    public interface Types {
        int WITHDRAWAL = 0;
        int DEPOSIT = 1;
        int RECEIVED = 2;
        int SENT = 3;
        int PAYBILL = 4;
        int BUYGOODS = 5;
        int AIRTIME_PUCHASE_SELF = 6;
        int AIRTIME_PUCHASE_OTHER = 7;
        int TRANSACTION_REVERSAL = 8;
        int TRANSFER_FROM = 9;
        int TRANSFER_TO = 10;
        int BALANCE_INQUIRY = 11;
    }

}
