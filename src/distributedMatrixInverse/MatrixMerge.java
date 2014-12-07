package distributedMatrixInverse;

import Jama.Matrix;

public class MatrixMerge {
	private int nRow = 10;
	private int aveRow = 3;
	private int nColumn = 10;
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
		if(node >= 1){
			matrixs = matrixList;
			nNodes = node;
		}
		else{
			//
		}
	}
	
	////////////////////////////////
	// This function has been set as public
	// you can get the merged result directly 
	public Matrix getMergedMatrix(){
		Matrix mergedMatrix = null;
		return mergedMatrix;
	}
	
	
	// calculate the Matrix through: M = CI + sum_i H_i*H_i'
	public Matrix getDirectMatrix(){
		Matrix mergedMatrix = null;
		return mergedMatrix;
	}
	
	public void compareMatrixResult(){
		// calculate the Frobenius norm difference
		double result = Math.abs(getMergedMatrix().normF() 
				- getDirectMatrix().normF());
		// output the result to console
		System.out.println("Result:"+ result);
		// output the result to file
	}
	
	private void generateRandomMatrics(){
		int mergedNum = matrixs.length;
		for (int i = 0; i < mergedNum-1; i++) {
			matrixs[i] = Matrix.random(aveRow, nColumn);
		}
	}
}
