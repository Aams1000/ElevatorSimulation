# ElevatorControlPanel
A coding challenge for a company to create and simulate an elevator control panel. The program creates a specified number
of elevators that operate the floors of a building. Through the command line, the operator can add requests, pickups and dropoffs, view elevator statuses, and receive logs of an elevator's history. The commands include:

    request <floor_number>                : puts in a Request for an Elevator to visit <floor_number>
    pickup <pickup_floor> <dropoff_floor> : puts in a Request to pickup at <pickup_floor> and dropoff at <dropoff_floor>
    status 				                  : prints statuses of all Elevators
    status <elevator_number>              : prints status of <elevator_number>
    log <elevator_number>                 : prints the log of <elevator_number> Request history
    exit 					              : exits the program

The system uses the Thread.sleep() function to time-step the simulation. Traveling one floor takes two seconds.

The most interesting part of the simulation is assigning requests to elevators. The program uses a proximity score to gauge the ease of fulfilling a request for each elevator. This is calculated by weighting the distance to the request, if traveling there would require changing direction, and if subsequently fulfilling the request would require reversing our current trajectory. For instance:

  If the Elevator is on floor 10 and traveling UP, a request on floor 5 to go DOWN would be five floors away, require      switching direction to arrive, and make us change our current direction to DOWN.

This request would receive a high cost. Further documentation on this strategy can be found in '''Elevator.java'''.

Enjoy!
