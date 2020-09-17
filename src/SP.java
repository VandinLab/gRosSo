import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * This class is the implementation of gRosSo to mine stable sequential patterns.
 * The results are shown in Table 5 of the paper.
 */
public class SP {

    private String[] datasets;
    private int[] size;
    private double[] mu;
    private HashMap<String,double[]> candidate;
    SP(String[] datasets){
        this.datasets = datasets;
        size = new int[datasets.length];
        mu = new double[datasets.length];
        candidate = new HashMap<>();
    }

    /**
    * This main mines the stable sequential patterns using the chosen parameters from the input datasets
    * and stores the results in the provided output file  
    */
    public static void main(String[] args) throws IOException {
        // the parameters
        double theta = 0.2;
        double alpha = 0.1;
        double delta = 0.1;
        // the input datasets
        String[] datasets = {"../data/2005q1_SPMF.txt","../data/2005q2_SPMF.txt","../data/2005q3_SPMF.txt","../data/2005q4_SPMF.txt"};
        // the output dataset
        String fileOut = "../data/2005q_SP_1_2.txt";
        SP sp = new SP(datasets);
        sp.executeThetaAll(delta,alpha,theta,fileOut);
    }

    /**
     * Writes the mined stable sequential patterns in the provided file
     * @param  file  the output file
     */
    private void write(String file) throws IOException {
        System.out.println("Stable Sequential Patterns Found: " + candidate.size());
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        for (String sp: candidate.keySet()) {
            double[] freq = candidate.get(sp);
            bw.write(sp);
            for(int j=0;j<freq.length;j++){
                bw.write(" freq_" + (j+1) + ": " + freq[j]);
            }
            bw.write("\n");
        }
        bw.close();
        fw.close();
    }

    /**
     * Executes gRosSo to mine stable sequential patterns
     * @param  delta    the confidence parameter
     * @param  alpha    the error threshold
     * @param  theta    the minimum frequency threshold used for all the datasets
     * @param  fileOut  the output file
     */
    void executeThetaAll(double delta, double alpha, double theta, String fileOut) throws IOException {
        double[] minFreq = new double[datasets.length];
        int iMax = -1;
        double maxMinFreq = 0;
        for(int i=0;i<datasets.length;i++){
            computeMaxDev(i,delta/(datasets.length*1.));
            minFreq[i] = theta + mu[i];
            if(minFreq[i]>maxMinFreq){
                maxMinFreq = minFreq[i];
                iMax = i;
            }
        }
        String fileC = datasets[iMax].split("\\.txt")[0];
        fileC+=(System.currentTimeMillis())+"_mined.txt";
        mining(datasets[iMax],fileC,minFreq[iMax]);
        loadCandidate(iMax,fileC);
        for(int j=0;j<datasets.length;j++){
            if(j!=iMax) exploreDataset(j,alpha,theta);
        }
        write(fileOut);
        File del = new File(fileC);
        del.delete();
    }

    /**
     * Computes an upper bound on the maximum deviation using an upper bound on the VC-dimension of sequential patterns
     * for the index-th dataset
     * @param   index    the index of the dataset
     * @param   delta    the confidence parameter
     */
    private void computeMaxDev(int index, double delta) {
        int datasetSize = 0;
        int sIndex = 0;
        ArrayList<Triple> orderedSet = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        try {
            FileReader fr = new FileReader(datasets[index]);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                datasetSize++;
                if (!set.contains(line)) {
                    BigInteger cap = CapacityTest.getCapacity(line);
                    if (cap.compareTo(new BigInteger("2").pow(sIndex).subtract(BigInteger.ONE)) > 0) {
                        Object[] curr = convert(line);
                        boolean sub = false;
                        for (Triple t : orderedSet) {
                            sub = isSubsequence(curr, t.getValue());
                            if (sub || (cap.compareTo(t.getCapacity()) > 0)) break;
                        }
                        if (!sub) {
                            set.add(line);
                            int i = 0;
                            while (i < orderedSet.size() && (orderedSet.get(i).getCapacity().compareTo(cap) > 0)) i++;
                            orderedSet.add(i, new Triple(line, cap, curr));
                            if (orderedSet.get(orderedSet.size() - 1).getCapacity().compareTo(new BigInteger("2").pow(sIndex).subtract(BigInteger.ONE)) > 0)
                                sIndex++;
                            else {
                                String removable = orderedSet.remove(orderedSet.size() - 1).getSequence();
                                set.remove(removable);
                            }
                        }
                    }
                }
            }
            br.close();
            fr.close();
            mu[index] = Math.sqrt(1 / (2. * datasetSize) * (sIndex + Math.log(1. / delta)));
            size[index] = datasetSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a transaction from a string to an array of itemsets, that are arrays of int
     * @param   s   the transaction
     * @return  the converted transaction
     */
    private Object[] convert(String s) {
        String[] splited = s.split("-2")[0].split(" -1 ");
        Object[] transaction = new Object[splited.length];
        int j = 0;
        for (String itemset_string : splited) {
            String[] splited2 = itemset_string.split(" ");
            int[] itemset = new int[splited2.length];
            int i = 0;
            for (String item_string : splited2) {
                int item = Integer.parseInt(item_string);
                itemset[i++] = item;
            }
            transaction[j++] = itemset;
        }
        return transaction;
    }

    /**
     * Converts a transaction from a string to an array of itemsets, that are arrays of int, and updates
     * an index structure that stores the transactions in which an itemset appears
     * @param   s         the transaction
     * @param   transac   the index of the transaction
     * @param   itemsMap  an index structure that stores the transactions in which an itemset appears   
     * @return  the converted transaction
     */
    private Object[] convertAndUpdate(String s, int transac, Int2ObjectOpenHashMap<IntOpenHashSet> itemsMap){
        String[] splited = s.split("-2")[0].split(" -1 ");
        Object[] transaction = new Object[splited.length];
        int j = 0;
        for(String itemset_string: splited){
            String[] splited2 = itemset_string.split(" ");
            int[] itemset = new int[splited2.length];
            int i = 0;
            for(String item_string: splited2){
                int item = Integer.parseInt(item_string);
                itemset[i++] = item;
                if(itemsMap.containsKey(item)){
                    itemsMap.get(item).add(transac);
                }
                else{
                    IntOpenHashSet transactions = new IntOpenHashSet();
                    transactions.add(transac);
                    itemsMap.put(item,transactions);
                }
            }
            transaction[j++] = itemset;
        }
        return transaction;
    }

    /**
     * Explores the i-th dataset to prune the starting candidate
     * @param   i         the index of the dataset to explore
     * @param   alpha     the error threshold
     * @param   theta     the minimum frequency threshold  
     */
    private void exploreDataset(int i,double alpha,double theta) throws IOException {
        ObjectArrayList<Object[]> dataset = new ObjectArrayList<>();
        Int2ObjectOpenHashMap<IntOpenHashSet> itemsMap = new Int2ObjectOpenHashMap<>();
        FileReader fr = new FileReader(datasets[i]);
        BufferedReader br = new BufferedReader(fr);
        String line;
        int transac = 0;
        while((line=br.readLine())!=null){
            dataset.add(convertAndUpdate(line,transac++,itemsMap));
        }
        br.close();
        fr.close();
        Set<String> remove = new HashSet<>();
        for (String s: candidate.keySet()){
            IntOpenHashSet toCheck = null;
            String[] splited = s.split(" -1 ");
            Object[] transaction = new Object[splited.length];
            int k = 0;
            for(String itemset_string: splited){
                String[] splited2 = itemset_string.split(" ");
                int[] itemset = new int[splited2.length];
                int l = 0;
                for(String item_string: splited2){
                    int item = Integer.parseInt(item_string);
                    itemset[l++] = item;
                    if(!itemsMap.containsKey(item)) toCheck = new IntOpenHashSet();
                    else {
                        if (toCheck == null) {
                            toCheck = itemsMap.get(item).clone();
                        } else {
                            toCheck.retainAll(itemsMap.get(item));
                        }
                    }
                }
                transaction[k++] = itemset;
            }
            double freq = computeFrequency(transaction,dataset,toCheck);
            if(freq-mu[i]>=theta) {
                boolean rem =  false;
                for(int j=0;j<datasets.length && !rem;j++) {
                    double f2 = candidate.get(s)[j];
                    if(f2!=-1) {
                        if ((freq + mu[i] - (f2 - mu[j]) > alpha) || (f2 + mu[j] - (freq - mu[i]) > alpha)) {
                            remove.add(s);
                            rem = true;
                        }
                        if (!rem) candidate.get(s)[i] = freq;
                    }
                }
            }
            else remove.add(s);
        }
        for(String s: remove) candidate.remove(s);
    }

    /**
     * Check if the sequence min is a subsequence of sequence max
     * @param   min  the first sequence
     * @param   max  the second sequence
     * @return  true if min is a subsequence of max, false otherwise
     */
    private boolean isSubsequence(Object[] min,Object[] max){
        if(min.length>max.length) return false;
        int i = 0;
        int j = 0;
        while(i<min.length && j<max.length) {
            int[] itemsetMin = (int[])min[i];
            int[] itemsetMax = (int[])max[j];
            if (itemsetMin.length <= itemsetMax.length) {
                int l = 0;
                int k = 0;
                while (k < itemsetMin.length && l < itemsetMax.length) {
                    if (itemsetMax[l++]==itemsetMin[k]) k++;
                }
                if (k == itemsetMin.length) i++;
            }
            j++;
        }
        return i == min.length;
    }

    /**
     * Computes the frequency of the input sequence in the input dataset considering the transactions
     * stored in the index structure
     * @param   s         the input sequence
     * @param   dataset   the input dataset
     * @param   toCheck   an index structure that stores the transactions in which an itemset appears
     * @return  the computed frequency
     */
    double computeFrequency(Object[] s, ObjectArrayList<Object[]> dataset, IntOpenHashSet toCheck){
        int supp = 0;
        for (int i: toCheck) {
            if(isSubsequence(s,dataset.get(i))) supp++;
        }
        return supp/(dataset.size()*1.);
    }

    /**
     * Loads the FSP mined and stored in the provided file
     * @param   index     the index of the mined dataset
     * @param   file      the file that contains the FSP starting candidates
     */
    void loadCandidate(int index, String file){
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line=br.readLine())!=null){
                String[] splited = line.split("#SUP: ");
                double[] freq = new double[datasets.length];
                for(int k=0;k<freq.length;k++) freq[k] = -1;
                freq[index] = Double.parseDouble(splited[1])/(1.*size[index]);
                candidate.put(splited[0],freq);
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mines the input dataset and stored the FSP in the output file using the PrefixSpan algorithm
     * @param   fileIn    the input dataset
     * @param   fileFSP   the output file to store the FSP
     * @param   theta     the minimum frequency threshold
     * @return  the number of mined FSP
     */
    private int mining(String fileIn, String fileFSP, double theta){
        try {
            AlgoPrefixSpan alg = new AlgoPrefixSpan();
            alg.runAlgorithm(fileIn, theta, fileFSP);
            return alg.patternCount;
        }catch(IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Private class that implements a simple triple structure
     */
    private class Triple implements Comparable<Triple> {
        Object[] val;
        private String sp;
        private BigInteger cap;
        Triple(String sp, BigInteger cap, Object[] val) {
            this.sp = sp;
            this.cap = cap;
            this.val = val;
        }
        BigInteger getCapacity() {
            return cap;
        }
        String getSequence() {
            return sp;
        }
        Object[] getValue() {
            return val;
        }
        public int compareTo(Triple p2) {
            return -cap.compareTo(p2.getCapacity());
        }
    }
}