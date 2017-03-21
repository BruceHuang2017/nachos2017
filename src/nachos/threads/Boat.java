package nachos.threads;
import nachos.ag.BoatGrader;

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
    private Condition passengerOnBoat;
    
    public void letBrucePlay( int adults, int child, BoatGrader b ){
        isPlay = new Lock();

        childrenOnOahu = new Condition(isPlay); // will eventually leave island
        childrenOnMolokai = new Condition(isPlay); // might need to back
        adultsOnOahu = new Condition(isPlay);  // will be considered firstly
        adultsOnMolokai = new Condition(isPlay); // finish, never need lock
        passengerOnBoat = new Condition(isPlay);

        Oahu = new Island(adults, child);
        Oahu.currentAdults = adults;
        Oahu.currentChildern = child;
        Molokai = new Island(0, 0);

        someBoat noahsArk = new someBoat(born, destination, 2);

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
        int currentChildern;
        int currentAdults;
        public Island(){}
        private Island(int adults, int child){
            currentChildern = child;
            currentAdults = adults;
            this.initialPopulation = adults + child;
        }
        private int getCurrentPopulation(){
            return currentAdults + currentChildern;
        }
    }

    public class someBoat{
        public someBoat(){
        }
        private someBoat(Island made, Island dst, int weightLimit){
            this.made = made;
            this.dst = dst;
            this.currentLocation = made;
            this.weightLimit = weightLimit;
        }
        Hawaiian passenger = null;
        Hawaiian pilot = null;
        int weightLimit = 2;
        int currentWeight = 0;
        Island made;
        Island dst;
        Island currentLocation;
        boolean isEmpty(){
            return noahsArk.currentWeight == 0;
        }
        boolean canEmbark(){
            return noahsArk.currentWeight != weightLimit;
        }
        boolean isFull(){
            return noahsArk.currentWeight == weightLimit;
        }
        boolean isOnBoat(Hawaiian person){
            return person == passenger || person == pilot;
        }
        boolean isPilot(Hawaiian person){
            return person == pilot;
        }
        boolean isPassenger(Hawaiian person){
            return person == passenger;
        }
        void clearBoat(){
            currentWeight = 0;
            passenger = null;
            pilot = null;
        }

        void depart(Island from, Island to){
            if (from == to) return; // should never happen
            isPlay.acquire();
            if (from == this.currentLocation && !isEmpty()){
                this.currentLocation = to;
                if(pilot instanceof Adult){
                    // must from O to M
                    // always wake up and off the boat, sleep on M
                    Oahu.currentAdults --;
                    Molokai.currentAdults ++;
                    ((Adult) pilot).setCurrentLocation(to);
                    adultsOnMolokai.sleep();
                    clearBoat();
                }else if (passenger != null){
                    // must from O to M
                    // passenger sleep on boat, and pilot is current thread.
                    Oahu.currentChildern = Oahu.currentChildern - 2;
                    Molokai.currentChildern = Molokai.currentChildern + 2;
                    pilot.currentLocation = to;
                    childrenOnMolokai.sleep();
                    passengerOnBoat.wake();
                    passenger.currentLocation = to;
                    childrenOnMolokai.sleep();
                    clearBoat();
                }else{
                    // must from M to O, 1 pilot 0 passenger, child
                    if (from != Molokai) return; // should never happen
                    Molokai.currentChildern --;
                    Oahu.currentChildern ++;
                    pilot.currentLocation = to;
                    childrenOnOahu.sleep();
                    clearBoat();
                }
            }
            isPlay.release();
        }

        void onBoard(Hawaiian person){
            if(person instanceof Adult){
                // has to from O to M, will wake up all the time.
                if (currentLocation != Oahu) currentLocation = Oahu;
                this.pilot = person;
                this.passenger = person;
                currentWeight += ((Adult) person).weight; // 2

            }else if(passenger == null) {
                this.passenger = person;
                passengerOnBoat.sleep();
                currentWeight += ((Child) person).weight; // 1

            }else if(pilot == null){
                this.pilot = person;
                currentWeight += ((Child) person).weight; // 2
            }
        }

        void ppSwitch(){
            Hawaiian a = passenger;
            passenger = pilot;
            pilot = a;
        }

    }

    public abstract class Hawaiian implements Runnable{

        Island born = Oahu;
        Island currentLocation;
        final Island targetLocation = Molokai;
        abstract void bookTicket();
        public void run(){
            isPlay.acquire();
            while (true){
                if (Oahu.initialPopulation == Molokai.getCurrentPopulation()) return; // finished
                if (this.currentLocation == noahsArk.currentLocation && noahsArk.canEmbark())

                    bookTicket();

            }
        }

        void debug(String a){
            System.out.println(KThread.currentThread().getName() + ": " + a);
        }

    }

    public class Adult extends Hawaiian{
        private Adult(Island born){
            this.born = born;
            this.setCurrentLocation(born);
        }
        public int weight = 2;
        Island born = null;
        private Island currentLocation = null;

        @Override
        void bookTicket(){
            if(noahsArk.isEmpty()){
                noahsArk.onBoard(this);
                // bc i am pilot
                isPlay.release();
                noahsArk.depart(getCurrentLocation(), targetLocation);

            }else{
                // not able to embark
                adultsOnOahu.sleep();
                isPlay.release();
            }

        }


        public Island getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(Island currentLocation) {
            this.currentLocation = currentLocation;
        }
    }

    public class Child extends Hawaiian{
        private Child(Island born){
            this.born = born;
            this.currentLocation = born;
        }
        public int weight = 1;
        Island born = null;
        Island currentLocation = null;

        @Override
        void bookTicket(){
            if(currentLocation == Oahu){
                // make sure two children on board
                noahsArk.onBoard(this);
                if(noahsArk.canEmbark() && noahsArk.isPassenger(this)) isPlay.release();
                if(noahsArk.isFull() && noahsArk.isPilot(this)) {
                    isPlay.release();
                    noahsArk.depart(currentLocation, Molokai);
                }

            }else if(currentLocation == Molokai){
                // only one child back to O
                noahsArk.onBoard(this);
                if(noahsArk.isPassenger(this) && noahsArk.canEmbark()){
                    noahsArk.ppSwitch();
                    isPlay.release();
                    noahsArk.depart(currentLocation, Oahu);

                }

            }

        }

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
