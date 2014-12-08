package distributedMatrixInverse;

import java.security.Identity;
import java.util.LinkedList;
import java.util.Queue;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class MatrixMerge {
	private int nRow = 7;
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
		Queue<Matrix> lQueue = new LinkedList<Matrix>();
		// add each svd result to queue
		int mergedNum = matrixs.length;
		SingularValueDecomposition tmpSVD = null;
		for (int i = 0; i < mergedNum; i++) {
			tmpSVD = matrixs[i].svd();
			Matrix V = tmpSVD.getV();
			Matrix S = tmpSVD.getS();
			//V.times(S.times(S)).times(V.transpose()).print(nColumn, nColumn);
			// Lambda = S' * S, S is diagonal matrix
			Matrix L = S.times(S);
			vQueue.add(V);
			lQueue.add(L);
		}
		// merge until one left
		while(vQueue.size() > 1){
			// queue size > 2
			Matrix Vi = vQueue.poll();
			Matrix Vj = vQueue.poll();
			Matrix Li = lQueue.poll();
			Matrix Lj = lQueue.poll();
			// Uq = svd(S_i^{-1/2}*V_i'*V_j*S_j^{1/2})
			Matrix tmp1 = calDiagMatrixPower(Li,-0.5).times(Vi.transpose());
			Matrix tmp2 = tmp1.times(Vj).times(calDiagMatrixPower(Lj, 0.5));
			tmpSVD = tmp2.svd();
			Matrix Uq = tmpSVD.getU();
			Matrix Sq = tmpSVD.getS();
			Matrix Identity = Matrix.identity(Sq.getRowDimension(), Sq.getColumnDimension());
			// P^{-1} = (V_i*S_i^{1/2}*Uq)'
			Matrix P = (Vi.times(calDiagMatrixPower(Li, 0.5)).times(Uq)).transpose();
			// M_{ij} = (P^{-1})'*(I+S_q*S_q')*P^{-1}
			mergedMatrix = (P.transpose()).times(Identity.plus(Sq.times(Sq.transpose()))).times(P);

//			mergedMatrix.print(nColumn, nColumn);
//			tmp1 = matrixs[0].transpose().times(matrixs[0]);		
//			tmp1.print(nColumn, nColumn);
//			tmp2 = matrixs[1].transpose().times(matrixs[1]);
//			Matrix tmp3 = tmp1.plus(tmp2);
//			tmp3.print(nColumn, nColumn);
//			tmp1 = Vi.times(Li).times(Vi.transpose());
//			tmp1.print(nColumn, nColumn);
//			tmp2 = Vj.times(Lj).times(Vj.transpose());
//			tmp3 = tmp1.plus(tmp2);
//			tmp3.print(nColumn, nColumn);
			
			// add svd merged result to queue
			tmpSVD = mergedMatrix.svd();
			vQueue.add(tmpSVD.getV());
			lQueue.add(tmpSVD.getS().times(tmpSVD.getS()));
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
}
