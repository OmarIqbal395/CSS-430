import java.util.Vector;

/**
 * @author Thuan Tran
 * University of Washington Bothell
 * CSS 430 Program 3
 * Date: May 6th, 2017.
 */
public class QueueNode

{
    private Vector<Integer> theQueue;

    /**
     * This is the default constructor that will create a new vector to hold
     * a number of thread with the same condition
     */
    public QueueNode()
    {
        theQueue = new Vector<>();
    }

    /**
     * This method is used to sleep a thread with a certain condition
     * @return an integer that is the thread id of the thread that has woken up the sleeping thread
     */
    public synchronized int sleep()
    {
        if (theQueue.size() ==0)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                SysLib.cout("Error in QueueNode sleep method");
            }
            // Get the first one
            return theQueue.remove(0);

        }
        return -1;
    }

    /**
     * This method is used to wake up a thread with its thread id
     * @param tid the thread id of the thread that need to be wake up
     */
    public synchronized void  wake(int tid)
    {
        theQueue.add(tid);
        notify();
    }
}
