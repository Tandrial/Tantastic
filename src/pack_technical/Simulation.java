package pack_technical;

import pack_1.Utility;
import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public abstract class Simulation {

    int nextWaypoint;
    ArrayList<Boid_generic> defenderBoids;
    ArrayList<Boid_generic> attackBoids;
    AI_type ai_type;
    PatrollingScheme patrollingScheme;
    CollisionHandler collisionHandler;
    ArrayList<int[]> cords;

    public Simulation(ArrayList<Boid_generic> defenders, ArrayList<int[]> cords, ArrayList<Boid_generic> attackers, CollisionHandler handler) {
        this.collisionHandler = handler;
        this.cords = cords;
        this.defenderBoids = defenders;
        this.ai_type = new AI_type(Utility.randFloat(AI_manager.neighbourhoodSeparation_lower_bound, AI_manager.neighbourhoodSeparation_upper_bound), 70, 70, 2.0, 1.2, 0.9f, 0.04f, "Simulator2000");
        this.attackBoids = copyStateOfBoids(attackers);
        this.patrollingScheme = new PatrollingScheme(ai_type.getWayPointForce());
    }

    protected Simulation() {
    }

    public ArrayList<Boid_generic> copyStateOfBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();

        for (Boid_generic boid : boids) {
            Boid_generic bi = new Boid_standard(boid.getLocation().x, boid.getLocation().y, 6, 10);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }
        return boidListClone;
    }

    public AI_type getSimulator() {
        return ai_type;
    }

    public void waypointSetup(ArrayList<Boid_generic> defenders) {
        for(int[] cord : cords){
            patrollingScheme.getWaypoints().add(new PVector(cord[0],cord[1]));
        }

        //FOLLOW THE SIMILLAR WAYPOINT AS DEFENDERS
        // TODO - Magic numbers!!
        float shortestDistanceSq = 3000 * 3000;
        float shortestVectorAngle=0;
        float nextToShortestVectorAngle=0;
        int positionInTheList = 0;
        for(int i=0;i<patrollingScheme.getWaypoints().size();i++) {
            PVector checkpoint = patrollingScheme.getWaypoints().get(i);
            PVector nextCheckPoint = patrollingScheme.getWaypoints().get((i+1)%patrollingScheme.getWaypoints().size());
            float distanceSq = Utility.distSq(defenders.get(0).getLocation(), checkpoint);
            if (distanceSq < shortestDistanceSq) {
                shortestDistanceSq = distanceSq;
                shortestVectorAngle = PVector.angleBetween(defenders.get(0).getLocation(), checkpoint);
                nextToShortestVectorAngle = PVector.angleBetween(defenders.get(0).getLocation(), nextCheckPoint);
            }
        }

        if (shortestVectorAngle < nextToShortestVectorAngle) {
            nextWaypoint = positionInTheList;
        }else{
            nextWaypoint = (positionInTheList + 1) % patrollingScheme.getWaypoints().size();
        }

        patrollingScheme.currentPosition = nextWaypoint;
    }



}