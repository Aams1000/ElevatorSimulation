import java.util.Random;
import java.util.Scanner;

public class ControlPanel {

	//number of elevators
	private static final int NUM_ELEVATORS = 2;
	
	//possible directions
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int STATIONARY = 2;
	
	//parameters for generating random requests and user requests
	private static final int MAX_FLOOR = 20;
	private static final int NUM_CONDITIONS = 3;
	private static final int NUM_REQUESTS = 50;
	
	//random number generator for generating requests
	private static Random rand = new Random();
	
	//array of elevators
	private static Elevator[] elevators = new Elevator[NUM_ELEVATORS];
	
	//scanner for user input
	private static Scanner scanner = new Scanner(System.in);
	private static final String INPUT_SPLITTER = " ";
	private static final String EXIT_COMMAND = "exit";
	private static final String REQUEST_LOG = "log";
	
	public static void main(String[] args) {
		initializeElevators();
		boolean exit = false;
		System.out.println("Welcome to the elevator control panel. Please input a command.");
		while (!exit){
			String line = scanner.nextLine();
			String[] input = line.split(INPUT_SPLITTER);
			if (input.length == 1 && input[0].equals("status")){
				checkStatus();
			}
			else if (input[0].equals("status") && isLegalElevator(input[1])){
				checkStatus(Integer.parseInt(input[1]));
			}
			else if (input.length == 2 && input[0].equals(REQUEST_LOG) && isLegalElevator(input[1])){
				printRequestLog(Integer.parseInt(input[1]));
			}
			else if (input.length == 2 && input[0].equals("request") && isLegalFloor(input[1])){
				addRequest(STATIONARY, Integer.parseInt(input[1]));
				System.out.println("Request added.");
			}
			else if (input.length == 3 && input[0].equals("pickup") && isLegalFloor(input[1]) && isLegalFloor(input[2])){
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
		System.out.println("Goodbye!");
//		for (int i = 0; i < NUM_REQUESTS; i++){
//			Request randomRequest = generateRequest();
//			addPickupAndDropoff(0, randomRequest.getDestination(), 2, randomRequest.getDestination() + 1);
//			//printElevators();
//			//System.out.println();
//		}
//		for (Request request : elevators[0].getRequestLog())
//			request.print();
		
		//printElevators();

	}
	
	public static void addRequest(int direction, int destination){
		//create new request, make sure it's valid
		Request request = new Request(direction, destination);
		if (request.isValid() == false)
			return;
		//check which elevator is best recipient of request
		int bestElevator = 0;
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < NUM_ELEVATORS; i++){
//			System.out.println("Elevator " + i + " proximity score: " + elevators[i].calculateProximityScore(request));
//			elevators[i].getStatus().print();
			double score = elevators[i].calculateProximityScore(request);
			if (score < bestScore){
				bestScore = score;
				bestElevator = i;
			}
		}
		elevators[bestElevator].addRequest(request);
	}
	
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
//					System.out.println("Elevator " + i + " proximity score: " + elevators[i].calculateProximityScore(request));
//					elevators[i].getStatus().print();
			double score = elevators[i].calculateProximityScore(pickup) + elevators[i].calculateProximityScore(dropoff) * elevators[i].getDestinationWeight();
			if (score < bestScore){
				bestScore = score;
				bestElevator = i;
			}
		}
		elevators[bestElevator].addPickup(pickup, dropoff);
	}
	
	public static void initializeElevators(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			elevators[i] = new Elevator(i);
		}
	}
	
	public static void printElevators(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			System.out.println("Elevator " + i + ":");
			elevators[i].getStatus().print();
		}
	}
	
	public static Request generateRequest(){
		Request randomRequest = new Request(rand.nextInt(NUM_CONDITIONS), rand.nextInt(MAX_FLOOR));
		return randomRequest;
	}
	
	private static void checkStatus(){
		for (int i = 0; i < NUM_ELEVATORS; i++){
			System.out.println("Elevator " + i + ": " );
			elevators[i].getStatus().print();
			System.out.println();
		}
	}
	
	private static void printRequestLog(int i){
		for (Request request : elevators[i].getRequestLog()){
			request.print();
		}
	}
	
	private static void checkStatus(int i){
		System.out.println("Elevator " + i + ": " );
		elevators[i].getStatus().print();
		System.out.println();
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

//    Querying the state of the elevators (what floor are they on and where they are going),
//    receiving an update about the status of an elevator,
//    receiving a pickup request,
//    time-stepping the simulation.


}
