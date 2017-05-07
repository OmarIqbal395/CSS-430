/**
 * @author Thuan Tran
 *         University of Washington Bothell
 *         CSS 430 Program 3
 *         Date: May 6th, 2017
 */
public class SyncQueue
{
    public static int DEFAULTNUMBEROFNODE = 10;
    private QueueNode[] queue;

    /**
     * This is the default constructor for SyncQueue
     * It will generate a queue of QueueNode with 10 elements in it
     */
    public SyncQueue()
    {
        queue = new QueueNode[DEFAULTNUMBEROFNODE];
        initialize(DEFAULTNUMBEROFNODE);
    }

    /**
     * This is a constructor for SyncQueue that allow custom number of condition
     *
     * @param numberOfNode number of conditions we want to have
     */
    public SyncQueue(int numberOfNode)
    {
        queue = new QueueNode[numberOfNode];
        initialize(numberOfNode);

    }

    /**
     * This method is used to create new QueueNode in the queue array
     *
     * @param number the number of QueueNode to be created
     */
    private void initialize(int number)
    {
        for (int i = 0; i < number; i++)
        {
            queue[i] = new QueueNode();
        }
    }

    /**
     * This method is used to enqueue and sleep a thread based on a condition
     *
     * @param condition The condition that the thread will sleep
     * @return an integer that is the ID of the thread that has woken up the calling thread
     */
    public int enqueAndSleep(int condition)
    {

        return queue[condition].sleep();

    }

    /**
     * This default method is used to wake up a thread upon a condition
     *
     * @param condition the condition to wake up the thread
     */
    public void dequeueAndWakeup(int condition)
    {

        queue[condition].wake(0);

    }

    /**
     * This method is used to wake up a thread based upon a condition and the thread id
     *
     * @param condition the condition to wake up
     * @param tid       the id of the thread that we want to wake up
     */
    public void dequeueAndWakeup(int condition, int tid)
    {

        queue[condition].wake(tid);


    }


}
