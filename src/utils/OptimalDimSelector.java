package utils;

import java.io.IOException;
import java.util.ArrayList;
import main.NETS;
import main.Tuple;
import utils.StreamGenerator;

public class OptimalDimSelector {
	public static void main(String[] args) throws IOException {
		String dataset ="EM";
		double R = 115; // distance threshold, default=6.5(HPC), 115(EM), 1.9(TAO), 0.45(STOCK), 0.028(GAUSS), 525(FC), 2.75(GAS)
		int K = 50; // neighborhood threshold, default = 50
		int dim = 16; // dimension, default = 7(HPC), 16(EM), 55(FC), 3(TAO)
		int S = 5000; // sliding size, default = 500(FC, TAO), 5000(Otherwise)
		int W = 100000; // sliding size, default = 10000(FC, TAO), 100000(Otherwise)
		int nS = W/S;
		int nW = 1;
				
		windowLoop:
		for(int j = 0; j < nW; j++) {
		System.out.println("Window"+j);
		double minTEC = Double.MAX_VALUE;
		int optSD = 0;
		
		dimLoop:
		for(int subDim = 1; subDim <= dim; subDim++) {
			StreamGenerator sg = new StreamGenerator(dataset,0);
			NETS detector = new NETS(dim, subDim, R, K, S, W, nW, sg.getMaxValues(), sg.getMinValues());
			
			for(int i = 0; i< nS+nW+j ; i++) {
				ArrayList<Tuple> slideIn = sg.getNewSlideTuples(i, S);
				detector.calcNetChange(slideIn, i);
				if(i>=nS+j) {
					
					detector.getInfCellIndices();
	
					int neiCellSum = 0;
					for(int infIdx:detector.influencedCells) {
						ArrayList<Integer> neighboringCells = detector.getSortedCandidateCellIndices(infIdx);
						neiCellSum += neighboringCells.size();
					}
					double avgNeiCellNum = neiCellSum*1.0/detector.influencedCells.size();
					double subDimCellNum = detector.windowCnt.size()*1.0;
					double fullDimCellNum = detector.fullDimCellWindowCnt.size()*1.0;
					double avgfull_subCellNum = fullDimCellNum/subDimCellNum;
					double avgSubCellTuple = W/subDimCellNum;
					double TEC = subDimCellNum + (avgNeiCellNum*avgfull_subCellNum) + (avgNeiCellNum*avgSubCellTuple); 
					
					System.out.println(TEC);

					if(TEC < minTEC) {
						minTEC = TEC;
						optSD = subDim;
						if(subDim == dim) {
							System.out.println("Window "+j+"/ TEC "+minTEC+ "/ Sub-dim chosen: " +optSD);
							continue windowLoop;
						}
						continue dimLoop;
					}else {
						if(subDim < dim) {
							subDim = dim-1;
							continue dimLoop;
						}
						System.out.println("Window "+j+"/ TEC "+minTEC+ "/ Sub-dim chosen: " +optSD);
						continue windowLoop;						
						//continue dimLoop;
					}
				}
			}
		}
		
		}
		

	}
}
