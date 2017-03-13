package to.us.datagrip.accountability.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import to.us.datagrip.accountability.utils.DateUtils;
import to.us.datagrip.accountability.utils.PayloadRunnable;

public class DayAccount {
    private Date accountDate;
    private List<Journal> accounts;
    private double totalExpense, totalIncome;
    private PayloadRunnable action;
    private boolean selected;

    public Date getAccountDate() {
        return accountDate;
    }

    public void setAccountDate(Date account_date) {
        this.accountDate = account_date;
    }

    public List<Journal> getAccounts() {
        return accounts;
    }

    public PayloadRunnable getAction() {
        return action;
    }

    public void setAction(PayloadRunnable action) {
        this.action = action;
    }

    public void addAccount(Journal account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        if (accounts.size() == 0) {
            setAccountDate(account.getCreatedAt());
            this.accounts.add(account);
        } else {
            if (DateUtils.isSameDay(accountDate, account.getCreatedAt())) {
                this.accounts.add(account);
            } else {
                throw new IllegalArgumentException("Date is not same date as previous dates");
            }
        }
        if (account.getType() == Journal.EXPENDITURE) {
            totalExpense += account.getAmount();
        } else {
            totalIncome += account.getAmount();
        }
    }

    public String getWeekDay() {
        SimpleDateFormat format = new SimpleDateFormat("E", Locale.getDefault());
        return format.format(accountDate);
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        return format.format(accountDate);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
