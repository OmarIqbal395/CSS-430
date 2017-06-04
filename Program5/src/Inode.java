public class Inode
{
    private final static int iNodeSize = 32;
    private final static int directSize = 11;

    public int length;
    public short count;
    public short flag;
    public short direct[] = new short[directSize];
    public short indirect;
//-----------------------------------------------------------------
//Constructor methods below
//First constructor function creates a default inode that intializes 
//indirect and direct pointers to -1. 

    public Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < directSize; i++)
        {
            direct[i] = -1;
        }
        indirect = -1;
    }

    //2nd constructor function takes in inumber and creates an inode
//by pulling information from disk.	
    public Inode(short iNumber)
    {
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, data);
        int offset = (Number % 16) * 32;
        length = SysLib.bytes2int(data, offset);
        offset += 4;
        count = SysLib.bytes2short(data, offset);
        offset += 2;
        flag = SysLib.bytes2short(data, offset);
        offset += 2;

        for (short i = 0; i < directSize; i++)
        {
            short data = SysLib.bytes2short(data, offset);
            direct[i] = data;
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }


//-------------------------------------------------------------------

    /**
     * This method look for the block that the seek pointer is currently pointing too
     *
     * @param locationSeek The location of the pointer
     * @return an integer represent the location of the block
     */
    public int getBlockNumPointer(int locationSeek)
    {
        int offset = locationSeek / 512;
        // Still in the direct block of the iNode
        if (offset < 11)
        {
            return direct[offset];
        } else if (indirect == -1)
        {
            return -1;
        } else
        {
            byte[] tempData = new byte[512];
            // Get the number of blocks that the indirect block is pointing to
            SysLib.rawread(indirect, tempData);
            // How far we are going to ?
            int difference = offset - 11;
            // Since difference is in int, we need to transfer it back to bytes by multiplying 2
            return SysLib.bytes2short(tempData, difference * 2);
        }
    }


    //-------------------------------------------------------------------
//Moves from memory into disk using the inumber value
    public void toDisk(short iNumber)
    {
        if (iNumber < 0)
        {
            return;
        }
        // An inode only have 32 byte of info
        byte[] blockInfo = new byte[32];
        byte offset = 0;
        // Write the length, starting at 0
        SysLib.int2bytes(this.length, blockInfo, offset);
        int offsetForInt = offset + 4;
        // Write the count
        SysLib.short2bytes(this.count, blockInfo, offsetForInt);

        offsetForInt += 2;
        // Write the flag
        SysLib.short2bytes(this.flag, blockInfo, offsetForInt);
        offsetForInt += 2;

        // Now write back the block number that the direct pointer point to
        int pointerIndex;
        for (pointerIndex = 0; pointerIndex < directSize; pointerIndex++)
        {
            SysLib.short2bytes(direct[pointerIndex], blockInfo, offsetForInt);
            offsetForInt += 2;
        }
        // Write back the block number for the indirect pointer
        SysLib.short2bytes(this.indirect, blockInfo, offsetForInt);
        offsetForInt += 2;

        // Which Inode are we at given there are 16 inodes in 1 block
        pointerIndex = 1 + iNumber / 16;
        byte[] tempData = new byte[Disk.blockSize];
        SysLib.rawread(pointerIndex, tempData);
        offsetForInt = iNumber % 16 * iNodeSize;
        // Now write back the data into the disk given the location
        System.arraycopy(blockInfo, 0, tempData, offsetForInt, iNodeSize);
        SysLib.rawwrite(pointerIndex, tempData);


    }

    public boolean registerIndexBlock(short var1)
    {
        for (int var2 = 0; var2 < 11; ++var2)
        {
            if (this.direct[var2] == -1)
            {
                return false;
            }
        }

        if (this.indirect != -1)
        {
            return false;
        } else
        {
            this.indirect = var1;
            byte[] var4 = new byte[512];

            for (int var3 = 0; var3 < 256; ++var3)
            {
                SysLib.short2bytes(-1, var4, var3 * 2);
            }

            SysLib.rawwrite(var1, var4);
            return true;
        }
    }
}
