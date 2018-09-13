import java.lang.*;
import java.util.*;
import java.io.*;

public class PrefixSpan{

    /** the name of the transcation file */
    private String inputPath;
    /** total number of sequences in transcation file */
    private int numSequences;     
    /** minimum support for a frequent itemset in percentage, e.g. 0.8 */
    private double minSup; 
    /** minimum support count for a frequent itemset in percentage, e.g. 500 */
    private double threshold;
    /** the list of collected frequent sequential patterns */
    private Map<String, Integer> freqSeqPatterns = new HashMap<String, Integer>();
    /** the symbol represents the prefix item */
    private final String PREFIX_SYMBOL = "_";

    

    public  PrefixSpan(String[] args){
    	inputPath = args[0];
        minSup = Double.valueOf(args[1]).doubleValue();
        log("Threshold of Support:" + minSup);
    }

    public Map<String, Integer> findSequentialPatterns(List<Sequence> sequences){
        freqSeqPatterns.clear();

        Map<String, Integer> suffixItemMap = countSuffixItems(sequences, null);
        for(String suffixItem: suffixItemMap.keySet()){
            if(suffixItemMap.get(suffixItem) >= threshold){
                //log("--- Prefix: <> "+ suffixItem +", Support Count: "+ suffixItemMap.get(suffixItem)+"---");
                List<String[]> prefixSeq = new ArrayList<String[]>();
                prefixSeq.add(new String[] {suffixItem});
                freqSeqPatterns.put(getPrefixString(prefixSeq), suffixItemMap.get(suffixItem));

                findFreqSequences(sequences, prefixSeq);
            }
        }

        return freqSeqPatterns;
    }

    private List<String[]> copyStringList(List<String[]> seq){
        List<String[]> newList = new ArrayList<String[]>();
        for(String[] itemset : seq){
            newList.add(Arrays.copyOf(itemset, itemset.length));
        }
        return newList;
    }


    static String[] addStringArr(String[] a, String e) {
        //a  = Arrays.copyOf(a, a.length + 1);
        //a[a.length - 1] = e;
        String[] newArr = new String[a.length+1];
        Boolean added = false;
        for(int i=0; i<a.length+1; i++){
            if(added){
                newArr[i] = a[i-1];
            }
            else if(i==a.length || e.compareTo(a[i])==-1){
                newArr[i] = e;
                added = true;
            }
            else{
                newArr[i] = a[i];
            }
        }
        return newArr;
    }

    private List<String[]> appendPrefixSeq(List<String[]> prefixSeq, String suffixItem){
        List<String[]> newPrefixSeq = copyStringList(prefixSeq);
        if(suffixItem.contains(PREFIX_SYMBOL)){
            String[] itemset = newPrefixSeq.get(prefixSeq.size()-1);
            String[] lastPrefix = addStringArr(itemset, suffixItem.replace(PREFIX_SYMBOL, ""));
            newPrefixSeq.set(newPrefixSeq.size()-1, lastPrefix);
        }
        else{
            newPrefixSeq.add(new String[]{suffixItem});
        }
        return newPrefixSeq;
    }

    private String getPrefixString(List<String[]> prefixSeq){
        String prefix = "";
        for(String[] itemset:prefixSeq){
            prefix += Arrays.toString(itemset);
        }
        return prefix;
    }
    public void findFreqSequences(List<Sequence> sequences, List<String[]> prefixSeq){   
        // Get the projected sequences respect to the current prefix sequence
        List<Sequence> projectedSequences = projectSequences(sequences, prefixSeq);
        
        // Calculate the suffix items in the projected sequences
        Map<String, Integer> suffixItemMap = countSuffixItems(projectedSequences, prefixSeq.get(prefixSeq.size()-1));
        
        // For each frequent suffix item, append it to the prefix sequence and
        // add the appended sequence to the result..
        // Afterwards, continue to find sequential patterns with the updated prefix sequence.
        for(String suffixItem: suffixItemMap.keySet()){
            if(suffixItemMap.get(suffixItem) >= threshold){
                //log("--- Prefix: "+"<"+getPrefixString(prefixSeq)+"> "+ suffixItem +"\tSupport Count: "+ suffixItemMap.get(suffixItem)+"---");
                List<String[]> updatedprefixSeq = appendPrefixSeq(prefixSeq, suffixItem);
                
                if(freqSeqPatterns.containsKey(getPrefixString(updatedprefixSeq))){
                    continue;
                }
                freqSeqPatterns.put(getPrefixString(updatedprefixSeq), suffixItemMap.get(suffixItem));
                
                /*String result = "";
                for(String[] itemset: updatedprefixSeq){
                    result+=Arrays.toString(itemset);
                }
                log("-->"+result);*/
                
                findFreqSequences(projectedSequences, updatedprefixSeq);
            }
        }
        
        return;
    }


    private List<Sequence> projectSequences(List<Sequence> sequences, List<String[]> prefixSeq){
        List<Sequence> projectedSequences = new ArrayList<Sequence>();

        for(Sequence seq: sequences){

            // Iterate through itemsets in this sequence
            for(int i=seq.getStartIdx(); i<seq.size(); i++){
                List<String> itemset = new ArrayList<String>(seq.get(i));                
                List<String> lastPrefix = Arrays.asList(prefixSeq.get(prefixSeq.size()-1));
                
                // Check if the new prefix item exists in this itemset
                Boolean existPrefixItem = true;
                if(lastPrefix.size()==1 && itemset.contains(PREFIX_SYMBOL)){
                    existPrefixItem = false;
                }
                else if(!itemset.contains(lastPrefix.get(lastPrefix.size()-1))){
                    existPrefixItem = false;
                }
                else{
                    for(String prefixItem: lastPrefix){
                        if(!itemset.contains(prefixItem) && !itemset.contains(PREFIX_SYMBOL)){
                            existPrefixItem = false;
                            break;
                        }
                        // If exists, then remove the new prefix item
                        itemset.remove(prefixItem);
                    }
                }

                // If this itemset exists the new prefix item, then examinate its content
                if(existPrefixItem){
                    
                    // Remove any item that already apperes in the prefix sequence
                    for(int j=0; j<prefixSeq.size()-1; j++){
                        for(String prefixItem: prefixSeq.get(j)){
                            itemset.remove(prefixItem);
                        }
                    }

                    // Remove "_"
                    itemset.remove(PREFIX_SYMBOL);

                    // If remain any item in this itemset that is neither the new prefix item
                    // nor any items in the prefix sequence, then add it with "PREFIX_SYMBOL" (e.g. "[_,a]");
                    // otherwise, add it using the new prefix item itself (e.g. "[a]")
                    if(itemset.size()>0){ 
                        itemset.add(PREFIX_SYMBOL);
                        Sequence newSeq = new Sequence(i, itemset, seq.getSeq());
                        projectedSequences.add(newSeq);
                        break;
                    }
                    else if((i+1)<seq.size()){
                        Sequence newSeq = new Sequence(i+1, null, seq.getSeq());
                        projectedSequences.add(newSeq);
                        break;
                    }
                }
            }            
            
        }
        /*for(Sequence seq:  projectedSequences){
            log(seq.toString());
        }*/
        return projectedSequences;
    }

    private Map<String,Integer> addItem2Map(String item, Map<String,Integer> map){
        Integer count = map.get(item);
        if (count == null) {
            map.put(item, 1);
            count = 1;
        }
        else {
            map.put(item, count + 1);
        }
        return map;
    }

    private Map<String, Integer> countSuffixItems(List<Sequence> sequences, String[] lastPrefix){
        Map<String, Integer> suffixItems = new HashMap<String, Integer>();
        for(Sequence seq: sequences){
            Set<String> addedItems = new HashSet<String>();

            for(int i=seq.getStartIdx(); i<seq.size(); i++){
                List<String> itemset = new ArrayList<String>(seq.get(i));

                Boolean existPrefixSymbol = itemset.contains(PREFIX_SYMBOL);
                if(existPrefixSymbol) itemset.remove(PREFIX_SYMBOL);
                else{
                    for(String item: itemset){
                        if(!addedItems.contains(item)){
                            suffixItems = addItem2Map(item, suffixItems);
                            addedItems.add(item);
                        }
                    }
                }

                Boolean existPrefixItems = false;
                if(lastPrefix!=null){
                    existPrefixItems = true;
                    for (String prefixItem: lastPrefix){
                        if(!itemset.contains(prefixItem)){
                            existPrefixItems = false;
                            break;
                        }
                        itemset.remove(prefixItem);
                    }                    
                }
                
                if(existPrefixSymbol || existPrefixItems){
                    for(String item: itemset){
                        if(!addedItems.contains(PREFIX_SYMBOL+item)){
                            suffixItems = addItem2Map(PREFIX_SYMBOL+item, suffixItems);
                            addedItems.add(PREFIX_SYMBOL+item);
                        }
                    }
                }
            }
        }        
        
        return suffixItems;
    }

    public List<Sequence> readTransactionDB(){
        List<Sequence> sequences = new ArrayList<Sequence>();
        Map<String, PriorityQueue<Entry>> sequenceData = new HashMap<String, PriorityQueue<Entry>>();
        try{
            BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
            while (data_in.ready()) {
                String[] t = data_in.readLine().trim().split("[\\s]+");
                String sid = t[0];
                Integer tid = Integer.valueOf(t[1]);
                String item = t[2];

                PriorityQueue<Entry> pq = sequenceData.get(sid);
                if (pq == null) {
                    PriorityQueue<Entry> seq = new PriorityQueue<Entry>();
                    seq.add(new Entry(item, tid));
                    sequenceData.put(sid, seq);
                }
                else {
                    pq.add(new Entry(item, tid));
                    sequenceData.put(sid, pq);
                }     
            }
            data_in.close();

            for(String sid : sequenceData.keySet()){
                PriorityQueue<Entry> pq = sequenceData.get(sid);
                List<String> itemset = new ArrayList<String>();
                List<List<String>> sequence = new ArrayList<List<String>>();
                Integer tid = null;
                while(!pq.isEmpty()){
                    Entry item = pq.poll();
                    if(tid==null || item.getValue().equals(tid)){
                        tid = item.getValue();
                        itemset.add(item.getKey());
                    }
                    else{
                        sequence.add(itemset);
                        itemset = new ArrayList<String>();
                        
                        tid = item.getValue();
                        itemset.add(item.getKey());
                    }
                    if(pq.isEmpty()){
                        sequence.add(itemset);
                        itemset = new ArrayList<String>();
                    }
                }
                sequences.add(new Sequence(0, null, sequence));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 

        numSequences = sequences.size();
        threshold = minSup * numSequences;        
        
        log("Number of Sequences: " + numSequences);
        /*for(Sequence seq:  sequences){
            log(seq.toString());
        }*/

        return sequences;
    }
    private void log(String message){
        System.out.println(message);
    }
}