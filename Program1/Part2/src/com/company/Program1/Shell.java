

public class Shell extends Thread
{

	public Shell ( )
	{

	}

	public void run ( )
	{
		int status = 1;
		while ( true )
		{
			StringBuffer input = new StringBuffer();


			String command = "";
			SysLib.cout ( "shell[" + status + "]% " );
			SysLib.cin ( input );
			status++;
			command = new String(input);
			String[] result = SysLib.stringToArgs ( command );
			if ( result.length == 1 && result[ 0 ].equals ( "exit" ) )
			{
				break;
			} else
			{
				if ( result.length < 1 )
				{
					continue;
				}
				String[] parsed = command.split ( "&" );
				for ( String s : parsed )
				{
					handle ( s );
				}
				int counter = 0;
				while ( counter < parsed.length)
				{
					SysLib.join();
				}
			}



		}
		SysLib.cout ( "Out of the Shell" + "\n");
		SysLib.exit ( );
	}


	public void handle ( String command )
	{

		{
			int i = 0;
			String[] s = {command};
			SysLib.cout ("" + command + "\n" );
			//String[] result = SysLib.stringToArgs ( s[0] );
			String[] concurrent = s[0].split(";");
			SysLib.cout (" " + concurrent.length+ "\n");
			for (String a: concurrent)
			{
				SysLib.cout ( a + "\n" );

			}
			if (concurrent.length ==1)
			{
				SysLib.cout ( concurrent[0] + " We are here " + "\n");
				int k = SysLib.exec ( SysLib.stringToArgs ( command ) );

				return;
			}
			i = 0;
			while (i < concurrent.length)
			{
				String execute = concurrent[i];
				String[] passedIn = {execute};

				int id = SysLib.exec (  SysLib.stringToArgs ( execute ));
				while(true)
				{
					int afterExecute = SysLib.join ();
					if (afterExecute == id)
					{
						break;
					}
				}
			}
			return;


		}

	}


}
