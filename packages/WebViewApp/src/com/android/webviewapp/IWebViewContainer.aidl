package com.android.webviewapp;

import android.os.IBinder;

interface IWebViewContainer {

	/* Register the secure web view's interface so client can call back. */
	void registerChildInterface(IBinder childInterface);

}
