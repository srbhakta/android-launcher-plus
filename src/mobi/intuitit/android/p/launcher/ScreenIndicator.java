package mobi.intuitit.android.p.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

/**
 * Show which screen is on
 * 
 * @author bo
 * 
 */
public final class ScreenIndicator extends View implements AnimationListener {

    private int mCurrentScreen;
    private int mTotalScreens;

    private Paint mPaint;

    Animation indicatorFadeOut;
    Animation indicatorFadeIn;

    public ScreenIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    private Workspace mWorkspace;

    void setWorkspace(Workspace w) {
        mWorkspace = w;
    }

    static final int GAP = 2;
    static final int DARK = 0xff333333;

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTotalScreens == 0 || mWorkspace == null)
            return;

        // Get screen dimensions
        final int scrollX = mWorkspace.getScrollX();
        final View screen = mWorkspace.getChildAt(mCurrentScreen);
        int screenLeft = screen.getLeft();
        int screenWidth = screen.getWidth();

        // Get biased screen position for display
        int drawScreen = mCurrentScreen;

        int next = drawScreen + ((scrollX > screenLeft) ? 1 : -1);
        next = (next + mTotalScreens) % mTotalScreens;

        int diff = Math.abs(screenLeft - scrollX);

        int indicatorWidth = getWidth();
        int indicatorHeight = getHeight();

        int interval;
        if (indicatorWidth > indicatorHeight) {
            // Horizontal
            interval = indicatorWidth / mTotalScreens;
            for (int i = 0, left = 0; i < mTotalScreens; i++, left += interval) {
                mPaint.setColor(DARK);
                canvas.drawRect(left + GAP, 0, left + interval - GAP, indicatorHeight, mPaint);
                if (i == drawScreen) {
                    mPaint.setColor(Color.WHITE);
                    int rest = screenWidth - diff;
                    mPaint.setAlpha((rest * 255) / screenWidth);
                    canvas.drawRect(left + GAP, 0, left + interval - GAP, indicatorHeight, mPaint);
                } else if (i == next) {
                    mPaint.setColor(Color.WHITE);
                    mPaint.setAlpha((diff * 255) / screenWidth);
                    canvas.drawRect(left + GAP, 0, left + interval - GAP, indicatorHeight, mPaint);
                }
            }
        } else {
            // Vertical
            drawScreen = mTotalScreens - drawScreen - 1;
            next = mTotalScreens - next - 1;
            interval = indicatorHeight / mTotalScreens;
            for (int i = 0, top = 0; i < mTotalScreens; i++, top += interval) {
                // Draw base
                mPaint.setColor(DARK);
                canvas.drawRect(0, top + GAP, indicatorWidth, top + interval - GAP, mPaint);
                if (i == drawScreen) {
                    // draw current one
                    mPaint.setColor(Color.WHITE);
                    int rest = screenWidth - Math.abs(screenLeft - scrollX);
                    mPaint.setAlpha((rest * 255) / screenWidth);
                    canvas.drawRect(0, top + GAP, indicatorWidth, top + interval - GAP, mPaint);
                } else if (i == next) {
                    // draw next one
                    mPaint.setColor(Color.WHITE);
                    int rest = Math.abs(screenLeft - scrollX);
                    mPaint.setAlpha((rest * 255) / screenWidth);
                    canvas.drawRect(0, top + GAP, indicatorWidth, top + interval - GAP, mPaint);
                }
            }
        }

    }

    /**
     * 
     * @param current
     * @param total
     */
    void setScreen(int current, int total) {
        if (current >= total || total <= 0 || mWorkspace == null)
            return;

        mCurrentScreen = current;
        mTotalScreens = total;
        // ondraw will be invoked by others
    }

    /**
     * 
     */
    void fadeIn() {
        if (indicatorFadeIn == null)
            indicatorFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_fast);
        setVisibility(View.VISIBLE);
        startAnimation(indicatorFadeIn);
    }

    /**
     * 
     */
    void fadeOut() {
        if (indicatorFadeOut == null) {
            indicatorFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast);
            indicatorFadeOut.setAnimationListener(this);
        }
        startAnimation(indicatorFadeOut);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (indicatorFadeOut == animation)
            setVisibility(View.GONE);
    }

    public void onAnimationRepeat(Animation animation) {

    }

    public void onAnimationStart(Animation animation) {
        if (indicatorFadeOut == animation)
            setVisibility(View.VISIBLE);
    }

}
