# gRosSo: Mining Statistically Robust Patterns from a Sequence of Datasets
## Authors: Andrea Tonon (andrea.tonon@dei.unipd.it) and Fabio Vandin (fabio.vandin@unipd.it)
This repository contains the implementation of the algorithms introduced in the IEEE ICDM 2020 paper *gRosSo: Mining Statistically Robust Patterns from a Sequence of Datasets* and the code that has been used to test their performance.

The code has been developed in Java and executed using version 1.8.0_201. 
To mine sequential patterns, we used the PrefixSpan implementation provided by the [SPMF library](https://www.philippe-fournier-viger.com/spmf/). We also used the [fastutil library](http://fastutil.di.unimi.it) that provides type-specific maps, sets, lists and queues with a small memory footprint and fast access and insertion, for the data structures.

## Package Description
The package contains the following folders:
*	src/: contains the source code 
*	data/: contains the datasets used in the evaluation (they must be generated using the provided code, see Section Datasets).
*	lib/: contains the jar of the fastutil library.

## Compile
These are the instructions to compile the code (from the gRosSo folder):

```
javac -cp ./lib/fastutil-8.3.1.jar src/*.java
```
## Datasets
To generate the datasets used in the evaluation, you must download the [Netflix Prize Data](https://www.kaggle.com/netflix-inc/netflix-prize-data), decompress the folder and put the following files in the data/ folder:
*	movie_titles
*	combined_data_1
*	combined_data_2
*	combined_data_3
*	combined_data_4

Then, from the src/ folder execute:

```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. NetflixDataset
```  
This code generates all the real datasets used in the evaluation and stores them in the data/ folder. It also writes to the standard output the characteristics of all the generated datasets.

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. NetflixDataset > datasetsChar.txt &
``` 
where datasetsChar.txt is the file to store the standard output of the class.

## Reproducibility
We provide the source code to replicate the results shown in Section 6 of the paper. 

### Upper Bound to the Capacity
Usage:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. CapacityTest
```

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

The program writes to the standard output all results shown in Table 1 of the paper.

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. CapacityTest > capacityTest.txt &
``` 
where capacityTest.txt is the file to store the standard output of the class. 

### Results with Pseudo-Artificial Datasets
Usage:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. ArtificialTest
```

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

The parameters (alpha, epsilon, theta and the replication factor for the artificial datasets) must be changed directly in the source code. Remember to re-compile if you modified the source code. 

The program writes to the standard output all results shown in Table 2 and 3 of the paper.

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. ArtificialTest > artificialTest.txt &
``` 

where artificialTest.txt is the file to store the standard output of the class.

### Results with Real Datasets

#### Emerging Sequential Patterns
Usage:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. EP
```

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

The parameters (epsilon, delta, theta, and the input/output datasets) must be changed directly in the source code. Remember to re-compile if you modified the source code.

If you want to mine the emerging sequential patterns considering a minimum frequency threshold, you have to change the source code, following the instructions in the file.   

The program writes to the standard output the results shown in Table 4 of the paper.

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. EP > EPTest.txt &
``` 
where EPTest.txt is the file to store the standard output of the class.

#### Descending Sequential Patterns
Usage:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. DP
```

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

The parameters (epsilon, delta, theta, and the input/output datasets) must be changed directly in the source code. Remember to re-compile if you modified the source code.

If you want to mine the descending sequential patterns considering a minimum frequency threshold, you have to change the source code, following the instructions in the file.   

The program writes to the standard output the results shown in Table 4 of the paper.

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. DP > DPTest.txt &
``` 
where DPTest.txt is the file to store the standard output of the class.

#### Stable Sequential Patterns
Usage:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. SP
```

-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 50G).

The parameters (alpha, delta, theta, and the input/output datasets) must be changed directly in the source code. Remember to re-compile if you modified the source code.
  
The program writes to the standard output the results shown in Table 5 of the paper.

If you prefer to launch the execution in background storing the standard output of the class in a file, execute:
```
java -XmxRG -cp ../lib/fastutil-8.3.1.jar:. SP > SPTest.txt &
``` 
where SPTest.txt is the file to store the standard output of the class.

## gRosSo with other Datasets
If you want to execute gRosSo using your own datasets to mine statistically robust sequential patterns, you can follow the instructions to reproduce the experiments of the paper on real datasets, changing the input/output datasets and the desired parameters for the execution directly in the source code.  
	
## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
