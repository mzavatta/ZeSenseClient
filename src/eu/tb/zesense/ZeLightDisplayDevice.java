package eu.tb.zesense;

import java.awt.Color;

public class ZeLightDisplayDevice extends Thread {

	ZePlayoutManager<ZeLightElement> playoutManager;
	
	ZeMeters meter;
	
	float a = 5;
	
	ZeLightElement elem;
	

	@Override
	public void run() {
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    while (ZeSenseClient.loop) {
	    	
	    	ZePlayoutElement<ZeLightElement> elem = playoutManager.get();
	    	
	    	if (meter.lightBufferSeries.getItemCount() > Registry.REDRAW_THRESHOLD) {
				meter.lightBufferSeries.clear();
	    	}
			meter.lightBufferSeries.add(meter.lightBufferSeries.getItemCount()+1,
					playoutManager.size());
			

			if (elem.meaning == Registry.PLAYOUT_VALID) {
				meter.lightDataset.setValue(new Float(elem.element.light));
				if (meter.lightPlot.getNeedlePaint() == Color.GRAY)
					meter.lightPlot.setNeedlePaint(Color.GREEN);
			}
			else if (elem.meaning == Registry.PLAYOUT_INVALID) {
				//meter.lightDataset.setValue(new Float(0));
				meter.lightPlot.setNeedlePaint(Color.GRAY);
			}
			// else playout hold, don't change what's being displayeds
			else {
				//
			}
			
			//meter.lightDataset.setValue(new Float(0)); //invalid and buffer underflow result
			//in the same effect for the user but streamwise they are not the same
			//thing
			//meter.accelUnderflowSeries.add(meter.accelUnderflowSeries.getItemCount()+1, count);
			
			/*
	    	int ret = playoutManager.get(elem);
			meter.accelBufferSeries.add(meter.accelBufferSeries.getItemCount()+1,
					playoutManager.size());
		    if (ret == Registry.PLAYOUT_VALID) {
		    		meter.accelDataset.setValue(new Float(elem.z));
		    }
		    else if (ret == Registry.PLAYOUT_INVALID) {
		    		meter.accelDataset.setValue(new Float(0));
		    }*/
		    /*
	    	else {
	    		meter.accelDataset.setValue(new Float(0)); //invalid and buffer underflow result
	    		//in the same effect for the user but streamwise they are not the same
	    		//thing
	    		//meter.accelUnderflowSeries.add(meter.accelUnderflowSeries.getItemCount()+1, count);
	    	}*/
	    	
			try {
				Thread.sleep(Registry.LIGHT_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    //System.out.println("Underflow count light = "+playoutManager.underflowCount);
	}
}
