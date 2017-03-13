package to.us.datagrip.accountability.models;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Journal {
    public final static int EXPENDITURE = 1;
    public final static int INCOME = 0;
    double amount = 0D;
    List<String> tags;
    Date created_at;
    Date value_at;
    int type = INCOME;
    String description, comment;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = this.comment;
    }

    public String getMonth() {
        SimpleDateFormat format = new SimpleDateFormat("MMMM");
        return format.format(getCreatedAt());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMMM");
        return format.format(getCreatedAt());
    }

    public String getWeekDay() {
        SimpleDateFormat format = new SimpleDateFormat("E");
        return format.format(getCreatedAt());
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getValueAt() {
        return value_at;
    }

    public void setValueAt(Date value_at) {
        this.value_at = value_at;
    }

    public void save(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        db.addJournal(this);
    }

    public Long getDateTime() {
        return created_at.getTime();
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }
}