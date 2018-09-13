import java.lang.*;
import java.util.*;
public class Sequence {
    private int startIdx;
    private List<String> firstItem;
    private List<List<String>> seq;

    public Sequence(int idx){
        this.startIdx = idx;
        this.firstItem = null;
        this.seq = null;
    }
    public Sequence(int idx, List<String> firstItem, List<List<String>> seq){
        this.startIdx = idx;
        this.firstItem = firstItem;
        this.seq = seq;
    }
    public List<List<String>> getSeq(){ return seq; }
    public List<String> getFirstItem(){ return firstItem; }
    public int getStartIdx(){ return startIdx; }
    public void setSeq(List<List<String>> seq){ this.seq = seq; }
    public void setFirstItem(List<String> firstItem){ this.firstItem = firstItem; }
    public void setStartIdx(int idx){ this.startIdx = idx; }

    public int size(){ return this.seq.size();}
    public List<String> get(int i){
        if(i==startIdx && firstItem!=null){
            return firstItem;
        }
        return seq.get(i);
    }
    public String toString(){
        String str = "";
        for(int i=startIdx; i<seq.size(); i++){
            List<String> itemset = this.get(i);
            str += "["+String.join(",", itemset)+"]";
        }
        return str;
    }
}