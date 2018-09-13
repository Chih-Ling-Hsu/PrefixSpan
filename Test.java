import java.lang.*;
import java.util.*;
import java.io.*;

public class Test{
    public static void main(String[] args){
        long start, end;        
		Runtime instance = Runtime.getRuntime();    // get Runtime instance        

        PrefixSpan ap = new PrefixSpan(args);
        List<Sequence> sequences = ap.readTransactionDB();

        instance.gc();
        start = System.nanoTime();        
        Map<String, Integer> output = ap.findSequentialPatterns(sequences);
        end = System.nanoTime();
        

        System.out.println("\n\nList of sequential patterns found");
        System.out.println("*******************************");
        for(String seq: output.keySet()){
            System.out.println(seq + "\tSupport Count: " + output.get(seq));
        }

        System.out.println("------------------------------------");        
        System.out.println("Number of sequential patterns found: " + output.size());
        System.out.println("Execution time is: "+((double)(end-start)/1000000000) + " seconds.");

        checkMemory();
    }

    public static void checkMemory(){
 
		//System.out.println("\n***** Heap utilization statistics [MB] *****");

		Runtime instance = Runtime.getRuntime();    // get Runtime instance      
        int mb = 1024 * 1024;  

		// available memory
		//System.out.println("Total Memory: " + (double) instance.totalMemory() / mb);
 
		// free memory
		//System.out.println("Free Memory: " + (double) instance.freeMemory() / mb);
 
		// used memory
		System.out.println("Used Memory: "
				+ (double) (instance.totalMemory() - instance.freeMemory()) / mb + " MB");
 
		// Maximum available memory
		//System.out.println("Max Memory: " + (double) instance.maxMemory() / mb);

    }
}