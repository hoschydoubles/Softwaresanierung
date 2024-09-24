package robot;

import lejos.remote.ev3.RemoteRequestSampleProvider;

public class EasyRequestSampleProvider {
	
	private RemoteRequestSampleProvider internalSampleProvider;
	
	public EasyRequestSampleProvider(RemoteRequestSampleProvider remoteRequestSampleProvider) {
		this.internalSampleProvider = remoteRequestSampleProvider;
	}
	
	public float getSample() {
		float[] answers = new float[1];
		internalSampleProvider.fetchSample(answers, 0);
		return answers[0];
	}
	
	public void close() {
		internalSampleProvider.close();
	}
	
	public RemoteRequestSampleProvider internalRRSP() {
		return internalSampleProvider;
	}
}
