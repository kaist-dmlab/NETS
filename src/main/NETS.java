package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import utils.Utils;

public class NETS {
	public double R;
	public int K;
	public int dim;
	public int subDim;
	public boolean subDimFlag;
	public int S;
	public int W;
	public int nS;
	public int nW;
	public double neighCellIdxDist;
	public double neighCellFullDimIdxDist;
	public double[] maxValues;
	public double[] minValues;
	public double[] dimLength;
	public double[] subDimLength;
	
	public HashMap<Integer,Cell> slideIn;
	public HashMap<Integer,Cell> slideOut;
	public HashMap<Integer,Integer> windowCnt;
	public HashMap<Integer,ArrayList<Short>> idxDecoder;
	public HashMap<ArrayList<Short>,Integer> idxEncoder;
	public HashMap<Integer,Integer> slideDelta;
	public HashSet<Tuple> outliers;
	public HashMap<Integer,Integer> fullDimCellWindowCnt;
	public LinkedList<HashMap<Integer,Cell>> slides; 
	public HashMap<Integer,Integer> fullDimCellSlideInCnt;
	public HashMap<Integer,Integer> fullDimCellSlideOutCnt;
	public Queue<HashMap<Integer,Integer>> fullDimCellSlidesCnt; 
	public HashSet<Integer> influencedCells;
	
	public int candidateCellsTupleCnt = 0;
	public long Step2Time = 0;
		
	public NETS(int dim, int subDim, double R, int K, int S, int W, int nW, double[] maxValues, double[] minValues) {
		this.dim = dim;
		this.subDim = subDim;
		this.subDimFlag = dim != subDim;
		this.R = R;
		this.K = K;
		this.S = S;
		this.W = W;
		this.nW = nW;
		this.nS = W/S;
		this.neighCellIdxDist = Math.sqrt(subDim)*2;
		this.neighCellFullDimIdxDist = Math.sqrt(dim)*2;
		this.maxValues = maxValues;
		this.minValues = minValues;
		
		this.windowCnt = new HashMap<Integer,Integer>();
		this.slides = new LinkedList<HashMap<Integer,Cell>>();
		this.slideOut = new HashMap<Integer,Cell>();
		this.idxDecoder = new HashMap<Integer, ArrayList<Short>>();
		this.idxEncoder = new HashMap<ArrayList<Short>,Integer>();		
		this.fullDimCellWindowCnt = new HashMap<Integer,Integer>();
		this.fullDimCellSlidesCnt = new LinkedList<HashMap<Integer,Integer>>();
		this.fullDimCellSlideOutCnt = new HashMap<Integer,Integer>();
				
		this.outliers = new HashSet<Tuple>();
						
		/* Cell size calculation for all dim*/
		double minDimSize = Integer.MAX_VALUE;
		double[] dimSize = new double[dim];
		for(int i=0;i<dim;i++) {
			dimSize[i] = maxValues[i] - minValues[i]; 
			if(dimSize[i] <minDimSize) minDimSize = dimSize[i];
		}
		
		double dimWeightsSum = 0;
		int[] dimWeights = new int[dim];
		for(int i=0;i<dim;i++) {   
			//dimWeights[i] = dimSize[i]/minDimSize; //relative-weight
			dimWeights[i] = 1; //equal-weight
			dimWeightsSum+=dimWeights[i];
		}
		
		dimLength = new double[dim];
		double[] gapCount = new double[dim];
		for(int i = 0;i<dim;i++) {  
			dimLength[i] = Math.sqrt(R*R*dimWeights[i]/dimWeightsSum);
			gapCount[i] = Math.ceil(dimSize[i]/dimLength[i]);
			dimSize[i] = gapCount[i]*dimLength[i];
		}
		
		/* Cell size calculation for sub dim*/
		if (subDimFlag) {
			double minSubDimSize = Integer.MAX_VALUE;
			double[] subDimSize = new double[subDim];
			for(int i=0;i<subDim;i++) {
				subDimSize[i] = maxValues[i] - minValues[i]; 
				if(subDimSize[i] <minSubDimSize) minSubDimSize = subDimSize[i];
			}
			
			double subDimWeightsSum = 0;
			int[] subDimWeights = new int[subDim];
			for(int i=0;i<subDim;i++) {    
				//subDimWeights[i] = subDimSize[i]/minSubDimSize; //relative-weight
				subDimWeights[i] = 1; //equal-weight
				subDimWeightsSum+=subDimWeights[i];
			}
			
			subDimLength = new double[subDim];
			double[] subDimgapCount = new double[subDim];
			for(int i = 0;i<subDim;i++) {   
				subDimLength[i] = Math.sqrt(R*R*subDimWeights[i]/subDimWeightsSum);
				subDimgapCount[i] = Math.ceil(subDimSize[i]/subDimLength[i]);
				subDimSize[i] = subDimgapCount[i]*subDimLength[i];
			}
		}

	}
	
	public void indexingSlide(ArrayList<Tuple> slideTuples){
		slideIn = new HashMap<Integer,Cell>();
		fullDimCellSlideInCnt = new HashMap<Integer,Integer>();
		for(Tuple t:slideTuples) {
			ArrayList<Short> fullDimCellIdx = new ArrayList<Short>();
			ArrayList<Short> subDimCellIdx = new ArrayList<Short>();
			for (int j = 0; j<dim; j++) { 
				short dimIdx = (short) ((t.value[j]-minValues[j])/dimLength[j]);
				fullDimCellIdx.add(dimIdx);
			}
			if (subDimFlag) {
				for (int j = 0; j<subDim; j++) {
					short dimIdx = (short) ((t.value[j]-minValues[j])/subDimLength[j]);
					subDimCellIdx.add(dimIdx);
				}
			}else {
				subDimCellIdx = fullDimCellIdx;
			}

			t.fullDimCellIdx = fullDimCellIdx;
			t.subDimCellIdx = subDimCellIdx;
			
			if(!idxEncoder.containsKey(fullDimCellIdx)) {
				int id = idxEncoder.size(); 
				idxEncoder.put(fullDimCellIdx, id);
				idxDecoder.put(id, fullDimCellIdx);
			}
			if(!idxEncoder.containsKey(subDimCellIdx)) {
				int id = idxEncoder.size(); 
				idxEncoder.put(subDimCellIdx, id);
				idxDecoder.put(id, subDimCellIdx);
			}
			if(!slideIn.containsKey(idxEncoder.get(subDimCellIdx))) {
				double[] cellCenter = new double[subDim];
				if (subDimFlag) {
					for (int j = 0; j<subDim; j++) cellCenter[j] = minValues[j] + subDimCellIdx.get(j)*subDimLength[j]+subDimLength[j]/2;
				}else {
					for (int j = 0; j<dim; j++) cellCenter[j] = minValues[j] + fullDimCellIdx.get(j)*dimLength[j]+dimLength[j]/2;
				}
				slideIn.put(idxEncoder.get(subDimCellIdx), new Cell(subDimCellIdx, cellCenter, subDimFlag));
			}
			slideIn.get(idxEncoder.get(subDimCellIdx)).addTuple(t, subDimFlag);
			
			if(!fullDimCellSlideInCnt.containsKey(idxEncoder.get(fullDimCellIdx))) {
				fullDimCellSlideInCnt.put(idxEncoder.get(fullDimCellIdx), 0);
			}
			fullDimCellSlideInCnt.put(idxEncoder.get(fullDimCellIdx), fullDimCellSlideInCnt.get(idxEncoder.get(fullDimCellIdx))+1);
		}
		
		slides.add(slideIn);
		fullDimCellSlidesCnt.add(fullDimCellSlideInCnt);
	}
	
	public void calcNetChange(ArrayList<Tuple> slideTuples, int itr) {
		this.indexingSlide(slideTuples);
		
		/* Slide out */
		if(itr>nS-1) {
			slideOut = slides.poll();
			fullDimCellSlideOutCnt = fullDimCellSlidesCnt.poll();
		}
		slideDelta = new HashMap<Integer, Integer>();
				
		/* Update window */
		for(Integer key:slideIn.keySet()) {
			if(!windowCnt.containsKey(key)) {
				windowCnt.put(key, 0);
				idxDecoder.put(key, slideIn.get(key).cellIdx);
			}
			windowCnt.put(key, windowCnt.get(key)+ slideIn.get(key).getNumTuples());
			slideDelta.put(key, slideIn.get(key).getNumTuples());
		}
		
		for(Integer key:slideOut.keySet()) {
			windowCnt.put(key, windowCnt.get(key)-slideOut.get(key).getNumTuples());
			if(windowCnt.get(key) < 1) {
				windowCnt.remove(key);
			}
			
			if(slideDelta.containsKey(key)) {
				slideDelta.put(key, slideDelta.get(key)-slideOut.get(key).getNumTuples());
			}else {
				slideDelta.put(key, slideOut.get(key).getNumTuples()*-1);
			}
		}
		
		/* Update all Dim cell window count */
		for(Integer key:fullDimCellSlideInCnt.keySet()) {
			if(!fullDimCellWindowCnt.containsKey(key)) {
				fullDimCellWindowCnt.put(key, 0);
			}
			fullDimCellWindowCnt.put(key, fullDimCellWindowCnt.get(key) + fullDimCellSlideInCnt.get(key));
		}
		
		for(Integer key:fullDimCellSlideOutCnt.keySet()) {
			fullDimCellWindowCnt.put(key, fullDimCellWindowCnt.get(key) - fullDimCellSlideOutCnt.get(key));
			if(fullDimCellWindowCnt.get(key) < 1) {
				fullDimCellWindowCnt.remove(key);
			}
		}
	}
	
	
	public void getInfCellIndices() {
		influencedCells = new HashSet<Integer>();
		for (Integer cellIdxWin:windowCnt.keySet()) {
			if (!subDimFlag && windowCnt.get(cellIdxWin) > K) {
				continue;
			}
			for (Integer cellIdxSld:slideDelta.keySet()) {
				if(neighboringSet(idxDecoder.get(cellIdxWin), idxDecoder.get(cellIdxSld))) {
					if (!influencedCells.contains(cellIdxWin)) { 
						influencedCells.add(cellIdxWin);
					}
					break;
				}
			}
		}
	}
		
	public ArrayList<Integer> getSortedCandidateCellIndices(Integer cellIdxInf){
		ArrayList<Integer> candidateCellIndices = new ArrayList<Integer>();
				
		HashMap<Double, HashSet<Integer>> candidateCellIndicesMap = new HashMap<Double, HashSet<Integer>>();
		for (Integer cellIdxWin:windowCnt.keySet()) {
			double dist = neighboringSetDist(idxDecoder.get(cellIdxInf), idxDecoder.get(cellIdxWin));
			if(!subDimFlag) {
				if (!cellIdxInf.equals(cellIdxWin) && dist < neighCellIdxDist) {
					if(!candidateCellIndicesMap.containsKey(dist)) candidateCellIndicesMap.put(dist, new HashSet<Integer>());
					candidateCellIndicesMap.get(dist).add(cellIdxWin);
				}
			}else {
				if (dist < neighCellIdxDist) {
					if(!candidateCellIndicesMap.containsKey(dist)) candidateCellIndicesMap.put(dist, new HashSet<Integer>());
					candidateCellIndicesMap.get(dist).add(cellIdxWin);
				}
			}
		}
		
		Object[] keys = candidateCellIndicesMap.keySet().toArray();
		Arrays.sort(keys);
		for(Object key : keys) {
			candidateCellIndices.addAll(candidateCellIndicesMap.get(key));
			for(Integer cellIdxWin :candidateCellIndicesMap.get(key)) {
				candidateCellsTupleCnt += windowCnt.get(cellIdxWin);
			}
		}
		
		return candidateCellIndices;
	}

	public void findOutlier(String type, int itr) {
		// Remove expired or outdated outliers
		Iterator<Tuple> it = outliers.iterator();
		while (it.hasNext()) {
			Tuple outlier = it.next();
			if(slideOut.containsKey(idxEncoder.get(outlier.subDimCellIdx)) && slideOut.get(idxEncoder.get(outlier.subDimCellIdx)).tuples.contains(outlier)) {  
				it.remove();
			}else if(fullDimCellWindowCnt.get(idxEncoder.get(outlier.fullDimCellIdx))>K){ 
				it.remove();
			}
		}
		if(type == "NAIVE")
			this.findOutlierNaive();
		else if(type == "NETS")
			this.findOutlierNETS(itr);
	}

	public void findOutlierNaive() {
		HashSet<Tuple> allTuples = new HashSet<Tuple>();
		for(HashMap<Integer, Cell> slide:slides) {
			for(Cell cell: slide.values()) {
				allTuples.addAll(cell.tuples);
			}
		}
		outliers.clear();
		
		for(Tuple candTuple:allTuples) {
			boolean outlierFlag = true;
			candTuple.nn =0;
			for(Tuple otherTuple:allTuples) {
				if ((candTuple.id != otherTuple.id) && (neighboringTuple(candTuple, otherTuple,R))) {
					candTuple.nn+=1;
				}
				if (candTuple.nn>=K) {
					outlierFlag = false;
					break;
				}
			}
			if(outlierFlag) outliers.add(candTuple);
		}
	}
	
	public void findOutlierNETS(int itr) {
		// Get influenced cells by changes
		long infCellStart = Utils.getCPUTime();
		getInfCellIndices();
		Step2Time += Utils.getCPUTime() - infCellStart;
		
		// for each influenced cell 
		InfCellLoop:
		for (Integer infCellIdx: influencedCells) {
			
			long neighCellStart = Utils.getCPUTime();
			//find neighbor cells
			candidateCellsTupleCnt = 0;
			ArrayList<Integer> candCellIndices = getSortedCandidateCellIndices(infCellIdx);		
			if(!subDimFlag) candidateCellsTupleCnt += windowCnt.get(infCellIdx);
			//verify if outlier cell 
			if(candidateCellsTupleCnt < K+1) {
				for(HashMap<Integer, Cell> slide: slides) {
					if(!slide.containsKey(infCellIdx)) continue;
					outliers.addAll(slide.get(infCellIdx).tuples);
				}
				continue InfCellLoop;
			}
			Step2Time += Utils.getCPUTime() - neighCellStart;

			
			//get candidate tuples
			HashSet<Tuple> candOutlierTuples = new HashSet<Tuple>();			
			for(HashMap<Integer, Cell> slide: slides) {
				if(!slide.containsKey(infCellIdx)) continue;
				for (Tuple t:slide.get(infCellIdx).tuples) {
					if(t.safeness) {
						continue;
					}
					t.nnIn = fullDimCellWindowCnt.get(idxEncoder.get(t.fullDimCellIdx))-1;
					t.removeOutdatedNNUnsafeOut(itr, nS);
					if(t.getNN()<K) {
						candOutlierTuples.add(t);
					}else if(outliers.contains(t)){
						outliers.remove(t);
					}
				}
			}
			
			TupleLoop:
			for (Tuple tCand:candOutlierTuples) {
				Iterator<HashMap<Integer, Cell>> slideIterator = slides.descendingIterator();
				int currentSlideID = itr+1;
				
				SlideLoop:
				while(slideIterator.hasNext()) {
					HashMap<Integer, Cell> currentSlide = slideIterator.next();
					currentSlideID--;						
					if(tCand.unSafeOutNeighbors.containsKey(currentSlideID)) {
						continue SlideLoop;
					}else {
						tCand.unSafeOutNeighbors.put(currentSlideID,0);
					}
											
					CellLoop:
					//for(ArrayList<Integer> otherCellIdx: currentSlide.keySet()) {
					for(Integer otherCellIdx: candCellIndices) {
						if(!currentSlide.containsKey(otherCellIdx) 
							|| !neighboringTupleSet(tCand.value, currentSlide.get(otherCellIdx).cellCenter, 1.5*R)) //check if subdim is still ok with this
							continue CellLoop;
						
						HashSet<Tuple> otherTuples = new HashSet<Tuple>();
						if(subDimFlag) {
							for(Cell allIdxCell: currentSlide.get(otherCellIdx).childCells.values()) {
								if(!allIdxCell.cellIdx.equals(tCand.fullDimCellIdx) 
								   && neighboringSet(allIdxCell.cellIdx, tCand.fullDimCellIdx))
									otherTuples.addAll(allIdxCell.tuples);
							}
						}else{								
							otherTuples = currentSlide.get(otherCellIdx).tuples;
						}
						
						for (Tuple tOther: otherTuples) {
							if(neighboringTuple(tCand, tOther,R)) {
								if(tCand.slideID <= tOther.slideID) {
									tCand.nnSafeOut+=1;
								}else {
									tCand.nnUnSafeOut+=1;
									tCand.unSafeOutNeighbors.put(currentSlideID, tCand.unSafeOutNeighbors.get(currentSlideID) + 1);
								}
								if(tCand.nnSafeOut >= K) {
									if(outliers.contains(tCand)) outliers.remove(tCand);
									tCand.safeness = true;
									//tCand.truncate();
									continue TupleLoop;
								}
							}
						}
					}
					if (tCand.getNN() >= K) {
						if(outliers.contains(tCand)) outliers.remove(tCand);
						continue TupleLoop;
					}
				}
				outliers.add(tCand);
			}
			
			
		}		
		
	}	
	
	public double distTuple(Tuple t1, Tuple t2) {
		double ss = 0;
		for(int i = 0; i<dim; i++) { 
			ss += Math.pow((t1.value[i] - t2.value[i]),2);
		}
		 return Math.sqrt(ss);
	}
	
	public boolean neighboringTuple(Tuple t1, Tuple t2, double threshold) {
		double ss = 0;
		threshold *= threshold;
		for(int i = 0; i<dim; i++) { 
			ss += Math.pow((t1.value[i] - t2.value[i]),2);
			if(ss>threshold) return false;
		}
		return true;
	}

	public boolean neighboringTupleSet(double[] v1, double[] v2, double threshold) {
	
		double ss = 0;
		threshold *= threshold;
		for(int i = 0; i<v2.length; i++) { 
			ss += Math.pow((v1[i] - v2[i]),2);
			if(ss > threshold) return false;
		}
		 return true;
	}
	
	public double neighboringSetDist(ArrayList<Short> c1, ArrayList<Short> c2) {
		double ss = 0;
		double cellIdxDist = (c1.size() == dim ? neighCellFullDimIdxDist : neighCellIdxDist);
		double threshold = cellIdxDist*cellIdxDist;
		for(int k = 0; k<c1.size(); k++) {
			ss += Math.pow((c1.get(k) - c2.get(k)),2);
			if (ss >= threshold) return Double.MAX_VALUE;
		}
		 return Math.sqrt(ss);
	}
	
	public boolean neighboringSet(ArrayList<Short> c1, ArrayList<Short> c2) {
		double ss = 0;
		double cellIdxDist = (c1.size() == dim ? neighCellFullDimIdxDist : neighCellIdxDist);
		double threshold =cellIdxDist*cellIdxDist;
		for(int k = 0; k<c1.size(); k++) {
			ss += Math.pow((c1.get(k) - c2.get(k)),2);
			if (ss >= threshold) return false;
		}
		 return true;
	}

}