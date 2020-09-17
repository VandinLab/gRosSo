import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * This class implements 3 algorithms to compute the capacity of a sequence:
 * - our proposed method
 * - the method described in Reference [19] of the paper 
 * - the naive approach
 * and it is used to compare the three algorithms.
 * The results obtained with this class are shown in Table 1 of the paper.
 */

public class CapacityTest {

    /**
     * Private class that implements a simple and generic pair structure
     */
    private static class Pair<K, V> {
        K key;
        V value;
        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        K getKey() {
            return key;
        }
        V getValue() {
            return value;
        }
    }

    /**
     * Compute the capacity of a sequence using the naive approach.
     * @param   tr  the input sequence
     * @return  the capacity of the input sequence
     */
    private static BigInteger getCapacityNaive(String tr) {
        String[] tokens = tr.split(" ");
        int length = 0;
        for (int j = 0; j < tokens.length - 2; j++) {
            int current = Integer.parseInt(tokens[j]);
            if (current > 0) {
                length++;
            }
        }
        BigInteger capacity = new BigInteger("2").pow(length).subtract(BigInteger.ONE);
        return capacity;
    }

    /**
     * Compute the capacity of a sequence using the method described in [19].
     * @param   tr  the input sequence
     * @return  the capacity of the input sequence
     */
    private static BigInteger getCapacityProsecco(String tr) {
        String[] tokens = tr.split(" ");
        int length = 0;
        ObjectArrayList<IntArrayList> sequence = new ObjectArrayList<>();
        IntArrayList itemset = new IntArrayList();
        for (int j = 0; j < tokens.length - 1; j++) {
            int current = Integer.parseInt(tokens[j]);
            if (current > 0) {
                length++;
                itemset.add(current);
            } else {
                sequence.add(itemset);
                itemset = new IntArrayList();
            }
        }
        sequence.sort(((o1, o2) -> {
            if (o1.size() > o2.size()) return -1;
            if (o1.size() < o2.size()) return 1;
            return 0;
        }));
        BigInteger capacity = new BigInteger("2").pow(length).subtract(BigInteger.ONE);
        for (int i = 0; i < sequence.size() - 1; i++) {
            IntArrayList current = sequence.get(i);
            for (int k = i + 1; k < sequence.size(); k++) {
                IntArrayList itemsett = sequence.get(k);
                if (isSubsequencePros(itemsett, current)) {
                    sequence.remove(itemsett);
                    capacity = capacity.subtract(new BigInteger("2").pow(itemsett.size()).subtract(BigInteger.ONE));
                }
            }
        }
        return capacity;
    }

    /**
     * Check if itemset min is a subset of itemset max
     * @param   min  the first itemset
     * @param   max  the second itemset
     * @return  true if min is a subset of max, false otherwise
     */
    private static boolean isSubsequencePros(IntArrayList min, IntArrayList max) {
        int i = 0;
        int j = 0;
        while (i < min.size()) {
            while (j < max.size() && min.getInt(i) != max.getInt(j)) j++;
            if (j == max.size() || max.size() - j < min.size() - i) return false;
            i++;
            j++;
        }
        return true;
    }

    /**
     * Compute the capacity of a sequence using our method.
     * @param   tr  the input sequence
     * @return  the capacity of the input sequence
     */
    static BigInteger getCapacity(String tr) {
        String[] tokens = tr.split(" ");
        int length = 0;
        ObjectArrayList<Pair<IntArrayList, Integer>> sequence = new ObjectArrayList<>();
        IntArrayList itemset = new IntArrayList();
        for (int j = 0; j < tokens.length - 1; j++) {
            int current = Integer.parseInt(tokens[j]);
            if (current > 0) {
                length++;
                itemset.add(current);
            } else {
                sequence.add(new Pair(itemset, length - itemset.size()));
                itemset = new IntArrayList();
            }
        }
        sequence.sort(((o1, o2) -> {
            if (o1.getKey().size() > o2.getKey().size()) return 1;
            if (o1.getKey().size() < o2.getKey().size()) return -1;
            return 0;
        }));
        BigInteger capacity = new BigInteger("2").pow(length).subtract(BigInteger.ONE);
        int len = length;
        for (int i = 0; i < sequence.size() - 1; i++) {
            BigInteger maxValue = BigInteger.ZERO;
            BigInteger currValue;
            Pair<IntArrayList, Integer> min = sequence.get(i);
            for (int j = i + 1; j < sequence.size(); j++) {
                Pair<IntArrayList, Integer> max = sequence.get(j);
                IntArrayList intersection = min.getKey().clone();
                intersection.retainAll(max.getKey());
                if (intersection.size() > 0) {
                    currValue = new BigInteger("2").pow(Math.min(max.getValue(), min.getValue()));
                    currValue = currValue.multiply((new BigInteger("2").pow(intersection.size())).subtract(BigInteger.ONE));
                    currValue = currValue.multiply(new BigInteger("2").pow(len - Math.max(max.getValue() + max.getKey().size(), min.getValue() + min.getKey().size())));
                    if (maxValue.compareTo(currValue) < 0) maxValue = currValue;
                }
            }
            if (!maxValue.equals(BigInteger.ZERO)) {
                len -= min.getKey().size();
                capacity = capacity.subtract(maxValue);
                for (int k = i + 1; k < sequence.size(); k++) {
                    Integer curr = sequence.get(k).getValue();
                    if (curr > min.getValue()) {
                        Pair<IntArrayList, Integer> currPair = sequence.remove(k);
                        sequence.add(k, new Pair<>(currPair.getKey(), curr - min.getKey().size()));
                    }
                }
            }
        }
        return capacity;
    }

    /**
     * Compute the capacity of each transaction of the input dataset using the three methods and
     * compute and output the averages (over all transactions) of the relative differences between
     * our novel method and the previously proposed ones. 
     * @param   file  the input dataset
     */
    static private void compareCapacities(String file) {
        int datasetSize = 0;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            BigDecimal NN = BigDecimal.ZERO;
            BigDecimal PN = BigDecimal.ZERO;
            while ((line = br.readLine()) != null) {
                datasetSize++;
                BigDecimal capNaive = new BigDecimal(getCapacityNaive(line));
                BigDecimal capPros = new BigDecimal(getCapacityProsecco(line));
                BigDecimal capOur = new BigDecimal(getCapacity(line));
                NN = NN.add((capNaive.subtract(capOur)).divide(capNaive, 10, RoundingMode.HALF_UP));
                PN = PN.add((capPros.subtract(capOur)).divide(capPros, 10, RoundingMode.HALF_UP));
            }
            br.close();
            fr.close();
            double nn = NN.doubleValue();
            double pn = PN.doubleValue();
            nn *= 100. / datasetSize;
            pn *= 100. / datasetSize;
            System.out.println(file);
            System.out.println("Delta_no: " + nn + "%  Delta_po: " + pn + "%");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This main computes and compares the algorithms to compute the capacity of a sequence using
     *  all the datasets considered in the paper. The results are shown in Table 1 of the paper. 
     */
    public static void main(String[] args) {
        String file = "../data/2004q1_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004q2_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004q3_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004q4_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005q1_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005q2_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005q3_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005q4_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004t1_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004t2_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004t3_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005t1_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005t2_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005t3_SPMF.txt";
        compareCapacities(file);
        file = "../data/2003_SPMF.txt";
        compareCapacities(file);
        file = "../data/2004_SPMF.txt";
        compareCapacities(file);
        file = "../data/2005_SPMF.txt";
        compareCapacities(file);
    }
}