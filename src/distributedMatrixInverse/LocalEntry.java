package distributedMatrixInverse;

import Jama.Matrix;

public class LocalEntry {
	
	private static void testCase1(){
		// Row > Column but the H is not positive definite (each is 3*2 matrix)
		double [][] ma = {{1,3},{2,6},{3,9}};
		double [][] mb = {{1,4},{2,8},{3,12}};
		double [][] mc = {{2,3},{4,6},{6,9}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 9, 2, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase2(){
		// Row > Column and the H is not positive definite (each is 3*2 matrix)
		double [][] ma = {{1,3},{2,7},{3,11}};
		double [][] mb = {{1,4},{2,9},{3,13}};
		double [][] mc = {{2,3},{4,7},{6,17}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 9, 2, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase3(){
		// Row < Column but the H is not positive definite (each is 2*3 matrix, rank = 1)
		double [][] ma = {{1,2,3},{2,4,6}};
		double [][] mb = {{1,3,4},{2,6,8}};
		double [][] mc = {{2,4,3},{4,8,6}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 6, 3, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase4(){
		// Row < Column but the H is not positive definite (each is 2*3 matrix, rank = 2)
		double [][] ma = {{1,2,3},{2,4,7}};
		double [][] mb = {{1,3,4},{2,6,9}};
		double [][] mc = {{2,4,3},{4,8,5}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 6, 3, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase5(){
		// Row == Column but the H is not positive definite (each is 3*3 matrix)
		double [][] ma = {{1,2,3},{2,4,6},{3,6,9}};
		double [][] mb = {{1,3,4},{2,6,8},{3,9,12}};
		double [][] mc = {{2,4,3},{4,8,6},{6,12,9}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 6, 3, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase6(){
		// Row == Column and the H is not positive definite
		double [][] ma = {{1,2,3},{3,4,7},{3,6,19}};
		double [][] mb = {{1,3,4},{5,6,9},{3,9,13}};
		double [][] mc = {{2,4,3},{3,8,5},{6,12,17}};
		Matrix[] mList = new Matrix[3];
		mList[0] = new Matrix(ma);
		mList[1] = new Matrix(mb);
		mList[2] = new Matrix(mc);
		MatrixMerge matrixMerge = new MatrixMerge(mList, 6, 3, 3);
		matrixMerge.compareMatrixResult();
	}
	
	private static void testCase7(){
		// Large random matrix (total row = 100000, col = 1000, computers = 3)
		MatrixMerge matrixMerge = new MatrixMerge(100000, 100, 3);
		matrixMerge.compareMatrixResult();
	}
	
	public static void main(String[] args) throws Exception {  
		// local test
		testCase1();
		testCase2();
		testCase3();
		testCase4();
		testCase5();
		testCase6();
		testCase7();
	}
}
