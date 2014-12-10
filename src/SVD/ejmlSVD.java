package SVD;

import java.util.Random;

import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.hadoop.hdfs.server.namenode.status_jsp;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;

import distributedMatrixInverse.MatrixMerge;
import Jama.Matrix;

public class ejmlSVD {
    Random rand = new Random(23423);


    public void basic() {
        checkMatrix(7,5);
        checkMatrix(5,5);
        checkMatrix(7,7);
    }

    private void checkMatrix( int numRows , int numCols ) {
        SimpleMatrix A = SimpleMatrix.random(numRows,numCols,rand);
        SingularValueDecomposition svd = A.computeSVD();
        DenseMatrix64F U = svd.getU();
        DenseMatrix64F V = svd.getV();
        DenseMatrix64F S = setSingularValue(svd.getSingularValues(),V.numRows);
    }

	private DenseMatrix64F setSingularValue(double[] sValue,int dimension){
		DenseMatrix64F S = new DenseMatrix64F(dimension, dimension);
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
	
	public void main(String[] args) throws Exception {  
		checkMatrix(7,5);
	}
}
