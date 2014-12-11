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
import org.omg.CORBA.MARSHAL;

import Jama.Matrix;

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
		double [][] ma = {{1,3},{2,6},{3,9}};
		double [][] mb = {{1,4},{2,8},{3,12}};
		double [][] mc = {{2,3},{4,6},{6,9}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge();
		matrixMerge.compareMatrixResult();
	}
}
