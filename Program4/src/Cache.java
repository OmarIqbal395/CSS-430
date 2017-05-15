import java.util.Vector;

/**
 * @author Thuan Tran
 * University of Washing Bothell
 * CSS 430 Program 4
 * Sunday May 14,2017
 */
public class Cache
{
    private Vector<Data> theCache;
    /**
     * Constructor for the Cache class that allow
     * how many block to create and the size of each block
     * @param blockSize Size of each block
     * @param cacheBlocks Number of block
     */
    public Cache(int blockSize, int cacheBlocks)
    {
        theCache = new Vector<Data>(cacheBlocks);
        for (int i = 0; i < cacheBlocks;i++)
        {
            Data newBlock = new Data();
            newBlock.blockNumber = i;
            newBlock.dirtyBit = false;
            newBlock.referenceBit = false;
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

    }

    /**
     * This method is used to write the data from the buffer into a block
     * @param blockID the block that we need to read data from
     * @param buffer the buffer that is used to hold the data
     * @return True if the data has been write from the buffer to the block
     */
    public synchronized boolean write(int blockID, byte buffer[])
    {

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

    private class Data
    {
        private int blockNumber;
        private boolean referenceBit;
        private boolean dirtyBit;
    }


}
