package eu.tb.zesense;

import java.awt.Color;

public class ZeAccelDisplayDevice extends Thread {
	
	ZePlayoutManager<ZeAccelElement> playoutManager;
	
	ZeMeters meter;
	
	ZeAccelElement elem;
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    while (ZeSenseClient.loop) {
	    	
	    	ZePlayoutElement<ZeAccelElement> elem = playoutManager.get();
	    	
	    	if (meter.accelBufferSeries.getItemCount() > Registry.REDRAW_THRESHOLD) {
				meter.accelBufferSeries.clear();
	    	}
			meter.accelBufferSeries.add(meter.accelBufferSeries.getItemCount()+1,
					playoutManager.size());

			if (elem.meaning == Registry.PLAYOUT_VALID) {
				meter.accelDataset.setValue(new Float(elem.element.z));
				if (meter.accelPlot.getNeedlePaint() == Color.GRAY)
					meter.accelPlot.setNeedlePaint(Color.GREEN);
			}
			else if (elem.meaning == Registry.PLAYOUT_INVALID) {
				meter.accelPlot.setNeedlePaint(Color.GRAY);
			}
			else {
				//playout hold or playout not started, don't change what's being displayed
			}
	    	
			try {
				Thread.sleep(Registry.ACCEL_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    //System.out.println("Underflow count accel = "+playoutManager.underflowCount);
	}
}
