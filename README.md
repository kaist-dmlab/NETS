# NETS:Extremely Fast Outlier Detection from a Data Stream via Set-Based Processing

## 1. Overview
This paper addresses the problem of efficiently detecting outliers from a data stream as old data points expire from and new data points enter the window incrementally. The proposed method is based on a newly discovered characteristic of a data stream, that is, the change in the locations of data points in the data space is typically very insignificant. This observation has led to the finding that the existing distance-based outlier detection algorithms perform excessive unnecessary computations that are repetitive and/or cancelling out the effects. Thus, in this paper, we propose a novel *set-based* approach to detecting outliers, whereby data points at similar locations are grouped and the detection of outliers or inliers is handled at the group level. Specifically, a new algorithm NETS is proposed to achieve a remarkable performance improvement by realizing set-based early identification of outliers or inliers and taking advantage of the "net effect" between expired data points and new data points. Additionally, NETS is capable of achieving the same efficiency even for a high-dimensional data stream through *two dimensional-level filtering*.  Comprehensive experiments using six real-world data streams show 5 to 150 times faster processing time than state of the art algorithms with comparable memory consumption. We assert that NETS opens a new possibility to real-time data stream outlier detection.

## 2. Algorithms
- **NETS**: our algorithm 
- MCOD [1]([source code[3]](https://infolab.usc.edu/Luan/Outlier/CountBasedWindow/DODDS/))
- LEAP [2]([source code[3]](https://infolab.usc.edu/Luan/Outlier/CountBasedWindow/DODDS/))

>__*Reference*__</br>
[1]   M. Kontaki, A. Gounaris, A. N. Papadopoulos, K. Tsichlas, and Y. Manolopoulos, "Continuous monitoring of distance-based outliers over data streams," in 2011 IEEE 27th International Conference on Data Engineering, pp. 135-146, 2011.</br>
[2] L. Cao, D. Yang, Q. Wang, Y. Yu, J. Wang, and E. A. Rundensteiner, "Scalable distance-based outlier detection over high-volume data streams," in 2014 IEEE 30th International Conference on Data Engineering, pp. 76-87, 2014.</br>
[3] L. Tran, L. Fan, and C. Shahabi, "Distance-based outlier detection in data streams," in Proceedings of the VLDB Endowment, vol. 9, pp. 1089-1100, 2016.</br>

## 3. Data Sets
| Name    | # data points  | # Dim    | Size    | Link           |
| :-----: | :------------: | :------: |:-------:|:--------------:|
| GAU     | 1M             | 1        |  7.74MB  |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/gaussian.txt) |
| STK     | 1.05M          | 1        |  7.57MB |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/stock.txt) |
| TAO     | 0.58M          | 3        |  10.7MB |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/tao.txt) |
| HPC     | 1M             | 7        |  28.4MB  |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/household2.txt) |
| GAS     | 0.93M          | 10       |  70.7MB  |[link](http://archive.ics.uci.edu/ml/machine-learning-databases/00362/HT_Sensor_UCIsubmission.zip) |
| EM      | 1M             | 16       |  119MB  |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/ethylene.txt) |
| FC      | 1M             | 55       |  72.2MB  |[link](https://infolab.usc.edu/Luan/Outlier/Datasets/fc.data) |

## 4. Configuration
NETS algorithm was implemented in JAVA and run on **JDK 1.8.0_191.**
- Compile
```
cd ~/NETS/src
javac test/testBase.java
```

## 5. How to run
- Parameter options
```
--dataset: title of datasets (string, one of {GAU, STK, TAO, HPC, GAS, EM, FC})
--W: the size of a window (integer)
--S: the size of a slide (integer)
--R: the distance threshold (double)
--K: the number of neighbors threshold (integer)
--D: the number of full dimensions (integer)
--sD: the number of sub dimensions (integer)
--nW: the number of windows (integer)
```

- Run a sample experiment
```
cd ~/NETS/src
java test.testBase --dataset TAO --W 10000 --S 500 --R 1.9 --K 50 --D 3 --sD 3 --nW 1
```
- Check the result
```
cd ~/NETS/src/Result
cat Result_TAO_NETS_D3_sD3_rand0_R1.9_K50_S500_W10000_nW1.txt
At window 0, # outliers: 169
# Dataset: TAO
Method: NETS
Dim: 3
subDim: 3
R/K/W/S: 1.9/50/10000/500
# of windows: 1
Avg CPU time(s) 	 Peak memory(MB)
0.0025	4.3
```
