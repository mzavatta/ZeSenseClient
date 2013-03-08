package eu.tb.zesense;

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
			meter.proxBufferSeries.add(meter.proxBufferSeries.getItemCount()+1,
					playoutManager.size());
			
			if (elem != null) { //buffer found empty

				if (elem.meaning == Registry.PLAYOUT_VALID) {
					meter.proxDataset.setValue(new Float(elem.element.distance));
				}
				else if (elem.meaning == Registry.PLAYOUT_INVALID) {
					meter.proxDataset.setValue(new Float(0));
				}
			}
			else {
				meter.proxDataset.setValue(new Float(0)); //invalid and buffer underflow result
				//in the same effect for the user but streamwise they are not the same
				//thing
				//meter.accelUnderflowSeries.add(meter.accelUnderflowSeries.getItemCount()+1, count);
			}
	    	
			try {
				Thread.sleep(Registry.PROX_PLAYOUT_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    System.out.println("Underflow count prox = "+playoutManager.underflowCount);
	}
}
