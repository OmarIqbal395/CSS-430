import java.util.Date;

/**
 * Created by thuan on 06/05/17.
 */
public class Test3 extends Thread
{
    private int numberOfPairs;

    public Test3(String[] args)
    {
        numberOfPairs = Integer.parseInt(args[0]);
    }

    public void run()
    {
        long start = new Date().getTime();
        String[] first = SysLib.stringToArgs("TestThread3a");
        String[] second = SysLib.stringToArgs("TestThread3b");
        for (int i = 0; i < numberOfPairs; i++)
        {
            SysLib.exec(first);
            SysLib.exec(second);
        }

        // Wait for termination of twice the number of Pairs
        for (int i = 0; i < numberOfPairs * 2; i++)
        {
            SysLib.join();
        }


        long end = new Date().getTime();
        long result = end - start;
        SysLib.cout("The elapsed time is " + result + " ms" + "\n");
        SysLib.exit();


    }
}
