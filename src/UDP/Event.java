package UDP;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Event implements Comparable<Event>, Serializable
{
    // Automatically have events conclude at 11:59 PM of the day set
    public static final LocalTime CONCLUDING_TIME = LocalTime.parse("23:59:00");
    private String name;
    private double goal;
    private double currentEarnings;
    private double remainingForGoal;
    private String deadline;
    private boolean hasConcluded;

    public Event(String name, double goal, String deadline)
    {
        this.name = name;
        this.goal = goal;
        this.currentEarnings = 0.0;
        this.remainingForGoal = this.goal;
        this.hasConcluded = false;
        this.deadline = deadline;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getGoal()
    {
        return goal;
    }

    public String getFormattedGoal()
    {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(goal);
    }

    public void setGoal(double goal)
    {
        this.goal = goal;
    }

    public double getCurrentEarnings()
    {
        return currentEarnings;
    }

    public void setCurrentEarnings(double currentEarnings)
    {
        this.currentEarnings = currentEarnings;
    }

    public double getRemainingForGoal()
    {
        return remainingForGoal;
    }

    public void setRemainingForGoal(double remainingForGoal)
    {
        this.remainingForGoal = remainingForGoal;
    }

    public String getDeadline()
    {
        return deadline;
    }

    public void String(String deadline)
    {
        this.deadline = deadline;
    }

    public boolean getHasConcluded()
    {
        return hasConcluded;
    }

    /**
     * Updates the event's "hasConcluded" variable to true if the local time is equal to the
     */
    public void updateConcludedState()
    {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime deadlineDateTime = Utils.dateFormatConverter(deadline);

        // If the current date and time is after the event's deadline, it has concluded
        if(currentDateTime.isAfter(deadlineDateTime))
        {
            this.hasConcluded = true;
        }
    }

    public void addMoney(double amount)
    {
        currentEarnings += amount;
        remainingForGoal -= amount;

        if(remainingForGoal <= 0)
        {
            remainingForGoal = 0;
        }
    }

    @Override
    public int compareTo(Event event)
    {
        LocalDateTime deadline = Utils.dateFormatConverter(this.deadline);
        LocalDateTime otherDeadline = Utils.dateFormatConverter(event.deadline);

        return otherDeadline.compareTo(deadline);
    }

    @Override
    public String toString()
    {
        return "Event name:\t\t\t\t\t" + name +
                "\nGoal:\t\t\t\t\t\t$" + goal +
                "\nCurrent Amount Raised:\t\t$" + currentEarnings +
                "\nRemaining for Goal:\t\t\t$" + remainingForGoal +
                "\nDeadline:\t\t\t\t\t" + deadline;
    }
}
