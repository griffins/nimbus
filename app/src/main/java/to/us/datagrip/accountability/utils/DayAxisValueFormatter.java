package to.us.datagrip.accountability.utils;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DayAxisValueFormatter implements AxisValueFormatter {

    private long dates[];
    private String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private LineChart chart;

    public DayAxisValueFormatter(LineChart chart, long dates[]) {
        this.chart = chart;
        this.dates = dates;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value < 0) {
            return "";
        } else if (value >= dates.length) {
            return "";
        }
        long time = dates[(int) value];
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date(time));
//        Log.d("Formatter", String.format("date %s from value %s", calendar.getTime(), time));
        int year = calendar.get(GregorianCalendar.YEAR);

        int month = calendar.get(GregorianCalendar.MONTH);

        String monthName = mMonths[month % mMonths.length];

        String yearName = String.valueOf(year);

//        Log.d("Formatter", String.format("Visible range is: %s", chart.getVisibleXRange()));
        if (chart.getVisibleXRange() > 30 * 6) {
            return monthName + " " + yearName;
        } else if (chart.getVisibleXRange() > 30 * 6) {
            return monthName + " " + yearName;
        } else {

            int dayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);

            String appendix = "th";

            switch (dayOfMonth) {
                case 1:
                    appendix = "st";
                    break;
                case 2:
                    appendix = "nd";
                    break;
                case 3:
                    appendix = "rd";
                    break;
                case 21:
                    appendix = "st";
                    break;
                case 22:
                    appendix = "nd";
                    break;
                case 23:
                    appendix = "rd";
                    break;
                case 31:
                    appendix = "st";
                    break;
            }
            return dayOfMonth == 0 ? "" : dayOfMonth + appendix + " " + monthName;
        }
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
