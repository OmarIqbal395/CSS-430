/**
 * @author Thuan Tran
 * University of Washington Bothell
 * CSS 430 Program 3
 * Date: May 7th,2017
 */
public class TestThread3b extends Thread
{

    /**
     * This method is used to read the data from the block into a byte array
     * And print out a message indicating that it is done
     */
    public void run()
    {

        byte[] block = new byte[512];
        for (int i = 0; i < 300; i++)
        {
            SysLib.rawread(i, block);
        }
        SysLib.cout("Done writing and reading" + "\n");
        SysLib.exit();
    }
}
