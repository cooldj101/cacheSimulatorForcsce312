package Cache;

import java.util.LinkedList;
import java.util.Vector;

public class CacheSet {
    private CacheLine[] set;
    private LinkedList<Integer> used = null;

    CacheSet(Integer associativity, Integer bSize){
        used = new LinkedList<>();
        for (Integer j = 0; j < associativity; j++) {
            used.addLast(j);
        }

        set = new CacheLine[associativity];
        for(Integer i = 0; i < associativity; i++){
            set[i] = new CacheLine(bSize);
        }

    }

    @Override
    public String toString(){
        String ret = "";
        for(Integer i = 0; i < this.set.length; i++){
            ret = ret + set[i].toString();
            if(i< this.set.length -1){
                ret += "\n";
            }
        }
        return ret;
    }

    //todo make sure this will actually zero out the whole cache
    public void zeroOut(){
        for(Integer i = 0; i < set.length; i++){
            set[i].zerOut();
        }
    }

//    public CacheLine[] getSet(){
//        return set;
//    }

    public Vector<CacheLine> getDirtyBits(){
        Vector<CacheLine> temp = new Vector<>();
        for(int i = 0; i < set.length; i++){
            if (set[i].getDirtyBit() == 1){
                temp.add(set[i]);
            }
        }
        return temp;
    }

    public CacheLine at(Integer index){
        return set[index];
    }

    public void updateUsage(Integer index){
        /*
        this function will change the order of the doubly linked list based off which index is recently used
         */
        used.removeFirstOccurrence(index);
        used.addLast(index);
    }

    public Integer updateMiss(){
        used.addLast(used.removeFirst());
        return used.getLast();
    }






}
