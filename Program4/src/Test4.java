import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
/**
 * @author Thuan Tran
 *         University of Washington Bothell
 *         CSS 430 Program 4
 *         Monday May 15th, 2017
 */
public class Test4 extends Thread
{
    private int size;
    private int numberOfLoop;
    private boolean useCache;
    private int whichTest;
    private ArrayList<Long> averageTime;

    public Test4(String args[])
    {
        // Assume that the user only type either enabled or disabled
        useCache = args[0].equals("enabled");
        averageTime = new ArrayList<>();
        whichTest = Integer.parseInt(args[1]);
        size = 512;
        numberOfLoop = 5;
    }

    public void randomAccess()
    {

        byte[] randomData = new byte[size];
        // Fill the array with random bytes
        new Random().nextBytes(randomData);
        // write the data
        ArrayList<Integer> index = new ArrayList<>();

        for (int i = 0; i < numberOfLoop; i++)
        {
            // Generate random index from 1 to 512
            int randomIndex = new Random().nextInt(512) + 1;
            index.add(randomIndex);
            long startTime = new Date().getTime();
            if (useCache)
            {
                SysLib.cwrite(randomIndex, randomData);
            } else
            {
                SysLib.rawwrite(randomIndex, randomData);
            }
            long endTime = new Date().getTime();

        }

        byte[] empty = new byte[size];
        for (int i = 0; i < numberOfLoop; i++)
        {
            int indexToAccess = index.get(i);
            if (useCache)
            {
                SysLib.cread(indexToAccess, empty);
            } else
            {
                SysLib.rawread(indexToAccess, empty);
            }
        }


    }

    public void localizedAccess()
    {

    }

    public void mixedAccess()
    {

    }

    public void adversaryAccess()
    {

    }

    public void run()
    {
        SysLib.flush();
        switch (whichTest)
        {
            case 1:
                randomAccess();
                break;
            case 2:
                localizedAccess();
                break;
            case 3:
                mixedAccess();
                break;
            case 4:
                adversaryAccess();
                break;
            default:
                SysLib.cout("Invalid test to test");
                break;
        }
    }
}
