package mobi.intuitit.android.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.intuitit.android.content.LauncherIntent;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RemoteViews;
import android.widget.ViewFlipper;

public class ViewFlipperProvider extends BroadcastReceiver {

	private static final String TAG = "ViewFlipperProvider";

	private final WidgetSpace mWidgetSpace;

	class ViewFlipperInfo {
		public ViewFlipper flipper;
		public int flipPosition;

		public List<RemoteViews> pages;
		public SwipeGestureDetector gesturedetector;

		private final int mCacheSize = 3;
		public ViewFlipperInfo() {
			pages = new ArrayList<RemoteViews>(mCacheSize);
		}
	}

	private final HashMap<Integer, HashMap<Integer, ViewFlipperInfo>> mFlipperInfos
	= new HashMap<Integer, HashMap<Integer, ViewFlipperInfo>>();

	public ViewFlipperProvider(WidgetSpace widgetSpace) {
		mWidgetSpace = widgetSpace;
	}


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG, "onReceive action" + action);
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if (widgetId < 0)
            widgetId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID, -1);
        if (widgetId < 0) {
            Log.e(TAG, "Flipper Provider cannot get a legal widget id");
            return;
        }

        AppWidgetHostView widgetView = mWidgetSpace.findWidget(widgetId);

        if (widgetView == null)
            return;

        if (LauncherIntent.Action.ACTION_PAGE_SCROLL_WIDGET_START.equals(action))
        	prepareViewFlipper(widgetId, widgetView, intent);
    }


    private HashMap<Integer, ViewFlipperInfo> getFlipperInfos(int appWidgetId) {
    	Integer id = new Integer(appWidgetId);
    	if (!mFlipperInfos.containsKey(id)) {
    		HashMap<Integer, ViewFlipperInfo> info = new HashMap<Integer, ViewFlipperInfo>();
    		mFlipperInfos.put(id, info);
    	}
    	return mFlipperInfos.get(id);
    }

    private ViewFlipperInfo getFlipperInfo(int appWidgetId, int viewId) {
    	HashMap<Integer, ViewFlipperInfo> info = getFlipperInfos(appWidgetId);
    	Integer id = new Integer(viewId);
    	if (!info.containsKey(id)) {
    		ViewFlipperInfo result = new ViewFlipperInfo(); // TODO  get from intent here!
    		info.put(id, result);
    	}
    	return info.get(id);
    }

    private String prepareViewFlipper(int appWidgetId, AppWidgetHostView view, Intent intent) {
    	final int dummyViewId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, -1);

    	if (dummyViewId <= 0)
            return "Need Dummy View";

        final ComponentName appWidgetProvider = view.getAppWidgetInfo().provider;

        try {
        	Context context = mWidgetSpace.getContext();

            // Create a context for loading resources
            Context remoteContext = context.createPackageContext(
                    view.getAppWidgetInfo().provider.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);

            View dummyView = view.findViewById(dummyViewId);
            if (dummyView == null)
                return "Invalid EXTRA_VIEW_ID";

            ViewFlipperInfo info = getFlipperInfo(appWidgetId, dummyViewId);

            if (dummyView instanceof ViewFlipper)
            	info.flipper = (ViewFlipper) dummyView;
            else {
            	final int flipperViewResId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, -1);
                if (flipperViewResId <= 0) {
                	info.flipper = new ViewFlipper(context);
                	if (info.flipper != null)
                		mWidgetSpace.replaceView(view, dummyViewId, info.flipper);
                	else
                        return "Cannot create the default view flipper.";
                } else {
                    // Inflate it
                    LayoutInflater inflater = LayoutInflater.from(remoteContext);
                    dummyView = inflater.inflate(flipperViewResId, null);
                    if (dummyView instanceof ViewFlipper) {
                        info.flipper = (ViewFlipper) dummyView;
                        if (!mWidgetSpace.replaceView(view, dummyViewId, info.flipper))
                            return "Cannot replace the dummy with the list view inflated from the passed layout resource id.";
                    } else
                        return "Cannot inflate a ViewFlipper from the passed layout id.";
                }
            }

            Parcelable[] pages = intent.getParcelableArrayExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_REMOTEVIEWS);
            info.pages.clear();
            for(Parcelable page : pages) {
            	if (page instanceof RemoteViews)
            		info.pages.add((RemoteViews)page);
            	else
            		return "Viewflipper page is no RemoteViews";
            }
            if (info.gesturedetector == null) {
            	info.gesturedetector = new SwipeGestureDetector(info);
            }
            info.gesturedetector.animationtime =
            	intent.getLongExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_ANIMATION_DURATION, 250);
            final GestureDetector gd = new GestureDetector(info.gesturedetector);

            info.flipper.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gd.onTouchEvent(event);
					return false;
				}
			});

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }



    class SwipeGestureDetector extends SimpleOnGestureListener {

    	private final ViewFlipperInfo mInfo;

    	public SwipeGestureDetector(ViewFlipperInfo info) {
    		mInfo = info;
    	}

		// from:
		// http://www.codeshogun.com/blog/2009/04/16/how-to-implement-swipe-action-in-android/

		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		private static final int LEFT = 0;
		private static final int RIGHT = 1;

		public long animationtime = 250;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

					mInfo.flipPosition++;
					addChild(RIGHT);

					// right to left swipe
					mInfo.flipper.setInAnimation(animateInFrom(RIGHT));
					mInfo.flipper.setOutAnimation(animateOutTo(LEFT));
					mInfo.flipper.showNext();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// left to right swipe

					mInfo.flipPosition--;
					addChild(LEFT);

					mInfo.flipper.setInAnimation(animateInFrom(LEFT));
					mInfo.flipper.setOutAnimation(animateOutTo(RIGHT));
					mInfo.flipper.showPrevious();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}




		private Animation animateInFrom(int fromDirection) {

			Animation inFrom = null;

			switch (fromDirection) {
			case LEFT:
				inFrom = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
				break;
			case RIGHT:
				inFrom = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
				break;
			}

			inFrom.setDuration(animationtime);
			inFrom.setInterpolator(new AccelerateInterpolator());
			return inFrom;
		}

		private Animation animateOutTo(int toDirection) {

			Animation outTo = null;

			switch (toDirection) {
			case LEFT:
				outTo = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
				break;
			case RIGHT:
				outTo = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
				break;
			}

			outTo.setDuration(animationtime);
			outTo.setInterpolator(new AccelerateInterpolator());
			return outTo;
		}











		private View getView() {
			RemoteViews views = mInfo.pages.get(mInfo.flipPosition);
			return views.apply(mWidgetSpace.getContext(), mInfo.flipper);
		}


		private void addChild(int direction) {
			View view = getView();

			if (direction == RIGHT) {
				if (mInfo.flipper.getDisplayedChild() == mInfo.flipper.getChildCount() - 1) {
					mInfo.flipper.addView(view, mInfo.flipper.getChildCount());
				}
				// flip.setDisplayedChild(1);
				// if (flip.getChildCount() >= 2)
				// flip.removeViewAt(0);
			} else {
				if (mInfo.flipper.getDisplayedChild() == 0) {
					mInfo.flipper.addView(view, 0);
					mInfo.flipper.setDisplayedChild(1);
				}
				// if (flip.getChildCount() >= 2)
				// flip.removeViewAt(2);
			}
		}
	}










}
