package eu.tb.zesense;

public class ZeAccelDisplayDevice extends Thread {
	
	ZePlayoutManager playoutManager;
	
	ZeMeters meter;
	
	float a = 5;
	int count = 0;
	
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
	    	if (elem.meaning == Registry.PLAYOUT_VALID) {
	    		meter.accelDataset.setValue(new Float(elem.z));
	    	}
	    	else if (elem.meaning == Registry.PLAYOUT_BUFFER_EMPTY) {
	    		count++;
	    		meter.accelUnderflowSeries.add(meter.accelUnderflowSeries.getItemCount()+1, count);
	    	}
	    	
			try {
				Thread.sleep(Registry.ACCEL_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	}
}
