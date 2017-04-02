/*
 * @author Thuan Tran
 * Date : April 2nd, 2017
 * Program 1 Part 1: Implement  " ps -A | grep argv[1] | wc -l " using processes
 */


#include <unistd.h>    //for fork, pipe
#include <stdlib.h>    //for exit
#include <iostream>
#include <sys/wait.h>

using namespace std;


int main(int argc , char *argv[])
{
    // I used to put the ps statement at the else clause of the first fork and return 0 when the process finish
    // That did not work out since it did not execute the other command
    // Hence instead I decided to use a seperate wait for all other child process
    int status; // This is used to indicate the ending of this current program

    int fileD1[1]; // pipe for ps
    int fileD2[2]; // pipe for grep
    int pid1;
    pid1 = fork(); // Create a child process and wait ( last else statement)
    if ( pipe(fileD1) != 0 || pipe(fileD2) != 0 )
    {
        cerr << "Can not create the pipe";
    }
    if ( pid1 < 0 )
    {
        cerr << "Error: First Fork Failed" << endl;
        _exit(EXIT_FAILURE);
    }
    else if ( pid1 == 0 )
    {
        pid1 = fork(); // Create another child
        if ( pid1 < 0 )
        {
            cerr << "Error: Second Fork Failed" << endl;
            _exit(EXIT_FAILURE);
        }
        else if ( pid1 == 0 )
        {
            pid1 = fork();
            if ( pid1 < 0 )
            {
                cerr << "Error: Third Fork Failed " << endl;
            }
            else if ( pid1 == 0 )
            {
                // The grand child of the parent way below
                close(fileD1[ 0 ]); // We are done with the 1st pipe
                close(fileD1[ 1 ]);
                close(fileD2[ 1 ]);// close write of the second pipe
                dup2(fileD2[ 0 ] , 0);
                int rc = execlp("wc" , "wc" , "-l" ,
                                (char *) 0); // Now we read the info that was written into the second pipe below
                if ( rc == -1 )
                {
                    cerr << "Error on excelp at wc -l" << endl;
                }
                //         wait(NULL);
            }
            else
            {   // The child of the parent below
                close(fileD1[ 1 ]); // close write
                close(fileD2[ 0 ]); // close read
                dup2(fileD2[ 1 ] , 1); // open write to second pipe
                dup2(fileD1[ 0 ] , 0); // open read the information that we written to the first pipe below

                int rc = execlp("grep" , "grep" , argv[ 1 ] ,
                                (char *) 0); // Now this statement read the info we write below and write to second pipe
                if ( rc == -1 )
                {
                    cerr << "Error on excelp at grep" << endl;
                }
                //  wait(NULL);
            }
        }
        else
        {
            // The First Parent to execute
            close(fileD1[ 0 ]); // close read at first pipe
            close(fileD2[ 0 ]); // close write at second pipe
            close(fileD2[ 1 ]); //close write at second pipe
            dup2(fileD1[ 1 ] , 1); // Write to the first pipe
            int rc = execlp("ps" , "ps" , "-A" , (char *) 0);
            if ( rc == -1 )
            {
                cerr << "Error on execlp at ps" << endl;
            }
        }
    }
    else
    {
        // All other processes were finished
        wait(&status);
    }
    return 0;
}