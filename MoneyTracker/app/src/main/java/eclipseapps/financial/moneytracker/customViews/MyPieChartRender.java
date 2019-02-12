package eclipseapps.financial.moneytracker.customViews;

import android.graphics.Canvas;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class MyPieChartRender extends PieChartRenderer {
   float txtSize=15f;
    public MyPieChartRender(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValues(Canvas c) {
        super.drawValues(c);
    }

    @Override
    public void drawValue(Canvas c, IValueFormatter formatter, float value, Entry entry, int dataSetIndex, float x, float y, int color) {
        mValuePaint.setColor(color);
        String[] lines=formatter.getFormattedValue(value,entry,dataSetIndex,mViewPortHandler).split("\n");
        float lineSpace = txtSize * 0.2f;  //default line spacing
        y=y-txtSize*.8f;
        for (int i = 0; i < lines.length; ++i) {
            c.drawText(lines[i], x, y + (txtSize + lineSpace) * i, mValuePaint);
        }

    }

    @Override
    protected void drawDataSet(Canvas c, IPieDataSet dataSet) {
        txtSize=dataSet.getValueTextSize();
        super.drawDataSet(c, dataSet);
    }
}
