import java.util.Random;
import java.util.Scanner;

//ControlPanel allows the Elevator operator to oversee and control the elevator system. The system allows
//NUM_ELEVATORS Elevators, which the operator can adjust to his or her liking. The operator
//controls the system through the command line by adding Requests, pickup and dropoff combinations,
//checking the status of all Elevators or a specific Elevator, and viewing the log of a specific
//Elevator's requests. The commands are as follows:

//request <floor_number> : puts in a Request for an Elevator to visit <floor_number>
//pickup <pickup_floor> <dropoff_floor> : puts in a Request to pickup at <pickup_floor> and dropoff at <dropoff_floor>
//status 				: prints statuses of all Elevators
//status <elevator_number> : prints status of <elevator_number>
//log <elevator_number> : prints the log of <elevator_number> Request history
//exit 					: exits the program

//Illegal commands will receive an error message. Have fun!
public class ControlPanel {

	//number of elevators
	private static final int NUM_ELEVATORS = 16;
	
	//possible directions
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int STATIONARY = 2;
	
	//parameters for generating random requests and user requests
	private static final int MAX_FLOOR = 20;
	private static final int NUM_CONDITIONS = 3;
	
	//random number generator for generating requests
	private static Random rand = new Random();
	
	//array of elevators
	private static Elevator[] elevators = new Elevator[NUM_ELEVATORS];
	
	//scanner for user input
	private static Scanner scanner = new Scanner(System.in);
	private static final String INPUT_SPLITTER = " ";
	
	//scanner commands
	private static final String EXIT_COMMAND = "exit";
	private static final String REQUEST_LOG = "log";
	private static final String REQUEST_STATUS = "status";
	private static final String REQUEST_REQUEST = "request";
	private static final String REQUEST_PICKUP = "pickup";
	private static final String EXIT_MESSAGE = "Goodbye!";
	
	public static void main(String[] args) {
		initializeElevators();
		boolean exit = false;
		//read in operator commands
		System.out.println("Welcome to the elevator control panel. Please input a command.");
		while (!exit){
			String line = scanner.nextLine();
			String[] input = line.split(INPUT_SPLITTER);
			if (input.length == 1 && input[0].equals(REQUEST_STATUS)){
				checkStatus();
			}
			else if (input[0].equals(REQUEST_STATUS) && isLegalElevator(input[1])){
				checkStatus(Integer.parseInt(input[1]));
			}
			else if (input.length == 2 && input[0].equals(REQUEST_LOG) && isLegalElevator(input[1])){
				printRequestLog(Integer.parseInt(input[1]));
			}
			else if (input.length == 2 && input[0].equals(REQUEST_REQUEST) && isLegalFloor(input[1])){
				addRequest(STATIONARY, Integer.parseInt(input[1]));
				System.out.println("Request added.");
			}
			else if (input.length == 3 && input[0].equals(REQUEST_PICKUP) && isLegalFloor(input[1]) && isLegalFloor(input[2])){
				if (Integer.parseInt(input[2]) - Integer.parseInt(input[1]) > 0){
					//passenger going up
					addPickupAndDropoff(UP, Integer.parseInt(input[1]), STATIONARY, Integer.parseInt(input[2]));
				}
				else if (Integer.parseInt(input[2]) - Integer.parseInt(input[1]) < 0){
					//passenger going down
					addPickupAndDropoff(DOWN, Integer.parseInt(input[1]), STATIONARY, Integer.parseInt(input[2]));
				}
				System.out.println("Pickup and dropoff added.");
			}
			else if (input.length == 1 && input[0].equals(EXIT_COMMAND))
				exit = true;
			else
				System.out.println("Error: please input a proper command. See the README file for a list of proper commands.");	
		}
		System.out.println(EXIT_MESSAGE);
	}
	
	//addRequest function adds a request to the Elevator best situated to fulfill it
	public static void addRequest(int direction, int destination){
		//create new request, make sure it's valid
		Request request = new Request(direction, destination);
		if (request.isValid() == false)
			return;
		//check which elevator is best recipient of request
		int bestElevator = 0;
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < NUM_ELEVATORS; i++){
			double score = elevators[i].calculateProximityScore(request);
			if (score < bestScore){
				bestScore = score;
				bestElevator = i;
			}
		}
		elevators[bestElevator].addRequest(request);
	}
	
	//addPickupAndDropoff function adds pickup and dropoff pair to Elevator best situated to fulfill them
	public static void addPickupAndDropoff(int pickupDirection, int pickupDestination, int dropoffDirection, int dropoffDestination){
		//create new requests, make sure they're valid
		Request pickup = new Request(pickupDirection, pickupDestination);
		Request dropoff = new Request(dropoffDirection, dropoffDestination);
		if (pickup.isValid() == false || dropoff.isValid() == false)
			return;
		//check which elevator is best recipient of pickup and dropoff combo
		int bestElevator = 0;
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < NUM_ELEVATORS; i++){
			//incorporate destination into score
			double score = elevators[i].calculateProximityScore(pickup) + elevators[i].calculateProximityScore(dropoff) * elevators[i].getDestinationWeight();
			if (score < bestScore){
				bestScore = score;
				bestElevator = i;
			}
		}
		elevators[bestElevator].addPickupAndDropoff(pickup, dropoff);
	}
	
	public static void initializeElevators(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			elevators[i] = new Elevator(i);
		}
	}
	
	private static void checkStatus(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			System.out.println("Elevator " + i + ": " );
			elevators[i].getStatus().print();
			System.out.println();
		}
	}
	
	private static void checkStatus(int i){
		System.out.println("Elevator " + i + ": " );
		elevators[i].getStatus().print();
		System.out.println();
	}
	
	private static void printRequestLog(int i){
		if (elevators[i].getRequestLog().isEmpty())
			System.out.println("Elevator " + i + " has not received any requests.");
		for (Request request : elevators[i].getRequestLog()){
			request.print();
		}
	}
	
	private static boolean isLegalElevator(String s){
		if (Integer.parseInt(s) >= 0 && Integer.parseInt(s) < NUM_ELEVATORS)
			return true;
		return false;
	}
	
	private static boolean isLegalFloor(String s){
		if (Integer.parseInt(s) >= 0 && Integer.parseInt(s) < MAX_FLOOR)
			return true;
		return false;
	}
	
	//DON'T DELETE ME! I AM A TIMELESS RELIC OF THE PAST
	public static Request generateRequest(){
		Request randomRequest = new Request(rand.nextInt(NUM_CONDITIONS), rand.nextInt(MAX_FLOOR));
		return randomRequest;
	}
	
	public static void printElevators(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			System.out.println("Elevator " + i + ":");
			elevators[i].getStatus().print();
		}
	}
}
