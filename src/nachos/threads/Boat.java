package nachos.threads;
import com.sun.deploy.util.SyncAccess;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    public static void selfTest() {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b ) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here
        Lock boatLock = new Lock();

        island origin = new island();
        origin.waitingList = new Condition(boatLock);
        origin.adults = adults;
        origin.child = children;

        island dst = new island();
        dst.waitingList = new Condition(boatLock);
        dst.adults = 0;
        dst.child = 0;

        theboat aboat = new theboat();
        aboat.passengers = new Condition(boatLock);
        aboat.weight = 0;
        aboat.loc = origin;

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.
        Runnable a = new Runnable() {
            island myloc = origin;
            @Override
            public void run() {
                boatLock.acquire();
                while (true)
                {
                    if (dst.child>0 && aboat.weight==0 && aboat.loc == origin)
                    {
                        AdultItinerary(aboat,origin,dst,boatLock,myloc);

                    }
                }
            }
        };

        Runnable c = new Runnable() {
            island myloc = origin;
            @Override
            public void run() {
                boatLock.acquire();
                while (true)
                {
                    if (aboat.weight<2 && myloc == aboat.loc)
                        ChildItinerary(aboat,origin,dst,boatLock,myloc);
                }
            }
        };

        for (int i=0;i<adults; i++){
            KThread t = new KThread(a);
            t.setName("Adult " + i);
            t.fork();
            System.out.println("Fork " + t.getName());
        }

        for (int i=0;i<children; i++){
            KThread t = new KThread(c);
            t.setName("Child " + i);
            t.fork();
            System.out.println("Fork " + t.getName());
        }

        Runnable r = new Runnable() {
            public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();

    }

    static class theboat {
        Condition passengers;
        int weight;
        island loc;
        void clear(){
            weight=0;
            passengers.wakeAll();
            System.out.println("BoatClear");
        }
        void gameover(){
            clear();
            weight=2;
            System.out.println("GameOver.");
        }
    }

    static class island {
        Condition waitingList;
        int adults;
        int child;
    }

    static void AdultItinerary(theboat aboat, island dst, island origin, Lock boatLock, island myloc) {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
        origin.adults--;
        aboat.weight = 2;
        aboat.passengers.sleep();
        aboat.loc = dst;
        aboat.clear();
        myloc = dst;
        bg.AdultRowToMolokai();

        dst.waitingList.wakeAll();
        dst.adults++;
        dst.waitingList.sleep();

        boatLock.release(); // boat is ready

    }

    static void ChildItinerary(theboat aboat, island dst, island origin, Lock boatLock, island myloc){
        if (myloc == origin){
            // origin logic
            if(aboat.weight ==0){
                // need one more
                origin.child--;
                aboat.weight=1;
                aboat.passengers.sleep();
                // this is the place when i wake up next time.
                myloc=dst;
                bg.ChildRideToMolokai();

                dst.child++;
                dst.waitingList.sleep();

                boatLock.release();

            }else if (aboat.weight == 1){
                // embark and depart
                origin.child--;
                aboat.weight=2;
                //aboat.passengers.sleep(); pilot cannot sleep
                aboat.loc=dst;
                aboat.clear();
                myloc=dst;
                bg.ChildRowToMolokai();

                dst.waitingList.wakeAll();
                dst.child++;
                dst.waitingList.sleep();

                boatLock.release();

            }

        }else if (myloc == dst){
            // dst logic
            if (origin.adults == 0 && origin.child == 0){
                aboat.gameover();
                // not release lock
            }else if (aboat.weight == 0){
                dst.child--;
                aboat.weight=1;
                // aboat.passengers.sleep(); pilot cannot sleep
                aboat.loc=origin;
                aboat.clear();
                myloc=origin;
                bg.ChildRowToOahu();

                origin.waitingList.wakeAll();
                origin.child++;
                origin.waitingList.sleep();

                boatLock.release();

            }else if (aboat.weight == 1){
                dst.waitingList.sleep();
                boatLock.release();

            }else {
                System.out.println("unforeseen situation");
            }

        }else {
            System.out.println("idk where i am.");
            boatLock.release();
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
