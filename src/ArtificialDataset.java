import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class is used to generated new random datasets in the ArtificialTest class.
 */

public class ArtificialDataset{

    /**
     * Creates a random dataset sampling random transactions from the dataset provided in input.
     *
     * @param  fileIn  input dataset
     * @param  fileOut random dataset in output
     * @param  size    size of the random dataset
     * @param  seed    seed of the random generator
     */
    static void randomDataset(String fileIn, String fileOut, int size,int seed) throws IOException {
        ArrayList<String> dataset = new ArrayList<>();
        FileReader fr = new FileReader(fileIn);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while((line=br.readLine())!=null){
            dataset.add(line);
        }
        br.close();
        fr.close();
        int dim = dataset.size();
        Random r = new Random(seed);
        FileWriter fw = new FileWriter(fileOut);
        BufferedWriter bw = new BufferedWriter(fw);
        for(int i=0;i<size;i++){
            bw.write(dataset.get(r.nextInt(dim)) + "\n");
        }
        bw.close();
        fw.close();
    }

    /**
    * This main creates a random dataset of the provided size starting from the input dataset. 
    * fileIn is the name of the input dataset (e.g., data/2005T1_SPMF.txt)
    * fileOut is the name of the random dataset in output (e.g., data/RAND_2005T1.txt)
    * size is the number of transactions of the new random dataset
    * seed is the seed for the random generator  
    */
    public static void main(String[] args) throws IOException {
        String fileIn = "";
        String fileOut = "";
        int size = 0;
        int seed = 0;
        randomDataset(fileIn,fileOut,size,seed);

    }
}