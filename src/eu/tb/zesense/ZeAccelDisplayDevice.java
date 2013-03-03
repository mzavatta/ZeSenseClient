package eu.tb.zesense;

import org.jfree.ui.RefineryUtilities;

public class ZeAccelDisplayDevice extends Thread {
	
	ZePlayoutManager playoutManager;
	
	ZeMeters meter;
	
	float a = 5;
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    while (true) {
	   
	    	ZeAccelElement elem = (ZeAccelElement) playoutManager.get();
	    	meter.accelDataset.setValue(new Float(elem.z));
	    	
			try {
				Thread.sleep(Registry.ACCEL_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	}
}
