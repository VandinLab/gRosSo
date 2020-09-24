import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to reproduce the experiments with the pseudo-artificial datasets.
 * It also implements the observed frequency-based approaches.
 * The results are shown in Table 2 and 3 of the paper.
 */
public class ArtificialTest {

    String[] datasets;
    int[] size;
    Object2ObjectOpenHashMap<String,double[]> candidate;

    ArtificialTest(String[] datasets, int[] size){
        this.datasets = datasets;
        this.size = size;
        candidate = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Writes the mined patterns using the observed frequency-based approaches. 
     * @param  file  the output file
     */
    private void write(String file) throws IOException {
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        for (String sp: candidate.keySet()) {
            double[] freq = candidate.get(sp);
            bw.write(sp);
            for(int j=1;j<=freq.length;j++){
                bw.write(" freq_" + j + ": " + freq[j-1]);
            }
            bw.write("\n");
        }
        bw.close();
        fw.close();
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
     * Explores the i-th dataset to prune the starting candidate for the stable sequential patterns
     * using the observed frequency-based approach 
     * @param   i         the index of the dataset to explore
     * @param   alpha     the error threshold
     * @param   theta     the minimum frequency threshold  
     */
    private void exploreDatasetSP(int i,double alpha, double theta) throws IOException {
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
            if(freq>=theta) {
                for (int j = 0; j < i; j++) {
                    if (Math.abs(candidate.get(s)[j] - freq) > alpha) {
                        remove.add(s);
                    } else candidate.get(s)[i] = freq;
                }
            }
            else remove.add(s);
        }
        for(String s: remove) candidate.remove(s);
    }

    /**
     * Explores the i-th dataset to prune the starting candidate for the emerging sequential patterns
     * using the observed frequency-based approach 
     * @param   i         the index of the dataset to explore
     * @param   epsilon   the emerging threshold
     */
    private void exploreDatasetEP(int i,double epsilon) throws IOException {
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
            if(candidate.get(s)[i+1]-freq<=epsilon){
                remove.add(s);
            }
            else candidate.get(s)[i] = freq;
        }
        for(String s: remove) candidate.remove(s);
    }

    /**
     * Explores the i-th dataset to prune the starting candidate for the descending sequential patterns
     * using the observed frequency-based approach 
     * @param   i         the index of the dataset to explore
     * @param   epsilon   the emerging threshold
     */
    private void exploreDatasetDP(int i,double epsilon) throws IOException {
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
            if(candidate.get(s)[i-1]-freq<=epsilon){
                remove.add(s);
            }
            else candidate.get(s)[i] = freq;
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
    private double computeFrequency(Object[] s, ObjectArrayList<Object[]> dataset, IntOpenHashSet toCheck){
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
    private void loadCandidate(int i,String file){
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line=br.readLine())!=null){
                String[] splited = line.split("#SUP: ");
                double[] freq = new double[datasets.length];
                freq[i] = Double.parseDouble(splited[1])/(1.*size[i]);
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
     * Executes the observed frequency-based approach to mine emerging sequential patterns
     * @param  theta    the minimum frequency threshold used for all the datasets
     * @param  epsilon  the emerging threshold
     * @param  fileOut  the output file
     */
    void executeEP(double theta, double epsilon, String fileOut, boolean gt) throws IOException {
        String fileC = datasets[datasets.length-1].split("\\.txt")[0];
        fileC+=(System.currentTimeMillis())+"_mined.txt";
        mining(datasets[datasets.length-1],fileC,theta);
        loadCandidate(datasets.length-1,fileC);
        for(int j=datasets.length-2;j>=0;j--){
            exploreDatasetEP(j,epsilon);
        }
        if(gt) System.out.println("GT EP: " + candidate.size());
        write(fileOut);
        File del = new File(fileC);
        del.delete();
    }

    /**
     * Executes the observed frequency-based approach to mine stable sequential patterns
     * @param  theta    the minimum frequency threshold used for all the datasets
     * @param  alpha    the error threshold
     * @param  fileOut  the output file
     */
    void executeSP(double theta, double alpha, String fileOut, boolean gt) throws IOException {
        String fileC = datasets[0].split("\\.txt")[0];
        fileC+=(System.currentTimeMillis())+"_mined.txt";
        mining(datasets[0],fileC,theta);
        loadCandidate(0,fileC);
        for(int j=1;j<datasets.length;j++){
            exploreDatasetSP(j,alpha,theta);
        }
        if(gt) System.out.println("GT SP: " + candidate.size());
        write(fileOut);
        File del = new File(fileC);
        del.delete();
    }

    /**
     * Executes the observed frequency-based approach to mine descending sequential patterns
     * @param  theta    the minimum frequency threshold used for all the datasets
     * @param  epsilon  the emerging threshold
     * @param  fileOut  the output file
     */
    void executeDP(double theta, double epsilon, String fileOut, boolean gt) throws IOException {
        String fileC = datasets[0].split("\\.txt")[0];
        fileC+=(System.currentTimeMillis())+"_mined.txt";
        mining(datasets[0],fileC,theta);
        loadCandidate(0,fileC);
        for(int j=1;j<datasets.length;j++){
            exploreDatasetDP(j,epsilon);
        }
        if(gt) System.out.println("GT DP: " + candidate.size());
        write(fileOut);
        File del = new File(fileC);
        del.delete();
    }

    /**
     * Computes the statistics in terms of false positives
     * @param  gtFile   the file congaing the ground truth
     * @param  epsilon  the file congaing the patterns mined using gRosSo or the observed frequency-based approaches 
     * @return the computed statistics
     */
    static double[] percentageFP(String gtFile, String file){
        double[] res = new double[2];
        int tot = 0;
        int FP = 0;
        try {
            FileReader fr = new FileReader(gtFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            ObjectOpenHashSet<String> gt = new ObjectOpenHashSet<>();
            while((line=br.readLine())!=null){
                String[] splited = line.split("  ");
                gt.add(splited[0]);
            }
            br.close();
            fr.close();
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            while((line=br.readLine())!=null){
                String[] splited = line.split("  ");
                if(!gt.contains(splited[0])) FP++;
                tot++;
            }
            br.close();
            fr.close();
            res[0] = FP/(tot*1.);
            res[1] = tot/(gt.size()*1.);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
    * This main executes all the experiments with pseudo-artificial datasets shown in the paper  
    */
    public static void main(String[] args) throws IOException {
        String[] datasets = {"../data/2005t1_SPMF.txt", "../data/2005t2_SPMF.txt", "../data/2005t3_SPMF.txt"};
        int[] size = {290287,331117,326668};

        // Choose the parameters
        double theta = 0.3;
        double alpha = 0.1;
        double epsilon = 0.01;

        System.out.println("Theta: " + theta);
        System.out.println("Alpha: " + alpha);
        System.out.println("Epsilon: " + epsilon);

        // Create ground truth for the EP
        ArtificialTest at = new ArtificialTest(datasets,size);
        at.executeEP(theta,epsilon,"../data/GT_EP.txt",true);
        // Create ground truth for the DP
        at = new ArtificialTest(datasets,size);
        at.executeDP(theta,epsilon,"../data/GT_DP.txt",true);
        // Create ground truth for the SP
        at = new ArtificialTest(datasets,size);
        at.executeSP(theta,alpha,"../data/GT_SP.txt",true);

        // Create 5 random sequences with fixed seed for reproducibility with size replicationFactor*originalSize
        int replicationFactor = 1;
        System.out.println("Replication Factor for Random Datasets: " + replicationFactor);
        int[] sampleSize = {290287*replicationFactor,331117*replicationFactor,326668*replicationFactor};
        for(int j=0;j<datasets.length;j++) {
            for (int i = 1; i < 6; i++) {
                ArtificialDataset.randomDataset(datasets[j],"../data/sample2005t"+(j+1)+"_"+i+".txt",sampleSize[j],i);
            }
        }

        double delta = 0.1;
        for (int i = 1; i < 6; i++) {
            String[] samples = {"../data/sample2005t1_"+i+".txt", "../data/sample2005t2_"+i+".txt", "../data/sample2005t3_"+i+".txt"};
            // Mining of the EP using the observed frequencies in the samples
            at = new ArtificialTest(samples,size);
            at.executeEP(theta,epsilon,"../data/sample_"+i+"_EP_freq.txt",false);
            // Mining of the DP using the observed frequencies in the samples
            at = new ArtificialTest(samples,size);
            at.executeDP(theta,epsilon,"../data/sample_"+i+"_DP_freq.txt",false);
            // Mining of the SP using the observed frequencies in the samples
            at = new ArtificialTest(samples,size);
            at.executeSP(theta,alpha,"../data/sample_"+i+"_SP_freq.txt",false);

            // Mining of the EP using gRosSo in the samples
            EP ep = new EP(samples);
            ep.executeTheta(delta,epsilon,theta,"../data/sample_"+i+"_EP_grosso.txt");
            // Mining of the DP using gRosSo in the samples
            DP dp = new DP(samples);
            dp.executeTheta(delta,epsilon,theta,"../data/sample_"+i+"_DP_grosso.txt");
            // Mining of the SP using gRosSo in the samples
            SP sp = new SP(samples);
            sp.executeThetaAll(delta,alpha,theta,"../data/sample_"+i+"_SP_grosso.txt");
        }

        // Compute artificial tests statistics
        int p_EP = 0;
        int p_DP = 0;
        int p_SP = 0;
        for(int i=1;i<6;i++) {
            if (percentageFP("../data/GT_EP.txt", "../data/sample_" + i + "_EP_freq.txt")[0] > 0) p_EP++;
            if (percentageFP("../data/GT_DP.txt", "../data/sample_" + i + "_DP_freq.txt")[0] > 0) p_DP++;
            if (percentageFP("../data/GT_SP.txt", "../data/sample_" + i + "_SP_freq.txt")[0] > 0) p_SP++;
        }
        System.out.println("Times FP_frequency EP: " + (p_EP/5.));
        System.out.println("Times FP_frequency DP: " + (p_DP/5.));
        System.out.println("Times FP_frequency SP: " + (p_SP/5.));

        p_EP = 0;
        p_DP = 0;
        p_SP = 0;
        double perc_EP =0;
        double perc_DP =0;
        double perc_SP =0;
        double[] res;
        for(int i=1;i<6;i++){
            res = percentageFP("../data/GT_EP.txt","../data/sample_" + i + "_EP_grosso.txt");
            if(res[0]>0) p_EP++;
            perc_EP+=res[1];
            res = percentageFP("../data/GT_DP.txt","../data/sample_" + i + "_DP_grosso.txt");
            if(res[0]>0) p_DP++;
            perc_DP+=res[1];
            res = percentageFP("../data/GT_SP.txt","../data/sample_" + i + "_SP_grosso.txt");
            if(res[0]>0) p_SP++;
            perc_SP+=res[1];
        }
        System.out.println("Times FP_gRosSo EP: " + (p_EP/5.) + " |A|/|GT|: " + (perc_EP/5.));
        System.out.println("Times FP_gRosSo DP: " + (p_DP/5.) + " |A|/|GT|: " + (perc_DP/5.));
        System.out.println("Times FP_gRosSo SP: " + (p_SP/5.) + " |A|/|GT|: " + (perc_SP/5.));
    }
}