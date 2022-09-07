package Cache;
/*
    For a single line of a set.
    validBit                    will turn 1 when a value is inserted
    dirtyBit                    If value is inserted then it will turn to one, and when 1 it should write the value into the physical memeory
    tag                         This is the value the function will be looking for
    data                        This array will contain the block offset data
 */
public class CacheLine {
    private Integer validBit;
    private Integer dirtyBit;
    private Integer tag; //todo: make sure that this shouldn't be a string
    private String[] data;



    CacheLine(Integer bSize){

        validBit = 0;
        dirtyBit = 0;
        tag = 0;
        data = new String[bSize];
        for(Integer i = 0; i < bSize; i++){
            data[i] = "00";
        }
    }

    public void zerOut(){

        validBit = 0;
        dirtyBit = 0;
        tag = 0;
        for(Integer i = 0; i < data.length; i++){
            data[i] = "00";
        }
    }


    //todo: to string is not making the tag bit into 2 valuues for instance if it is f then it will print f, not 0f
    @Override
    public String toString(){
        String tg;
        if(tag <= 0xf){
            tg = "0" + Integer.toHexString(tag);
        }
        else{
            tg = Integer.toHexString(tag);
        }

        return validBit.toString() +" " + dirtyBit.toString() +" " + tg + " " +printBlock(); //todo: we need to make sure that tag is printed as hex
    }

    private String printBlock(){
        String ret = "";
        for(String d: data){
            ret = ret + d + " ";
        }
        return ret;
    }


    /*
        getValidBit                 = tells if we need to do anything
        getDirtyBit                 = tells if we need to write to the ram
        getData                     = gets the data we need to write into the ram
        Note: the index we will need is going to be in another function
     */

    public Integer getValidBit(){
        return this.validBit;
    }
    public Integer getDirtyBit(){
        return this.validBit;
    }
    public String[] getdata(){
        return this.data;
    }
    public Integer getTag(){return this.tag;}

    public void setData(String[] value){
        for(Integer i = 0; i < value.length; i++){
            data[i] = value[i];
        }
    }
    public void setTag(Integer value){
        this.tag = value;
    }
    public void setValidBit(Integer value){
        this.validBit = value;
    }
    public void setDirtyBit(Integer value){
        this.dirtyBit = value;
    }

    public void setSingleData(String value, Integer index){
        data[index] = value;
    }




}
