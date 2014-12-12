package distributedMatrixInverse;

import Jama.Matrix;

public class LocalEntry {
	public static void main(String[] args) throws Exception {  
		// local test
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
}
