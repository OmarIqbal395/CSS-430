import java.util.Vector;

public class Scheduler extends Thread
{
    private static final int DEFAULT_MAX_THREADS = 10000;
    private static final int DEFAULT_TIME_SLICE = 1000;

    private Vector firstQueue; // The queue with timeSlice / 2 quantum
    private Vector secondQueue;// The queue with timeSlice quantum time
    private Vector thirdQueue; // The queue with timeSlice * 2 quantum time
    private int timeSlice;
    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;

    /**
     * Default Constructor for the Scheduler
     * Set time quantum to default value (1000 ms )
     * Set maximum threads can hold to 10000
     *
     * @param none
     * @return none
     */
    public Scheduler()
    {
        timeSlice = DEFAULT_TIME_SLICE;
        firstQueue = new Vector();
        secondQueue = new Vector();
        thirdQueue = new Vector();
        initTid(DEFAULT_MAX_THREADS);
    }

    /**
     * Constructor that allow custom time quantum
     *
     * @param quantum the time quantum
     * @return none
     */
    public Scheduler(int quantum)
    {
        //timeSlice = quantum;
        //firstQueue = new Vector();
        //nitTid(DEFAULT_MAX_THREADS);
        this();
        timeSlice = quantum;

    }

    /**
     * A constructor that allow custom time quantum and custom maximum threads
     *
     * @param quantum    the Time quantum
     * @param maxThreads maximum number of threads
     * @return none
     */
    public Scheduler(int quantum, int maxThreads)
    {
        timeSlice = quantum;
        firstQueue = new Vector();
        secondQueue = new Vector();
        thirdQueue = new Vector();
        initTid(maxThreads);
    }

    /**
     * This method is used to initialize the tids[] array
     * The tids[] array is like a flag that indicate if the thread is working or not
     *
     * @param maxThreads How many maximum threads The array can hold
     * @return none
     */
    private void initTid(int maxThreads)
    {
        tids = new boolean[maxThreads];
        for (int i = 0; i < maxThreads; i++)
            tids[i] = false;
    }

    /**
     * This method is used to search an available threadID and return it
     *
     * @param none
     * @return int the index of the available thread in the tids[] array
     */
    private int getNewTid()
    {
        for (int i = 0; i < tids.length; i++)
        {
            int tentative = (nextId + i) % tids.length;
            if (tids[tentative] == false)
            {
                tids[tentative] = true;
                nextId = (tentative + 1) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    /**
     * Return the thread ID and set the corresponding tids element to be unused
     *
     * @param tid the thread identifier (in this case the index) of tids[]
     * @return a boolean if the thread has been set to false
     */


    private boolean returnTid(int tid)
    {
        if (tid >= 0 && tid < tids.length && tids[tid] == true)
        {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    /**
     * This method is used to get the Thread Control Block of the thread that is using
     *
     * @param none
     * @return a Thread Control Block that has the current Thread
     */
    public TCB getMyTcb()
    {
        Thread myThread = Thread.currentThread(); // Get my thread object
        // Search through the first Queue
        synchronized (firstQueue)
        {
            for (int i = 0; i < firstQueue.size(); i++)
            {
                TCB tcb = (TCB) firstQueue.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread) // if this is my TCB, return it
                    return tcb;
            }
        }
        // Search through the second queue
        synchronized (secondQueue)
        {
            for (int i = 0; i < secondQueue.size(); i++)
            {
                TCB tcb = (TCB) secondQueue.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread)
                {
                    return tcb;
                }
            }

        }
        // Search through the third queue
        synchronized (thirdQueue)
        {
            for (int i = 0; i < thirdQueue.size(); i++)
            {
                TCB tcb = (TCB) thirdQueue.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread)
                {
                    return tcb;
                }
            }
        }
        return null;
    }

    /**
     * This method is used to query how many threads can the Scheduler hold
     *
     * @param none
     * @return an integer denote how many threads it can hold
     */
    public int getMaxThreads()
    {
        return tids.length;
    }

    private void schedulerSleep()
    {
        try
        {
            sleep(timeSlice);
        } catch (InterruptedException e)
        {
        }
    }

    /**
     * This method is used to add the thread control block to the first queue for later execution
     *
     * @param t The thread that is going to be added
     * @return a new Thread Control block with the thread id, its parent thread
     */
    public TCB addThread(Thread t)
    {
        t.setPriority(2);
        TCB parentTcb = getMyTcb(); // get my TCB and find my TID
        int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
        int tid = getNewTid(); // get a new TID
        if (tid == -1)
            return null;
        TCB tcb = new TCB(t, tid, pid); // create a new TCB
        firstQueue.add(tcb);
        return tcb;
    }

    /**
     * Get the current Thread Control Block that is running and terminate it
     *
     * @param none
     * @return a boolean value indicate the if the Thread Control Block has been terminated
     */
    public boolean deleteThread()
    {
        TCB tcb = getMyTcb();
        if (tcb != null)
            return tcb.setTerminated();
        else
            return false;
    }

    /**
     * Sleep the scheduler for a certain amount of milliseconds
     *
     * @param milliseconds the duration to sleep
     * @return none
     */
    public void sleepThread(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e)
        {
        }
    }

    /**
     * This method is used to execute all the TCB in the First Queue
     * If within the time quantum it does not able to execute them, then the TCB will be move to the second queue
     *
     * @param none
     * @none none
     */
    private synchronized void executeFirstQueue()
    {
        Thread current = null;
        if (firstQueue.size() != 0)
        {
            while (firstQueue.size() > 0)
            {
                // Get the current TCB and check to see if its threads are ok
                TCB currentTCB = (TCB) firstQueue.firstElement();
                if (currentTCB.getTerminated() == true)
                {
                    firstQueue.remove(currentTCB);
                    returnTid(currentTCB.getTid());
                    continue;
                }
                current = currentTCB.getThread();
                if (current != null)
                {
                    current.resume();
                    if (current.isAlive())
                    {
                        current.setPriority(4);
                    } else
                    {
                        {
                            current.start();
                            current.setPriority(4);
                        }
                    }
                } else
                {
                    continue;
                }
                // Let the Scheduler sleep for that amount of time while the thread does its work
                sleepThread(timeSlice / 2);
                synchronized (firstQueue)
                {
                    if (current != null && current.isAlive())
                    {
                        // At this time, the thread has not finish even though the time has passed
                        // Move it to another queue
                        current.suspend();
                        current.setPriority(2);
                    }
                    firstQueue.remove(currentTCB);
                    secondQueue.add(currentTCB);
                }
            }
        }
    }

    /**
     * This method is used to execute all the threads in the second queue
     * During its execution, it will also check for the first queue and stop the threads if the first Queue need to be
     * executed
     *
     * @param none
     * @return none
     */
    private synchronized void executeSecondQueue()
    {
        // Same like executeFirstQueue(): get the TCB, check if it is ok, execute it...
        Thread current = null;
        if (secondQueue.size() != 0)
        {
            while (secondQueue.size() > 0)
            {
                TCB currentTCB = (TCB) secondQueue.firstElement();
                if (currentTCB.getTerminated() == true)
                {
                    secondQueue.remove(currentTCB);
                    returnTid(currentTCB.getTid());
                    continue;
                }
                current = currentTCB.getThread();
                if (current != null)
                {
                    current.resume();
                    if (current.isAlive())
                    {
                        current.resume();
                        current.setPriority(4);
                    } else
                    {

                        current.start();
                        current.setPriority(4);

                    }
                } else
                {
                    continue;
                }
                // First time to sleep
                sleepThread(timeSlice / 2);

                if (firstQueue.size() != 0)
                {
                    // Some threads just arrived at first Queue, work on first Queue first
                    current.suspend();
                    executeFirstQueue();
                }
                current.resume();
                // Do the remaining time quantum
                sleepThread(timeSlice / 2);
                synchronized (secondQueue)
                {
                    if (current != null && current.isAlive())
                    {
                        // Not yet finish, go to third queue
                        current.suspend();
                        current.setPriority(2);
                    }
                    secondQueue.remove(currentTCB);
                    thirdQueue.add(currentTCB);
                }

            }
        }
    }

    /**
     * This method is used to execute the elements in the Third Queue
     * Kinda like the previous two, now it will check both the first and second queue
     *
     * @param none
     * @return none
     */
    private synchronized void executeThirdQueue()
    {
        Thread current = null;
        if (thirdQueue.size() != 0)
        {
            while (thirdQueue.size() > 0)
            {
                TCB currentTCB = (TCB) thirdQueue.firstElement();
                if (currentTCB.getTerminated() == true)
                {
                    thirdQueue.remove(currentTCB);
                    returnTid(currentTCB.getTid());
                    continue;
                }
                current = currentTCB.getThread();
                if (current != null)
                {
                    current.resume();
                    if (current.isAlive())
                    {
                        current.resume();
                        current.setPriority(4);
                    } else
                    {
                        current.start();
                        current.setPriority(4);

                    }
                } else
                {
                    continue;
                }
                // It will have 4 times the quantum size of the first queue and at each iteration,
                // At each iteration, the Scheduler will sleep 1/4 time
                int numberOfCounter = 1;
                while (numberOfCounter <= 4)
                {

                    sleepThread(timeSlice / 2);

                    // Check to see if firstQueue and secondQueue are clear
                    if (firstQueue.size() != 0 || secondQueue.size() != 0)
                    {
                        current.suspend();
                        executeFirstQueue();
                        executeSecondQueue();
                        current.resume();
                    }
                    numberOfCounter++;
                }
                synchronized (thirdQueue)
                {
                    if (current != null && current.isAlive())
                    {
                        // Not yet finished, move to the tail of the Queue
                        current.suspend();
                        current.setPriority(2);
                    }
                    thirdQueue.remove(currentTCB); // rotate this TCB to the end
                    thirdQueue.add(currentTCB);
                }
            }
        }
    }

    /**
     * This method is used to run the Scheduler
     *
     * @param none
     * @return none
     */
    public void run()
    {
        this.setPriority(6);

        while (true)
        {
            // Execute all three queue
            try
            {
                executeFirstQueue();
                executeSecondQueue();
                executeThirdQueue();

            } catch (NullPointerException e3)
            {
            }
        }
    }
}
