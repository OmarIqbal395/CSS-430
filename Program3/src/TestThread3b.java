/**
 * Created by thuan on 06/05/17.
 */
public class TestThread3b extends Thread
{
    private byte[] block;

    public void run()
    {
        block = new byte[512];
        for (int i = 0; i < 1000; i++)
        {
            SysLib.rawwrite(i, block);
            SysLib.rawread(i, block);

        }
        SysLib.cout("Done writing and reading" + "\n");
        SysLib.exit();
    }
}
