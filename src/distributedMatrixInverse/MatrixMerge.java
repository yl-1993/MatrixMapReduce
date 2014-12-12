package distributedMatrixInverse;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import org.ejml.data.Matrix64F;
import org.ejml.data.SimpleMatrix;

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
	private int nRow = 9;
	private int nColumn = 2;
	private int nNodes = 3;
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
	
	public MatrixMerge(Matrix[] matrixList, int row, int column, int node) {
		if(node >= 1 && matrixList.length >= 1){
			// TODO: check the dimension of the input matrix
			matrixs = matrixList;
			nRow = row;
			nColumn = column;
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
	

	public Matrix getMergedMatrix(){
		try{
			Matrix mergedMatrix = new Matrix(nColumn, nColumn, 0.0);
			SingularValueDecomposition tmpSVD = null;
			Queue<Matrix> vQueue = new LinkedList<Matrix>();
			Queue<Matrix> lQueue = new LinkedList<Matrix>();
			// add each svd result to queue
			addSVDResultToQueue(vQueue, lQueue);
			// merge until one left
			while(vQueue.size() > 1){
				// queue size >= 2
				Matrix Vi = vQueue.poll();
				Matrix Li = lQueue.poll();
				Vi = checkMatrixRank(Vi,Li);
				Matrix Vj = vQueue.poll();
				Matrix Lj = lQueue.poll();
				Vj = checkMatrixRank(Vj,Lj);
				// Q = S_i^{-1/2}*V_i'*V_j*S_j^{1/2})
				Matrix Q = calDiagMatrixPower(Li,-0.5).times(Vi.transpose()).times(Vj).times(calDiagMatrixPower(Lj, 0.5));
				Matrix Uq = new Matrix(Q.getRowDimension(), Q.getRowDimension());
				Matrix Sq = new Matrix(Q.getRowDimension(), Q.getRowDimension());
				// Uq = svd(Q)
				if(Q.getColumnDimension()>Q.getRowDimension()){
					tmpSVD = Q.transpose().svd();
					Uq = tmpSVD.getV();
				} else{
					tmpSVD = Q.svd();
					Uq = tmpSVD.getU();
				}
				
				Sq = getSingularValue(tmpSVD.getSingularValues());
				
				Matrix Identity = Matrix.identity(Sq.getRowDimension(), Sq.getColumnDimension());
				// P^{-1} = (V_i*S_i^{1/2}*Uq)'
				Matrix P = (Vi.times(calDiagMatrixPower(Li, 0.5)).times(Uq)).transpose();
				// M_{ij} = (P^{-1})'*(I+S_q*S_q')*P^{-1}
				mergedMatrix = (P.transpose()).times(Identity.plus(Sq.times(Sq))).times(P);
				// add svd merged result to queue (eigenvalue decomposition can also be used here)
				tmpSVD = mergedMatrix.svd();
				Matrix V = tmpSVD.getV();
				int rank = V.getColumnDimension();
				Matrix L = getSingularValue(tmpSVD.getSingularValues());
				if(rank > L.getColumnDimension()){
					rank = L.getColumnDimension();
					V = reshape(V, V.getRowDimension(), rank);
				}
				vQueue.add(V);
				lQueue.add(L);
			}
			// the V and S of the final merged matrix stored in vQueue and sQueue 
			//mergedMatrix.print(nColumn, nColumn);
			return mergedMatrix;
		} catch(Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	/**
	 * M = \sum_i^K V_i\Lambda_i V_i^T
	 * @return
	 */
	public Matrix getVMergedMatrix(){
		try{
			Matrix vmergedMatrix = new Matrix(nColumn, nColumn, 0.0);
			SingularValueDecomposition tmpSVD = null;
			Queue<Matrix> vQueue = new LinkedList<Matrix>();
			Queue<Matrix> lQueue = new LinkedList<Matrix>();
			// add each svd result to queue
			addSVDResultToQueue(vQueue, lQueue);
			// merge until one left
			while(vQueue.size() > 1){
				// queue size >= 2
				Matrix Vi = vQueue.poll();
				Matrix Li = lQueue.poll();
				Matrix Vj = vQueue.poll();
				Matrix Lj = lQueue.poll();
				
				Matrix m1 = Vi.times(Li).times(Vi.transpose());
				Matrix m2 = Vj.times(Lj).times(Vj.transpose());
				vmergedMatrix = m1.plus(m2);
				
				// add svd merged result to queue (eigenvalue decomposition can also be used here)
				tmpSVD = vmergedMatrix.svd();
				Matrix V = tmpSVD.getV();
				int rank = V.getColumnDimension();
				Matrix L = getSingularValue(tmpSVD.getSingularValues());
				if(rank > L.getColumnDimension()){
					rank = L.getColumnDimension();
					V = reshape(V, V.getRowDimension(), rank);
				}
				vQueue.add(V);
				lQueue.add(L);
			}
			// the V and S of the final merged matrix stored in vQueue and sQueue 
			//vmergedMatrix.print(nColumn, nColumn);
			return vmergedMatrix;
		} catch(Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	// calculate the Matrix through: M = CI + sum_i H_i*H_i'
	public Matrix getDirectMatrix(){
		Matrix directMatrix = new Matrix(nColumn, nColumn, 0.0);
		int mergedNum = matrixs.length;
		for (int i = 0; i < mergedNum; i++) {
			directMatrix.plusEquals((matrixs[i].transpose()).times(matrixs[i]));
		}
		//directMatrix.print(nColumn, nColumn);
		return directMatrix;
	}
	
	/**
	 * Accuracy is defined as  Frobenius norm difference between merged matrix and directly computed matrix
	 * Time is the running time of merging algorithm
	 */
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
		System.out.println("---------------------------");
		// output the result to file
		outputResultToFile(accuracy, time);
	}
	
	/**
	 * 
	 * @param V
	 * @param L
	 * @return
	 * @throws Exception 
	 */
	private Matrix checkMatrixRank(Matrix V, Matrix L) throws Exception{
		int vRank = Math.min(V.getRowDimension(), V.getColumnDimension());
		int lRank = L.getRowDimension();
		if(lRank < vRank){
			String errString = "Matrix are not positive definite!\r\n";
			System.out.println("Error: "+errString);
			outputResultToFile(errString);
			// Uncomment the next line if you want to terminate the program when errors happen.
			//throw new Exception(errString);
			return reshape(V, V.getRowDimension(), lRank);
		}
		return V;
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
			if(Math.abs(element) > 1e-10){
				element = Math.pow(element, p);
				res.set(i, i, element);
			}
			else{
				break; // others eigen values below zero
			}
		}
		return res;
	}
	
	/**
	 * Add svd result to queue
	 * @param vQueue
	 * @param lQueue
	 */
	private  void addSVDResultToQueue(Queue<Matrix> vQueue, Queue<Matrix> lQueue){
		int mergedNum = matrixs.length;
		SingularValueDecomposition tmpSVD = null;
		for (int i = 0; i < mergedNum; i++) {
			if(nColumn <= Math.min(calAverageRow(), calLastRow(calAverageRow()))){
				tmpSVD = matrixs[i].svd();
				Matrix V = tmpSVD.getV();
				Matrix S = getSingularValue(tmpSVD.getSingularValues());
				// Lambda = S' * S,  (S is diagonal matrix)
				Matrix L = S.times(S);
				vQueue.add(V);
				lQueue.add(L);
			} else{
				//Matrix V = new Matrix(nColumn, nColumn);
				//Matrix S = new Matrix(nColumn, nColumn);
				//computeSVDByEJML(matrixs[i], V, S);
				tmpSVD = matrixs[i].transpose().svd();
				Matrix V = tmpSVD.getU();
				Matrix S = getSingularValue(tmpSVD.getSingularValues());
				
				Matrix L = S.times(S);
				vQueue.add(V);
				lQueue.add(L);
			}
		}
	}
	
	/**
	 * Use EJML package to compute the SVD of matrix
	 * SVD computation in EJML is quicker than that in JAMA
	 * @param matrix
	 * @param V
	 * @param S
	 */
	private void computeSVDByEJML(Matrix matrix, Matrix V, Matrix S){
		SimpleMatrix tmp = new SimpleMatrix(matrix.getArray());
		org.ejml.alg.dense.decomposition.SingularValueDecomposition svd = tmp.computeSVD();
		Matrix64F V1 = svd.getV();
		int row = V1.numRows;
		if(row == nColumn){
			for (int j = 0; j < row; j++) {
				for (int j2 = 0; j2 < row; j2++) {
					V.set(j, j2, V1.get(j, j2));
				}
			}
			S.plusEquals(getSingularValue(svd.getSingularValues()));
		} else {
			System.out.println("EJML SVD error!");
		}
	}
	
	private Matrix getSingularValue(double[] sValue){
		int dimension = sValue.length;
		Matrix S = new Matrix(dimension, dimension);
		int rank = 0;
		for (rank = 0; rank < dimension; rank++) {
			if(sValue[rank] > 1e-6){
				S.set(rank, rank, sValue[rank]);
			}
			else{
				break;
			}
		}
		if(rank < dimension){
			dimension = rank;
			S = reshape(S, rank, rank);
		}
		return S;
	}
	
	private Matrix reshape(Matrix matrix, int row, int col){
		Matrix reMatrix = new Matrix(row, col);
		int[] rowArr = new int[row];
		int[] colArr = new int[col];
		for (int i = 0; i < row; i++) {
			rowArr[i] = i;
		}
		for (int i = 0; i < col; i++) {
			colArr[i] = i;
		}
		reMatrix = matrix.getMatrix(rowArr, colArr);
		return reMatrix;
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
			String pre = "##### matrixMerged-log-"+getLogDate() + " begin #####\r\n";
			String suf = "##### matrixMerged-log-"+getLogDate() + " end #####\r\n\r\n";
			writer.write(pre+content+suf);
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
			
		}
	}
	
	private void outputResultToFile(String content){
		try {
			FileWriter writer = new FileWriter(logFileName, true);
			String pre = "##### matrixMerged-log-"+getLogDate() + " begin #####\r\n";
			String suf = "##### matrixMerged-log-"+getLogDate() + " end #####\r\n\r\n";
			writer.write(pre+content+suf);
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
			
		}
	}
	
	private String getOutputInfo(double accuracy, double time){
		String content = 	"nRow:\t\t"+ nRow + "\r\n"+ "nColumn:\t"+ nColumn + 
							"\r\n"+"nComputer:\t"+ nNodes +  
							"\r\n"+"aveRow:\t\t"+ calAverageRow() +
							"\r\n"+"lastRow:\t"+ calLastRow(calAverageRow()) + "\r\n"+ 
							"Accuracy:\t"+ accuracy + "\r\n"+"Time:\t\t" + time + "ms"+ "\r\n";
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
