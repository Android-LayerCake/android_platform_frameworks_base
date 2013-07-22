/* Added by Franzi Roesner, 2013 */

package com.android.systemui.acg;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class IntentAcg extends Activity {
	
	public static String TAG = "IntentAcg";
	
	private ImageView imageView;
	private Intent onClickIntent;
	private IIntentAcgContainer containerInterface;
	
	@Override
    public void onStart() {
		super.onStart();
		
		// Get client interface
		Intent i = getIntent();
		IBinder b = i.getIBinderExtra("parentBinder");
		containerInterface = IIntentAcgContainer.Stub.asInterface(b);
		
		
		// Set up FrameLayout with ImageView (client will set arbitrary Bitmap inside of it)
		FrameLayout contentView = new FrameLayout(this);
		imageView = new ImageView(this);	
		contentView.addView(imageView);
		
		setContentView(contentView);
		
		// Register child (own) interface
    	IBinder ownInterface = new IRemoteIntentAcg.Stub() {
        	@Override
        	public void setBitmap(Bitmap b) throws RemoteException {
        		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) imageView.getLayoutParams();
        		lp.height = b.getHeight();
        		lp.width = b.getWidth();
        		imageView.setImageBitmap(b);
        		
        		imageView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(onClickIntent != null) {
							startActivity(onClickIntent);
						}
					}
        		});
        	}
        	
        	@Override
        	public void setIntent(Intent i) throws RemoteException {
        		onClickIntent = i;
        	}
        };
        try {
        	containerInterface.registerChildInterface(ownInterface);
        } catch (RemoteException e) {
        	Log.e(TAG, "Error trying to register child interface: " + e.getMessage());
        }

	}

}
