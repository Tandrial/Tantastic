package pack_technical;

import pack_AI.AI_type;
import pack_boids.BoidGeneric;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Random;

import pack_1.Constants;
import pack_1.Utility;


//TODO: Rename location to currentAttackerLocation
//TODO: seems to be a lot of redundant shared code between InnerSim and EnvSim. Tidy.
//TODO: change currentDistance to be currentDistanceToTarget
//TODO: change theClosetDistance to be closetDistanceToTarget
//TODO: I think 'waypointCoords' are the waypoint co-ordinates. If so change waypointCoords to be waypointCoordinates
//TODO: rename r0acceleration & r0velocity local variables

public class InnerSimulation extends Simulation {

    boolean victory = false;
    //Integer nextWaypoint;
    Random randG = new Random();
    //what does targetVector actually represent
    PVector targetVector = new PVector(0,0);
    PVector randomVector;
    float theClosetDistance;
    float currentDistance;
    double avgReward;
    int nodeDepth;
    boolean simulating=true;

    public boolean isSimulating() {
        return simulating;
    }

    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }

    public void createSimulationsAndRandomVectors(){
        float rand = randG.nextFloat() * 1;
        float rand2 = randG.nextFloat() * 1;
        randomVector = new PVector(-1+2*rand, -1+2*rand2);
        randomVector.setMag(0.1f);
    }

    public InnerSimulation(AI_type ai, ArrayList<BoidGeneric> defenderBoids, ArrayList<int[]> waypointCoords, ArrayList<BoidGeneric> attackBoids, CollisionHandler collisionHandler, int nodeDepth) {
        super(copyStateOfBoids(defenderBoids), waypointCoords, copyStateOfBoids(attackBoids), collisionHandler);
        this.ai_type = ai;
        this.nodeDepth = nodeDepth;
        waypointSetup(defenderBoids);
        createSimulationsAndRandomVectors();
    }



    public void run(){
        if (simulating) {
            PVector theClosest = new PVector(0,0);
            float distance = 150; //distance to target from start?
            theClosetDistance = 2000;
            PVector location = getAttackBoid().getLocation();

            for (BoidGeneric defenderBoid : defenderBoids) {
                //For each layer in the MCTS, moves every defender boid one iteration
                for(int i=0; i < nodeDepth; i++) {
                    defenderBoid.move(defenderBoids);
                    defenderBoid.update();
                }
                if (Utility.distSq(defenderBoid.getLocation(), getAttackBoid().getLocation()) < Constants.COLLISION_DISTANCE_SQ) {
                    getAttackBoid().setHasFailed(true);
                }
            }

            if((Utility.distSq(location, Constants.TARGET) <= Constants.HIT_DISTANCE_SQ || Utility.distSq(getAttackBoid().getLocation(), location) >= distance * distance)
                && !getAttackBoid().hasFailed() ){
                simulating = false;
            }

            getAttackBoid().update(randomVector);
            // TODO - Could replace this dist with distSq, but that will change all of the currentDistance etc. vars to be currentDistanceSq
            currentDistance = PVector.dist(location, Constants.TARGET);


            if (!getAttackBoid().hasFailed()) {
                if (currentDistance < theClosetDistance) {
                    theClosest = randomVector;
                    theClosetDistance = currentDistance;
                }
                if (!simulating) {
                    targetVector = theClosest;
                }
                if(currentDistance < 15){
                    victory = true;
                }
            } else {
                simulating = false;
            }

            //I think this is the random rollout from newly expanded node
            if(simulating && !victory) {
                PVector locationRollOut = new PVector(location.x, location.y);
                PVector rOacceleration = getAttackBoid().getAcceleration();
                PVector rOvelocity = getAttackBoid().getVelocity();
                //avgReward is more like instantaneous reward rather than an average reward?

                avgReward = 0;
                for(int j=0; j<1000; j++){
                    locationRollOut.add(rOvelocity.add(rOacceleration.add(randomVector)));
                    //float rand = randG.nextFloat() * 1;
                    //float rand2 = randG.nextFloat() * 1;
                    //locationRollOut.add(rOvelocity.add(rOacceleration.add(new PVector(-1+2*rand, -1+2*rand2))));

                    if(Utility.distSq(locationRollOut, Constants.TARGET) < 20 * 20) {
                        avgReward = 1;
                        break;
                    } else {
                        for (BoidGeneric defenderBoid : defenderBoids) {
                            if (Utility.distSq(defenderBoid.getLocation(), locationRollOut) < 16 * 16) {  // was 3
                                avgReward = -1;
                                break;
                            }
                        }
                        if(avgReward < 0){
                            break;
                        }
                    }
                }
            }
        }
    }
}