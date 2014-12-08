package distributedMatrixInverse;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class Entry {
	public static class MapClass 
		extends Mapper<LongWritable, Text, Text, Text> {   
 
	  public static HashMap<String , Double> features = new HashMap<String, Double>();
	  @Override
	  public void map(LongWritable key, Text value, Context context
	          ) throws IOException, InterruptedException {
	
	  }
	}

	public static class Combine extends 
	Reducer<Text, Text, Text, Text> {
	 
	 @Override
	   public void reduce(Text key, Iterable<Text> values,
	 		  Context context)
	             throws IOException, InterruptedException  {
	 }
	
	}
	
	public static class Reduce extends 
			Reducer<Text, Text, Text, Text> {
	
	 @Override
	  public void reduce(Text key, Iterable<Text> values,
			  Context context)
	            throws IOException, InterruptedException  {
		 
	  }
	}
	
	public static void main(String[] args) throws Exception {  
		// local test
		MatrixMerge matrixMerge = new MatrixMerge(2);
		matrixMerge.compareMatrixResult();
	}
}
