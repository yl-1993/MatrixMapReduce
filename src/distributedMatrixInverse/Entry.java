package distributedMatrixInverse;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.omg.CORBA.MARSHAL;

import Jama.Matrix;
import MatrixMultiple.MatrixMultiple.Combine;
import MatrixMultiple.MatrixMultiple.MapClass;
import MatrixMultiple.MatrixMultiple.Reduce;

public class Entry {
	public static class MapClass 
		extends Mapper<LongWritable, Text, Text, Text> {   
 
	  public static HashMap<String , Double> features = new HashMap<String, Double>();
	  @Override
	  public void map(LongWritable key, Text value, Context context
	          ) throws IOException, InterruptedException {
			MatrixMerge matrixMerge = new MatrixMerge(1000, 10, 3);
			matrixMerge.compareMatrixResult();
			System.out.println("#Map success");
	  }
	}

	public static class Combine extends 
	Reducer<Text, Text, Text, Text> {
	 
	 @Override
	   public void reduce(Text key, Iterable<Text> values,
	 		  Context context)
	             throws IOException, InterruptedException  {
		 System.out.println("#Combine success");
	 }
	
	}
	
	public static class Reduce extends 
			Reducer<Text, Text, Text, Text> {
	
	 @Override
	  public void reduce(Text key, Iterable<Text> values,
			  Context context)
	            throws IOException, InterruptedException  {
		 System.out.println("#Reduce success");
	  }
	}
	
	public static void main(String[] args) throws Exception {  
		// hadoop test
	    Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
//	    if (otherArgs.length != 2) {
//	      System.err.println("Usage: mmult <in> <out>");
//	      System.exit(2);
//	    }
	    Job job = new Job(conf, "dmi");
	    ((JobConf) job.getConfiguration()).setJar("dmi.jar");
	    //job.setJarByClass(MatrixMultiple.class);
	    job.setMapperClass(MapClass.class);
	    job.setCombinerClass(Combine.class);
	    job.setReducerClass(Reduce.class);
	    job.setNumReduceTasks(1);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
//	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
//	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
