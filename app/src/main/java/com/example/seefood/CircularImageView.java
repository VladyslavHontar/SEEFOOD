package com.example.seefood;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class CircularImageView extends AppCompatImageView {
    private Paint paint;
    private BitmapShader shader;
    private Matrix shaderMatrix;

    public CircularImageView(Context context) {
        super(context);
        init();
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        shaderMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = getBitmapFromDrawable(getDrawable());
        if (bitmap != null) {
            // Create the BitmapShader
            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            float radius = Math.min(viewWidth / 2.0f, viewHeight / 2.0f);

            // Compute the scale and translation to center the image properly
            float scale;
            float dx = 0;
            float dy = 0;

            if (bitmap.getWidth() * viewHeight > viewWidth * bitmap.getHeight()) {
                scale = viewHeight / (float) bitmap.getHeight();
                dx = (viewWidth - bitmap.getWidth() * scale) * 0.5f;
            } else {
                scale = viewWidth / (float) bitmap.getWidth();
                dy = (viewHeight - bitmap.getHeight() * scale) * 0.5f;
            }

            shaderMatrix.setScale(scale, scale);
            shaderMatrix.postTranslate(dx, dy);
            shader.setLocalMatrix(shaderMatrix);

            paint.setShader(shader);

            canvas.drawCircle(viewWidth / 2.0f, viewHeight / 2.0f, radius, paint);
        } else {
            super.onDraw(canvas);
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = getWidth() > 0 ? getWidth() : drawable.getIntrinsicWidth();
        int height = getHeight() > 0 ? getHeight() : drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
