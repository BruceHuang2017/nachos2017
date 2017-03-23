package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    private static Lock boat = new Lock();
    private static Condition adultsOnO = new Condition(boat);
    private static Condition childrenOnO = new Condition(boat);
    private static Condition adultsOnM = new Condition(boat);
    private static Condition childrenOnM = new Condition(boat);
    private static int numberOfAdultsOnO;
    private static int numberOfAdultsOnM;
    private static int numberOfChildrenOnO;
    private static int numberOfChildrenOnM;
    private static boolean mHaveChild = false;
    private static boolean boatAtO = true;
    private static boolean passengerAvailableOnO = false;

    public static void selfTest(){
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);

//        System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//        begin(1, 2, b);

//        System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//        begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b ) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here
        numberOfAdultsOnO = adults;
        numberOfChildrenOnO = children;
        numberOfAdultsOnM = 0;
        numberOfChildrenOnM = 0;

        /*       Runnable r = new Runnable() {
            public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
        */

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        Runnable ad = new Runnable() {
            @Override
            public void run() {
                AdultItinerary();
            }
        };

        Runnable cd = new Runnable() {
            @Override
            public void run() {
                ChildItinerary();
            }
        };

        for(int i=0; i<adults; i++){
            KThread a = new KThread(ad);
            a.fork();
            System.out.println("fork adult");
        }

        for(int i=0; i<children; i++){
            KThread c = new KThread(cd);
            c.fork();
            System.out.println("fork child");
        }
    }

    static void AdultItinerary() {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	    boat.acquire();
        if (mHaveChild && boatAtO){
            boatAtO = false;
            numberOfAdultsOnO--;
            bg.AdultRowToMolokai();
            numberOfAdultsOnM++;
            childrenOnM.wake(); // does not mean this child will get lock, maybe next one is child on island O
            adultsOnM.sleep();
            boat.release();

        }else{
            adultsOnO.sleep();
            boat.release();

        }

    }

    static void ChildItinerary() {
        boolean imOnO = true;
        boat.acquire();
        // children will continue if the task is not finished.
        while (true) {
            if (passengerAvailableOnO && imOnO){
                passengerAvailableOnO = false;
                numberOfChildrenOnO--;
                bg.ChildRideToMolokai();
                numberOfChildrenOnM++;
                if (!mHaveChild)
                    mHaveChild = true;
                if(numberOfChildrenOnO!=0 || numberOfAdultsOnO!=0)
                    childrenOnM.wake();
                imOnO = false;
                boatAtO = false;
                childrenOnM.sleep();
                boat.release();


            }else if (!passengerAvailableOnO && imOnO){
                // as pilot
                numberOfChildrenOnO--;
                bg.ChildRowToMolokai();
                numberOfChildrenOnM++;
                imOnO = false;
                boatAtO = false;
                passengerAvailableOnO = true;
                childrenOnM.sleep();
                boat.release();


            }else if (!imOnO){

                numberOfChildrenOnM--;
                if(numberOfChildrenOnM==0) mHaveChild =false;
                bg.ChildRowToOahu();
                numberOfChildrenOnO++;
                imOnO = true;
                boatAtO = true;
                if(numberOfAdultsOnO!=0) adultsOnO.wake();
                childrenOnO.wake();
                childrenOnO.sleep();
                boat.release();

            }else{
                System.out.println("unexpected error");
            }
        }

    }

    static void SampleItinerary()
    {
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
