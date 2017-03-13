package to.us.datagrip.accountability.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.models.DatabaseHelper;
import to.us.datagrip.accountability.models.DayAccount;
import to.us.datagrip.accountability.utils.DayAxisValueFormatter;
import to.us.datagrip.accountability.utils.Misc;

public class Analysis extends BaseActivity {

    private static final String TAG = "Analysis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        init();
    }

    private void init() {
        LineChart mChart = (LineChart) findViewById(R.id.chart);
        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        setData(mChart);
        mChart.animateX(2500);

        Legend l = mChart.getLegend();
        l.setEnabled(true);
        l.setXEntrySpace(4f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(getResources().getColor(R.color.colorAccent));
        xAxis.setCenterAxisLabels(true);
        mChart.getAxisRight().setEnabled(false);
    }


    public void setData(LineChart chart) {
        DatabaseHelper db = new DatabaseHelper(this);
        List<DayAccount> journals = Misc.getDayAcounts(db.getJournals(0, 100, false));
        ArrayList<Entry> ins = new ArrayList<>();
        ArrayList<Entry> outs = new ArrayList<>();
        Log.d(TAG, String.format("Found %d journals", journals.size()));
        long dates[] = new long[journals.size()];
        float minTime = 0, maxTime = 0, x = 0;

        for (DayAccount journal : journals) {
            float time = journal.getAccountDate().getTime();
            if (minTime != 0) {
                minTime = Math.min(minTime, time);
            } else {
                minTime = time;
            }
            maxTime = Math.max(maxTime, time);
            outs.add(new Entry(x, (float) journal.getTotalExpense()));
            ins.add(new Entry(x, (float) journal.getTotalIncome()));
            dates[(int) x] = (long) time;
            x++;
            Log.d(TAG, journal.getDate());
        }

        LineDataSet income, expenses;
        income = new LineDataSet(ins, "Income");

        income.setAxisDependency(YAxis.AxisDependency.LEFT);
        income.setColor(ColorTemplate.getHoloBlue());
        income.setCircleColor(Color.WHITE);
        income.setLineWidth(2f);
        income.setCircleRadius(3f);
        income.setFillAlpha(65);
        income.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        income.setFillColor(ColorTemplate.getHoloBlue());
        income.setHighLightColor(Color.rgb(244, 117, 117));
        income.setDrawCircleHole(false);

        // create a dataset and give it a type
        expenses = new LineDataSet(outs, "Expenditure");

        expenses.setAxisDependency(YAxis.AxisDependency.LEFT);
        expenses.setColor(Color.RED);
        expenses.setCircleColor(Color.WHITE);
        expenses.setLineWidth(2f);
        expenses.setCircleRadius(3f);
        expenses.setFillAlpha(65);
        expenses.setFillColor(Color.RED);
        expenses.setDrawCircleHole(false);
        expenses.setHighLightColor(Color.rgb(244, 117, 117));

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(income);
        dataSets.add(expenses);

        // create a data object with the datasets
        LineData data = new LineData(dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        chart.setDescription("Income / Expenditure");
        chart.setData(data);

        chart.getAxisLeft().setAxisMinValue(0);
        XAxis xAxis = chart.getXAxis();
//        xAxis.setGranularity(1);
//        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DayAxisValueFormatter(chart, dates));

//          chart.setViewPortOffsets(0f, 0f, 0f, 0f);
    }
}
