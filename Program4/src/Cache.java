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
    private int pointerIndex;
    /**
     * Constructor for the Cache class that allow
     * how many block to create and the size of each block
     *
     * @param blockSize   Size of each block
     * @param cacheBlocks Number of block
     */
    public Cache(int blockSize, int cacheBlocks)
    {
        pointerIndex = 0;
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
     *
     * @param blockID the block that we need to read data from
     * @param buffer  the buffer that is used to hold the data
     * @return True if the data has been written to the buffer
     */
    public synchronized boolean read(int blockID, byte buffer[])
    {
        // Find coresponding block that has the same block ID
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == blockID)
            {
                System.arraycopy(theBlock.buff, 0, buffer, 0, size);
                theBlock.referenceBit = true;
                return true;
            }

        }
        // If didn't find, fine the one that is empty
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == -1)
            {
                SysLib.rawread(blockID, theBlock.buff);
                System.arraycopy(theBlock.buff, 0, buffer, 0, size);
                theBlock.referenceBit = true;
                theBlock.blockNumber = blockID;
                return true;
            }
        }
        // Find the next victim
        int victim = findNextVictim();
        // Update to the disk and read from the disk to the cache
        // Replace that block by the block in the parameter
        SysLib.rawwrite(theCache.get(victim).blockNumber, theCache.get(victim).buff);
        theCache.get(victim).dirtyBit = false; // Reset the dirty bit to false
        SysLib.rawread(blockID, theCache.get(victim).buff);
        theCache.get(victim).referenceBit = true; // Just been used
        theCache.get(victim).blockNumber = blockID; // Update the block
        return true;
        }


    /**
     * This method is used to write the data from the buffer into a block
     *
     * @param blockID the block that we need to read data from
     * @param buffer  the buffer that is used to hold the data
     * @return True if the data has been write from the buffer to the block
     */
    public synchronized boolean write(int blockID, byte buffer[])
    {


        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == blockID)
            {
                System.arraycopy(buffer, 0, theBlock.buff, 0, size);
                theBlock.referenceBit = true;
                return true;
            }
        }   // If didn't find, fine the one that is empty
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == -1)
            {

                System.arraycopy(theBlock.buff, 0, buffer, 0, size);
                theBlock.referenceBit = true;
                theBlock.blockNumber = blockID;
                theBlock.dirtyBit = true;
                return true;
            }
        }
        int victim = findNextVictim();
        Data theBlock = theCache.get(victim);
        if (theBlock.dirtyBit)
        {
            SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
        }
        System.arraycopy(buffer, 0, theBlock.buff, 0, size);
        theBlock.blockNumber = blockID;
        theBlock.referenceBit = true;
        theBlock.dirtyBit = true;




        return false;


    }

    /**
     * This method is used to sync
     */
    public synchronized void sync()
    {

        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.dirtyBit)
            {
                SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
                theBlock.dirtyBit = false;
            }

        }
        SysLib.sync();

    }
    /**
     * This method is used to flush all the data
     */
    public synchronized void flush()
    {
        for (int i = 0; i < size; i++)

        {
            Data theBlock = theCache.get(i);
            if (theBlock.dirtyBit)
            {
                SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
                theBlock.dirtyBit = false;

            }
            theBlock.referenceBit = false;
            theBlock.blockNumber = -1;


        }
        SysLib.sync();
    }

    /**
     * This algorithm find the next victim
     * @return
     */
    private int findNextVictim()
    {
        int count = pointerIndex;
        while (true)
        {
            pointerIndex = (pointerIndex + 1) % theCache.size()
            if (theCache.get(pointerIndex).referenceBit == false)
            {
                return pointerIndex;
            }

            theCache.get(pointerIndex).referenceBit = false;


        }
    }

    /**
     * This clas theBlock.dirtyBit = false;                              s is used to represent a block that hold the data
     * The block number (ID). And its bits to determine if it has updated or if it
     * is being accessed
     */
    private class Data
    {
        // Data, ID, being accessed and updated
        private byte[] buff = new byte[512];
        private int blockNumber;
        private boolean referenceBit = false;
        private boolean dirtyBit = false;
    }
}
