package MatrixMultiple;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MatrixMultiple {
     public static final String CONTROL_I = "\u0009";
     public static final int MATRIX_I = 4;
     public static final int MATRIX_J = 3;
     public static final int MATRIX_K = 2;
    
     public static String makeKey(String[] tokens, String separator) {
          StringBuffer sb = new StringBuffer();
          boolean isFirst = true;
          for (String token : tokens) {
               if (isFirst)
                    isFirst = false;
               else
                    sb.append(separator);
               sb.append(token);
          }
          return sb.toString();
     }
    
     public static class MapClass 
     		extends Mapper<LongWritable, Text, Text, Text> {   
    	 
          public static HashMap<String , Double> features = new HashMap<String, Double>();
          @Override
          public void map(LongWritable key, Text value, Context context
                  ) throws IOException, InterruptedException {
               // 获取输入文件的全路径和名称
               //String pathName = (reporter.getInputSplit()).getLocations().toString();
               String pathName = ((FileSplit) context.getInputSplit()).getPath().getName();
        	   System.out.println(pathName);
              
               if (pathName.contains("mm_a")) {         
                    String line = value.toString();
                    
                    if (line == null || line.equals("")) return;
                    String[] values = line.split(CONTROL_I);
                   
                    //System.out.println(values[0]+"," + values[1]+",value:"+values[2]);
                    if (values.length < 3) {
                    	System.out.println("Only:" + values.length);
                    	return;
                    }
                   
                    System.out.println(line);
                    
                    String rowindex = values[0];
                    String colindex = values[1];
                    String elevalue = values[2];
                   
                    for (int i = 1; i <= MATRIX_K; i ++) {
                         //output.collect(new Text(rowindex + CONTROL_I + i), new Text("a#"+colindex+"#"+elevalue));
                    	System.out.println("#re:"+rowindex + CONTROL_I + i+","+"a#"+colindex+"#"+elevalue);  
                    	context.write(new Text(rowindex + CONTROL_I + i), new Text("a#"+colindex+"#"+elevalue));
                    }
               }
              
               if (pathName.contains("mm_b")) {              
                    String line = value.toString();
                    
                    if (line == null || line.equals("")) return;
                    String[] values = line.split(CONTROL_I);
                    //System.out.println(values[0]+"," + values[1]+",value:"+values[2]);
                    if (values.length < 3) {
                    	System.out.println("Only:" + values.length);
                    	return;
                    }
                   
                    System.out.println(line);
                    
                    String rowindex = values[0];
                    String colindex = values[1];
                    String elevalue = values[2];
                   
                    for (int i = 1; i <= MATRIX_I; i ++) {
                         //output.collect(new Text(i + CONTROL_I + colindex), new Text("b#"+rowindex+"#"+elevalue));
                        System.out.println("#re:"+i + CONTROL_I + colindex+","+"b#"+rowindex+"#"+elevalue); 
                    	context.write(new Text(i + CONTROL_I + colindex), new Text("b#"+rowindex+"#"+elevalue));
                    }
               }
               
               System.out.println("#Map success");
          }
     }

     public static class Combine extends 
		Reducer<Text, Text, Text, Text> {
    	 
    	 @Override
		   public void reduce(Text key, Iterable<Text> values,
		 		  Context context)
		             throws IOException, InterruptedException  {
    		 
    		 	//super.reduce(key, values, context);
    		 
			   System.out.println("combine");
			   HashSet<String> hashSet = new HashSet<String>();
			   Iterator<Text> valuesIterator = values.iterator();
			   System.out.println(valuesIterator.hasNext());
               while (valuesIterator.hasNext()) {
                   String value = valuesIterator.next().toString();
                   hashSet.add(value);
               }
               for (String str : hashSet) {
                   // 输出合并后的结果
                   System.out.println(key+":"+str);
                   context.write(key, new Text(str));
               }
               //context.write(key, new Text("hello"));
		   }
     }
     
     public static class Reduce extends 
     		Reducer<Text, Text, Text, Text> {
    	
    	 @Override
    	  public void reduce(Text key, Iterable<Text> values,
        		  Context context)
                    throws IOException, InterruptedException  {
    		 
    		 //super.reduce(key, values, context);
    		 
        	  System.out.println("##########reduce:");
               int[] valA = new int[MATRIX_J];
               int[] valB = new int[MATRIX_J];
              
               int i;
               for (i = 0; i < MATRIX_J; i ++) {
                    valA[i] = 0;
                    valB[i] = 0;
               }
              
               
               Iterator<Text> valuesIterator = values.iterator();
               System.out.println(valuesIterator.hasNext());
               while (valuesIterator.hasNext()) {
            	   //check有没进来
            	    System.out.println("----Iteration----");
                    String value = valuesIterator.next().toString();
                    System.out.println(value);
                    if (value.startsWith("a#")) {
	                     StringTokenizer token = new StringTokenizer(value, "#");
	                     String[] temp = new String[3];
	                     int k = 0;
	                     while(token.hasMoreTokens()) {
	                          temp[k] = token.nextToken();
	                          k++;
	                     }
	                     System.out.println(temp[1]+","+temp[2]);
                         valA[Integer.parseInt(temp[1])-1] = Integer.parseInt(temp[2]);
                    } else if (value.startsWith("b#")) {
                         StringTokenizer token = new StringTokenizer(value, "#");
                         String[] temp = new String[3];
                         int k = 0;
                         while(token.hasMoreTokens()) {
                              temp[k] = token.nextToken();
                              k++;
                         }
                         System.out.println(temp[1]+","+temp[2]);
                         valB[Integer.parseInt(temp[1])-1] = Integer.parseInt(temp[2]);
                    }
               }
              
               int result = 0;
               for (i = 0; i < MATRIX_J; i ++) {
                    result += valA[i] * valB[i];
               }
               System.out.println("#res:"+result);
               //output.collect(key, new Text(Integer.toString(result)));
               context.write(key, new Text(Integer.toString(result)));
          }
     }

     public static void main(String[] args) throws Exception {  
    	    Configuration conf = new Configuration();
    	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    	    if (otherArgs.length != 2) {
    	      System.err.println("Usage: mmult <in> <out>");
    	      System.exit(2);
    	    }
    	    Job job = new Job(conf, "mmult");
    	    ((JobConf) job.getConfiguration()).setJar("mm.jar");
    	    //job.setJarByClass(MatrixMultiple.class);
    	    job.setMapperClass(MapClass.class);
    	    job.setCombinerClass(Combine.class);
    	    job.setReducerClass(Reduce.class);
    	    job.setNumReduceTasks(1);
    	    job.setOutputKeyClass(Text.class);
    	    job.setOutputValueClass(Text.class);
    	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    	    System.exit(job.waitForCompletion(true) ? 0 : 1);
    	  }  
}