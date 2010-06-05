package mobi.intuitit.android.content;

import android.content.Intent;

public class LauncherIntent extends Intent {

    /**
     * 
     */
    public static final String PNAME = "mobi.intuitit.android.hpp.";

    /**
     * 
     * @author bo
     * 
     */
    public static final class Broadcast {

        public static final String BROADCAST_HOME_PAUSE = PNAME + "BROADCAST_HOME_PAUSE";
        public static final String BROADCAST_HOME_RESUME = PNAME + "BROADCAST_HOME_RESUME";

    }

    /**
     * 
     * @author bo
     * 
     */
    public static final class Notification {

        public static final String NOTIFICATION_IN_VIEWPORT = PNAME + "NOTIFICATION_IN_VIEWPORT";
        public static final String NOTIFICATION_OUT_VIEWPORT = PNAME + "NOTIFICATION_OUT_VIEWPORT";

        public static final String NOTIFICATION_WIDGET_SETTINGS_CHANGED = PNAME
                + "NOTIFICATION_WIDGET_SETTINGS_CHANGED";

        public static final String NOTIFICATION_FRAME_ANIMATION_STARTED = PNAME
                + "NOTIFICATION_FRAME_ANIMATION_STARTED";
        public static final String NOTIFICATION_FRAME_ANIMATION_STOPPED = PNAME
                + "NOTIFICATION_FRAME_ANIMATION_STOPPED";

        public static final String NOTIFICATION_TWEEN_ANIMATION_STARTED = PNAME
                + "NOTIFICATION_TWEEN_ANIMATION_STARTED";
        public static final String NOTIFICATION_TWEEN_ANIMATION_REPEATED = PNAME
                + "NOTIFICATION_TWEEN_ANIMATION_REPEATED";
        public static final String NOTIFICATION_TWEEN_ANIMATION_ENDED = PNAME
                + "NOTIFICATION_TWEEN_ANIMATION_ENDED";

    }

    /**
     * 
     * @author bo
     * 
     */
    public static final class Action {

        public static final String ACTION_READY = PNAME + "ACTION_READY";
        public static final String ACTION_FINISH = PNAME + "ACTION_FINISH";

        public static final String ACTION_ITEM_CLICK = PNAME + "ACTION_ITEM_CLICK";
        public static final String ACTION_VIEW_CLICK = PNAME + "ACTION_VIEW_CLICK";

        public static final String ACTION_START_FRAME_ANIMATION = PNAME
                + "ACTION_START_FRAME_ANIMATION";
        public static final String ACTION_STOP_FRAME_ANIMATION = PNAME
                + "ACTION_STOP_FRAME_ANIMATION";

        public static final String ACTION_START_TWEEN_ANIMATION = PNAME
                + "ACTION_START_TWEEN_ANIMATION";

        public static final String ACTION_SCROLL_WIDGET_START = PNAME
                + "ACTION_SCROLL_WIDGET_START";
        public static final String ACTION_SCROLL_WIDGET_CLOSE = PNAME
                + "ACTION_SCROLL_WIDGET_CLOSE";

    }

    /**
     * 
     * @author bo
     * 
     */
    public static final class Error {

        public static final String ERROR_FRAME_ANIMATION = PNAME + "ERROR_FRAME_ANIMATION";
        public static final String ERROR_TWEEN_ANIMATION = PNAME + "ERROR_TWEEN_ANIMATION";

        public static final String ERROR_SCROLL_CURSOR = PNAME + "ERROR_SCROLL_CURSOR";

    }

    /**
     * 
     * @author bo
     * 
     */
    public static final class Extra {

        public static final class Scroll {
            /**
             * The data uri to query
             */
            public static final String EXTRA_DATA_URI = PNAME + "EXTRA_DATA_URI";

            /**
             * Allow or not contentProvider requery 
             * - disable requery for simple MatrixCursor (not handled by Android)
             * - enable requery for SQL database Cursor 
             */
            public static final String EXTRA_DATA_PROVIDER_ALLOW_REQUERY = PNAME + "EXTRA_DATA_PROVIDER_ALLOW_REQUERY";

            /**
             * Whether Home++ is going to add a onItemClickListener or several onClickListener on
             * childern views of item
             */
            public static final String EXTRA_ITEM_CHILDREN_CLICKABLE = PNAME
                    + "EXTRA_ITEM_CHILDREN_CLICKABLE";

            /**
             * Arguments in query
             */
            public static final String EXTRA_PROJECTION = PNAME + "EXTRA_PROJECTION";
            public static final String EXTRA_SELECTION = PNAME + "EXTRA_SELECTION";
            public static final String EXTRA_SELECTION_ARGUMENTS = PNAME
                    + "EXTRA_SELECTION_ARGUMENTS";
            public static final String EXTRA_SORT_ORDER = PNAME + "EXTRA_SORT_ORDER";

            /**
             * The layout resource to be inflated
             */
            public static final String EXTRA_LISTVIEW_LAYOUT_ID = PNAME
                    + "EXTRA_LISTVIEW_LAYOUT_ID";

            /**
             * Listview id
             */
            public static final String EXTRA_LISTVIEW_ID = PNAME + "EXTRA_LISTVIEW_ID";

            /**
             * The layout resource used to inflate an item in adapter
             */
            public static final String EXTRA_ITEM_LAYOUT_ID = PNAME + "EXTRA_ITEM_LAYOUT_ID";

            /**
             * The position of an item
             */
            public static final String EXTRA_ITEM_POS = PNAME + "EXTRA_ITEM_POS";

            /**
             * The uri for onItemClickListener
             */
            public static final String EXTRA_ITEM_ACTION_VIEW_URI_INDEX = PNAME
                    + "EXTRA_ITEM_ACTION_VIEW_URI_INDEX";

            /**
             * Mapping views to cursor indices
             * 
             * @author Bo
             * 
             */
            public static final class Mapping {
                public static final String EXTRA_VIEW_TYPES = PNAME + "EXTRA_VIEW_TYPES";
                public static final String EXTRA_VIEW_IDS = PNAME + "EXTRA_VIEW_IDS";
                public static final String EXTRA_VIEW_CLICKABLE = PNAME + "EXTRA_VIEW_CLICKABLE";
                public static final String EXTRA_DEFAULT_RESOURCES = PNAME
                        + "EXTRA_DEFAULT_RESOURCES";
                public static final String EXTRA_CURSOR_INDICES = PNAME + "EXTRA_CURSOR_INDICES";
            }

            /**
             * View types used in mapping, button could use TEXTVIEW
             * 
             * @author Bo
             * 
             */
            public static final class Types {
                public static final int VIEW = 99;
                public static final int TEXTVIEW = 100;
                public static final int IMAGEBLOB = 101;
                public static final int IMAGERESOURCE = 102;
                public static final int IMAGEURI = 103;
                public static final int TEXTVIEWHTML = 104;
            }

        }

        public static final String EXTRA_IMAGEVIEW_ID = PNAME + "EXTRA_IMAGEVIEW_ID";
        public static final String EXTRA_ANIMATION_ID = PNAME + "EXTRA_ANIMATION_ID";
        public static final String EXTRA_VIEW_ID = PNAME + "EXTRA_VIEW_ID";

        public static final String EXTRA_ANIMATION_STARTTIME = PNAME + "EXTRA_ANIMATION_STARTTIME";

        public static final String EXTRA_ERROR_MESSAGE = PNAME + "EXTRA_ERROR_MESSAGE";

    }

}
