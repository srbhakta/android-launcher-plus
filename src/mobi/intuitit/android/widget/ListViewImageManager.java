package mobi.intuitit.android.widget;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class ListViewImageManager {

	private static final String TAG = "ListViewImageManager";

	private static final boolean LOGD = false;

	private final HashMap<String, SoftReference<Drawable>> mCacheForImageByUri = new HashMap<String, SoftReference<Drawable>>();

	private final HashMap<Integer, SoftReference<Bitmap>> mCacheForImageById = new HashMap<Integer, SoftReference<Bitmap>>();

	public Drawable getImageFromUri(Context mContext, String imgUri) {
		// Bitmap bmp = null;
		Drawable d = null;
		if (mCacheForImageByUri.containsKey(imgUri) && mCacheForImageByUri.get(imgUri) != null) {
			SoftReference<Drawable> ref = mCacheForImageByUri.get(imgUri);
			if (ref != null) {
				d = ref.get();
			}
		}

		if (LOGD)
			if (d != null)
				Log.d(TAG, "image URI restored (width = " + d.getMinimumWidth() + " / weight = " + d.getMinimumHeight()
						+ ")");

		if (d == null) {
			// bmp = BitmapFactory.decodeFile(imgUri);
			Uri mUri = Uri.parse(imgUri);
			String scheme = mUri.getScheme();
			if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
				// try {
				// // Load drawable through Resources, to get the source density
				// information
				// ContentResolver.OpenResourceIdResult r =
				// mContext.getContentResolver().getResourceId(mUri);
				// d = r.r.getDrawable(r.id);
				// } catch (Exception e) {
				// Log.w("ImageView", "Unable to open content: " + mUri, e);
				// }
				Log.w("ImageView", "Unable to open content: " + mUri);
			} else if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
				try {
					d = Drawable.createFromStream(mContext.getContentResolver().openInputStream(mUri), null);
				} catch (Exception e) {
					Log.w("ImageView", "Unable to open content: " + mUri, e);
				}
			} else {
				d = Drawable.createFromPath(mUri.toString());
			}

			if (LOGD)
				Log.d(TAG, "image URI decoded (width = " + d.getMinimumWidth() + " / weight = " + d.getMinimumHeight()
						+ ")");
			mCacheForImageByUri.put(imgUri, new SoftReference<Drawable>(d));
		}
		return d;
	}

	public Bitmap getImageFromId(Context ctx, int imgId) {
		Bitmap bmp = null;
		if (mCacheForImageById.containsKey(imgId) && mCacheForImageById.get(imgId) != null) {
			SoftReference<Bitmap> ref = mCacheForImageById.get(imgId);
			if (ref != null) {
				bmp = ref.get();
			} else {
				Log.d(TAG, "image ID missing !!!!!!!!!!");
			}
		}

		if (LOGD)
			if (bmp != null)
				Log.d(TAG, "image ID restored");

		if (bmp == null) {
			Log.d(TAG, "image ID decoded");
			bmp = BitmapFactory.decodeResource(ctx.getResources(), imgId);
			mCacheForImageById.put(imgId, new SoftReference<Bitmap>(bmp));
		}
		return bmp;
	}

}
