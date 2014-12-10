package distributedMatrixInverse;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * 
 * The algorithm proposed by Hongliang Guo to handle large matrix inversion
 * This method is suitable for matrix with not very large row number and prohibitively large column number
 * @author Lei Yang (Jerryyanglei@gmail.com)
 *
 */
public class MatrixMerge {
	private int nRow = 12;
	private int nColumn = 4;
	private int nNodes = 7;
	String logFileName = "log.txt";
	private Matrix[] matrixs;
	
	public MatrixMerge() {
		generateRandomMatrics();
	}
	
	public MatrixMerge(int node) {
		if(node >= 1){
			nNodes = node;
			generateRandomMatrics();
		}
		else{
			System.out.println("Node number should be larger than zero!");
		}
	}
	
	public MatrixMerge(int row, int column, int node) {
		if(node >= 1 && row >= 1 && column >= 1){
			nRow = row;
			nColumn = column;
			nNodes = node;
			generateRandomMatrics();
		}
		else{
			System.out.println("Node number, row number and column number should be larger than zero!");
		}
	}
	
	public MatrixMerge(Matrix[] matrixList, int node) {
		if(node >= 1 && matrixList.length >= 1){
			// TODO: check the dimension of the input matrix
			matrixs = matrixList;
			nNodes = node;
		}
		else{
			System.out.println("Node number and matrix number should be larger than zero!");
		}
	}
	
	/**
	 * Break the big matrix into nNodes matrix
	 * @param directMatrix
	 * @param node
	 */
	public MatrixMerge(Matrix bigMatrix, int node) {
		if(node >= 1 && bigMatrix != null){
			nNodes = node;
			nRow = bigMatrix.getRowDimension();
			nColumn = bigMatrix.getColumnDimension();
			int aveRow = calAverageRow();
			int lastRow = calLastRow(aveRow);
			if(lastRow > 0){
				matrixs = new Matrix[nNodes];
			}
			else{
				matrixs = new Matrix[nNodes - 1];
			}
			// split up the big matrix
			int[] colArr = new int[nColumn];
			for (int i = 0; i < colArr.length; i++) {
				colArr[i] = i;
			}
			// matrix in first n-1 computer node
			for (int i = 0; i < node-1; i++) {
				int[] rowArr = new int[aveRow];
				for (int j = 0; j < rowArr.length; j++) {
					rowArr[j] = i*aveRow + j;
				}
				matrixs[i] = bigMatrix.getMatrix(rowArr, colArr);
			}
			// matrix in last(n) computer node
			if(lastRow > 0){
				int[] rowArr = new int[lastRow];
				for (int i = 0; i < rowArr.length; i++) {
					rowArr[i] = (node - 1)*aveRow+ i;
				}
				matrixs[node-1] = bigMatrix.getMatrix(rowArr, colArr);
			}
		}
		else{
			System.out.println("Node number should be larger than zero and bigMatrix should not be null!");
		}
	}
	
	////////////////////////////////
	// This function has been set as public
	// you can get the merged result from outside
	public Matrix getMergedMatrix(){
		Matrix mergedMatrix = new Matrix(nColumn, nColumn, 0.0);
		SingularValueDecomposition tmpSVD = null;
		Queue<Matrix> vQueue = new LinkedList<Matrix>();
		Queue<Matrix> lQueue = new LinkedList<Matrix>();
		// add each svd result to queue
		addSVDResultToQueue(vQueue, lQueue);
		// merge until one left
		int num = 0;
		Matrix tmp3 = matrixs[num].transpose().times(matrixs[num]);
		while(vQueue.size() > 1){
			// queue size >= 2
			Matrix Vi = vQueue.poll();
			Matrix Vj = vQueue.poll();
			Matrix Li = lQueue.poll();
			Matrix Lj = lQueue.poll();
			// Uq = svd(S_i^{-1/2}*V_i'*V_j*S_j^{1/2})
			Matrix tmp1 = calDiagMatrixPower(Li,-0.5).times(Vi.transpose());
			Matrix tmp2 = tmp1.times(Vj).times(calDiagMatrixPower(Lj, 0.5));
			Matrix Uq = null;
			tmpSVD = tmp2.transpose().svd();
			Uq = tmpSVD.getV();
			Matrix Sq = setSingularValue(tmpSVD.getSingularValues(), Uq.getRowDimension());
			
			Matrix Identity = Matrix.identity(Sq.getRowDimension(), Sq.getColumnDimension());
			// P^{-1} = (V_i*S_i^{1/2}*Uq)'
			Matrix P = (Vi.times(calDiagMatrixPower(Li, 0.5)).times(Uq)).transpose();
			// M_{ij} = (P^{-1})'*(I+S_q*S_q')*P^{-1}
			mergedMatrix = (P.transpose()).times(Identity.plus(Sq.times(Sq))).times(P);

			System.out.println("my:");
			mergedMatrix.print(nColumn, nColumn); 		
			tmp2 = matrixs[num+1].transpose().times(matrixs[num+1]);
//			num++;
//			tmp3.plusEquals(tmp2);
			System.out.println("correct1:");
			tmp3.print(nColumn, nColumn);
			tmp2.print(nColumn, nColumn);
			
			System.out.println("correct2:");
			tmp1 = Vi.times(Li).times(Vi.transpose());
			tmp1.print(nColumn, nColumn);
			tmp2 = Vj.times(Lj).times(Vj.transpose());
			tmp2.print(nColumn, nColumn);
			tmp1.plus(tmp2).print(nColumn, nColumn);
			
			// add svd merged result to queue (eigenvalue decomposition can also be used here)
			tmpSVD = mergedMatrix.svd();
			vQueue.add(tmpSVD.getV());
			lQueue.add(tmpSVD.getS());
		}
		// the V and S of the final merged matrix stored in vQueue and sQueue 
		mergedMatrix.print(nColumn, nColumn);
		return mergedMatrix;
	}
	
	
	// calculate the Matrix through: M = CI + sum_i H_i*H_i'
	public Matrix getDirectMatrix(){
		Matrix directMatrix = new Matrix(nColumn, nColumn, 0.0);
		int mergedNum = matrixs.length;
		System.out.println("direct:");
		for (int i = 0; i < mergedNum; i++) {
			directMatrix.plusEquals((matrixs[i].transpose()).times(matrixs[i]));
			directMatrix.print(nColumn, nColumn);
		}
		directMatrix.print(nColumn, nColumn);
		return directMatrix;
	}
	
	public void compareMatrixResult(){
		// record the start time
		long startTime = System.nanoTime();
		Matrix mergedMatrix = getMergedMatrix();
		// record the algorithm time (ms)
		double time = (System.nanoTime() - startTime)/1000/1000;
		// calculate the Frobenius norm difference
		double accuracy = Math.abs(mergedMatrix.normF() 
				- getDirectMatrix().normF());
		// output the result to console
		System.out.println(getOutputInfo(accuracy,time));
		// output the result to file
		outputResultToFile(accuracy, time);
	}
	
	private int calAverageRow(){
		int aveRow = nRow/nNodes;
		double decimal = (nRow*1.0)/nNodes - aveRow;
		// round to the nearest number
		if(decimal >= 0.5){
			aveRow = aveRow + 1;
		}
		return aveRow;
	}
	
	private void generateRandomMatrics(){
		int aveRow = calAverageRow();
		int lastRow = calLastRow(aveRow);
		if(lastRow > 0){
			matrixs = new Matrix[nNodes];
		}
		else{
			matrixs = new Matrix[nNodes - 1];
		}
		// matrix in first n-1 computer node
		for (int i = 0; i < nNodes-1; i++) {
			matrixs[i] = Matrix.random(aveRow, nColumn);
		}
		// matrix in last(n) computer node
		if(lastRow > 0){
			matrixs[nNodes-1] = Matrix.random(lastRow, nColumn);
		}
	}
	
	private Matrix calDiagMatrixPower(Matrix S, double p){
		int size = S.rank();
		double element = 0.0;
		Matrix res = S.copy();
		for (int i = 0; i < size; i++) {
			element = res.get(i, i);
			if(element != 0){
				element = Math.pow(element, p);
				res.set(i, i, element);
			}
			else{
				break; // others eigen values are zero
			}
		}
		return res;
	}
	
	/**
	 * 
	 * @param vQueue
	 * @param lQueue
	 */
	private  void addSVDResultToQueue(Queue<Matrix> vQueue, Queue<Matrix> lQueue){
		int mergedNum = matrixs.length;
		SingularValueDecomposition tmpSVD = null;
		for (int i = 0; i < mergedNum; i++) {
			tmpSVD = matrixs[i].svd();
			Matrix V = tmpSVD.getV();
			Matrix S = setSingularValue(tmpSVD.getSingularValues(), V.getRowDimension());
			//V.times(S.times(S)).times(V.transpose()).print(nColumn, nColumn);
			// Lambda = S' * S, S is diagonal matrix
			Matrix L = S.times(S);
			vQueue.add(V);
			lQueue.add(L);
		}
	}
	
	private Matrix setSingularValue(double[] sValue,int dimension){
		Matrix S = new Matrix(dimension, dimension);
		int rank = sValue.length;
		for (int i = 0; i < dimension; i++) {
			if(i<rank){
				S.set(i, i, sValue[i]);
			}
			else{
				S.set(i, i, 0.0);
			}
		}
		return S;
	}
	
	private boolean isTransposed(){
		return nColumn>calAverageRow()+1?true:false;
	}
	
	private boolean isTransposed(Matrix m){
		return m.getColumnDimension()>m.getRowDimension()+1?true:false;
	}
	
	private int calLastRow(int aveRow){
		return nRow - aveRow*(nNodes - 1);
	}
	
	/**
	 * 
	 * Record the detail information to log file
	 * 
	 * @param accuracy
	 * @param time
	 */
	private void outputResultToFile(double accuracy, double time){
		try {
			String content = getOutputInfo(accuracy, time);
			FileWriter writer = new FileWriter(logFileName, true);
			writer.write(content);
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
			
		}
	}
	
	private String getOutputInfo(double accuracy, double time){
		String content = "##### matrixMerged-log-"+getLogDate() + " begin #####\r\n" + 
							"nRow:\t\t"+ nRow + "\r\n"+ "nColumn:\t"+ nColumn + 
							"\r\n"+"nComputer:\t"+ nNodes +  
							"\r\n"+"aveRow:\t\t"+ calAverageRow() +
							"\r\n"+"lastRow:\t"+ calLastRow(calAverageRow()) + "\r\n"+ 
							"Accuracy:\t"+ accuracy + "\r\n"+"Time:\t\t" + time + "ms"+ "\r\n" + 
							"##### matrixMerged-log-"+getLogDate() + " end #####\r\n\r\n";
		return content;
	}
	
	private String getLogDate(){
		String date = "";
		int y,m,d,h,mi,s;    
		Calendar cal=Calendar.getInstance();    
		y=cal.get(Calendar.YEAR);    
		m=cal.get(Calendar.MONTH);    
		d=cal.get(Calendar.DATE);    
		h=cal.get(Calendar.HOUR_OF_DAY);    
		mi=cal.get(Calendar.MINUTE);    
		s=cal.get(Calendar.SECOND);
		date = m+"/"+d+"/"+y+" "+h+":"+mi+":"+s;
		return date;
	}
}
