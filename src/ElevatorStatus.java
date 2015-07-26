//ElevatorStatus contains an elevator's current floor and it's direction. With more time, I'd love to add
//it's scheduled stops
public class ElevatorStatus {

	//possible directions
	private final int UP = 0;
	private final int DOWN = 1;
	private final int STATIONARY = 2;
	
	//current state and locations
	private int direction;
	private int currentFloor;
	
	//constructor takes initial floor
	public ElevatorStatus(int initialFloor){
		currentFloor = initialFloor;
		direction = STATIONARY;
	}
	
	public void print(){
		System.out.println("Current floor: " + currentFloor);
		if (direction == UP)
			System.out.println("Direction: UP");
		else if (direction == DOWN)
			System.out.println("Direction: DOWN");
		else
			System.out.println("Direction: STATIONARY");
	}
	
	//getters and setters
	public int getDirection(){
		return direction;
	}
	public int getCurrentFloor(){
		return currentFloor;
	}
	public void setDirection(int direction){
		this.direction = direction;
	}
	public void setCurrentFloor(int currentFloor){
		this.currentFloor = currentFloor;
	}
	
}
