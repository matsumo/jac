/**
 * "Just once" auto clicker. Copyright (C) 2012 matsumo All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.matsumo.jac;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class GetWindowStateService extends AccessibilityService {
    private static final String LOG_TAG = "GetWindowStateService";

    private BroadcastReceiver mReceiver;
	private boolean screenOn = true;
	private String buttonText;

    @Override
    public void onCreate() {
        super.onCreate();
        buttonText = getString(R.string.button_name);
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
    }
 
    @Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
    public void onServiceConnected() {
		super.onServiceConnected();
    }
 
	@Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
		if(!screenOn) return;		//ignore when screen is off.
        int eventType = event.getEventType();
        switch (eventType) {
        case AccessibilityEvent.TYPE_VIEW_CLICKED :
//            String pkg = event.getPackageName().toString();
//            Log.d(LOG_TAG, "time:"+event.getEventTime());
//            Log.d(LOG_TAG, "pkg:"+pkg);
//            Log.d(LOG_TAG, "cls:"+event.getClassName());
//            Log.d(LOG_TAG, "text:"+event.getText());
//	          Log.d(LOG_TAG, "desc:"+event.getContentDescription());
//	          Log.d(LOG_TAG, "before:"+event.getBeforeText());

            AccessibilityNodeInfo info = AccessibilityEvent.obtain(event).getSource();
            if(info != null){
            	if(info.getText() == null){
//    	  	        Log.d(LOG_TAG, "caption:"+info.getText());
	            	while(info.getParent() != null) info = info.getParent();	//move to root view
	                findAndClickButton(info, "", false);
	                recycleAllInfo(info);
            	}
            }
            break;
        }
    }

	@Override
	public void onInterrupt() {
	}
	
	private boolean findAndClickButton(AccessibilityNodeInfo info, String pad, boolean found){
		if(info == null){
//            Log.d(LOG_TAG, pad+"node not found!");
			return found;
		}
		for(int i=0; i<info.getChildCount(); i++){
			AccessibilityNodeInfo child = info.getChild(i);
			if(child == null) continue;
//			Rect rc = new Rect();
//    		child.getBoundsInScreen(rc);
    		String cls = child.getClassName().toString();
//            Log.d(LOG_TAG, pad+"cls:"+cls+",txt:"+child.getText());
    		if(cls.compareTo("android.widget.Button") == 0 && child.getText().toString().compareTo(buttonText) == 0 && child.isEnabled()){
//                Log.d(LOG_TAG, "FOUND BUTTON!!");
    			child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    			return true;
    		}
            if(child.getChildCount() > 0) found = findAndClickButton(child, pad+" ", found);
		}
		return found;
	}

	private void recycleAllInfo(AccessibilityNodeInfo info){
		if(info == null){
			return;
		}
		for(int i=0; i<info.getChildCount(); i++){
			AccessibilityNodeInfo child = info.getChild(i);
			if(child == null) continue;
			recycleAllInfo(child);
			child.recycle();
		}
	}
	
	private class ScreenReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	    		screenOn = false;
//	    		Log.d(LOG_TAG, "ACTION_SCREEN_OFF");
	        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
	    		screenOn = true;
//	    		Log.d(LOG_TAG, "ACTION_SCREEN_ON");
	        }
	    }
	}
}
