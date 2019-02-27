package main;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Cell {
	public ArrayList<Short> cellIdx;
	public HashMap<ArrayList<Short>,Cell> childCells;
	public HashSet<Tuple> tuples;
	double[] cellCenter;
	
	public Cell(ArrayList<Short> cellIdx){
		this.cellIdx = cellIdx;
		this.tuples = new HashSet<Tuple>();
	}

	
	public Cell(ArrayList<Short> cellIdx,  double[] cellCenter){
		this.cellIdx = cellIdx;
		this.tuples = new HashSet<Tuple>();
		this.cellCenter = cellCenter;
	}
	
	public Cell(ArrayList<Short> cellIdx, double[] cellCenter, Boolean subDimFlag){
		this.cellIdx = cellIdx;
		this.cellCenter = cellCenter;
		this.tuples = new HashSet<Tuple>();
		if(subDimFlag) this.childCells = new HashMap<ArrayList<Short>,Cell>();
	}
	
	public int getNumTuples() {
		return this.tuples.size();
	}
	
	public void addTuple(Tuple t, double[] fullDimCellCenter, Boolean subDimFlag) {
		this.tuples.add(t);
		if(subDimFlag) {
			if(!this.childCells.containsKey(t.fullDimCellIdx))
				this.childCells.put(t.fullDimCellIdx, new Cell(t.fullDimCellIdx, fullDimCellCenter));
			this.childCells.get(t.fullDimCellIdx).addTuple(t, fullDimCellCenter, false);
		}
	}
	
	public void addTuple(Tuple t,  Boolean subDimFlag) {
		this.tuples.add(t);
		if(subDimFlag) {
			if(!this.childCells.containsKey(t.fullDimCellIdx))
				this.childCells.put(t.fullDimCellIdx, new Cell(t.fullDimCellIdx));
			this.childCells.get(t.fullDimCellIdx).addTuple(t, false);
		}
	}
}
	