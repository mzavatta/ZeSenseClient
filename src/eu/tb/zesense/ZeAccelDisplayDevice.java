package eu.tb.zesense;

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
		
	    while (ZeSenseClient.loop) {
	   
	    	ZeAccelElement elem = (ZeAccelElement) playoutManager.get();
			meter.accelBufferSeries.add(meter.accelBufferSeries.getItemCount()+1,
					playoutManager.size());
	    	if (elem != null) { //buffer found empty

		    	if (elem.meaning == Registry.PLAYOUT_VALID) {
		    		meter.accelDataset.setValue(new Float(elem.z));
		    	}
		    	else if (elem.meaning == Registry.PLAYOUT_INVALID) {
		    		meter.accelDataset.setValue(new Float(0));
		    	}
	    	}
	    	else {
	    		meter.accelDataset.setValue(new Float(0)); //invalid and buffer underflow result
	    		//in the same effect for the user but streamwise they are not the same
	    		//thing
	    		//meter.accelUnderflowSeries.add(meter.accelUnderflowSeries.getItemCount()+1, count);
	    	}
	    	
			try {
				Thread.sleep(Registry.ACCEL_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    System.out.println("Underflow count = "+playoutManager.underflowCount);
	}
}
