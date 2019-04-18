import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
/*
 * Lattice.java
 *
 * Defines a new "Lattice" type, which is a directed acyclic graph that
 * compactly represents a very large space of speech recognition hypotheses
 *
 * Note that the Lattice type is immutable: after the fields are initialized
 * in the constructor, they cannot be modified.
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util 
 *     java.io
 *     
 * as well as the class java.math.BigInteger
 * 
 * Use of any additional Java Class Library components is not permitted 
 * 
 * Your Name Goes Here
 *Roman Alonzo, Bradley Hoefel
 */

public class Lattice {
    private String utteranceID;       // A unique ID for the sentence
    private int startIdx, endIdx;     // Indices of the special start and end tokens
    private int numNodes, numEdges;   // The number of nodes and edges, respectively
    private Edge[][] adjMatrix;       // Adjacency matrix representing the lattice
                                      //   Two dimensional array of Edge objects
                                      //   adjMatrix[i][j] == null means no edge (i,j)
    private double[] nodeTimes;       // Stores the timestamp for each node

    private int [] topSort;
    // Constructor

    // Lattice
    // Preconditions:
    //     - latticeFilename contains the path of a valid lattice file
    // Post-conditions
    //     - Field id is set to the lattice's ID.

    //     - Field startIdx contains the node number for the start node
    //     - Field endIdx contains the node number for the end node
    //     - Field numNodes contains the number of nodes in the lattice
    //     - Field numEdges contains the number of edges in the lattice
    //     - Field adjMatrix encodes the edges in the lattice:
    //        If an edge exists from node i to node j, adjMatrix[i][j] contains
    //        the address of an Edge object, which itself contains
    //           1) The edge's label (word)
    //           2) The edge's acoustic model score (amScore)
    //           3) The edge's language model score (lmScore)
    //        If no edge exists from node i to node j, adjMatrix[i][j] == null
    //     - Field nodeTimes is allocated and populated with the timestamps for each node
    // Notes:
    //     - If you encounter a FileNotFoundException, print to standard error
    //         "Error: Unable to open file " + latticeFilename
    //       and exit with status (return code) 1
    //     - If you encounter a NoSuchElementException, print to standard error
    //         "Error: Not able to parse file " + latticeFilename
    //       and exit with status (return code) 2
    public Lattice(String latticeFilename) {
        //System.out.println("Lattice is being called");
        try {
            File file = new File(latticeFilename);
            Scanner scan = new Scanner(file);


            //scans and assigns values for utteranceID, startIdx, endIdx, numNodes, and numEdges
            scan.next();
            this.utteranceID = scan.next();
            scan.nextLine();
            scan.next();
            this.startIdx = scan.nextInt();
            scan.nextLine();
            scan.next();
            this.endIdx = scan.nextInt();
            scan.nextLine();
            scan.next();
            this.numNodes = scan.nextInt();
            scan.nextLine();
            scan.next();
            this.numEdges = scan.nextInt();
            scan.nextLine();

            this.adjMatrix = new Edge[this.numNodes][this.numNodes];
            this.nodeTimes = new double[this.numNodes];
            for (int i = 0; i < this.numNodes; i++){
                scan.next();
                scan.next();
                this.nodeTimes[i] = Double.parseDouble(scan.next());
                //System.out.println(this.nodeTimes[i]);

                scan.nextLine();
            }
            scan.next();
            for (int i = 0; i < this.numEdges; i++){
                int x = scan.nextInt();
                int y = scan.nextInt();

                this.adjMatrix[x][y] = new Edge(scan.next(),scan.nextInt(),scan.nextInt());

                //System.out.println(this.adjMatrix[x][y].getLabel());
                if (scan.hasNext()){
                    scan.nextLine();
                    scan.next();
                }

            }

        }

        catch (FileNotFoundException e){
            System.out.println("Error: Unable to open file " + latticeFilename);
            System.exit(1);
        }
        catch (NoSuchElementException e) {
            System.out.println("Error: Not able to parse file " + latticeFilename);
            System.exit(2);
        }
         catch (Exception e) {
            System.out.println(e);
         }
        return;
        }



    // Accessors 

    // getUtteranceID
    // Pre-conditions:
    //    - None
    // Post-conditions:

    public String getUtteranceID() {
        return this.utteranceID;
    }

    // getNumNodes
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of nodes in the lattice
    public int getNumNodes() {
        return this.numNodes;
    }

    // getNumEdges
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of edges in the lattice
    public int getNumEdges() {
        return this.numEdges;
    }

    // toString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Constructs and returns a string describing the lattice in the same
    //      format as the input files.  Nodes should be sorted ascending by node 
    //      index, edges should be sorted primarily by start node index, and 
    //      secondarily by end node index 
    // Notes:
    //    - Do not store the input string verbatim: reconstruct it on the fly
    //      from the class's fields
    //    - toString simply returns a string, it should not print anything itself
    // Hints:
    //    - You can use the String.format method to print a floating point value 
    //      to two decimal places
    //    - A StringBuilder is asymptotically more efficient for accumulating a
    //      String than repeated concatenation
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        //writes id line
        returnString.append("id "+this.utteranceID+"\n");

        returnString.append("start 0\n");
        returnString.append("end "+ (numNodes-1)+"\n");
        returnString.append("numNodes "+ numNodes+"\n");
        returnString.append("numEdges "+ numEdges+"\n");
        int i = 0;
        for (double d : nodeTimes ){
            returnString.append("node " + i +" "+ d+"\n");
            i++;
        }

        for(i = 0; i<numNodes; i++){
            for (int j = 0; j<numNodes; j++){
                if(this.adjMatrix[i][j]!= null){
                    returnString.append("edge " + i+ " "+j+" "+ this.adjMatrix[i][j].getLabel()+ " "+ this.adjMatrix[i][j].getAmScore()+" "+this.adjMatrix[i][j].getLmScore()+"\n");
                }
            }
        }
        return returnString.toString();

    }

    // decode
    // Pre-conditions:
    //    - lmScale specifies how much lmScore should be weighted
    //        the overall weight for an edge is amScore + lmScale * lmScore
    // Post-conditions:
    //    - A new Hypothesis object is returned that contains the shortest path
    //      (aka most probable path) from the startIdx to the endIdx
    // Hints:
    //    - You can create a new empty Hypothesis object and then
    //      repeatedly call Hypothesis's addWord method to add the words and 
    //      weights, but this needs to be done in order (first to last word)
    //      Backtracking will give you words in reverse order.
    //    - java.lang.Double.POSITIVE_INFINITY represents positive infinity
    // Notes:
    //    - It is okay if this algorithm has time complexity O(V^2)
    public Hypothesis decode(double lmScale) {
        Hypothesis hype = new Hypothesis();
        Double[] cost = new Double[numNodes];
        int [] parentNode = new int[numNodes];
        for (int i = 0; i < numNodes; i++){
            cost[i] = Double.POSITIVE_INFINITY;
        }
        cost[0] = 0.0;
        // search - find the correct values for cost and parent
        for (int n: topSort){
            for (int o = 0; o < (numNodes-1); o++){
                if (adjMatrix[o][n]!= null) {
                    //System.out.println(o+ " , " + n);
                    double edgeCost = adjMatrix[o][n].getAmScore() + adjMatrix[o][n].getLmScore() * lmScale;
                    //if ((adjMatrix[o][n].getAmScore()+(adjMatrix[o][n].getLmScore()*lmScale)) + cost[o] < cost[n]){
                    if ((edgeCost + cost[o]) < cost[n]) {
                        cost[n] = (adjMatrix[o][n].getAmScore() + (adjMatrix[o][n].getLmScore() * lmScale)) + cost[o];
                        parentNode[n] = o;
                    }
                }
            }
        }
        //backtrack
        int resultNode = endIdx;
        ArrayList<Integer> resultPath = new ArrayList<>();

        while (resultNode!= 0){
            resultPath.add(0, resultNode);
            //System.out.println(parentNode[resultNode]+ " " + resultNode);
            //System.out.println(adjMatrix[parentNode[resultNode]][resultNode].getLabel());
            resultNode = parentNode[resultNode];
        }
        resultPath.add(0, 0);
        for (int i = 0; i < resultPath.size()-1; i++) {
            hype.addWord((adjMatrix[resultPath.get(i)][resultPath.get(i + 1)].getLabel()), (adjMatrix[resultPath.get(i)][resultPath.get(i + 1)].getAmScore() + (adjMatrix[resultPath.get(i)][resultPath.get(i + 1)].getLmScore() * lmScale)));

        }
        //return path defined by the sequence of nodes named results
        return hype;
    }

    
    // topologicalSort
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - A new int[] is returned with a topological sort of the nodes
    //      For example, the 0'th element of the returned array has no 
    //      incoming edges.  More generally, the node in the i'th element 
    //      has no incoming edges from nodes in the i+1'th or later elements
    public int[] topologicalSort() {
        Edge [][] inDegrees = this.adjMatrix;
        List<Integer> S = new ArrayList<>();
        int[] result = new int[this.numNodes];
        boolean[] resultContains = new boolean[this.numNodes];
        int resultPosition = 0;
        S.add(0);
        while(!S.isEmpty()){
            int n = S.get(0);
            S.remove(0);
            result[resultPosition] = n;
            resultPosition++;
            for (int b = 0; b < numNodes; b++){
                if(inDegrees[n][b] != null && resultContains[b]==false){
                    resultContains[b] = true;
                    S.add(b);
                }
            }
        }
        this.topSort = result;
        return result;
    }



    // countAllPaths
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the total number of distinct paths from startIdx to endIdx
    //       (do not count other subpaths)
    // Hints:
    //    - The straightforward recursive traversal is prohibitively slow
    //    - This can be solved efficiently using something similar to the 
    //        shortest path algorithm used in decode
    //        Instead of min'ing scores over the incoming edges, you'll want to 
    //        do some other operation...
    public java.math.BigInteger countAllPaths() {
        System.out.println(2+numEdges-numNodes);
        int[]topSort = topologicalSort();
        int totalPaths = 1;
        int numOutgoingEdgesForNode = 0;
        int numIncomingEdgesForNode = 0;
        for (int i = 0; i < numNodes; i++){
            if (i == endIdx) break;
            numOutgoingEdgesForNode = 0;
            numIncomingEdgesForNode = 0;
            for (int j = 0; j < numNodes; j++){
                if (adjMatrix[i][j]!= null) numOutgoingEdgesForNode++;
                if (adjMatrix[j][i]!= null) numIncomingEdgesForNode++;
            }
            //works for double in,single out or single in, double out
            totalPaths+= (numOutgoingEdgesForNode -1);
            if (numIncomingEdgesForNode>1&&numOutgoingEdgesForNode>1){
                totalPaths+= numIncomingEdgesForNode* (numOutgoingEdgesForNode-1);
            }

        }
        System.out.println("There are "+totalPaths+" total paths.");





        return null;
    }
    // getLatticeDensity
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the lattice density, which is defined to be:
    //      (# of non -silence- words in lattice) / (# seconds from start to end index)
	//      Note that multiwords (e.g. to_the) count as a single non-silence word
    public double getLatticeDensity() {
        double numWords = 0.0;
        for (int i = 0; i< numNodes; i++){
            for (int j = 0; j< numNodes; j++) {
                 if (adjMatrix[i][j] != null && !adjMatrix[i][j].getLabel().equals("-silence-")) {
                        numWords++;
                }
            }
        }
        //System.out.println(numWords/nodeTimes[endIdx]);
        return numWords/nodeTimes[endIdx];
    }

    // writeAsDot - write lattice in dot format
    // Pre-conditions:
    //    - dotFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice is written in the specified dot format to dotFilename
    // Notes:
    //    - See the assignment description for the exact formatting to use
    //    - For context on the dot format, see    
    //        - http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29
    //        - http://www.graphviz.org/pdf/dotguide.pdf
    public void writeAsDot(String dotFilename) {
        String fullFileName = dotFilename+".txt";
        try{
            PrintWriter writer = new PrintWriter(dotFilename);
            writer.println("digraph g {");
            writer.println("    rankdir=\"LR\"");
            for (int i = 0; i < numNodes; i++){
                for (int j = 0; j < numNodes; j++){
                    if (adjMatrix[i][j] != null){
                        writer.println("    "+i + " -> "+j+" [label = \"" + adjMatrix[i][j].getLabel()+"\"]");
                    }
                }
            }
            writer.println("}");
            writer.close();
        }
        catch (Exception e ){
            System.out.println(e);
        }
        return;
    }
    // saveAsFile - write in the simplified lattice format (same as input format)
    // Pre-conditions:
    //    - latticeOutputFilename is the name of the intended output file
    // Post-conditions:
    //    - The lattice's toString() representation is written to the output file
    // Note:
    //    - This output file should be in the same format as the input .lattice file
    public void saveAsFile(String latticeOutputFilename) {
        try{
            PrintWriter writer = new PrintWriter(latticeOutputFilename);
            writer.print(this.toString());
            writer.close();
        }
        catch (Exception e){

        }
        return;
    }
    // uniqueWordsAtTime - find all words at a certain point in time
    // Pre-conditions:
    //    - time is the time you want to query
    // Post-conditions:
    //    - A HashSet is returned containing all unique words that overlap 
    //      with the specified time
    //     (If the time is not within the time range of the lattice, the Hashset should be empty)
    public java.util.HashSet<String> uniqueWordsAtTime(double time) {
        HashSet<String> stringHash = new HashSet<>();
        for (int i = 0; i < numNodes; i++){
            for (int j = 0; j < numNodes-1; j++){
                if (adjMatrix[i][j] != null && nodeTimes[i]<=time && nodeTimes[j]>=time && !stringHash.contains(adjMatrix[i][j].getLabel())){
                    stringHash.add(adjMatrix[i][j].getLabel());
                }
            }
        }
        stringHash.forEach(System.out::println);
        return stringHash;
    }





    // printSortedHits - print in sorted order all times where a given token appears
    // Pre-conditions:
    //    - word is the word (or multiword) that you want to find in the lattice
    // Post-conditions:
    //    - The midpoint (halfway between start and end time) for each instance of word
    //      in the lattice is printed to two decimal places in sorted (ascending) order
    //      All times should be printed on the same line, separated by a single space character
    //      (If no instances appear, nothing is printed)
    // Note:
	//    - java.util.Arrays.sort can be used to sort
    //    - PrintStream's format method can print numbers to two decimal places
    public void printSortedHits(String word) {
        double[] doubArr = new double[numEdges];
        int arrayPosition = 0;
        for (int i = 0; i < numNodes; i++){
            for (int j = 0; j < numNodes-1; j++){
                if (adjMatrix[i][j] != null && adjMatrix[i][j].getLabel().equals(word)){
                    System.out.println("NODE TIMES: " + nodeTimes[i] + " " + nodeTimes[j]);
                    System.out.println(((double)nodeTimes[i]+(double)nodeTimes[j])/2);
                    doubArr[arrayPosition] = ((double)nodeTimes[i]+(double)nodeTimes[j])/2;
                    arrayPosition++;

                }
            }
        }
        Arrays.sort(doubArr);
        for (double d: doubArr){
            System.out.println(d);
        }


        return;
    }
}