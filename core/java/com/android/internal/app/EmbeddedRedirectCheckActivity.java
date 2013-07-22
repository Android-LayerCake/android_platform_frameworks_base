/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.app;

import com.android.internal.R;
import com.android.internal.content.PackageMonitor;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This activity is displayed when an embedded Activity sends an Intent (and did not
 * use an IntentSenderAcg. It shows a dialog to the user to confirm whether the
 * embedded Activity to be able to open another Activity full-screen.
 * It is not normally used directly by application developers.
 */
public class EmbeddedRedirectCheckActivity extends AlertActivity {
    private static final String TAG = "EmbeddedRedirectCheckActivity";

    private Intent mRealIntent;
    private ComponentName mParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	setTheme(R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        super.onCreate(savedInstanceState);
        
        setupMetadata();

        AlertController.AlertParams ap = mAlertParams;
        ap.mTitle = getResources().getText(com.android.internal.R.string.embeddedIntentAlert);
        
        ap.mView = getLayoutInflater().inflate(R.layout.embedded_alert, null);

        setupAlert();
        
        setupUI((FrameLayout)ap.mView.findViewById(com.android.internal.R.id.embedded_alert_info));
    }
    
    private void setupMetadata() {
        Intent intent = new Intent(getIntent());
        // The resolver activity is set to be hidden from recent tasks.
        // we don't want this attribute to be propagated to the next activity
        // being launched.  Note that if the original Intent also had this
        // flag set, we are now losing it.  That should be a very rare case
        // and we can live with this.
        intent.setFlags(intent.getFlags()&~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        
        // Remove this Activity's component
        intent.setComponent(null);
        
        // If a component was specified, use that.
        String packageName = intent.getStringExtra("EmbeddedRedirectCheckActivity_PackageName");
        String className = intent.getStringExtra("EmbeddedRedirectCheckActivity_ClassName");
        if (packageName != null && className != null) {
        	intent.setComponent(new ComponentName(packageName, className));
        	intent.removeExtra("EmbeddedRedirectCheckActivity_PackageName");
        	intent.removeExtra("EmbeddedRedirectCheckActivity_ClassName");
        }
        
        // Set up parent info
        String pPackageName = intent.getStringExtra("EmbeddedRedirectCheckActivity_ParentPackageName");
        String pClassName = intent.getStringExtra("EmbeddedRedirectCheckActivity_ParentClassName");
        if (pPackageName != null && pClassName != null) {
        	mParent = new ComponentName(pPackageName, pClassName);
        	intent.removeExtra("EmbeddedRedirectCheckActivity_ParentPackageName");
        	intent.removeExtra("EmbeddedRedirectCheckActivity_ParentClassName");
        }
        
        mRealIntent = intent;
    }
    
    private void setupUI(FrameLayout frame) {    	
    	final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int iconDpi = am.getLauncherLargeIconDensity();
        int iconSize = am.getLauncherLargeIconSize();
        
        final PackageManager pm = getPackageManager();
        
        CharSequence displayLabel;
        Drawable iconDrawable;
        
        // Get info for that Activity
        try {
        	ActivityInfo aInfo = pm.getActivityInfo(mParent, 0);
        	int icon = aInfo.icon;
        	if (icon == 0) {
        		icon = aInfo.applicationInfo.icon;
        	}
        	Resources res = pm.getResourcesForApplication(aInfo.packageName);
            iconDrawable = res.getDrawableForDensity(icon, iconDpi);
            displayLabel = aInfo.loadLabel(pm);
        } catch (NameNotFoundException e) {
        	// Well this is crap
        	return;
        }      
        
        View view = getLayoutInflater().inflate(
                com.android.internal.R.layout.resolve_list_item, frame, true);

        // Fix the icon size even if we have different sized resources
        ImageView iconView = (ImageView)view.findViewById(R.id.icon);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) iconView.getLayoutParams();
        lp.width = lp.height = iconSize;
        
        TextView text = (TextView)view.findViewById(com.android.internal.R.id.text1);
        TextView text2 = (TextView)view.findViewById(com.android.internal.R.id.text2);
        text.setText(displayLabel);
        text2.setText("This embedded application wants to open something full-screen.");
        iconView.setImageDrawable(iconDrawable);
    }
    
    public void onYesClick(View v) {
    	startActivity(mRealIntent);
    	finish();
    }
    
    public void onNoClick(View v) {
    	finish();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if ((getIntent().getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourself since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }
}
