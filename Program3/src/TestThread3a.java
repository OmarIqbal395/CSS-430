import java.util.Random;

/**
 * Created by thuan on 06/05/17.
 */
public class TestThread3a extends Thread

{
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
