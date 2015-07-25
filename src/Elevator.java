import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Elevator {

	//storing history of requests
	ArrayList<Request> requestLog = new ArrayList<Request>();
	
	//the prioirty queue wants an initial capacity. let's give it one
	private final int INITIAL_CAPACITY = 15;
	
	//possible directions
	private final int UP = 0;
	private final int DOWN = 1;
	private final int STATIONARY = 2;
	
	//weights for determining proximity score of a request. Proximity score is used to determine
	//which requests have highest priority (lowest score)
	private final double DISTANCE_WEIGHT = 1.25;
	private final double CHANGE_DIRECTION_TO_GET_THERE_WEIGHT = 2.5; //people hate changing directions mid trip
	private final double CHANGE_DIRECTION_AFTERWARD_WEIGHT = 2.0;	
	private final double DESTINATION_WEIGHT = 0.35; 				//weight for the second leg of a pickup and drop off request. Used in ControlPanel
	
	//proximity score adjustment for making sure destination floors are not visited before picking up the passenger
	private final double SCORE_ADJUSTMENT = 0.00005;
	
	//elevator status
	private ElevatorStatus status;
	private boolean active = false;

	//list of destinations
	PriorityBlockingQueue<ScheduledStop> destinations = new PriorityBlockingQueue<ScheduledStop>(INITIAL_CAPACITY, new ScheduledStopComparator());
	
	//constructor takes initial location
	public Elevator(int initialFloor){
		status = new ElevatorStatus(initialFloor);
	}
	
	//addRequest function inserts request into elevator destinations
	public void addRequest(Request request){
		if (!request.isValid())
			return;
		ScheduledStop newStop = new ScheduledStop(request, calculateProximityScore(request));
		destinations.add(newStop);
		requestLog.add(request);
		//operate elevator
		if (!active)
			operate();
	}
	
	//addPickup function inserts pickup location and passenger's destination into destination
	public void addPickup(Request pickup, Request dropoff){
		if (!pickup.isValid() || !dropoff.isValid())
			return;
		//if the dropoff stop would be placed in front of pickup up the passenger, adjust proximityScores
		double pickupProximityScore = calculateProximityScore(pickup);
		double dropoffProximityScore = calculateProximityScore(dropoff);
		if (dropoffProximityScore < pickupProximityScore)
			dropoffProximityScore = pickupProximityScore + SCORE_ADJUSTMENT;
		//add stops to dropoffs
		ScheduledStop firstStop = new ScheduledStop(pickup, pickupProximityScore);
		ScheduledStop secondStop = new ScheduledStop(dropoff, dropoffProximityScore);
		destinations.add(firstStop);
		destinations.add(secondStop);
		requestLog.add(pickup);
		requestLog.add(dropoff);
		//operate elevator
		if (!active)
			operate();
	}
	
	//operate function makes all stops in the priority queue. Takes no parameters, returns void
	private void operate(){
		active = true;
		//make stops
		while (!destinations.isEmpty()){
			Request curr = destinations.poll().getRequest();
			status.setCurrentFloor(curr.getDestination());
			status.setDirection(curr.getDirection());
			
		}
		active = false;
	}

	//calculateProximityScore function finds weighted value representing how close a request is
	//takes current direction and distance into account
	public double calculateProximityScore(Request request){
		//check distance to request, if getting there requires changing directions,
		//and if the elevator would have to change directions afterward
		int distance = Math.abs(status.getCurrentFloor() - request.getDestination());
		boolean onCurrentPath = onCurrentPath(request);
		boolean directionsMatch = directionsMatch(request);
		double proximityScore = distance * DISTANCE_WEIGHT;
		if (onCurrentPath == false)
			proximityScore *= CHANGE_DIRECTION_TO_GET_THERE_WEIGHT;
		if (directionsMatch == false)
			proximityScore *= CHANGE_DIRECTION_AFTERWARD_WEIGHT;
		
		return proximityScore;
	}
	
	//getStatus function
	public ElevatorStatus getStatus(){
		return status;
	}
	
	public ArrayList<Request> getRequestLog(){
		return requestLog;
	}
	
	public double getDestinationWeight(){
		return DESTINATION_WEIGHT;
	}
	//onCurrentpath checks if elevator would have to change direction to get to request
	private boolean onCurrentPath(Request request){
		//if elevator is not moving or if request is on same floor, elevator does not have to change direction
		if (status.getDirection() == STATIONARY)
			return true;
		if (request.getDestination() == status.getCurrentFloor())
			return true;
		//check required direction
		int requiredDirection;
		if (status.getCurrentFloor() - request.getDestination() > 0){
			//request is below elevator
			requiredDirection = DOWN;
		}
		else{ //request is above elevator
			requiredDirection = UP;
		}
		if (requiredDirection == status.getDirection())
			return true;
		return false;	
	}
	
	//directionsMatch checks if request's direction matches the elevator's current direction
	private boolean directionsMatch(Request request){
		//if request has no next direction or elevator is stationary, we do not have to worry about a mismatch
		if (request.getDirection() == STATIONARY || status.getDirection() == STATIONARY)
			return true;
		//check next direction
		if (status.getDirection() == request.getDirection())
			return true;
		return false;
	}
	
	//comparator for priority queue compares ScheduledStops based on proximity score
	class ScheduledStopComparator implements Comparator<ScheduledStop>{
		public int compare(ScheduledStop a, ScheduledStop b){
			if (a.getProximityScore() < b.getProximityScore())
				return -1;
			if (a.getProximityScore() == b.getProximityScore())
				return 0;
			return 1;
		}
	}
}	
