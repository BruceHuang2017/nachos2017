package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {

    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        if(speaking){
            speakLine++;
            speak.sleep();
            speakLine--;
        }
        speaking = true;
        if(listening){
            connect.wake();
        }else {
            connect.sleep();
        }
        speak.wake();
        this.word=word;
        speaking = false;
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        lock.acquire();
        if(listening){
            listenLine++;
            listen.sleep();
            listenLine--;
        }
        listening = true;
        if(speaking){
            connect.wake();
        }else{
            connect.sleep();
        }
        listen.wake();
        listening = false;
        lock.release();
        return word;
    }

    private int word = 0;
    private Lock lock;
    private Condition speak = new Condition(lock);
    private Condition listen = new Condition(lock);
    private Condition connect = new Condition(lock);
    boolean listening = false;
    boolean speaking = false;
    private int listenLine = 0;
    private int speakLine = 0;

}
