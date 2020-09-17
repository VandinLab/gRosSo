import java.io.*;
import java.util.*;

/**
 * This class is used to generated the real Netflix datasets.
 */

public class NetflixDataset {

    private HashMap<Integer, ArrayList<Pair>>[] datasets;
    private HashMap<Integer, Integer> movieYears;
    private int movie = -1;
    private int[] intervals;
    

    /**
     * Constructor
     * @param   intervals  an array that contains the dates to generate the datasets  
     */
    NetflixDataset(int[] intervals) throws IOException {
        datasets = new HashMap[intervals.length/2];
        this.intervals = intervals;
        for (int i = 0; i < intervals.length / 2; i++) datasets[i] = new HashMap<>();
        movieYears = new HashMap<>();
        loadYears("../data/movie_titles");
        loadData("../data/combined_data_1");
        loadData("../data/combined_data_2");
        loadData("../data/combined_data_3");
        loadData("../data/combined_data_4");
    }

    /**
     * Put the download Netflix Prize files in the data folder. In particular, the files:
     *  - movie_titles
     *  - combined_data_1
     *  - combined_data_2
     *  - combined_data_3
     *  - combined_data_4
     */
    public static void main(String[] args) throws IOException {
        // Create the sequence 2004(Q1-Q4) composed by 2004Q1, 2004Q2, 2004Q3 and 2004Q4
        int[] separator1 = {20040101,20040331,20040401,20040630,20040701,20040930,20041001,20041231};
        String[] fileDataset1 = {"../data/2004q1_SPMF.txt","../data/2004q2_SPMF.txt","../data/2004q3_SPMF.txt","../data/2004q4_SPMF.txt"};
        NetflixDataset nd = new NetflixDataset(separator1);
        nd.writeData(fileDataset1);
        for(int k = 0;k<fileDataset1.length;k++){
            nd.analyze(fileDataset1[k]);
        }
        // Create the sequence 2005(Q1-Q4) composed by 2005Q1, 2005Q2, 2005Q3 and 2005Q4
        int[] separator2 = {20050101,20050331,20050401,20050630,20050701,20050930,20051001,20051231};
        String[] fileDataset2 = {"../data/2005q1_SPMF.txt","../data/2005q2_SPMF.txt","../data/2005q3_SPMF.txt","../data/2005q4_SPMF.txt"};
        nd = new NetflixDataset(separator2);
        nd.writeData(fileDataset2);
        for(int k = 0;k<fileDataset2.length;k++){
            nd.analyze(fileDataset2[k]);
        }
        // Create the sequence 2004(T1-T3) composed by 2004T1, 2004T2 and 2004T3
        int[] separator3 = {20040101,20040430,20040501,20040831,20040901,20041231};
        String[] fileDataset3 = {"../data/2004t1_SPMF.txt","../data/2004t2_SPMF.txt","../data/2004t3_SPMF.txt"};
        nd = new NetflixDataset(separator3);
        nd.writeData(fileDataset3);
        for(int k = 0;k<fileDataset3.length;k++){
            nd.analyze(fileDataset3[k]);
        }
        // Create the sequence 2005(T1-T3) composed by 2005T1, 2005T2 and 2005T3
        int[] separator4 = {20050101,20050430,20050501,20050831,20050901,20051231};
        String[] fileDataset4 = {"../data/2005t1_SPMF.txt","../data/2005t2_SPMF.txt","../data/2005t3_SPMF.txt"};
        nd = new NetflixDataset(separator4);
        nd.writeData(fileDataset4);
        for(int k = 0;k<fileDataset4.length;k++){
            nd.analyze(fileDataset4[k]);
        }
        // Create the sequence 2003-2005(Y) composed by 2003, 2004 and 2005
        int[] separator5 = {20030101,20031231,20040101,20041231,20050101,20051231};
        String[] fileDataset5 = {"../data/2003_SPMF.txt","../data/2004_SPMF.txt","../data/2005_SPMF.txt"};
        nd = new NetflixDataset(separator5);
        nd.writeData(fileDataset5);
        for(int k = 0;k<fileDataset5.length;k++){
            nd.analyze(fileDataset5[k]);
        }
    }

    /**
     * Load the years of the movies contained in the input file
     * @param file  the input file
     */
    private void loadYears(String file) throws IOException {
        FileReader fr = new FileReader(file + ".csv");
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null && !line.equals("")) {
            Scanner sc = new Scanner(line);
            sc.useDelimiter("(\\p{javaWhitespace}|\\.|,)+");
            int id = sc.nextInt();
            String year = sc.next();
            if(!year.equals("NULL")) movieYears.put(id,Integer.parseInt(year));
            else movieYears.put(id,0);
            line = br.readLine();
        }
        br.close();
        fr.close();
    }

    /**
     * Load the ratings of the movies contained in the input file
     * @param file  the input file
     */
    private void loadData(String file) {
        try {
            FileReader fr = new FileReader(file + ".txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            boolean valid=true;
            while (line != null) {
                try {
                    if (line.charAt(line.length() - 1) == ':') {
                        movie = Integer.parseInt(line.split(":")[0]);
                        if(movieYears.get(movie)==0) valid = false;
                        else valid = true;

                    } else if(valid){
                        String[] splitLine = line.split(",");
                        int user = Integer.parseInt(splitLine[0]);
                        int date = Integer.parseInt(splitLine[2].replaceAll("-", ""));
                        int year = date / 10000;
                        if(year>=movieYears.get(movie)) {
                            int index = -1;
                            for (int j = 0; j < intervals.length / 2; j++) {
                                if (date >= intervals[2 * j] && date <= intervals[2 * j + 1]) {
                                    index = j;
                                    break;
                                }
                            }
                            if (index != -1) {
                                ArrayList<Pair> movieList;
                                if (datasets[index].containsKey(user)) movieList = datasets[index].remove(user);
                                else movieList = new ArrayList<>();
                                movieList.add(new Pair(movie, date));
                                datasets[index].put(user, movieList);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println(line);
                }
                line = br.readLine();
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the generated datasets in the files provided in input
     * @param file  an array that contains the names of the files of the generated datasets
     */
    public void writeData(String[] file) {
        try {
            for (int i = 0; i < intervals.length / 2; i++) {
                FileWriter fw = new FileWriter(file[i]);
                BufferedWriter bw = new BufferedWriter(fw);
                ArrayList<Pair> movieList;
                for (int user : datasets[i].keySet()) {
                    movieList = datasets[i].get(user);
                    movieList.sort(Pair::compareTo);
                    HashSet<Integer> currentSet = new HashSet<>();
                    for (int j = 0; j < movieList.size() - 1; j++) {
                        Pair current = movieList.get(j);
                        currentSet.add(movieYears.get(current.getMovie()));
                        if (current.getDate() != movieList.get(j + 1).getDate()){
                            ArrayList<Integer> c = new ArrayList<>(currentSet);
                            c.sort(Integer::compareTo);
                            for(int f:c){
                                bw.write(f + " ");
                            }
                            bw.write("-1 ");
                            currentSet = new HashSet<>();
                        }
                    }
                    currentSet.add(movieYears.get(movieList.get(movieList.size() - 1).getMovie()));
                    ArrayList<Integer> c = new ArrayList<>(currentSet);
                    c.sort(Integer::compareTo);
                    for(int f:c){
                        bw.write(f + " ");
                    }
                    bw.write("-1 -2\n");
                }
                bw.close();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute and write to the standard output the statistics of the input dataset
     * @param file  the input dataset
     */
    public void analyze(String file) {
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            int nLine = 0;
            int lengthTran = 0;
            int itemlengthTran = 0;
            boolean repeated = false;
            HashSet<Integer> items = new HashSet<>();
            HashSet<Integer> itemsTransaction;
            while (line != null) {
                itemsTransaction = new HashSet<>();
                nLine++;
                String[] splitLine = line.split(" -1 ");
                lengthTran += splitLine.length - 1;
                for (int i = 0; i < splitLine.length - 1; i++) {
                    String[] splitItemset = splitLine[i].split(" ");
                    itemlengthTran += splitItemset.length;
                    for (String s : splitItemset) {
                        int item = Integer.parseInt(s);
                        if (!repeated) {
                            repeated = itemsTransaction.contains(item);
                            itemsTransaction.add(item);
                        }
                        items.add(item);
                    }
                }
                line = br.readLine();
            }
            System.out.println("Dataset: " + file);
            System.out.println("Number of items: " + items.size());
            System.out.println("Number of transactions: " + nLine);
            System.out.println("AVG transaction length: " + (lengthTran / (nLine * 1.)));
            System.out.println("AVG transaction item-length: " + (itemlengthTran / (nLine * 1.)));
            System.out.println("Repeated items: " + repeated);
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private class that implements a simple and comparable pair structure that represents a rating 
     * of a movie in a particular date
     */
    private class Pair implements Comparable<Pair> {
        private int movie;
        private int date;

        Pair(int movie, int date) {
            this.movie = movie;
            this.date = date;
        }

        int getMovie() {
            return movie;
        }

        int getDate() {
            return date;
        }

        public int compareTo(Pair m2) {
            if (date == m2.getDate()) return movie - m2.getMovie();
            return date - m2.getDate();
        }
    }
}