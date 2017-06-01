import java.util.Vector;

/**
 * @author Thuan Tran
 * CSS 430 Final Project
 * May 31th, 2017
 */
public class FileTable {

    private Vector table;         // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable( Directory directory ) { // constructor
        table = new Vector( );     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods





    public synchronized FileTableEntry falloc( String filename, String mode )
    {
        Inode newInode = null;

        // Apple product, must have costs a ton
        short iNumber = 0;
        while(true) {

                iNumber = dir.namei(filename);


            if(iNumber >= 0) {
                newInode = new Inode(iNumber);
                // If we are trying to read the file and something is happening on the file
                if(mode.equals("r") )
                {

                        if (newInode.flag != 0 && newInode.flag != 1)
                        {
                            try
                            {
                            wait();
                        }
                    catch(InterruptedException something)
                        {
                            SysLib.cout("Something bad happen")
                        }
                    }
                    // Change the status of the file to being used
                    newInode.flag = 1;
                    break;
                }

                if(newInode.flag != 0 && newInode.flag != 3)
                {
                    if(newInode.flag == 1 || newInode.flag == 2)
                    {
                        newInode.flag = (short)(var4.flag + 3);
                        newInode.toDisk(var3);
                    }

                    try {
                        this.wait();
                    } catch (InterruptedException var6) {
                        ;
                    }
                    continue;
                }

                var4.flag = 2;
                break;
            }

            if(var2.compareTo("r") == 0) {
                return null;
            }

            var3 = this.dir.ialloc(var1);
            var4 = new Inode();
            var4.flag = 2;
            break;
        }

        ++var4.count;
        var4.toDisk(var3);
        FileTableEntry var5 = new FileTableEntry(var4, var3, var2);
        this.table.addElement(var5);
        return var5;





        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry
    }

    public synchronized boolean ffree( FileTableEntry e ) {



        // receive a file table entry reference
        // save the corresponding inode to the disk
        // free this file table entry.
        // return true if this file table entry found in my table
    }

    public synchronized boolean fempty( ) {
        return table.isEmpty( );  // return if table is empty
    }                            // should be called before starting a format
}