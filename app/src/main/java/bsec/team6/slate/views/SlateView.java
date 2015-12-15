package bsec.team6.slate.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import bsec.team6.slate.R;
import bsec.team6.slate.extras.Config;

public class SlateView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private static final String EXTRA_EVENT_LIST = "event_list";
    private static final String EXTRA_STATE = "instance_state";
    private ArrayList<MotionEvent> eventList = new ArrayList<MotionEvent>();
    private ArrayList<MotionEvent> restoreEventList = new ArrayList<MotionEvent>();
    private boolean clearCanvas = false;

    public SlateView(Context context, AttributeSet attrs) {

        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setDrawingCacheEnabled(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (clearCanvas) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            clearCanvas = false;
        } else {
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            default:
                performTouchEvent(event);
        }
        return true;
    }

    public void saveCanvas(View view) throws FileNotFoundException {
        Date date = new Date();
        File saveFolder = new File(Config.SAVE_LOCATION);
        File file = new File(Config.SAVE_LOCATION + date.toString() + ".jpg");
        if (!saveFolder.exists())
            saveFolder.mkdir();
        getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
        Snackbar.make(view, "Drawing saved to " + Config.SAVE_LOCATION, Snackbar.LENGTH_LONG)
                .setAction("Ok", null).show();
        MediaScannerConnection.scanFile(view.getContext(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });

    }

    public void clearCanvas() {
        clearCanvas = true;
        invalidate();
        path.reset();
        eventList.clear();
        restoreEventList.clear();
        Snackbar.make(findViewById(R.id.slateView), "Cleared canvas", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_STATE, super.onSaveInstanceState());
        bundle.putParcelableArrayList(EXTRA_EVENT_LIST, eventList);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(EXTRA_STATE));
            restoreEventList = bundle.getParcelableArrayList(EXTRA_EVENT_LIST);
            if (restoreEventList == null) {
                restoreEventList = new ArrayList<MotionEvent>();
            }
            for (MotionEvent event : restoreEventList) {
                performTouchEvent(event);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private void performTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(eventX, eventY);
                break;
            default:
                break;
        }
        invalidate();
        eventList.add(MotionEvent.obtain(event));
    }
}




