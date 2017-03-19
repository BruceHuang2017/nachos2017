package nachos.threads;
import nachos.ag.BoatGrader;
import org.omg.PortableInterceptor.LOCATION_FORWARD;

import java.util.LinkedList;
import java.util.stream.StreamSupport;

public class Boat
{
    static BoatGrader bg;

    // initial global references
    private Lock isPlay;
    private someBoat noahsArk;
    private Island Oahu;
    private Island Molokai;
    private final Island destination = Molokai;
    private final Island born = Oahu;
    private Condition childrenOnOahu;
    private Condition childrenOnMolokai;
    private Condition adultsOnOahu;
    private Condition adultsOnMolokai;
    private Condition hawaiianOnBoat;
    
    public void letBrucePlay( int adults, int child, BoatGrader b ){
        isPlay = new Lock();

        //assume hawaiians are all civilized citizens, they wait in line to embark.
        // childrenOnOahu = new LinkedList<>();
        // childrenOnMolokai = new LinkedList<>();
        // adultsOnOahu = new LinkedList<>();
        // adultsOnMolokai = new LinkedList<>();
        childrenOnOahu = new Condition(isPlay);
        childrenOnMolokai = new Condition(isPlay);
        adultsOnOahu = new Condition(isPlay);
        adultsOnMolokai = new Condition(isPlay);
        hawaiianOnBoat = new Condition(isPlay);

        Oahu = new Island(adults+child);
        Molokai = new Island(0);

        someBoat noahsArk = new someBoat(born, destination);

        for(int i=0; i<adults; i++){
            Hawaiian hawa = new Adult(born);
            KThread t = new KThread(hawa);
            t.setName("Adult "+i);
            t.fork();
            System.out.println("Fork " + t.getName());
        }

        for(int i=0; i<child; i++){
            Hawaiian hawa = new Child(born);
            KThread t = new KThread(hawa);
            t.setName("Child "+i);
            t.fork();
            System.out.println("Fork " + t.getName());
        }

        
    }



    private interface Hawaii{
        int numberOfIslandKnown = 2;
    }

    private class Island implements Hawaii{
        int initialPopulation;
        int currentPopulation;
        public Island(){}
        private Island(int initialPopulation){
            this.initialPopulation = initialPopulation;
            this.currentPopulation = initialPopulation;
        }
    }

    public class someBoat{
        public someBoat(){
        }
        private someBoat(Island made, Island dst){
            this.made = made;
            this.dst = dst;
            this.currentLocation = made;
        }
        Hawaiian passenger = null;
        Hawaiian pilot = null;
        int weightLimit = 2;
        int currentWeight = 0;
        Island made;
        Island dst;
        Island currentLocation;
        boolean isEmpty(someBoat a){
            return a.currentWeight == 0;
        }
        boolean canEmbark(someBoat a){
            return a.currentWeight != weightLimit;
        }
        boolean isFull(someBoat a){
            return a.currentWeight == weightLimit;
        }
        boolean isOnBoat(Hawaiian a){
            return a == passenger || a == pilot;
        }
        boolean isPilot(Hawaiian a){
            return a == pilot;
        }
        boolean isPassenger(Hawaiian a){
            return a == passenger;
        }

    }

    public abstract class Hawaiian implements Runnable{

        Island born;
        Island currentLocation;
        abstract

        public void run(){
            isPlay.acquire();

            if(){

            }


            isPlay.release();
        }

        void debug(String a){
            System.out.println(KThread.currentThread().getName() + ": " + a);
        }

    }

    public class Adult extends Hawaiian{
        private Adult(Island born){
            this.born = born;
            this.currentLocation = born;
        }
        public int weight = 2;
        Island born = null;
        Island currentLocation = null;
    }

    public class Child extends Hawaiian{
        private Child(Island born){
            this.born = born;
            this.currentLocation = born;
        }
        public int weight = 1;
        Island born = null;
        Island currentLocation = null;
    }



    public static void selfTest() {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 3 child***");
        begin(789, 3, b);

    }


    public static void begin( int adults, int child, BoatGrader b ) {
        // Store the externally generated autograder in a class
        // variable to be accessible by child.
        bg = b;

        // Instantiate global variables here

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        Runnable r = new Runnable() {
            public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
        Boat werido = new Boat();
        werido.letBrucePlay(adults, child, b);

    }

    static void AdultItinerary() {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

    }

    static void ChildItinerary() {

    }

    static void SampleItinerary() {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.

        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}
