package to.us.datagrip.accountability.models;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;

import to.us.datagrip.accountability.utils.AutoSpannable;

public class Tag implements AutoSpannable {
    private static final String TAG = "TAG";
    public final int ACTION = 0;
    public final int TEXT = 1;
    String text;
    int type = 0;
    private float fontSize = 14;
    private float marginSize = 12;
    private Context mContext;

    public Tag(String tag, Context context) {
        setText(tag);
        mContext = context;
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

    @Override
    public float autoSpan() {
        Resources resources = mContext.getResources();
        Paint paint = new Paint();
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fontSize, resources.getDisplayMetrics()));
        //Rect textBounds = new Rect();
        //paint.getTextBounds(value, 0, value.length(), textBounds);
        float textMeasure = paint.measureText(getText(), 0, getText().length());

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginSize, resources.getDisplayMetrics());
        float px1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, textMeasure, resources.getDisplayMetrics());

        Log.d(TAG, "Measured: " + px1);
        Log.d(TAG, "Margin:" + px);
        Log.d(TAG, "Total:" + (px + px1));
        return px + px1;
    }
}
