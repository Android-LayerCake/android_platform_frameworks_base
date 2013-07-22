package com.android.systemui.acg;

import android.content.Intent;
import android.graphics.Bitmap;

interface IRemoteIntentAcg {

	/* Set the intent ACG's bitmap */
    void setBitmap(in Bitmap b);

	/* Set the intent ACG's onclick intent */
	void setIntent(in Intent i);
}
