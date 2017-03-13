package to.us.datagrip.accountability.utils.mpesa;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

class DateParser {
    private Date date;

    private DateParser(String text) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        date = format.parse(text);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
    }

    public static Date from(String text) throws ParseException {
        return new DateParser(text).getDate();
    }

    private Date getDate() {
        return date;
    }
}