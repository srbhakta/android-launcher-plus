The goal is to introduce a ViewFlipper possiblity in the scrollable widget API.

How could it work :


# 1 - Page setup #

When the launcher is ready, it send ACTION\_READY.

On this action, the widget should prepare the viewFlipper initialisation and provider datas for this viewFlipper.

## 1.1 - Preparing ViewFlipper ##

The widget send a ACTION\_PAGE\_SCROLL\_WIDGET\_START intent with :
```
- AppWidgetManager.EXTRA_APPWIDGET_ID (Integer) : the widget ID

- LauncherIntent.Extra.EXTRA_VIEW_ID (Integer) : the dummy view ID of the main widget view to replace by the FlipView

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_LAYOUT_ID (Integer) : the ViewFlipper layout ID

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_ANIMATION_TYPE (Integer) : alpha, translation left/right or translation up/down

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_ANIMATION_DURATION (Long) : in milliseconds

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_CACHE_DEPTH (Integer) : how much pages must be stored in the launcher (from 1 to 3 pages in each direction).

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_WRAP_FIRST_LAST (Boolean): allow ViewFlipper to wrap from/to first/last page.

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_REMOTEVIEWS (RemoteViews[]) : with an array of RemoteViews composed of pages datas

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID (Integer) : ID of first page contained in the provided in the EXTRA_VIEW_FLIPPER_REMOTEVIEWS array
```

## 1.2 - Preparing page datas ##

Each view flipper page will be send in a ACTION\_PAGE\_SCROLL\_WIDGET\_ADD intent with :
```
- AppWidgetManager.EXTRA_APPWIDGET_ID (Integer) : the widget ID

- LauncherIntent.Extra.EXTRA_VIEW_ID (Integer) : the viewFlipper ID

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_REMOTEVIEWS (RemoteViews[]) : with an array of RemoteViews composed of pages datas

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID (Integer) : ID of first page contained in the provided in the EXTRA_VIEW_FLIPPER_REMOTEVIEWS array
```

## 1.3 - Handling in the launcher ##

On ACTION\_PAGE\_SCROLL\_WIDGET\_START receive, the widget viewFlipper is created and initialized with animations settings, and a dummy loading page is set.

A gestureDetector is created on the viewFlipper to detect UP/DOWN gestures.

On ACTION\_PAGE\_SCROLL\_WIDGET\_ADD receive, the widget viewFlipper is loaded with a new page with inflated RemoteViews.


# 2 - User interactions #

## 2.1 - Gestures ##

On user gesture, the new page is loaded by the launcher if the page is available in RemoteViews.

If not, no animation and no page change.

If the page is available and changed, the launcher send a ACTION\_PAGE\_SCROLL\_WIDGET\_REQUEST\_PAGE intent to refill his cache, with :
```
- AppWidgetManager.EXTRA_APPWIDGET_ID (Integer) : the widget ID

- LauncherIntent.Extra.EXTRA_VIEW_ID (Integer) : the viewFlipper ID

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID (Integer) : the current page ID

```

Then the widget should send a new ACTION\_PAGE\_SCROLL\_WIDGET\_ADD to provide the new viewFlipper page.

The launcher need to take care about page cache limits and drop 'old' pages.

## 2.2 - Clicks ##

User interactions with pages will use PendingIntent of RemoteViews

## 2.3 - Widget request to display a specific page ##

The widget can ask to display a specific page by sending a ACTION\_PAGE\_SCROLL\_WIDGET\_SET\_PAGE intent with :
```
- AppWidgetManager.EXTRA_APPWIDGET_ID (Integer) : the widget ID

- LauncherIntent.Extra.EXTRA_VIEW_ID (Integer) : the viewFlipper ID

- LauncherIntent.Extra.PageScroll.EXTRA_VIEW_FLIPPER_PAGE_ID (Integer) : page ID to display
```