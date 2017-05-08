import java.util.Date;

/**
 * @author Thuan Tran
 * University of Washington Bothell
 * CSS 430 Program 3
 * Date: May 6th, 2017
 */
public class Test3 extends Thread
{
    private int numberOfPairs;

    /**
     * Constructor for Test3 that generate X pairs of threads
     * @param args the input where args[0] is the X
     */
    public Test3(String[] args)
    {
        numberOfPairs = Integer.parseInt(args[0]);
    }

    /**
     * Run method to execute TestThread3a and TestThread3b
     */
    public void run()
    {
        // Get the starting time
        long start = new Date().getTime();
        String[] first = SysLib.stringToArgs("TestThread3a");
        String[] second = SysLib.stringToArgs("TestThread3b");
        for (int i = 0; i < numberOfPairs; i++)
        {
            // Execute 3a and 3b
            SysLib.exec(first);
            SysLib.exec(second);
        }

        // Wait for termination of twice the number of Pairs
        for (int i = 0; i < numberOfPairs * 2; i++)
        {
            SysLib.join();
        }


        long end = new Date().getTime();
        // Get the elapsed time
        long result = end - start;
        SysLib.cout("The elapsed time is " + result + " ms" + "\n");
        SysLib.exit();


    }
}
