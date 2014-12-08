package distributedMatrixInverse;

import java.security.Identity;
import java.util.LinkedList;
import java.util.Queue;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class MatrixMerge {
	private int nRow = 5;
	private int nColumn = 2;
	private int nNodes = 1;
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
			// handle mistakes
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
			//
		}
	}
	
	public MatrixMerge(Matrix[] matrixList, int node) {
		if(node >= 1 && matrixList.length >= 1){
			matrixs = matrixList;
			nNodes = node;
		}
		else{
			//
		}
	}
	
	////////////////////////////////
	// This function has been set as public
	// you can get the merged result from outside
	public Matrix getMergedMatrix(){
		Matrix mergedMatrix = new Matrix(nColumn, nColumn, 0.0);
		Queue<Matrix> vQueue = new LinkedList<Matrix>();
		Queue<Matrix> sQueue = new LinkedList<Matrix>();
		// add each svd result to queue
		int mergedNum = matrixs.length;
		SingularValueDecomposition tmpSVD = null;
		for (int i = 0; i < mergedNum; i++) {
			tmpSVD = matrixs[i].svd();
			vQueue.add(tmpSVD.getV());
			Matrix S = tmpSVD.getS();
			sQueue.add(tmpSVD.getS());
		}
		// merge until one left
		while(vQueue.size() > 1){
			// queue size > 2
			Matrix Vi = vQueue.poll();
			Matrix Vj = vQueue.poll();
			Matrix Si = sQueue.poll();
			Matrix Sj = sQueue.poll();
			// Uq = svd(S_i^{-1/2}*V_i'*V_j*S_j^{1/2})
			Matrix tmp1 = calDiagMatrixPower(Si,-0.5).times(Vi.transpose());
			Matrix tmp2 = tmp1.times(Vj).times(calDiagMatrixPower(Sj, 0.5));
			tmpSVD = tmp2.svd();
			Matrix Uq = tmpSVD.getU();
			Matrix Sq = tmpSVD.getS();
			Matrix Identity = Matrix.identity(Sq.getRowDimension(), Sq.getColumnDimension());
			// P^{-1} = (V_i*S_i^{1/2}*Uq)'
			Matrix P = (Vi.times(calDiagMatrixPower(Si, 0.5)).times(Uq)).transpose();
			// M_{ij} = (P^{-1})'*(I+S_q*S_q')*P^{-1}
			mergedMatrix = (P.transpose()).times(Identity.plus(Sq.times(Sq.transpose()))).times(P);
			// add svd merged result to queue
			tmpSVD = mergedMatrix.svd();
			vQueue.add(tmpSVD.getV());
			sQueue.add(tmpSVD.getS());
		}
		// the V and S of the final merged matrix stored in vQueue and sQueue 
		mergedMatrix.print(nColumn, nColumn);
		return mergedMatrix;
	}
	
	
	// calculate the Matrix through: M = CI + sum_i H_i*H_i'
	public Matrix getDirectMatrix(){
		Matrix directMatrix = new Matrix(nColumn, nColumn, 0.0);
		int mergedNum = matrixs.length;
		for (int i = 0; i < mergedNum-1; i++) {
			directMatrix.plusEquals((matrixs[i].transpose()).times(matrixs[i]));
		}
		directMatrix.print(nColumn, nColumn);
		return directMatrix;
	}
	
	public void compareMatrixResult(){
		// calculate the Frobenius norm difference
		double result = Math.abs(getMergedMatrix().normF() 
				- getDirectMatrix().normF());
		// output the result to console
		System.out.println("Result:"+ result);
		// output the result to file
	}
	
	private int calAverageRow(){
		int aveRow = nRow/nNodes;
		double decimal = (nRow*1.0)/nNodes - aveRow;
		if(decimal >= 0.5){
			aveRow = aveRow + 1;
		}
		return aveRow;
	}
	
	private void generateRandomMatrics(){
		matrixs = new Matrix[nNodes];
		int mergedNum = matrixs.length;
		int aveRow = calAverageRow();
		// matrix in first n-1 computer node
		for (int i = 0; i < mergedNum-1; i++) {
			matrixs[i] = Matrix.random(aveRow, nColumn);
		}
		// matrix in last(n) computer node
		int lastRow = nRow - aveRow*(mergedNum - 1);
		matrixs[mergedNum-1] = Matrix.random(lastRow, nColumn);
	}
	
	private Matrix calDiagMatrixPower(Matrix S, double p){
		int size = S.rank();
		double element = 0.0;
		for (int i = 0; i < size; i++) {
			element = S.get(i, i);
			if(element != 0){
				element = Math.pow(element, p);
				S.set(i, i, element);
			}
			else{
				break; // others eigen values are zero
			}
		}
		return S;
	}
}
