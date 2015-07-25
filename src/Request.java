
public class Request {
	
	//possible directions
	private final int UP = 0;
	private final int DOWN = 1;
	private final int STATIONARY = 2;
	private final int MIN_FLOOR = 0;
	private final int MAX_FLOOR = 20;
	
	//where the person wants to go
	private int direction;
	private int destination;
	
	//boolean to track whether the request is valid
	private boolean valid = true;
	
	//constructor takes the direction and destination of the request
	public Request(int direction, int destination){
		this.direction = direction;
		this.destination = destination;
		checkInput();
	}
	
	//make sure request is valid. We don't want this elevator to glitch and squish anybody
	private void checkInput(){
		if (direction != UP && direction != DOWN && direction != STATIONARY)
			valid = false;
		if (destination < MIN_FLOOR && destination > MAX_FLOOR)
			valid = false;
	}
	
	//allow elevator to check if request is valid
	public boolean isValid(){
		return valid;
	}
	
	public void print(){
		System.out.println("Destination: " + destination);
		if (direction == UP)
			System.out.println("Direction: UP");
		else if (direction == DOWN)
			System.out.println("Direction: DOWN");
		else
			System.out.println("Direction: STATIONARY");
	}
	
	//getter functions
	public int getDirection(){
		return direction;
	}
	public int getDestination(){
		return destination;
	}

}
