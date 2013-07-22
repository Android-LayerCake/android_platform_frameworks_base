package com.android.systemui.acg;

import android.os.IBinder;

interface IIntentAcgContainer {

	/* Register the intent ACG's interface so client can call back. */
	void registerChildInterface(IBinder childInterface);

}
