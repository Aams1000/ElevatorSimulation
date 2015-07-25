
public class ScheduledStop {
	
	//request and it's proximity score
	private Request request;
	private double proximityScore;
	
	//constructor takes request and proximityScore
	public ScheduledStop(Request request, double proximityScore){
		this.request = request;
		this.proximityScore = proximityScore;
	}
	
	//getters
	public Request getRequest(){
		return request;
	}
	public double getProximityScore(){
		return proximityScore;
	}

}
