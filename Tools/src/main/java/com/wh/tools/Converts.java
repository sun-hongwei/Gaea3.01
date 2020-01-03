package com.wh.tools;

import java.awt.Toolkit;
import java.math.BigDecimal;

public class Converts {
	public static float pxToMM(int px, Integer dpi) {
		if (dpi == null)
			dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		
		return pointToMM(px, dpi);
	}
	
	public static int mmToPoint(float mm) {
		return mmToPoint(mm, 72);
	}
	
	public static float pointToMM(int point) {
		return pointToMM(point, 72);
	}
	
	public static int mmToPoint(float mm, int dpi) {
		return (int)(mm * dpi * 10 / 254);
	}
	
	public static float pointToMM(int point, int dpi) {
		float mm = (float) ((float)point / dpi / 10 * 254);
		BigDecimal bg = new BigDecimal(mm);
		return bg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
	}
}
