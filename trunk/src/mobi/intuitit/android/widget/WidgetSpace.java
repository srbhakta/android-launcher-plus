package mobi.intuitit.android.widget;

import mobi.intuitit.android.content.LauncherIntent;
import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author Bo
 * 
 */
public abstract class WidgetSpace extends ViewGroup {

    protected boolean mAllowLongPress;

    protected int mCurrentScreen;

    public WidgetSpace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WidgetSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetSpace(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        // do nothing here
    }

    public abstract Activity getLauncherActivity();

    /**
     * 
     */
    BroadcastReceiver mAnimationProvider = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("AnimationProvider", "" + intent);

            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            AppWidgetHostView widgetView = null;

            try {
                // Find widget view in the current view
                widgetView = findWidget(mCurrentScreen, widgetId);

                // Do corresponding animation on it
                if (LauncherIntent.Action.ACTION_START_FRAME_ANIMATION.equals(action)) {
                    actFrameAnimation(widgetView, intent.getIntExtra(
                            LauncherIntent.Extra.EXTRA_IMAGEVIEW_ID, 0), intent, true);
                } else if (LauncherIntent.Action.ACTION_STOP_FRAME_ANIMATION.equals(action)) {
                    actFrameAnimation(widgetView, intent.getIntExtra(
                            LauncherIntent.Extra.EXTRA_IMAGEVIEW_ID, 0), intent, false);
                } else if (LauncherIntent.Action.ACTION_START_TWEEN_ANIMATION.equals(action)) {
                    startTweenAnimation(widgetView, intent.getIntExtra(
                            LauncherIntent.Extra.EXTRA_VIEW_ID, 0), intent);
                }
            } catch (FrameAnimationException ae) {
                // Reset the action and broadcast
                if (widgetView != null)
                    intent.setComponent(widgetView.getAppWidgetInfo().provider);
                getContext().sendBroadcast(
                        intent.setAction(ae.mAction).putExtra(
                                LauncherIntent.Extra.EXTRA_ERROR_MESSAGE, ae.getMessage()));
            } catch (TweenAnimationException ae) {
                // Reset the action and broadcast
                if (widgetView != null)
                    intent.setComponent(widgetView.getAppWidgetInfo().provider);
                getContext().sendBroadcast(
                        intent.setAction(ae.mAction).putExtra(
                                LauncherIntent.Extra.EXTRA_ERROR_MESSAGE, ae.getMessage()));
            } catch (Exception e) {
                // TODO may be flurry may help me collect this kind of exceptions
            }
        }

        /**
         * 
         * @param widgetView
         * @param imgViewId
         * @param intent
         * @param start
         *            true to start, false to stop
         * @throws AnimationException
         */
        void actFrameAnimation(AppWidgetHostView widgetView, int imgViewId, Intent intent,
                boolean start) throws FrameAnimationException {

            if (widgetView == null)
                throw new FrameAnimationException("Cannot find queried widget "
                        + intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        + " in the current screen.");

            // Start animation
            try {
                ImageView imgView = (ImageView) widgetView.findViewById(imgViewId);
                AnimationDrawable ad = (AnimationDrawable) ((ImageView) imgView).getDrawable();

                if (ad == null)
                    return;

                if (start) {
                    // Start the animation
                    ad.start();
                    getContext()
                            .sendBroadcast(
                                    intent
                                            .setComponent(widgetView.getAppWidgetInfo().provider)
                                            .setAction(
                                                    LauncherIntent.Notification.NOTIFICATION_FRAME_ANIMATION_STARTED));
                } else {
                    // Stop the animation
                    ad.stop();
                    getContext()
                            .sendBroadcast(
                                    intent
                                            .setComponent(widgetView.getAppWidgetInfo().provider)
                                            .setAction(
                                                    LauncherIntent.Notification.NOTIFICATION_FRAME_ANIMATION_STOPPED));
                }
            } catch (Exception e) {
                throw new FrameAnimationException(
                        "Fail to start frame animation on queried ImageView: " + imgViewId);
            }

        }

        /**
         * 
         * @param widgetView
         * @param viewId
         * @param intent
         * @param start
         *            true to start, false to stop
         * @throws TweenAnimationException
         * @throws AnimationException
         */
        void startTweenAnimation(AppWidgetHostView widgetView, int viewId, Intent intent)
                throws Exception, TweenAnimationException {

            if (widgetView == null)
                throw new NullPointerException("Cannot find queried widget "
                        + intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        + " in the current screen.");

            int animId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_ANIMATION_ID, -1);

            try {
                // Create a context for resources loading later
                Context remoteContext = getContext().createPackageContext(
                        widgetView.getAppWidgetInfo().provider.getPackageName(),
                        Context.CONTEXT_IGNORE_SECURITY);

                // Load animation from remote context
                Animation a = AnimationUtils.loadAnimation(remoteContext, animId);
                a.setAnimationListener(new TweenAnimListener(intent.setComponent(widgetView
                        .getAppWidgetInfo().provider)));

                // Generally, I don't recommend this, for you never know if the user is still
                // staying at this screen when the animation start
                long animStartTime = intent.getLongExtra(
                        LauncherIntent.Extra.EXTRA_ANIMATION_STARTTIME, -1);
                if (animStartTime > 0) {
                    a.setStartTime(animStartTime);
                }

                // Check and verify
                if (a.getRepeatCount() == Animation.INFINITE)
                    a.setRepeatCount(0);
                if (a.getRepeatCount() > 10)
                    a.setRepeatCount(10);

                // Start the animation
                widgetView.findViewById(viewId).startAnimation(a);

            } catch (NameNotFoundException e) {
                throw new TweenAnimationException("Cannot load resources");
            } catch (Exception e) {
                e.printStackTrace();
                throw new TweenAnimationException("Cannot start animation: " + animId);
            }
        }

        /**
         * 
         * @author bo
         * 
         */
        class TweenAnimListener implements AnimationListener {

            Intent mIntent;

            TweenAnimListener(Intent intent) {
                mIntent = intent;
            }

            public void onAnimationEnd(Animation animation) {
                getContext()
                        .sendBroadcast(
                                mIntent
                                        .setAction(LauncherIntent.Notification.NOTIFICATION_TWEEN_ANIMATION_ENDED));
                mIntent = null;
                animation.setAnimationListener(null);
            }

            public void onAnimationRepeat(Animation animation) {
                getContext()
                        .sendBroadcast(
                                mIntent
                                        .setAction(LauncherIntent.Notification.NOTIFICATION_TWEEN_ANIMATION_REPEATED));
            }

            public void onAnimationStart(Animation animation) {
                getContext()
                        .sendBroadcast(
                                mIntent
                                        .setAction(LauncherIntent.Notification.NOTIFICATION_TWEEN_ANIMATION_STARTED));
            }

        }

        /**
         * Find widget in a given screen
         * 
         * @param screen
         * @param appWidgetId
         * @return
         */
        private final AppWidgetHostView findWidget(int screen, int appWidgetId) {
            if (appWidgetId < 0)
                return null;

            WidgetCellLayout cells = (WidgetCellLayout) getChildAt(screen);
            for (int i = cells.getChildCount() - 1; i >= 0; i--) {
                try {
                    AppWidgetHostView widgetView = (AppWidgetHostView) cells.getChildAt(i);
                    if (widgetView.getAppWidgetId() == appWidgetId)
                        return widgetView;
                } catch (Exception e) {
                }
            }

            return null;
        }
    };

    ScrollViewProvider mScrollViewProvider = new ScrollViewProvider();

    class ScrollViewProvider extends BroadcastReceiver implements OnScrollListener {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("RefactorReceiver", "" + intent);

            // Try to get the widget view
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            AppWidgetHostView widgetView = findWidget(widgetId);

            if (widgetView == null) {
                getContext().sendBroadcast(
                        intent.setAction(LauncherIntent.Error.ERROR_SCROLL_CURSOR).putExtra(
                                LauncherIntent.Extra.EXTRA_ERROR_MESSAGE,
                                "Cannot find app widget with id: " + widgetId));
                return;
            }

            final ComponentName appWidgetProvider = widgetView.getAppWidgetInfo().provider;

            if (TextUtils.equals(action, LauncherIntent.Action.ACTION_SCROLL_WIDGET_START)) {
                String error = makeScrollable(context, intent, widgetView);
                if (error == null) {
                    // send finish signal
                    intent.setComponent(appWidgetProvider);
                    getContext().sendBroadcast(
                            intent.setAction(LauncherIntent.Action.ACTION_FINISH));
                } else {
                    // send error message
                    intent.setComponent(appWidgetProvider);
                    getContext().sendBroadcast(
                            intent.setAction(LauncherIntent.Error.ERROR_SCROLL_CURSOR).putExtra(
                                    LauncherIntent.Extra.EXTRA_ERROR_MESSAGE, error));
                }
            }
        }

        private synchronized String makeScrollable(Context context, Intent intent,
                AppWidgetHostView widgetView) {

            // get the dummy view to replace
            final int dummyViewId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, -1);
            if (dummyViewId <= 0)
                return "Dummy view id needed.";

            final ComponentName appWidgetProvider = widgetView.getAppWidgetInfo().provider;

            try {
                // Create a context for loading resources
                Context remoteContext = getContext().createPackageContext(
                        widgetView.getAppWidgetInfo().provider.getPackageName(),
                        Context.CONTEXT_IGNORE_SECURITY);

                ListView lv;

                final int listViewResId = intent.getIntExtra(
                        LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_LAYOUT_ID, -1);
                if (listViewResId <= 0) {
                    // try to post the newly created listview to the widget
                    lv = postListView(widgetView, dummyViewId);
                    if (lv == null)
                        return "Cannot create the default list view.";
                } else {
                    // TODO inflate it
                    LayoutInflater inflater = LayoutInflater.from(remoteContext);
                    View v = inflater.inflate(listViewResId, null);
                    if (v instanceof ListView) {
                        lv = (ListView) v;
                        if (!replaceView(widgetView, dummyViewId, lv))
                            return "Cannot replace the dummy with the list view inflated from the passed layout resource id.";
                    } else
                        return "Cannot inflate a list view from the passed layout resource id.";
                }

                final Activity a = getLauncherActivity();
                if (a == null)
                    return null;
        		
                // manage a query
                Cursor cursor = WidgetCursorAdapter.queryForNewContent(a, context.getContentResolver(), intent);
        	
                // create widget cursor adapter
                int aid = widgetView.getAppWidgetId();
                WidgetCursorAdapter wca = new WidgetCursorAdapter(a, remoteContext, cursor, intent,
                        appWidgetProvider, aid, dummyViewId);
                lv.setOnScrollListener(this);
                lv.setAdapter(wca);
                if (!wca.mItemChildrenClickable)
                    lv.setOnItemClickListener(new WidgetItemListener(appWidgetProvider, aid,
                            dummyViewId));

                return null;
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        class WidgetItemListener implements OnItemClickListener {

            ComponentName mAppWidgetProvider;
            int mAppWidgetId;
            int mListViewId;

            WidgetItemListener(ComponentName cname, int id, int viewId) {
                mAppWidgetProvider = cname;
                mAppWidgetId = id;
                mListViewId = viewId;
            }

            public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
                try {
                    Object tag = view.getTag();
                    if (tag != null && tag instanceof String) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tag));
                        getContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent(LauncherIntent.Action.ACTION_ITEM_CLICK);
                        intent.setComponent(mAppWidgetProvider);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_ID, mListViewId);
                        intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS, pos);
                        getContext().sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        /**
         * 
         * @param widgetView
         * @param dummyViewId
         * @return whether the dummy view is replaced by listview
         */
        ListView postListView(AppWidgetHostView widgetView, int dummyViewId) {
            ListView lv = new ListView(getContext());
            lv.setCacheColorHint(0);

            if (replaceView(widgetView, dummyViewId, lv))
                return lv;
            else
                return null;
        }

        /**
         * 
         * @param vg
         * @param id
         * @param replacement
         * @return
         */
        boolean replaceView(ViewGroup vg, int id, View replacement) {
            View child;
            boolean result = false;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                child = vg.getChildAt(i);
                if (child.getId() == id) {
                    // Remove the dummy
                    vg.removeView(child);
                    // Set the replacement id to be the old one
                    replacement.setId(id);
                    // Put the replacement in
                    vg.addView(replacement, i, child.getLayoutParams());
                    return true;
                } else if (child instanceof ViewGroup)
                    result |= replaceView((ViewGroup) child, id, replacement);
            }
            return result;
        }

        private final AppWidgetHostView findWidget(int appWidgetId) {
            AppWidgetHostView wv = null;
            for (int i = getChildCount() - 1; i >= 0; i--) {
                wv = findWidget(i, appWidgetId);
                if (wv != null)
                    break;
            }
            return wv;
        }

        /**
         * Find widget in a given screen
         * 
         * @param screen
         * @param appWidgetId
         * @return
         */
        private final AppWidgetHostView findWidget(int screen, int appWidgetId) {
            if (appWidgetId < 0)
                return null;

            WidgetCellLayout cells = (WidgetCellLayout) getChildAt(screen);
            for (int i = cells.getChildCount() - 1; i >= 0; i--) {
                try {
                    AppWidgetHostView widgetView = (AppWidgetHostView) cells.getChildAt(i);
                    if (widgetView.getAppWidgetId() == appWidgetId)
                        return widgetView;
                } catch (Exception e) {
                }
            }

            return null;
        }

        public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mAllowLongPress = scrollState == SCROLL_STATE_IDLE;
        }
    }

    /**
     * Register receivers given by this workspace
     */
    public void registerProvider() {
        final Context context = getContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction(LauncherIntent.Action.ACTION_START_FRAME_ANIMATION);
        filter.addAction(LauncherIntent.Action.ACTION_STOP_FRAME_ANIMATION);
        filter.addAction(LauncherIntent.Action.ACTION_START_TWEEN_ANIMATION);
        context.registerReceiver(mAnimationProvider, filter);

        IntentFilter scrollFilter = new IntentFilter();
        scrollFilter.addAction(LauncherIntent.Action.ACTION_SCROLL_WIDGET_START);
        context.registerReceiver(mScrollViewProvider, scrollFilter);
    }

    /**
     * Unregister receivers given by this workspace
     */
    public void unregisterProvider() {
        final Context context = getContext();
        unregisterReceiver(context, mAnimationProvider);
        unregisterReceiver(context, mScrollViewProvider);
    }

    /**
     * So en exception in unregistering last receiver will not bypass the second one
     * 
     * @param context
     * @param receiver
     */
    void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
        }
    }

    /**
     * 
     * @author bo
     * 
     */
    class AnimationException extends Exception {

        public String mAction;

        AnimationException(String action, String msg) {
            super(msg);
            mAction = action;

        }
    }

    class FrameAnimationException extends AnimationException {
        public FrameAnimationException(String msg) {
            super(LauncherIntent.Error.ERROR_FRAME_ANIMATION, msg);
        }

    }

    class TweenAnimationException extends AnimationException {

        public TweenAnimationException(String msg) {
            super(LauncherIntent.Error.ERROR_TWEEN_ANIMATION, msg);
        }

    }

}
