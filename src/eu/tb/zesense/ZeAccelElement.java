package eu.tb.zesense;

public class ZeAccelElement extends ZeSensorElement {
	
	float x;
	float y;
	float z;
	
	/* 0 if normal value, 1 if "hold previous", 2 if "invalid". */
	int meaning;
	
}
