package mobi.intuitit.android.p.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

/**
 * 
 * @author bo
 * 
 */
public class ScreenLayout implements Animation.AnimationListener, OnClickListener {

    public interface onScreenChangeListener {
        void onScreenChange(int toScreen);

        void onScreenManagerOpened();

        void onScreenManagerClosed();
    }

    private View mLayout;

    LinearLayout mLine1;
    LinearLayout mLine2;
    LinearLayout mLine3;

    Animation inAnim;
    Animation outAnim;

    onScreenChangeListener mScreenChangeListener;

    void setScreenChangeListener(onScreenChangeListener l) {
        mScreenChangeListener = l;
    }

    /**
     * 
     * @param layout
     */
    ScreenLayout(View layout) {
        mLayout = layout;
        layout.setClickable(true);
        layout.setOnClickListener(this);

        mLine1 = (LinearLayout) layout.findViewById(R.id.line1);
        mLine2 = (LinearLayout) layout.findViewById(R.id.line2);
        mLine3 = (LinearLayout) layout.findViewById(R.id.line3);

        inAnim = AnimationUtils.loadAnimation(layout.getContext(), R.anim.scale_fade_in);
        inAnim.setAnimationListener(this);

        outAnim = AnimationUtils.loadAnimation(layout.getContext(), R.anim.scale_fade_out);
        outAnim.setAnimationListener(this);

    }

    /**
     * 
     * @param context
     */
    void show(Context context, Bitmap[] screens) {
        setScreens(context, screens);
        mLayout.setVisibility(View.VISIBLE);
        mLayout.startAnimation(inAnim);
    }

    /**
     * 
     */
    void fadeOut() {
        mLayout.startAnimation(outAnim);
    }

    /**
     * 
     * @return
     */
    boolean isShown() {
        return mLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * 
     * @return
     */
    boolean isFilled(Bitmap[] screens) {
        return mLine1.getChildCount() + mLine2.getChildCount() + mLine3.getChildCount() == screens.length;
    }

    ImageView[] mScreenViews;
    static final LinearLayout.LayoutParams LAYOUT_PARAMS = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    static final int BACK_ID = R.drawable.screen_plate;

    /**
     * Setup imageviews
     */
    private void setScreens(Context context, Bitmap[] screens) {
        // Get screens bitmaps from launcher
        if (screens == null)
            return;

        Bitmap screenThumb;
        if (isFilled(screens)) {
            for (int i = screens.length - 1; i >= 0; i--) {
                screenThumb = screens[i];
                if (screenThumb != null && !screenThumb.isRecycled())
                    mScreenViews[i].setImageBitmap(screenThumb);
            }
            return;
        }

        // Clean up linear layout
        mLine1.removeAllViews();
        mLine2.removeAllViews();
        mLine3.removeAllViews();

        mScreenViews = new ImageView[screens.length];

        // Add imageviews to it
        switch (screens.length) {
        case 3:
            for (int i = 0; i < 3; i++)
                addImageView(mLine2, screens, i);
            break;
        case 4:
        case 6:
            final int perLine = screens.length / 2;
            for (int i = 0; i < perLine; i++) {
                addImageView(mLine1, screens, i);
                addImageView(mLine2, screens, i + perLine);
            }
            break;
        case 5:
            for (int i = 0; i < 3; i++)
                addImageView(mLine1, screens, i);
            // Add to the line vertical centered
            for (int i = 3; i < 5; i++)
                addImageView(mLine2, screens, i);
            break;
        case 7:
            final int offset = screens.length - 2;
            for (int i = 0; i < 2; i++) {
                addImageView(mLine1, screens, i);
                addImageView(mLine3, screens, i + offset);
            }
            // Add to the line vertical centered
            for (int i = 2; i < offset; i++)
                addImageView(mLine2, screens, i);
            break;
        }
    }

    /**
     * 
     * @param line
     * @param screens
     * @param i
     */
    private void addImageView(LinearLayout line, Bitmap[] screens, int i) {
        if (i >= mScreenViews.length)
            return;

        final Bitmap screenThumb = screens[i];
        if (screenThumb == null || screenThumb.isRecycled())
            return;

        mScreenViews[i] = new ImageView(line.getContext());

        mScreenViews[i].setScaleType(ScaleType.MATRIX);
        mScreenViews[i].setImageBitmap(screenThumb);
        mScreenViews[i].setBackgroundResource(BACK_ID);
        mScreenViews[i].setOnClickListener(this);

        line.addView(mScreenViews[i], LAYOUT_PARAMS);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == outAnim) {
            mLayout.setVisibility(View.GONE);
            if (mScreenChangeListener != null)
                mScreenChangeListener.onScreenManagerClosed();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onAnimationStart(Animation animation) {
        mLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v == mLayout) {
            fadeOut();
            return;
        } else if (v instanceof ImageView) {
            for (int i = mScreenViews.length - 1; i >= 0; i--)
                if (v == mScreenViews[i]) {
                    if (mScreenChangeListener != null)
                        mScreenChangeListener.onScreenChange(i);
                    fadeOut();
                    return;
                }
        }
    }

}
