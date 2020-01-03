package com.wh.gaea;

import com.wh.gaea.interfaces.IGaeaEditor;

public class GlobalInstance {
	static IGaeaEditor gaeaEditor;
	
	public static IGaeaEditor instance() {
		synchronized (GlobalInstance.class) {
			return gaeaEditor;			
		}
	}
	
	public static void setInstance(IGaeaEditor gaeaEditor) {
		synchronized (GlobalInstance.class) {
			GlobalInstance.gaeaEditor = gaeaEditor;
		}
	}
}
