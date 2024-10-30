package parallelAbelianSandpile;

/**
 * GridValidator - A program to compare cellular automatons.
 * @author Razeen Brey [BRYRAZ002]
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class GridValidator extends RecursiveAction
{   
    public String[] grid1;
    public String[] grid2;
    public int start;
    public int end;
    public static final int SEQUENTIALcutoff = 100;
    private boolean result = true;

    public GridValidator(String[] g1, String[] g2, int s, int e)
    {
        this.grid1 = g1;
        this.grid2 = g2;
        this.start = s;
        this.end = e;
    }

    public GridValidator(String[] g1, String[] g2)
    {
        this(g1, g2, 0, g1.length);
    }

    public boolean getResult()
    {
        return result;
    }

    public void compute()
    {
        if ((end - start) <= SEQUENTIALcutoff)
        {
            for (int i = start; i < end; i++)
            {
                if (!grid1[i].equals(grid2[i]))
                {
                    result =  false;
                    break;
                }
            }
        }
        else
        {
            // Split the task into two sub-tasks
            int mid = (start + end) / 2;
            GridValidator leftTask = new GridValidator(grid1, grid2, start, mid);
            GridValidator rightTask = new GridValidator(grid1, grid2, mid, end);

            leftTask.fork(); // Execute the left task asynchronously
            rightTask.compute(); // Execute the right task synchronously
            leftTask.join(); // Wait for the left task to complete
            result = leftTask.getResult() && rightTask.getResult();
        }
    }
    public static void main(String[] args)
    {
        System.out.println("File name 1:");
        Scanner keyboard = new Scanner(System.in);
        String fName1 = keyboard.nextLine();
        System.out.println("File name 2:");
        String fName2 = keyboard.nextLine();
        keyboard.close();

        try
        {
            File data1 = new File(fName1);
            Scanner fileReader1 = new Scanner(data1);
            String stringData1 = fileReader1.nextLine();
            String[] arrOut1 = stringData1.split(",");
            fileReader1.close();

            File data2 = new File(fName2);
            Scanner fileReader2 = new Scanner(data2);
            String stringData2 = fileReader2.nextLine();
            String[] arrOut2 = stringData2.split(",");
            fileReader2.close();

            GridValidator validator = new GridValidator(arrOut1, arrOut2);
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(validator);
            System.out.println("Grid Validation complete!\nStatus: " + validator.getResult());

        }
        catch(FileNotFoundException e)
        {
            System.out.println("Error: See details below.");
            e.printStackTrace();
        }

        

    }
}
