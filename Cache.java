package Cache;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.io.IOException;

public class Cache {
    private Integer cSize;
    private Integer bSize;
    private Integer associativity;
    private Integer replace;
    private Integer hitPol;
    private Integer missPol;
    private Integer hit;
    private Integer miss;
    private Integer setSize;
    private Integer setBits;
    private Integer blockBits;
    private Integer tagBits;
    private Ram ram;
    private CacheSet[] cache;


    /*
    cSize = Cache size                      some number between 8 - 256
    bSize = Data block size                 number of bytes a block can contain ******* should do math to make sure that number is allowed
    associativity = Associativity           1, 2, 4
    replace = Replacement policy            1 - 2 ; 3 IS FOR EXTRA CREDIT
    hitPol = Write hit policy               1 - 2
    missPol = Write miss policy             1 - 2
     */

    Cache(Integer maxRam){
        ram = new Ram(maxRam);
    }

    public void setCache(Integer cSize, Integer bSize, Integer associativity, Integer replace, Integer hitPol, Integer missPol){
        this.cSize = cSize;
        this.bSize = bSize;
        this.associativity = associativity;
        this.replace = replace;
        this.hitPol = hitPol;
        this.missPol = missPol;
        this.hit = 0;
        this.miss = 0;
        /*
        start calculating the set size,
         */
        this.setSize = (cSize / bSize) / associativity;
        cache = new CacheSet[setSize];
        /*
        get the individual bits, so we can calculate the tag bits
         */
        this.setBits = (int) (Math.log(setSize)/ Math.log(2));
        this.blockBits = (int) (Math.log(bSize)/ Math.log(2)); //todo: make sure that this works gotten from: https://www.techiedelight.com/calculate-log-base-2-in-java/
        this.tagBits = 8 - setBits - blockBits;

        //construct the cache
        for(Integer i = 0; i < setSize; i++){
            cache[i] = new CacheSet(associativity, bSize);
        }
        System.out.println("cache successfully configured!");
    }

    public Ram getRam(){
        return this.ram;
    }
    @Override
    public String toString(){
        String ret = "";
        for(Integer i =0; i< cache.length; i++){
            ret += cache[i];
            if(i < cache.length -1){
                ret += "\n";
            }
        }




        return ret;
    }


    public void cacheRead(String value){
        Integer tag;
        String tg;
        String set;
        Integer setIndex;
        String offset;
        Integer offsetIndex;
        //get the binary


        String bin = Integer.toBinaryString(Integer.parseInt(value, 16));


        bin = fillRestWithZeros(bin, 8);

        tg = bin.substring(0, this.tagBits);

        tag = Integer.parseInt(tg, 2);

        set = bin.substring(this.tagBits, this.tagBits + this.setBits);


        setIndex = Integer.parseInt(set, 2);
        offset = bin.substring(this.tagBits + this.setBits , 8);
        offsetIndex = Integer.parseInt(offset, 2);
        Integer assoc = this.associativity; //  IMPORTANT: use this when we get to set
        Integer currentTag;
        CacheLine currentLine =null;
        boolean cont = true;
        Integer eviction = 0;

        for(Integer i = 0; i < assoc; i++) {
            currentLine = cache[setIndex].at(i);
            currentTag = currentLine.getTag();//at associativiy i get tag
            if(currentTag == tag && cache[setIndex].at(i).getValidBit() == 1){
                this.hit += 1;
                eviction = -1;
                cache[setIndex].updateUsage(i);
                cont = false;
                break;
                /*
                this is how we are able to read the bit
                 */
            }
        }





        //if we didn't hit anything
        if (cont){
            Integer blockBitHolder = blockBits;
            char[] newBin = bin.toCharArray();
            //this will make the block offset zero so we can copy everything over from the ram.
            //todo: somethings gets deleted here that shouldn't be
            while(blockBitHolder != 1){
                newBin[newBin.length - blockBitHolder] = '0';
                blockBitHolder -= 1;
            }
            bin = "";
            for(char y: newBin){
                bin += y;
            }



            this.miss += 1;

            if(this.replace == 2){
                eviction = cache[setIndex].updateMiss();
                currentLine = cache[setIndex].at(eviction); // currentLine is least frequently used
            }
            else{
                Random rand = new Random();

                eviction = rand.nextInt(this.associativity);
                for(int i = 0; i < associativity; i++ ){
                    if(cache[setIndex].at(i).getValidBit() == 0){
                        eviction = i;
                        break;
                    }
                }
                currentLine = cache[setIndex].at(eviction);
            }
            if(currentLine.getDirtyBit() == 1){
                //storing victom block if not alrady storing values to the ram
                Integer beforeTag = currentLine.getTag();
                String pastTag = Integer.toBinaryString(beforeTag);
                pastTag = fillRestWithZeros(pastTag, this.tagBits);
                String past = pastTag + set;
                for(int ab = 0; ab < this.blockBits; ab++){
                    past = past + "0";
                }
                ram.assignRamWithBlockOfData(currentLine.getdata(), Integer.parseInt(past, 2));
            }

            currentLine.setTag(tag);
            currentLine.setValidBit(1);
            currentLine.setData(ram.getBlockOfRam(this.bSize, Integer.parseInt(bin, 2)));


        }
//reassign the data with the proper address


        System.out.println("set:" + setIndex);
        String tgHex = Integer.toHexString(tag);
        if(tgHex.length() < 2){
            tgHex = "0" + tgHex;
        }
        System.out.println("tag:" +tgHex);
        if(cont){
            System.out.println("hit:no");
        }
        else{
            System.out.println("hit:yes");
        }
        System.out.println("eviction_line:" + eviction);
        System.out.println("ram_addresss:0x"+ value);
        System.out.println( "data:0x" + currentLine.getdata()[offsetIndex].toUpperCase());
    }


    public CacheLine _cacheReadAndStore(String value){
        Integer tags;
        String tg;
        String set;
        Integer setIndex;
        String offset;
        Integer offsetIndex;
        //get the binary

        String bin = Integer.toBinaryString(Integer.parseInt(value, 16));
        bin = fillRestWithZeros(bin, 8);

        tg = bin.substring(0, this.tagBits);

        tags = Integer.parseInt(tg, 2);

        set = bin.substring(this.tagBits, this.tagBits + this.setBits);


        setIndex = Integer.parseInt(set, 2);
        offset = bin.substring(this.tagBits + this.setBits, 8);
        offsetIndex = Integer.parseInt(offset, 2);
        Integer assoc = this.associativity; //  IMPORTANT: use this when we get to set
        Integer currentTag =0;
        CacheLine currentLine = null;
        boolean cont = true;
        Integer eviction = 0;

        for (Integer i = 0; i < assoc; i++) {
            currentLine = cache[setIndex].at(i);
            currentTag = currentLine.getTag();//at associativiy i get tag
            if (currentTag == tags && cache[setIndex].at(i).getValidBit() == 1) {
                this.hit += 1;
                eviction = -1;
                cache[setIndex].updateUsage(i);
                cont = false;
                break;
                /*
                this is how we are able to read the bit
                 */
            }
        }


        //if we didn't hit anything
        if (cont) {
            Integer blockBitHolder = blockBits;
            char[] newBin = bin.toCharArray();
            //this will make the block offset zero so we can copy everything over from the ram.
            while (blockBitHolder != 1) {
                newBin[newBin.length - blockBitHolder] = '0';
                blockBitHolder -= 1;
            }
            bin = "";
            for (char y : newBin) {
                bin += y;
            }
            this.miss += 1;

            if(this.replace == 2){
                eviction = cache[setIndex].updateMiss();
//                currentLine = cache[setIndex].at(eviction); // currentLine is least frequently used
            }
            else{
                Random rand = new Random();

                eviction = rand.nextInt(this.associativity);
                for(int i = 0; i < associativity; i++ ){
                    if(cache[setIndex].at(i).getValidBit() == 0){
                        eviction = i;
                        break;
                    }
                }
//                currentLine = cache[setIndex].at(eviction);
            }


            currentLine = cache[setIndex].at(eviction); // currentLine is least frequently used
            if (currentLine.getDirtyBit() == 1) {
                currentLine.setDirtyBit(0);
                Integer beforeTag = currentLine.getTag();
                String pastTag = Integer.toBinaryString(beforeTag);
                pastTag = fillRestWithZeros(pastTag, this.tagBits);
                String past = pastTag + set;
                for(int ab = 0; ab < this.blockBits; ab++){
                    past = past + "0";
                }
                //storing victom block if not alrady storing values to the ram
                ram.assignRamWithBlockOfData(currentLine.getdata(), Integer.parseInt(past, 2));
            }



            //reassign the data with the proper address
            currentLine.setTag(tags);
            currentLine.setValidBit(1);
            currentLine.setData(ram.getBlockOfRam(this.bSize, Integer.parseInt(bin, 2)));



        }
        tg = Integer.toHexString(currentTag);
        if(tg.length() < 2){
            tg = "0" + tg;
        }

        System.out.println("set:"+ setIndex);
        System.out.println("tag:" + tg);
        if(cont){
            System.out.println("write_hit:no");
        }
        else{
            System.out.println("write_hit:yes");
        }
        if(cont){
            System.out.println("eviction_line:" + eviction);
        }
        else{
            System.out.println("eviction_line:-1");
        }

        System.out.println("ram_address:0x" + value.toUpperCase());
        return currentLine;
    }


    public CacheLine _cacheRead(String value) {
        Integer tag;
        String tg;
        String set;
        Integer setIndex;
        String offset;
        Integer offsetIndex;
        //get the binary

        String bin = Integer.toBinaryString(Integer.parseInt(value, 16));
        bin = fillRestWithZeros(bin, 8);

        tg = bin.substring(0, this.tagBits);

        tag = Integer.parseInt(tg, 2);

        set = bin.substring(this.tagBits, this.tagBits + this.setBits);


        setIndex = Integer.parseInt(set, 2);
        offset = bin.substring(this.tagBits + this.setBits, 8);
        offsetIndex = Integer.parseInt(offset, 2);
        Integer assoc = this.associativity; //  IMPORTANT: use this when we get to set
        Integer currentTag = 0;
        CacheLine currentLine = null;
        boolean cont = true;
        Integer eviction = 0;

        for (Integer i = 0; i < assoc; i++) {
            currentLine = cache[setIndex].at(i);
            currentTag = currentLine.getTag();//at associativiy i get tag
            if (currentTag == tag && cache[setIndex].at(i).getValidBit() == 1) {
                this.hit += 1;
                eviction = -1;
                cache[setIndex].updateUsage(i);
                cont = false;
                break;
                /*
                this is how we are able to read the bit
                 */
            }
        }


        //if we didn't hit anything
        if (cont) {
            this.miss += 1;
        }


        tg = Integer.toHexString(currentTag);

        if(tg.length() < 2){
            tg = "0" + tg;
        }

        System.out.println("set:"+ setIndex);
        System.out.println("tag" + tg);
        if(cont){
            System.out.println("write_hit:no");
        }
        else{
            System.out.println("write_hit:yes");
        }
        if(cont){
            System.out.println("eviction_line:" + eviction);
        }
        else{
            System.out.println("eviction_line:-1");
        }

        System.out.println("ram_address:0x" + value.toUpperCase());


        return currentLine;
    }


    public void cachewrite(String value, String insertValue){
        //The moment we did a read the line should have the valid bit equal to one
        //There is a tagBit is stored in currentLine
        //todo address the problem of not being able to access the set index.
            //we could get it by converting the value to a string
        Integer parsedValue = Integer.parseInt(value, 16); // parsed as hex
        String binValues = fillRestWithZeros(Integer.toBinaryString(parsedValue), 8);
        String setValue = binValues.substring(tagBits, tagBits + setBits);
        Integer setNumber = Integer.parseInt(setValue, 2);
        //now we have the binary values 00000000 to get the block offset
        String blockOffset = binValues.substring(tagBits + setBits);
        Integer blockIndex = Integer.parseInt(blockOffset, 2);
        Integer beforeValue = this.hit;
        CacheLine currentLine;
        String hitValue;

        //1) to cache
        if(missPol == 1){
            currentLine = _cacheReadAndStore(value);

            if(hitPol == 1){
                currentLine.setSingleData(insertValue, blockIndex);
                currentLine.setDirtyBit(1);
                ram.setRamIndex(parsedValue, insertValue);
            }
            else{
                currentLine.setSingleData(insertValue, blockIndex);
                currentLine.setDirtyBit(1);
            }




        }

        else{
            currentLine = _cacheRead(value);
            //if it missed then write directly into ram:
            if (beforeValue == this.hit){
                ram.setRamIndex(parsedValue, insertValue);
            }
            else{ // if it was a hit it doesn't matter and is only based on hit policies
                if(hitPol == 1){
                    currentLine.setSingleData(insertValue, blockIndex); //if it was a hit this should be the right value
                    currentLine.setDirtyBit(1);
                    ram.setRamIndex(parsedValue, insertValue);
                }
                else{
                    currentLine.setSingleData(insertValue, blockIndex);
                    currentLine.setDirtyBit(1);
                }
            }
        }

//        if(this.hit > beforeValue){
//            hitValue = "yes";
//        }
//        else{
//            hitValue = "no";
//        }
//
//        System.out.println("set:"+ setNumber);
//        String tgString = currentLine.getTag().toString();
//        if(tgString.length() < 2){
//            tgString = "0" + tgString;
//        }
//        System.out.println("tag:" + tgString);
//        System.out.println("write_hit:"+ hitValue);

        if(insertValue.length() < 2){
            insertValue = "0" + insertValue;
        }
        if(insertValue.length() < 2){
            insertValue = "0"+ insertValue;
        }
        System.out.println("data:0x" + insertValue.toUpperCase());
        System.out.println("dirty_bit:" + currentLine.getDirtyBit());









    }

    public void flush(){
        String ramIndex = "";
        String[] data;
        Integer tag;
        String tg;
        String setBinString;
        String blockBinBits;
        Integer ramIndexNum;
            Vector<CacheLine> writesToRam;
            //able to iterate all sets
            //with I we know the correct set we know the tag
        for (Integer i = 0; i < cache.length; i++) {
            writesToRam = cache[i].getDirtyBits();
            //with all dirty bits we need to write the data into the correct position of the ram
            for(Integer j = 0; j < writesToRam.size(); j++){
                data = writesToRam.elementAt(j).getdata();
                tag = writesToRam.elementAt(j).getTag();
                tg = fillRestWithZeros(Integer.toBinaryString(tag), this.tagBits);
                setBinString = fillRestWithZeros(Integer.toBinaryString(i), setBits);
                // lastly get the number of blockBits
                blockBinBits = fillRestWithZeros("0", blockBits);
                ramIndex = tg + setBinString + blockBinBits;
                ramIndexNum = Integer.parseInt(ramIndex, 2);
                for(Integer z = 0; z < data.length; z++){
                    ram.setRamIndex(ramIndexNum + z, data[z]);
                }
            }
                // now that we have assigned the values to the ram we can zero them out
                cache[i].zeroOut();
        }
    }


    private String fillRestWithZeros( String value, Integer to){
        String temp = value;
        while(temp.length() < to){
            temp = "0" + temp;
        }
        return temp;
    }


    public void memory_view(){
        System.out.println("memory_size:" + this.ram.length());
        System.out.println("memory_content:");
        System.out.println("address:data");
        String printHelper;
        //todo: this will print everything but might have a problem if not divisible by 8
        for(Integer i = 0; i < this.ram.length(); i += this.bSize){
            printHelper = Integer.toHexString(i);
            printHelper = fillRestWithZeros(printHelper, 2);
            System.out.println("0x" + printHelper+  ":" + viewHelper(ram.getBlockOfRam(this.bSize, i)));
        }
    }


    private String viewHelper(String[] value){
        String ret = "";
        for(Integer i = 0; i< value.length; i++){
            ret += value[i];
            if(i < value.length - 1){
                ret += " ";
            }
        }
        return ret;
    }





    public void cache_view(){
        System.out.println("cache_size:" + this.cSize);
        System.out.println("data_block_size:" + this.bSize);
        System.out.println("associativity:" + this.associativity);
        if (this.replace == 1){
            System.out.println("replacement_policy:Random_replacement");
        }
        else{
            System.out.println("replacement_policy:least_recently_used");
        }
        if(this.hitPol == 1){
            System.out.println("write_hit_policy:write_through");
        }else{
            System.out.println("write_hit_policy:write_back");
        }

        if(this.missPol == 1){
            System.out.println("write_miss_policy:write_allocate");
        }
        else{
            System.out.println("write_miss_policy:no_write_allocate");
        }

        System.out.println("number_of_cache_hits:" + this.hit);
        System.out.println("number_of_cache_misses:" + this.miss);
        System.out.println("cache_content:");
        System.out.println(this);
    }






    public void mem_dump(){
        PrintWriter outfile = null;
        try
        {
            outfile = new PrintWriter("ram.txt");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found");
            e.printStackTrace(); // prints error(s)
            System.exit(0); // Exits entire program
        }

        for(Integer i = 0; i < ram.length(); i++){
            outfile.println(ram.atIndex(i));
        }
        outfile.close();
    }

    public void cache_dump(){
        try{
            FileWriter outfile = new FileWriter("cache.txt");
            outfile.write(this.toString());
            outfile.close();
        }
        catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


}


