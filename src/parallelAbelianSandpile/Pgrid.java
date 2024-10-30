/*
 * Pgrid - an adaptation of Grid for Parallelisation of an Abeilan Sandpile
 * Adapted from Michelle Kuttel for BRYRAZ002's PCP1 Assignment
 */

/**
 * @author Michelle Kuttel
 * @author Razeen Brey [BRYRAZ002] (modifier/adaptor)
 */

package parallelAbelianSandpile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.RecursiveAction; // to implement compute()
import java.util.concurrent.ForkJoinPool;	 // to enable parallelism
import java.io.FileWriter;
import javax.imageio.ImageIO;

public class Pgrid extends RecursiveAction
{
    public static boolean effect;
    public final int SQUENTIALcutoff  = 12;
	public int widthI;
	public int widthJ;
	public ForkJoinPool pool = new ForkJoinPool(4);
	// pool parallelism set to 4 after determining it was most optimal during tests.

    public int width;
    public int height;
    public int[][] grid;
    public int[][] upGrid;

    /**
	 * Creates a Pgrid object given the number of rows and columns.
	 * @param r The integer value of the number of rows.
	 * @param c The integer value of the number of columns.
	 */
    public Pgrid(int r, int c)
    {
        width = r+2;
        height = c+2;
        grid = new int[this.width][this.height];
        upGrid = new int[this.width][this.height];

        // make sink with 0s border
        for(int i=0; i<this.width; i++ )
        {
			for( int j=0; j<this.height; j++ )
            {
				grid[i][j]=0;
				upGrid[i][j]=0;
			}
		}
    }

	/**
	 * Creates a Pgrid object given a 2D array of type int[][].
	 * @param newGrid - A 2D array of type int[][].
	 */
	public Pgrid(int[][] newGrid)
	{
		this(newGrid.length,newGrid[0].length);
		for(int i=1; i<width-1; i++ )
		{
			for( int j=1; j<height-1; j++ )
			{
				this.grid[i][j]=newGrid[i-1][j-1];
			}
		}
		
	}

	/**
	 * Creates a subgrid of Pgrid. This is split by rows.
	 * @param rowi The start row of the subgrid.
	 * @param rowj The end row of the subgrid.
	 * @param width The total rows from the main grid.
	 * @param height The total columns from the main grid.
	 * @param fg The grid component of the main grid.
	 * @param uGrid The upGrid component of the main grid.
	 */
	Pgrid(int rowi, int rowj, int width, int height, int[][] fg, int [][] uGrid)
	{
		grid = fg;
		upGrid = uGrid;
		this.width = width;
		this.height = height;
		this.widthI = rowi;
		this.widthJ = rowj;
	}

	/**
	 * Returns width (accounting for sink)
	 * @return width (int)
	 */
    public int getWidth()
    {return width-2;}

	/**
	 * Returns height (accounting for sink)
	 * @return height (int)
	 */
    public int getHeight()
    {return height-2;}

	/**
	 * Returns value at coordinate pair
	 * @param r The row of the coordinate pair.
	 * @param c The column of the coordinate pair.
	 * @return The value at (row, column).
	 */
    public int get(int r, int c)
    {
        return this.grid[r][c];
    }

    /**
	 * Replace grid with upGrid.
	 */
    public void updateGrid() {
		for(int i=1; i<width-1; i++ )
        {
			for( int j=1; j<height-1; j++ )
            {
				this.grid[i][j]=upGrid[i][j];
			}
		}
	}

	/**
	 * The first step in updating a grid.
	 * A Pgrid object is created and is passed over to the ForkJoinPool to handle the parallel processing of it.
	 * @return Returns true if changes have occurred to grid.
	 */
    public boolean update()
    {
		effect = false;
		//SQUENTIALcutoff = (width/8)+1;
		Pgrid grd = new Pgrid(1, this.getWidth(), width, height, grid, upGrid);
		pool.invoke(grd);

		if(effect)
		{updateGrid();}
		return effect;
	}
    
	/**
	 * Implementation of RecursiveAction's compute()
	 * Splits the Pgrid object into smaller ones until the size of the width is sufficiently small.
	 *  
	 */
    public void compute()
    {	
		if((widthJ-widthI) < (((int) (width-2)/SQUENTIALcutoff)+1) )
		{
			for( int i = widthI; i < widthJ+1; i++ )
        	{
				for( int j = 1; j<height-1; j++ )
            	{
					upGrid[i][j] = (grid[i][j] % 4) + (grid[i-1][j] / 4) +grid[i+1][j] / 4 +grid[i][j-1] / 4 + grid[i][j+1] / 4;
					if (grid[i][j]!=upGrid[i][j])
                	{  
						effect=true;
					}
		    	}
        	}
			
		}
		else
		{
			int m = (int) (widthI + widthJ)/2;
			Pgrid l = new Pgrid(widthI, m, width, height, grid, upGrid);
			Pgrid r = new Pgrid(m+1, widthJ, width, height, grid, upGrid);
			l.fork();
			r.compute();
			l.join();
		}
    }
	/**
	 * Outputs a csv file containing the final grid.
	 * output.csv - for validation.
	 */
	public void csvGrid()
	{
		try
		{
			FileWriter writer = new FileWriter("output.csv");
			//writer.write(this.get(2, 2));
			for (int i=0; i<= getWidth(); i++)
			{
				for (int j=0; j<= getHeight(); j++)
				{
					writer.write(this.get(i, j) + ",");
				}
			}
			writer.close();
            System.out.println("Successfully wrote to the file.");
		}
		catch (IOException e)
        {
            System.out.println("Error: Details below.");
            e.printStackTrace();
        }
	}
    
	/**
	 * Prints the grid to screen.
	 */
    public void printGrid( )
    {
		int i;
        int j;
		//not border is not printed
		System.out.printf("Grid:\n");
		System.out.printf("+");
		for( j=1; j<height-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n");
		for( i=1; i<width-1; i++ ) {
			System.out.printf("|");
			for( j=1; j<height-1; j++ ) {
				if ( grid[i][j] > 0) 
					System.out.printf("%4d", grid[i][j] );
				else
					System.out.printf("    ");
			}
			System.out.printf("|\n");
		}
		System.out.printf("+");
		for( j=1; j<height-1; j++ ) System.out.printf("  --");
		System.out.printf("+\n\n");
	}

    /**
	 * write grid out as an image
	 * @param fileName
	 * @throws IOException
	 */
	void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //integer values from 0 to 255.
        int a=0;
        int g=0;//green
        int b=0;//blue
        int r=0;//red

		for( int i=0; i<width; i++ ) {
			for( int j=0; j<height; j++ ) {
			     g=0;//green
			     b=0;//blue
			     r=0;//red

				switch (grid[i][j]) {
					case 0:
		                break;
		            case 1:
		            	g=255;
		                break;
		            case 2:
		                b=255;
		                break;
		            case 3:
		                r = 255;
		                break;
		            default:
		                break;
				
				}
		                // Set destination pixel to mean
		                // Re-assemble destination pixel.
		              int dpixel = (0xff000000)
		                		| (a << 24)
		                        | (r << 16)
		                        | (g<< 8)
		                        | b; 
		              dstImage.setRGB(i, j, dpixel); //write it out

			
			}}
		
        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
	}
}