import java.util.Random;

/**
 * @author Thuan Tran
 * University of Washington Bothell
 * CSS 430 Program 3
 * Date: May 7th, 2017
 */
public class TestThread3a extends Thread

{
    /**
     * This method is used to generate a random number and perform a loop that add 1 to
     * it 10 time
     * After the loop, it prints out a signal saying that the computation finished
     */
    public void run()
    {
        int random = new Random().nextInt(10);
        int initial = random;
        for (int i = 0; i < 10; i++)
        {
            random = random + 1;

        }
        SysLib.cout("After 10 loops, the number " + initial + " has become " + random + "\n");
        SysLib.exit();
    }
}
