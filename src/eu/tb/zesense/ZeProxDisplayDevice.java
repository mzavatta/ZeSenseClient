package eu.tb.zesense;

import java.awt.Color;

public class ZeProxDisplayDevice extends Thread {

	ZePlayoutManager<ZeProxElement> playoutManager;
	
	ZeMeters meter;
	
	ZeProxElement elem;
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    while (ZeSenseClient.loop) {
	   
	    	ZePlayoutElement<ZeProxElement> elem = playoutManager.get();
	    	
	    	if (meter.proxBufferSeries.getItemCount() > Registry.REDRAW_THRESHOLD) {
				meter.proxBufferSeries.clear();
	    	}
			meter.proxBufferSeries.add(meter.proxBufferSeries.getItemCount()+1,
					playoutManager.size());
			
			if (elem.meaning == Registry.PLAYOUT_VALID) {
				meter.proxDataset.setValue(new Float(elem.element.distance));
				if (meter.proxPlot.getNeedlePaint() == Color.GRAY)
					meter.proxPlot.setNeedlePaint(Color.GREEN);
			}
			else if (elem.meaning == Registry.PLAYOUT_INVALID) {
				meter.proxPlot.setNeedlePaint(Color.GRAY);
			}
			else {
				//playout hold, don't change what's being displayed
			}
				
			try {
				Thread.sleep(Registry.PROX_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    //System.out.println("Underflow count prox = "+playoutManager.underflowCount);
	}
}
