import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
/**
 * @author Thuan Tran
 *         University of Washington Bothell
 *         CSS 430 Program 4
 *         Monday May 15th, 2017
 */


// This class is a class that test the cache by using different methods to read and write
public class Test4 extends Thread
{
    private final int size = 512; // Size of the buffer
    private int numberOfLoop; // How many time to read and write in certain test
    private boolean useCache; // First argument when passed in
    private int whichTest; // Second Argument when passed in

    private long readTime; // average read and write time
    private long writeTime;

    public Test4(String args[])
    {
        // Assume that the user only type either enabled or disabled
        useCache = args[0].equals("enabled");

        whichTest = Integer.parseInt(args[1]);

        readTime = 0;
        writeTime = 0;

        numberOfLoop = 250;
    }

    /**
     * This method is used to sync all of the data either from the cache or disk
     */
    public void sync()
    {
        if (useCache)
        {
            SysLib.csync();
        } else
        {
            SysLib.sync();
        }
    }

    /**
     * This is a helper read method to either read from the cache or disk into the
     * byte array
     *
     * @param randomIndex the Index of the block to read from
     * @param randomData  the byte array to read into
     */
    private void helperRead(int randomIndex, byte[] randomData)
    {
        if (useCache)
        {
            SysLib.cread(randomIndex, randomData);
        } else
        {
            SysLib.rawread(randomIndex, randomData);
        }
    }

    /**
     * This is a helper write method to write to the cache or to the disk
     *
     * @param randomIndex the index of the block to write to
     * @param randomData  The byte array to write from
     */
    private void helperWrite(int randomIndex, byte[] randomData)
    {
        if (useCache)
        {
            SysLib.cwrite(randomIndex, randomData);
        } else
        {
            SysLib.rawwrite(randomIndex, randomData);
        }
    }

    /**
     * This is a method to output the average time for read and write
     */
    public void averageTime()
    {
        String withCache = useCache ? "Cache on" : "Cache off";
        SysLib.cout("This test class test for " + whichTest + "th test with " + withCache + "\n");
        SysLib.cout("The average read time is  " + readTime + "\n");
        SysLib.cout("The average write time is " + writeTime + "\n");
    }

    /**
     * This is a method to test for random access by using a random generator
     * It will generate random block index to read and write from
     */
    public void randomAccess()
    {
        byte[] randomData = new byte[size];
        // Fill the array with random bytes
        new Random().nextBytes(randomData);
        // write the data
        // Save the index that we used
        ArrayList<Integer> index = new ArrayList<>();
        long startTime = new Date().getTime();
        for (int i = 0; i < numberOfLoop; i++)
        {
            // Generate random index from 1 to 512
            int randomIndex = new Random().nextInt(512) + 1;
            index.add(randomIndex);

            // help write to random block
            helperWrite(randomIndex, randomData);

        }
        // Calculate the time
        long endTime = new Date().getTime();
        long elapsed = endTime - startTime;
        writeTime = elapsed / numberOfLoop;


        byte[] empty = new byte[size];
        startTime = new Date().getTime();
        for (int i = 0; i < numberOfLoop; i++)
        {
            // Get the index that we just accessed above
            int indexToAccess = index.get(i);
            // help read from random block
            helperRead(indexToAccess,empty);

        }
        // Check to see if the data are equals
        if (!Arrays.equals(randomData, empty))
        {
            SysLib.cout("read and write are different " + "\n");
        }
        endTime = new Date().getTime();
        elapsed = endTime - startTime;
        // Get average time
        readTime = elapsed / numberOfLoop;
        averageTime();

    }

    /**
     * This is a method to test for localized access using the cache
     * It will access 10 blocks that are sequentially nearby
     */
    public void localizedAccess()
    {
        byte[] randomData = new byte[size];
        // Fill the array with random bytes
        new Random().nextBytes(randomData);
        // write the data

        ArrayList<Integer> index = new ArrayList<>();
        long startTime = new Date().getTime();
        // I decided to choose one of the number from 1 to 100

        for (int i = 0; i < numberOfLoop; i++)
        {
            // Repeatedly access 10 nearby blocks
            for (int j = 0; j < 10; j++)
            {
                // help write to random block
                helperWrite(j, randomData);
            }
        }
        long endTime = new Date().getTime();
        long elapsed = endTime - startTime;
        writeTime = elapsed / 10;

        byte[] empty = new byte[size];
        startTime = new Date().getTime();
        // Only access 10 nearby block only
        for (int i = 0; i < numberOfLoop; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                // help read from random block
                helperRead(j, empty);
            }
        }
        // Same like above. Calculate average time and check to see if they are equal
        endTime = new Date().getTime();
        elapsed = endTime - startTime;
        readTime = elapsed / 10;
        if (!Arrays.equals(randomData, empty))
        {
            SysLib.cout("read and write are different " + "\n");
        }
        averageTime();
    }

    /**
     * This method test for mixed access using the cache
     * It will use 90% of its access for localized access. The 10% will be random
     */
    public void mixedAccess()
    {
        byte[] randomData = new byte[size];
        // Fill the array with random bytes
        new Random().nextBytes(randomData);
        // write the data

        ArrayList<Integer> index = new ArrayList<>();
        long startTime = new Date().getTime();
        int randomIndex;
        // 90% will be localized => 18 out of 20, 10% will be random => 2
        for (int i = 0; i < numberOfLoop; i++)
        {
            // remaining time is for random access
            if (i >= numberOfLoop * 0.9)
            {
                randomIndex = new Random().nextInt(512) + 1;
            } else
            {
                randomIndex = new Random().nextInt(10) + 1;
            }

            index.add(randomIndex);

            // help write to random block
            helperWrite(randomIndex, randomData);


        }
        long endTime = new Date().getTime();
        long elapsed = endTime - startTime;
        writeTime = elapsed / 20;

        byte[] empty = new byte[size];
        startTime = new Date().getTime();
        // Access the previous ones
        for (int i = 0; i < numberOfLoop; i++)
        {
            // Get the index that we just accessed above
            int indexToAccess = index.get(i);
            // help read from random block
            helperRead(indexToAccess, empty);

        }
        endTime = new Date().getTime();
        elapsed = endTime - startTime;
        readTime = elapsed / 20;
        if (!Arrays.equals(randomData, empty))
        {
            SysLib.cout("read and write are different " + "\n");
        }
        averageTime();
    }

    /**
     * This method test for adversary access where it will access the block that
     * This is to show that this test does not make use of the cache
     */
    public void adversaryAccess()
    {
        byte[] randomData = new byte[size];
        // Fill the array with random bytes
        new Random().nextBytes(randomData);
        long startTime = new Date().getTime();
        for (int i = 1; i < numberOfLoop; i = i + 2)
        {
            helperWrite(i, randomData);
        }
        long endTime = new Date().getTime();
        long elapsed = endTime - startTime;
        writeTime = elapsed / numberOfLoop;
        byte[] empty = new byte[size];
        startTime = new Date().getTime();
        for (int i = 1; i < numberOfLoop; i = i + 2)
        {
            helperRead(i, empty);
        }
        endTime = new Date().getTime();
        elapsed = endTime - startTime;
        readTime = elapsed / numberOfLoop;
        if (!Arrays.equals(randomData, empty))
        {
            SysLib.cout("read and write are different " + "\n");
        }
        averageTime();
    }

    /**
     * Run method for the test. Depending on the input argument, it will call
     * different test methods
     */
    public void run()
    {
        // Clear the data first
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
        sync();
        SysLib.exit();

    }
}
