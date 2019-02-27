package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import utils.StreamGenerator;
import main.Tuple;

public class Prioritizer {
	public static void main(String[] args) throws IOException {
		String dataset = "EM";
		StreamGenerator streamGen = new StreamGenerator(dataset, 0);
		int dim = streamGen.getMaxValues().length;
		Integer[] defaultList = new Integer[dim];
		for(int i = 0; i<dim; i++) defaultList[i] = i;
		streamGen.setPriorityList(defaultList);
		
		ArrayList<Tuple> sample = streamGen.getNewSlideTuples(0, 100000);
		HashMap<Integer, ArrayList<Double>> valuesMap = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, Double> VMRMap = new HashMap<Integer, Double>();
		for(int i = 0; i <dim ; i++) valuesMap.put(i, new ArrayList<Double>());
		
		for(Tuple t:sample) {
			for(int i = 0; i <dim ; i++) {
				valuesMap.get(i).add(t.value[i]);
			}
		}
		
		for(int i = 0; i <dim ; i++) {
			double max = getMax(valuesMap.get(i));
			double min = getMin(valuesMap.get(i));
			double gap = (max-min)/100;
			double[] cnt = new double[100];
			for(Double d:valuesMap.get(i)) {
				int idx = (int) Math.round((d-min)/gap);
				if(idx == 100) idx = 99;
				cnt[idx]++;
			}
			
			double mean = getMean(cnt);
			double var = getVariance(cnt, mean);
			double VMR = var/mean;
			VMRMap.put(i, VMR);
			//System.out.println("Dim "+i+": "+VMR);
		}
		
		HashMap<Double,Integer> VMRMap_Inv = new HashMap<Double,Integer>();
		
		for(int key:VMRMap.keySet()) {
			if(VMRMap_Inv.containsKey(VMRMap.get(key)))
				VMRMap.put(key,VMRMap.get(key)+Math.random());
			VMRMap_Inv.put(VMRMap.get(key), key);
		}
		Object[] sortedVMRs = VMRMap_Inv.keySet().toArray();
		Arrays.sort(sortedVMRs);
		for(int i = 0; i <dim; i++) {
			System.out.print(VMRMap_Inv.get(sortedVMRs[i])+",");
			//System.out.println(sortedVMRs[i]);
		}
	}
	
    static double getMean(double[] data) {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.length;
    }
    
    static double getMax(ArrayList<Double> data) {
        double max = Double.MIN_VALUE;
        for(double a : data) {
        	if(a>max) max = a;
        }
        return max;
    }
    
    static double getMin(ArrayList<Double> data) {
        double min = Double.MAX_VALUE;
        for(double a : data) {
        	if(a<min) min = a;
        }
        return min;
    }

    static double getVariance(double[] data, double mean) {
    	double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(data.length-1);
    }

}
