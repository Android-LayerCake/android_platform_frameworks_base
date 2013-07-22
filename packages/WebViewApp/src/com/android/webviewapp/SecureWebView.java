/* Added by Franzi Roesner, 2013 */

package com.android.webviewapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SecureWebView extends Activity {
	
	public static String TAG = "SecureWebView";
	
	private FrameLayout contentView;
	private WebView webView;
	
	private IWebViewContainer containerInterface;
	
	private boolean saveCookies = false;
	private boolean loadLocally = false;
	
	@Override
    public void onStart() {
		super.onStart();
		
		// Get client interface
		Intent i = getIntent();
		IBinder b = i.getIBinderExtra("parentBinder");
		containerInterface = IWebViewContainer.Stub.asInterface(b);
		
		// Set up FrameLayout containing a web view
		contentView = new FrameLayout(this);
		
		webView = new WebView(this);
		webView.setFilterTouchesWhenObscured(true);
		
		contentView.addView(webView);		
		setContentView(contentView);
		
		registerOwnInterface();
	}
	
	private void registerOwnInterface() {
		// Register child (own) interface
    	IBinder ownInterface = new IRemoteWebView.Stub() {
    		
    		@Override
        	public void setJavaScriptEnabled(boolean b) throws RemoteException {
    			webView.getSettings().setJavaScriptEnabled(b);
    		}
    		
    		@Override
        	public void setJavaScriptCanOpenWindowsAutomatically(boolean b) throws RemoteException {
    			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(b);
    		}
    		
    		@Override
        	public void setVerticalScrollBarEnabled(boolean b) throws RemoteException {
    			webView.setVerticalScrollBarEnabled(b);
    		}
    		
    		@Override
        	public void loadUrl(String url) throws RemoteException {
    			webView.loadUrl(url);
    		}
    		
    		@Override
        	public void loadDataWithBaseURL(String baseUrl, String data,
    	            String mimeType, String encoding, String historyUrl) throws RemoteException {
    			webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    		}
    		
    		@Override
    		public void shouldSaveCookies(boolean b) {
    			saveCookies = b;
    			updateWebViewClient();
    		}
    		
    		@Override
    		public void shouldLoadLinksInWebView(boolean b) {
    			loadLocally = b;
    			updateWebViewClient();
    		}
        };
        try {
        	containerInterface.registerChildInterface(ownInterface);
        } catch (RemoteException e) {
        	Log.e(TAG, "Error trying to register child interface: " + e.getMessage());
        }
	}
	
	private void updateWebViewClient() {
		if (saveCookies && loadLocally) {
			webView.setWebViewClient(new WebViewClient() {
				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        view.loadUrl(url);
			        return true;
			    }
				@Override
				public void onPageFinished(WebView view, String url) {
			        CookieSyncManager.getInstance().sync();
			    }
			});
		} else if (saveCookies && !loadLocally) {
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
			        CookieSyncManager.getInstance().sync();
			    }
			});
		} else if (!saveCookies && loadLocally) {
			webView.setWebViewClient(new WebViewClient() {
				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        view.loadUrl(url);
			        return true;
			    }
			});
		}
	}
	
	

}
