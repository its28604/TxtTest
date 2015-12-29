package its28604.txttest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by its28 on 2015/12/21.
 */
public class Maps extends View {

    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Canvas mCanvas = new Canvas();

    private float mSpeed = 10.0f;  //更改顯示速度(寬窄)，數字越小顯示越密;最小設1.0f。
    private int mColor;
    private float start_point_x;
    private float start_point_y;
    private float maxValue = 1024f;
    private float old_x = start_point_x;
    private float old_y = start_point_y;

    //界線
    private float mWidth;
    private float mHight;

    public Maps(Context context) {
        super(context);
        init();
    }

    public Maps(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mColor = Color.WHITE;
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG); //消除鋸齒
        mPaint.setColor(mColor);
    }

    public void addDataPoint(float x, float y) {
        Paint paint = mPaint;
        paint.setColor(mColor);
        x = old_x + x;
        y = old_y + y;
        Log.d("old_x", String.valueOf(old_x));
        mCanvas.drawLine(old_x, old_y, x, y, paint);
        old_x = x;
        old_y = y;
        invalidate();
    }

    public void Invalidate() {
        mCanvas.drawColor(Color.BLACK);
        old_x = start_point_x;
        old_y = start_point_y;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.BLACK);
        mWidth = w;
        mHight = h;
        start_point_x = w / 2;
        start_point_y = h / 2;
        old_x = start_point_x;
        old_y = start_point_y;
        Log.d("start_point", String.valueOf(start_point_x));
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
//                if (old_x >= mWidth || old_y >= mHight) {
//                    old_x = start_point_x;
//                    old_y = start_point_y;
//                    canvas.drawColor(Color.BLACK);
//                    canvas.drawLine(old_x, old_y, old_x, old_y, mPaint);
//                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }
    }
}
