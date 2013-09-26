package eu.tb.zesense;

public class Registry {
	
	// resource URI path used for discovery
	public static final String DISCOVERY_RESOURCE = "/.well-known/core";

	// indices of command line parameters
	public static final int IDX_METHOD          = 0;
	public static final int IDX_URI             = 1;
	public static final int IDX_PAYLOAD         = 2;

	// exit codes for runtime errors
	public static final int ERR_MISSING_METHOD  = 1;
	public static final int ERR_UNKNOWN_METHOD  = 2;
	public static final int ERR_MISSING_URI     = 3;
	public static final int ERR_BAD_URI         = 4;
	public static final int ERR_REQUEST_FAILED  = 5;
	public static final int ERR_RESPONSE_FAILED = 6;
	public static final int ERR_BAD_LINK_FORMAT = 7;
	
	public static final int DATAPOINT	 = 1;
	public static final int SENDREPORT = 2;
	public static final int RECREPORT	 = 3;
	
	
	public static final int BLIND_DELAY = /* 225000000 */ 0;

	
	/* <source>/hardware/libhardware/include/hardware/sensors.h */
	public static final int SENSOR_TYPE_ACCELEROMETER 		= 1;
	public static final int SENSOR_TYPE_MAGNETIC_FIELD 		= 2;
	public static final int SENSOR_TYPE_ORIENTATION			= 3;
	public static final int SENSOR_TYPE_GYROSCOPE			= 4;
	public static final int SENSOR_TYPE_LIGHT				= 5;
	public static final int SENSOR_TYPE_PRESSURE				= 6;
	public static final int SENSOR_TYPE_TEMPERATURE			= 7;  //deprecated
	public static final int SENSOR_TYPE_PROXIMITY			= 8;
	public static final int SENSOR_TYPE_GRAVITY				= 9;
	public static final int SENSOR_TYPE_LINEAR_ACCELERATION	= 10;
	public static final int SENSOR_TYPE_ROTATION_VECTOR		= 11;
	public static final int SENSOR_TYPE_RELATIVE_HUMIDITY	= 12;
	public static final int SENSOR_TYPE_AMBIENT_TEMPERATURE	= 13;
	// Definitions for our custom types
	public static final int ZESENSE_SENSOR_TYPE_LOCATION		= 14;
	
	public static final int RR_BANDWIDTH_THRESHOLD = 1000; //bytes
	public static final int PAYLOAD_HDR_LENGTH = 4; //bytes
	public static final int CNAME_LENGTH = 12;
	public static final String CNAME = "host@zesense";
	public static final int PAYLOAD_RR_LENGTH = PAYLOAD_HDR_LENGTH+20+CNAME_LENGTH; //bytes, whole payload
	public static final int RTPTS_LENGTH = 4;
	
	public static final int ACCEL_SAMPLE_LENGTH = 60;
	public static final int ORIENT_SAMPLE_LENGTH = 60;
	public static final int GYRO_SAMPLE_LENGTH = 60;
	public static final int PROX_SAMPLE_LENGTH = 20;
	public static final int LIGHT_SAMPLE_LENGTH = 20;
			
	/* Modeled as agreed in advance e.g. RTP A/V profile. */
	public static final int WALLCLOCK_FREQ = 1000000000; //Hz
	public static final int TIMESTAMP_FREQ = 1000; //Hz
	
	/* Debug value, in a good implementation this value is decided
	 * request by request and communicated to the server as a resource
	 * query string or something similar.
	 */
	public static final int ACCEL_STREAM_FREQ = 10;
	public static final int ACCEL_PLAYOUT_FREQ = 20; //HZ
	public static final int ACCEL_PLAYOUT_PERIOD = 50; //msec
	public static final int ACCEL_PLAYOUT_HALF_PERIOD = 25; //msec
	
	public static final int PROX_STREAM_FREQ = 10;
	public static final int PROX_PLAYOUT_FREQ = 20; //HZ
	public static final int PROX_PLAYOUT_PERIOD = 50; //msec
	public static final int PROX_PLAYOUT_HALF_PERIOD = 25; //msec
	
	public static final int LIGHT_STREAM_FREQ = 10;
	public static final int LIGHT_PLAYOUT_FREQ = 20; //HZ
	public static final int LIGHT_PLAYOUT_PERIOD = 50; //msec
	public static final int LIGHT_PLAYOUT_HALF_PERIOD = 25; //msec
	
	public static final int ORIENT_STREAM_FREQ = 10;
	public static final int ORIENT_PLAYOUT_FREQ = 20; //HZ
	public static final int ORIENT_PLAYOUT_PERIOD = 50; //msec
	public static final int ORIENT_PLAYOUT_HALF_PERIOD = 25; //msec
	
	public static final int GYRO_STREAM_FREQ = 10;
	public static final int GYRO_PLAYOUT_FREQ = 20; //HZ
	public static final int GYRO_PLAYOUT_PERIOD = 50; //msec
	public static final int GYRO_PLAYOUT_HALF_PERIOD = 25; //msec
	
	public static final int PLAYOUT_HOLD = 1;
	public static final int PLAYOUT_VALID = 2;
	public static final int PLAYOUT_INVALID = 3;
	public static final int PLAYOUT_OVERFLOW = 4;
	public static final int PLAYOUT_NOTSTARTED = 4;
	
	public static final String HOST = "coap://192.168.43.1:5683";
	public static final int LOCAL_PORT = 48225;
	public static final int LOCAL_TEST_PORT = 48226;
	public static final String ACCEL_RESOURCE_PATH = "/accel";
	public static final String PROX_RESOURCE_PATH = "/proximity";
	public static final String LIGHT_RESOURCE_PATH = "/light";
	public static final String ORIENT_RESOURCE_PATH = "/orientation";
	public static final String GYRO_RESOURCE_PATH = "/gyroscope";
	
	public static final int REDRAW_THRESHOLD = 500;

}
