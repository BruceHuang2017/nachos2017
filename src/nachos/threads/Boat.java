package nachos.threads;
import nachos.ag.BoatGrader;

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.LinkedList;

public class Boat
{
    static BoatGrader bg;

    private static void letBrucePlay( int adults, int children, BoatGrader b ){

        Lock tickets = new Lock();

        //assume hawaiians are all civilized citizens, they wait in line to embark.
        LinkedList<KThread> childrenInOahu = new LinkedList<>();
        LinkedList<KThread> childrenInMolokai = new LinkedList<>();
        LinkedList<KThread> adultInOahu = new LinkedList<>();
        LinkedList<KThread> adultInMolokai = new LinkedList<>();

        Island Oahu = new Island(adults+children);
        Island Molokai = new Island(0);

        someBoat noahsArk = new someBoat(Oahu, Molokai);

        for(int i=0; i<adults; i++){
            Hawaiian hawa = new Adult();
            KThread t = new KThread(hawa);
            t.setName("Adult "+i);
            t.fork();
            adultInOahu.add(t);
            System.out.println("Fork " + t.getName());
        }

        for(int i=0; i<children; i++){
            Hawaiian hawa = new Children();
            KThread t = new KThread(hawa);
            t.setName("Children "+i);
            t.fork();
            childrenInOahu.add(t);
            System.out.println("Fork " + t.getName());
        }



    }



    private interface Hawaii{
        int numberOfIslandKnown = 2;
    }

    private static class Island implements Hawaii{
        int initialPopulation;
        int currentPopulation;
        public Island(){}
        private Island(int initialPopulation){
            this.initialPopulation = initialPopulation;
            this.currentPopulation = initialPopulation;
        }

    }

    private static class someBoat{
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

    }


    public static abstract class Hawaiian implements Runnable{

        public void run(){

        }

        void debug(){

        }

    }

    public static class Adult extends Hawaiian{
        public int weight = 2;

    }

    public static class Children extends Hawaiian{
        public int weight = 1;

    }



    public static void selfTest() {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(789, 3, b);

    }


    public static void begin( int adults, int children, BoatGrader b ) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
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

        letBrucePlay(adults, children, b);

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
