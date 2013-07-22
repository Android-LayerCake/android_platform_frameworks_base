package com.android.systemui.acg;

import android.location.Location;
import android.os.IBinder;

interface ILocationAcgContainer {

	/* Receive callback with location information. */
	void locationAvailable(in Location location);
	
	/* Register the location ACG's interface so client can call back. */
	void registerChildInterface(IBinder childInterface);

}
