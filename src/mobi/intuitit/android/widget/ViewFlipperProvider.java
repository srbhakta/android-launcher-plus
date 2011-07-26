package mobi.intuitit.android.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mobi.intuitit.android.content.LauncherIntent;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	private static final int mCacheSize = 3;

	class ViewFlipperInfo {
		public ViewFlipper flipper;
		private int flipPosition;
		public boolean wrapFirstLast;
		
		public HashMap<Integer, RemoteViews> pages;
		public SwipeGestureDetector gesturedetector;

		
		public ViewFlipperInfo() {
			pages = new HashMap<Integer, RemoteViews>();
		}
		
		public int getFlipPosition() {
			return flipPosition;
		}
		
		public boolean flipRight() {
			flipPosition++;
			if (flipPosition >= pages.size()) {
				if (wrapFirstLast)
					flipPosition = 0;
				else {
					flipPosition = pages.size() - 1;
					return false;
				}
			}
			return true;
		}
		
		public boolean flipLeft() {
			flipPosition--;
			if (flipPosition < 0) {
				if (wrapFirstLast) 
					flipPosition = pages.size() -1;
				else {
					flipPosition = 0;
					return false;
				}
			}
			return true;
		}
	}

	private final HashMap<Integer, ViewFlipperInfo> mFlipperInfos
			= new HashMap<Integer, ViewFlipperInfo>();

	public ViewFlipperProvider(WidgetSpace widgetSpace) {
		mWidgetSpace = widgetSpace;
	}


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        logIntent(intent, true);
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
        else if (LauncherIntent.Action.ACTION_PAGE_SCROLL_WIDGET_ADD.equals(action))
        	addPages(widgetId, intent);
    }

    private void addPages(int widgetId, Intent intent) {
    	
    }

    private ViewFlipperInfo getFlipperInfo(int appWidgetId) {
    	Integer id = new Integer(appWidgetId);
    	if (!mFlipperInfos.containsKey(id)) {
    		mFlipperInfos.put(id, new ViewFlipperInfo());
    	}
    	return mFlipperInfos.get(id);
    }


    private void logIntent(Intent intent, boolean extended) {
        if (extended)
                Log.d(TAG, "------------Log Intent------------");
        Log.d(TAG, "Action       : " + intent.getAction());
        if (!extended)
                return;
        Log.d(TAG, "Data         : " + intent.getDataString());
        //Log.d(TAG, "Component    : " + intent.getComponent().toString());
        Log.d(TAG, "Package      : " + intent.getPackage());
        Log.d(TAG, "Flags        : " + intent.getFlags());
        Log.d(TAG, "Scheme       : " + intent.getScheme());
        Log.d(TAG, "SourceBounds : " + intent.getSourceBounds());
        Log.d(TAG, "Type         : " + intent.getType());
        Bundle extras = intent.getExtras();
        if (extras != null) {
                Log.d(TAG, "--Extras--");

                for(String key : extras.keySet()) {
                        Log.d(TAG, key + " --> " + extras.get(key));
                }
                Log.d(TAG, "----------");
        }
        Set<String> cats = intent.getCategories();
        if (cats != null) {
                Log.d(TAG, "--Categories--");
                for(String cat : cats) {
                        Log.d(TAG, " --> " + cat);
                }
                Log.d(TAG, "--------------");
        }
        Log.d(TAG, "----------------------------------");
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

            ViewFlipperInfo info = getFlipperInfo(appWidgetId);

            if (dummyView instanceof ViewFlipper)
            	info.flipper = (ViewFlipper) dummyView;
            else {
            	final int flipperViewResId = intent.getIntExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_LAYOUT_ID, -1);
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
            int curPageID = intent.getIntExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID, 0);
            for(Parcelable page : pages) {
            	if (page instanceof RemoteViews)
            		info.pages.put(new Integer(curPageID++), (RemoteViews)page);
            	else
            		return "Viewflipper page is no RemoteViews";
            }
            if (info.gesturedetector == null) {
            	info.gesturedetector = new SwipeGestureDetector(appWidgetId, info);
            }
            info.gesturedetector.animationtime =
            	intent.getLongExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_ANIMATION_DURATION, 250);
            final GestureDetector gd = new GestureDetector(info.gesturedetector);

            info.flipper.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gd.onTouchEvent(event);
					return true;
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
    	private final int mAppWidgetId;

    	public SwipeGestureDetector(int appWidgetId, ViewFlipperInfo info) {
    		mInfo = info;
    		mAppWidgetId = appWidgetId;
    		addChild(LEFT);
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
			Log.d(TAG, "onFling!");
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
					Log.d(TAG, " * exit1");
					return false;
				}
				Log.d(TAG, "e1:"+e1.getY());
				Log.d(TAG, "e2:"+e2.getY());
				if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d(TAG, " * fling right");
					if (!mInfo.flipRight())
						return false;
					
					addChild(RIGHT);

					// right to left swipe
					mInfo.flipper.setInAnimation(animateInFrom(RIGHT));
					mInfo.flipper.setOutAnimation(animateOutTo(LEFT));
					mInfo.flipper.showNext();
				} else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					// left to right swipe
					Log.d(TAG, " * fling left");
					if (!mInfo.flipLeft())
						return false;
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
			int fp = mInfo.getFlipPosition();
			RemoteViews views = mInfo.pages.get(fp);
			LinkedList<Integer> missingPages = new LinkedList<Integer>(); 
			
			for (int i = fp - mCacheSize; i <= fp + mCacheSize; i++) {
				Integer id = new Integer(i);
				if (!mInfo.pages.containsKey(id)) {
					missingPages.add(id);
				}
			}
			
			if (missingPages.size() > 0) {
				for(Integer ID : missingPages) {
					mWidgetSpace.getContext().sendBroadcast(new Intent(LauncherIntent.Action.ACTION_PAGE_SCROLL_WIDGET_REQUEST_PAGE)
					.putExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID, mAppWidgetId)
					.putExtra(LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID, ID.intValue()));				
				}
			}
			
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
