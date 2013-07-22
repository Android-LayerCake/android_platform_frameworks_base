// File added by Franzi Roesner (2012)

package android.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View.OnClickListener;

/**
 * A special type of view that can be used to embed a view hierarchy defined by 
 * another app (and running in another process).
 * 
 * Basically, the EmbeddedView contains a new ViewRoot.
 *
 */
public class EmbeddedActivityView extends View {
	
	private static final boolean DEBUG = true;

    /**
     * The logging tag used by this class with android.util.Log.
     */
    protected static final String LOG_TAG = "EmbeddedActivityView";
	
	// Specification of the third-party Activity embedded inside this view
	private String mPackageName;
	private String mActivityName;
	private int mGivenWidth = 0;
	private int mGivenHeight = 0;
	private int mGivenX = 0;
	private int mGivenY =0;
	
	private boolean windowCreated = false;
	private boolean layoutChanged = false;
	
	private IBinder parentBinder;
	
	private int id; // use to identify it to AM
	
	public EmbeddedActivityView(Context context) {
        super(context);
    }
    
    public EmbeddedActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmbeddedActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.EmbeddedActivityView,
                defStyle, 0);
        
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case com.android.internal.R.styleable.EmbeddedActivityView_packageName:
                    mPackageName = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.EmbeddedActivityView_activityName:
                    mActivityName = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.EmbeddedActivityView_setupBinder:
                	// Set up the binder by calling the provided method
                	String setupBinderMethodName = a.getString(attr);
                	Method setupBinderMethod;
                	try {
                		setupBinderMethod = getContext().getClass()
                				.getMethod(setupBinderMethodName, View.class);
                	} catch (NoSuchMethodException e) {
                		int id = getId();
                        String idText = id == NO_ID ? "" : " with id '"
                                + getContext().getResources().getResourceEntryName(
                                    id) + "'";
                        throw new IllegalStateException("Could not find a method " +
                                setupBinderMethodName + "(View) in the activity "
                                + getContext().getClass() + " for setupBinder handler"
                                + " on view " + this.getClass() + idText, e);
                	}
                	try {
                        parentBinder = (IBinder)setupBinderMethod.invoke(getContext(), this);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Could not execute non "
                                + "public method of the activity", e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException("Could not execute "
                                + "method of the activity", e);
                    }
                    break;
            }
        }
        a.recycle();    	
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	if (!windowCreated) {
    		initWindow();
    	} else {
    		maybeRefreshWindow();
    	}
    }
    
    @Override
    protected void onDetachedFromWindow() {
    	super.onDetachedFromWindow();
    	((Activity)getContext()).removeEmbeddedActivity(id);
    	windowCreated = false;
    }
    
    private void initWindow() {
    	if (mPackageName == null || mActivityName == null) {
    		throw new IllegalStateException("packageName and activityName attributes required for EmbeddedActivityViews");
    	}
    	if (parentBinder == null) {
    		throw new IllegalStateException("parentBinder is null in EmbeddedActivityView");
    	}
    	
    	if (DEBUG) {
    		Log.i(LOG_TAG, "mPackageName: " + mPackageName + ", mActivityName: " + mActivityName);
    	}
    	
    	id = this.getId();
    	if (id == NO_ID) {
    		throw new IllegalStateException("EmbeddedActivityView must have specified id");
    	}
    	
    	Bundle layoutParams = computeLayoutParams();
    	
    	// Start specified Activity with this as a parent (using Franzi's new API).
    	// The ActivityManager+WindowManager will take care of "embedding".
    	Intent intent = new Intent();
    	intent.putExtra("parentBinder", parentBinder);
    	intent.setComponent(new ComponentName(mPackageName, mActivityName));
    	((Activity)getContext()).startEmbeddedActivity(intent, layoutParams, id);
        
    	windowCreated = true;
    }
    
    private void maybeRefreshWindow() {
    	Bundle layoutParams = computeLayoutParams(); // will set layoutChanged
    	
    	if (layoutChanged) {    	
	    	// Ask ActivityManager to refresh the embedded Activity
			// (it will in turn ask WindowManager to refresh the window with new params)
	    	((Activity)getContext()).refreshEmbeddedActivity(id, layoutParams);		
	    	
	    	layoutChanged = false;
    	}
    }
    
    private Bundle computeLayoutParams() {
    	int newWidth, newHeight, newX, newY;
    	
    	// Setup layout params
    	newWidth = getWidth();
        newHeight = getHeight();       
        
        if (DEBUG) {
        	Log.i(LOG_TAG, "width: " + mGivenWidth + ", height: " + mGivenHeight);
        }
    	
    	int[] loc = new int[2]; 
    	getLocationOnScreen(loc);
    	newX = loc[0];
    	newY = loc[1];
    	
    	if (newWidth != mGivenWidth ||
    		newHeight != mGivenHeight ||
    		newX != mGivenX ||
    		newY != mGivenY) {
    		layoutChanged = true;
    	}
    	
    	mGivenWidth = newWidth;
    	mGivenHeight = newHeight;
    	mGivenX = newX;
    	mGivenY = newY;
    	
    	if (DEBUG) {
        	Log.i(LOG_TAG, "X: " + mGivenX + ", Y: " + mGivenY);
        }
    	
    	Bundle layoutParams = new Bundle();
    	layoutParams.putInt("x", mGivenX);
    	layoutParams.putInt("y", mGivenY);
    	layoutParams.putInt("width", mGivenWidth);
    	layoutParams.putInt("height", mGivenHeight);
    	
    	return layoutParams;    	
    }
}