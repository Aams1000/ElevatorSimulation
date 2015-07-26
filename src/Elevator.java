import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

//Elevator represents...an elevator. It contains an ElevatorStatus object, a PriorityQueue of ScheduledStops, and an ArrayList
//of all Requests is has received. Incoming Requests are ordered by proximityScore, a weighted value representing the
//ease of fulfilling the Request. This is calculated by weighting the distance to the Request, if traveling there would
//require changing direction, and if subsequently fulfilling the Request would require reversing our current trajectory. For instance:

//If the Elevator is on floor 10 and traveling UP, a request on floor 5 to go DOWN would be five floors away, require switching
//direction to arrive, and make us change our current direction to DOWN. This request would receive a high cost.

//The weights can be adjusted to match the operator's taste. When the Elevator receives a Request, it calls the operate()
//function if it is not currently active. The operate() function uses the Thread.sleep() function to simulate travel.
//The time between floors is set to TRAVEL_DELAY.
public class Elevator {

	//the priority queue wants an initial capacity. let's give it one
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
	
	//travel delay for going between floors
	private final int TRAVEL_DELAY = 2000;

	//list of destinations and log of received Requests
	PriorityBlockingQueue<ScheduledStop> destinations = new PriorityBlockingQueue<ScheduledStop>(INITIAL_CAPACITY, new ScheduledStopComparator());
	ArrayList<Request> requestLog = new ArrayList<Request>();
	
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
	
	//addPickup function inserts pickup and dropoff Requests into elevator destinations
	public void addPickupAndDropoff(Request pickup, Request dropoff){
		if (!pickup.isValid() || !dropoff.isValid())
			return;
		//if the dropoff stop would be placed in front of pickup up the passenger, adjust proximityScores
		double pickupProximityScore = calculateProximityScore(pickup);
		double dropoffProximityScore = calculateProximityScore(dropoff);
		if (dropoffProximityScore < pickupProximityScore)
			dropoffProximityScore = pickupProximityScore + SCORE_ADJUSTMENT;
		//add stops to elevator destinations and history log
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
	
	//operate function makes all stops in the priority queue. Implements time stepping of one second per floor.
	//Takes no parameters, returns void
	private void operate(){
		active = true;
		new Thread (new Runnable(){
			public void run(){
				while (!destinations.isEmpty()){
					Request curr = destinations.poll().getRequest();
					//move one floor at a time until we have reached our destination
					while (status.getCurrentFloor() != curr.getDestination()){
						//check if floor is above or below
						if (curr.getDestination() < status.getCurrentFloor()){
							try{
								Thread.sleep(TRAVEL_DELAY);
							}
							catch(InterruptedException ex){
							}
							status.setCurrentFloor(status.getCurrentFloor() - 1);
						}
						//floor is above
						else{
							try{
								Thread.sleep(TRAVEL_DELAY);
							}
							catch(InterruptedException ex){
							}
							status.setCurrentFloor(status.getCurrentFloor() + 1);
						}
					}
					status.setCurrentFloor(curr.getDestination());
					status.setDirection(curr.getDirection());
				}
			//elevator has completed all requests, going into standby
			active = false;	
			}
		}).start();
	}

	//calculateProximityScore function finds weighted value representing the ease of fulfilling a Request.
	//takes distance to Request, if traveling there would require changing direction, and if
	//fulfilling it would require changing current trajectory into account. A low value means low cost,
	//high value means high cost.
	public double calculateProximityScore(Request request){
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
	
	//onCurrentpath checks if elevator would have to change direction to get to Request
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
	
	//directionsMatch checks if Request's direction matches the Elevator's current direction
	private boolean directionsMatch(Request request){
		//if request has no next direction or Elevator is stationary, we do not have to worry about a mismatch
		if (request.getDirection() == STATIONARY || status.getDirection() == STATIONARY)
			return true;
		//check next direction
		if (status.getDirection() == request.getDirection())
			return true;
		return false;
	}
	
	//comparator for priority queue compares ScheduledStops based on proximity score. A low score means
	//Request is easy to fulfill, high score means difficult to fulfill
	class ScheduledStopComparator implements Comparator<ScheduledStop>{
		public int compare(ScheduledStop a, ScheduledStop b){
			if (a.getProximityScore() < b.getProximityScore())
				return -1;
			if (a.getProximityScore() == b.getProximityScore())
				return 0;
			return 1;
		}
	}
	
	//getters
	public ElevatorStatus getStatus(){
		return status;
	}
	public ArrayList<Request> getRequestLog(){
		return requestLog;
	}
	public double getDestinationWeight(){
		return DESTINATION_WEIGHT;
	}
}	
