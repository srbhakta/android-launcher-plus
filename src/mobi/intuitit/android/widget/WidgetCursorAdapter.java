package mobi.intuitit.android.widget;

import mobi.intuitit.android.content.LauncherIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Bo
 * 
 */
public class WidgetCursorAdapter extends CursorAdapter {

    // Cursor mCursor;

    static final int IMPOSSIBLE_INDEX = -100;

    final LayoutInflater mInflater;

    final int mItemLayoutId;

    final int mAppWidgetId;
    final int mListViewId;

    ItemMapping[] mItemMappings;

    class ItemMapping {
        int type;
        int layoutId;
        int defaultResource;
        int index;
        boolean clickable;

        /**
         * 
         * @param t
         *            view type
         * @param l
         *            layout id
         * @param i
         *            index
         * @param r
         *            default resource
         * @param u
         *            clickable
         */
        ItemMapping(int t, int l, int i, int r, boolean u) {
            type = t;
            layoutId = l;
            defaultResource = r;
            index = i;
            clickable = u;
        }

        ItemMapping(int t, int l, int i) {
            type = t;
            layoutId = l;
            index = i;
            defaultResource = -1;
            clickable = false;
        }
    }

    public final boolean mItemChildrenClickable;
    final int mItemActionUriIndex;
    ComponentName mAppWidgetProvider;

    /**
     * 
     * @param context
     *            remote context
     * @param c
     *            cursor for reading data
     * @param intent
     *            broadcast intent initiated the replacement, don't save it
     * @param appWidgetId
     * @param listViewId
     */
    public WidgetCursorAdapter(Context context, Cursor c, Intent intent, ComponentName provider,
            int appWidgetId, int listViewId) throws IllegalArgumentException {
        super(context, c, true);

        mAppWidgetId = appWidgetId;
        mListViewId = listViewId;

        mAppWidgetProvider = provider;
        mInflater = LayoutInflater.from(context);

        // Get the layout if for items
        mItemLayoutId = intent.getIntExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_LAYOUT_ID, -1);
        if (mItemLayoutId <= 0)
            throw (new IllegalArgumentException("The passed layout id is illegal"));

        mItemChildrenClickable = intent.getBooleanExtra(
                LauncherIntent.Extra.Scroll.EXTRA_ITEM_CHILDREN_CLICKABLE, false);

        mItemActionUriIndex = intent.getIntExtra(
                LauncherIntent.Extra.Scroll.EXTRA_ITEM_ACTION_VIEW_URI_INDEX, -1);

        // Generate
        generateItemMapping(intent);
    }

    /**
     * Collect arrays and put them together
     * 
     * @param t
     * @param ids
     * @param c
     * @param u
     *            uri indices; could be zero, IMPOSSIBLE_INDEX will be used
     */
    private void generateItemMapping(Intent intent) {

        // Read the mapping data
        int[] viewTypes = intent.getIntArrayExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_TYPES);
        int[] viewIds = intent.getIntArrayExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_IDS);
        int[] cursorIndices = intent
                .getIntArrayExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_CURSOR_INDICES);
        int[] defaultResources = intent
                .getIntArrayExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_DEFAULT_RESOURCES);
        boolean[] viewClickable = intent
                .getBooleanArrayExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_CLICKABLE);

        // Check
        if (viewTypes == null || viewIds == null || cursorIndices == null)
            throw (new IllegalArgumentException("A mapping component is missing"));

        if (viewTypes.length == viewIds.length && viewTypes.length == cursorIndices.length) {
        } else
            throw (new IllegalArgumentException("Mapping inconsistent"));

        // Init mapping array
        final int size = viewTypes.length;
        mItemMappings = new ItemMapping[size];
        for (int i = size - 1; i >= 0; i--)
            mItemMappings[i] = new ItemMapping(viewTypes[i], viewIds[i], cursorIndices[i]);

        // Put extra data in if they are available
        if (viewClickable != null && viewClickable.length == size)
            for (int i = size - 1; i >= 0; i--)
                mItemMappings[i].clickable = viewClickable[i];

        if (defaultResources != null && defaultResources.length == size)
            for (int i = size - 1; i >= 0; i--)
                mItemMappings[i].defaultResource = defaultResources[i];

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (mItemMappings == null)
            return;
        final int size = mItemMappings.length;

        ItemMapping itemMapping;
        View child;
        ImageView iv;
        try {
            // bind children views
            for (int i = size - 1; i >= 0; i--) {
                itemMapping = mItemMappings[i];

                child = view.findViewById(itemMapping.layoutId);

                switch (itemMapping.type) {
                case LauncherIntent.Extra.Scroll.Types.TEXTVIEW:
                    if (!(child instanceof TextView))
                        break;
                    String text = cursor.getString(itemMapping.index);
                    if (text != null)
                        ((TextView) child).setText(cursor.getString(itemMapping.index));
                    else
                        ((TextView) child).setText(itemMapping.defaultResource);
                    break;
                case LauncherIntent.Extra.Scroll.Types.IMAGEBLOB:
                    if (!(child instanceof ImageView))
                        break;
                    iv = (ImageView) child;
                    byte[] data = cursor.getBlob(itemMapping.index);
                    if (data != null)
                        iv.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    else if (itemMapping.defaultResource > 0)
                        iv.setImageResource(itemMapping.defaultResource);
                    else
                        iv.setImageDrawable(null);
                    break;
                case LauncherIntent.Extra.Scroll.Types.IMAGEURI:
                    if (!(child instanceof ImageView))
                        break;
                    iv = (ImageView) child;
                    iv.setImageURI(Uri.parse(cursor.getString(itemMapping.index)));
                    break;
                case LauncherIntent.Extra.Scroll.Types.IMAGERESOURCE:
                    if (!(child instanceof ImageView))
                        break;
                    iv = (ImageView) child;
                    int res = cursor.getInt(itemMapping.index);
                    if (res > 0)
                        iv.setImageResource(res);
                    else if (itemMapping.defaultResource > 0)
                        iv.setImageResource(itemMapping.defaultResource);
                    else
                        iv.setImageDrawable(null);
                    break;
                }

                // Prepare tag
                view.setTag(null);
                if (mItemChildrenClickable && itemMapping.clickable) {
                    child.setTag(cursor.getPosition());
                    child.setOnClickListener(new ItemViewClickListener());
                } else {
                    if (mItemActionUriIndex >= 0) {
                        view.setTag(cursor.getString(mItemActionUriIndex));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        return mInflater.inflate(mItemLayoutId, parent, false);
    }

    class ItemViewClickListener implements OnClickListener {

        public void onClick(View v) {
            try {
                int pos = (Integer) v.getTag();
                Intent intent = new Intent(LauncherIntent.Action.ACTION_VIEW_CLICK);
                intent.setComponent(mAppWidgetProvider);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.putExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, v.getId());
                intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_ID, mListViewId);
                intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS, pos);
                v.getContext().sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
