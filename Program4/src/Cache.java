import java.util.Vector;

/**
 * @author Thuan Tran
 * University of Washing Bothell
 * CSS 430 Program 4
 * Sunday May 14,2017
 */
public class Cache
{
    private int size;
    private Vector<Data> theCache;
    /**
     * Constructor for the Cache class that allow
     * how many block to create and the size of each block
     * @param blockSize Size of each block
     * @param cacheBlocks Number of block
     */
    public Cache(int blockSize, int cacheBlocks)
    {
        size = cacheBlocks;
        theCache = new Vector<Data>(size);
        for (int i = 0; i < size; i++)
        {
            // Adding null element. Meaning this place is available;
            Data newBlock = null;
            theCache.add(newBlock);

        }
    }

    /**
     * This method is used to read the data from a block into a buffer
     * @param blockID the block that we need to read data from
     * @param buffer the buffer that is used to hold the data
     * @return True if the data has been written to the buffer
     */
    public synchronized boolean read(int blockID, byte buffer[])
    {
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == blockID)
            {

                return true;
            }
        }
        return false;

    }

    /**
     * This method is used to write the data from the buffer into a block
     * @param blockID the block that we need to read data from
     * @param buffer the buffer that is used to hold the data
     * @return True if the data has been write from the buffer to the block
     */
    public synchronized boolean write(int blockID, byte buffer[])
    {
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == blockID)
            {

                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to sync
     */
    public void sync()
    {

    }

    /**
     * This method is used to flush all the data
     */
    public void flush()
    {

    }

    /**
     * This class is used to represent a block that hold the data
     * The block number (ID). And its bits to determine if it has updated or if it
     * is being accessed
     */
    private class Data
    {
        // Data, ID, being accessed and updated
        private byte[] buff = new byte[256];
        private int blockNumber;
        private boolean referenceBit;
        private boolean dirtyBit;
    }


}
