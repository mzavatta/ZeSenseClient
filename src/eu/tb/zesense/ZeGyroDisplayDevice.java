package eu.tb.zesense;

import java.awt.Color;

public class ZeGyroDisplayDevice extends Thread {

	ZePlayoutManager<ZeGyroElement> playoutManager;
	
	ZeMeters meter;
	
	ZeGyroElement elem;

	@Override
	public void run() {
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    while (ZeSenseClient.loop) {
	    	
	    	ZePlayoutElement<ZeGyroElement> elem = playoutManager.get();
	    	
	    	if (meter.gyroBufferSeries.getItemCount() > Registry.REDRAW_THRESHOLD) {
				meter.gyroBufferSeries.clear();
	    	}
			meter.gyroBufferSeries.add(meter.gyroBufferSeries.getItemCount()+1,
					playoutManager.size());
			
			if (elem.meaning == Registry.PLAYOUT_VALID) {
				meter.gyroDataset.setValue(new Float(elem.element.x));
				if (meter.gyroPlot.getNeedlePaint() == Color.GRAY)
					meter.gyroPlot.setNeedlePaint(Color.GREEN);
			}
			else if (elem.meaning == Registry.PLAYOUT_INVALID) {
				meter.gyroPlot.setNeedlePaint(Color.GRAY);
			}
			else {
				//playout hold, don't change what's being displayed
			}
				
	    	
			try {
				Thread.sleep(Registry.GYRO_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    //System.out.println("Underflow count gyro = "+playoutManager.underflowCount);
	}
	
}
