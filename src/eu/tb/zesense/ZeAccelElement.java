package eu.tb.zesense;

public class ZeAccelElement extends ZeSensorElement implements Cloneable {
	
	float x;
	float y;
	float z;
	
	@Override
	public Object clone() {
		try {
		return super.clone();
		} catch(Exception e) {
			return null;
		}
	}
}
