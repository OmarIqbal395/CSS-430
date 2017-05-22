import java.util.Arrays;

/**
 * Created by thuan on 21/05/17.
 */
public class Test
{
    public static void main(String[] args)
    {
        System.out.println(solution('abcd aabc bd','aaa aa'));
    }

    public static int[] solution(String A, String B)
    {
        // Split the string by using regular expression into an array
        String[] firstString = A.split("\\s+");
        String[] secondString = B.split("\\s+");
        // We are only interested in String B compare to String A
        int[] returnVal = new int[secondString.length];

        for (int i = 0; i < secondString.length; i++)
        {
            // Default value;
            returnVal[i] = 0;
            // Sort each String in the second String and use it to compare
            String eachValue = secondString[i];
            char[] charArray = eachValue.toCharArray();
            Arrays.sort(charArray);
            String sortedString = new String(charArray);

            // need to be bigger than this one
            int frequencyOfSmallest = 0;

            char smallest = sortedString.charAt(0);
            for (int count = 0; count < sortedString.length(); count++)
            {
                if (sortedString.charAt(count) != smallest)
                {
                    break;
                }
                frequencyOfSmallest++;

            }

            for (int j = 0; j < firstString.length; j++)
            {
                eachValue = firstString[i];
                charArray = eachValue.toCharArray();
                Arrays.sort(charArray);
                sortedString = new String(charArray);
                int fre = 0;
                for (int count = 0; count < sortedString.length(); count++)
                {
                    if (sortedString.charAt(count) != smallest)
                    {
                        break;
                    }
                    fre++;

                }
                if (frequencyOfSmallest > fre)
                {
                    returnVal[i] += 1;

                }


            }


        }
        return returnVal;
    }
}
