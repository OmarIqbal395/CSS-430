import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class DateClient {
    public DateClient() {
    }

    public static void main(String[] args) {

            try {
                Socket sock = new Socket("127.0.0.1", 6013);
                // Get the date that the server sent back
                InputStream in = sock.getInputStream();
                BufferedReader bin = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = bin.readLine()) != null) {
                    System.out.println(line);
                }

                sock.close();
            } catch (IOException var5) {
                System.err.println(var5);
            }


    }
}