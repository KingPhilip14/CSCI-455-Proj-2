package UDP;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Utils
{
    public static int menuSelection(int min, int max)
    {
        Scanner scan = new Scanner(System.in);
        int input;

        while(!scan.hasNextInt())
        {
            scan.nextLine();
            System.out.printf("Please provide a number between %d and %d to select from the menu > ", min, max);
        }

        input = scan.nextInt();

        while(input < min || input > max)
        {
            System.out.printf("Please provide a number between %d and %d to select from the menu > ", min, max);

            while(!scan.hasNextInt())
            {
                scan.nextLine();
                System.out.printf("Please provide a number between %d and %d to select from the menu > ", min, max);
            }

            input = scan.nextInt();
        }

        return input;
    }

    /**
     * Will scan in the user's input and ensure it's a double. If not, it prompts the user again.
     * @return a double representing the amount of money to raise
     */
    public static double validateMoney()
    {
        Scanner scan = new Scanner(System.in);

        // if a double is not provided, continue to prompt the user for valid input
        while(!scan.hasNextDouble())
        {
            scan.nextLine();
            System.out.print("Please provide the amount you want to raise up to 2 decimal points (e.g., 100.00) > ");
        }

        double goal = scan.nextDouble();

        // if the amount specified is less than or equal to 0, ask for an amount greater than 0
        while(goal <= 0)
        {
            System.out.print("Please provide an amount to raise that is greater than 0 > ");

            while(!scan.hasNextDouble())
            {
                scan.nextLine();
                System.out.print("Please provide the amount you want to raise up to 2 decimal points " +
                        "(e.g., 100.00) > ");
            }

            goal = scan.nextDouble();
        }

        // Format the double to have 2 decimal places. Then, convert the String back to a decimal
        DecimalFormat df = new DecimalFormat("#.00");

        return Double.parseDouble(df.format(goal));
    }

    public static String validateDeadline()
    {
        boolean validDate = false;
        String deadline = "";
        Scanner scan = new Scanner(System.in);

        // if an invalid date is provided, prompt the user again until valid input is given
        while(!validDate)
        {
            String givenDate = scan.nextLine();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);

            try
            {
                dateFormat.parse(givenDate);

                LocalDate parsedDate = LocalDate.parse(givenDate);
                LocalDate currentDate = LocalDate.now();

                // ensure the given date has not already past; only future dates and the current date are permitted
                if(!parsedDate.isBefore(currentDate))
                {
                    validDate = true;
                    LocalDateTime dateTime = LocalDateTime.of(parsedDate, Event.CONCLUDING_TIME);
                    deadline = dateTime.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss"));
                }
                else
                {
                    System.out.print("An invalid date was given. Please provide input in the format dd/mm/yyyy > ");
                }
            }
            catch(DateTimeParseException | ParseException e)
            {
                System.out.print("An invalid date was given. Please provide input in the format dd/mm/yyyy > ");
            }
        }

        return deadline;
    }

    /**
     * Takes a String representing an Event's deadline to convert it from a String in the MMM dd yyyy format to
     * the yyyy-MM-dd format.
     * @param toConvert
     * @return a LocalDateTime representing an Event's deadline
     */
    public static LocalDateTime dateFormatConverter(String toConvert)
    {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss");
        LocalDateTime dateTime = null;

        try
        {
            dateTime = LocalDateTime.parse(toConvert, inputFormatter);
        }
        catch(DateTimeParseException e)
        {
            System.out.printf("An invalid date was given when converting a date's format.\n" +
                    "The date: %s\n" +
                    e.getMessage() + "\n", toConvert);
        }

        return dateTime;
    }
}
