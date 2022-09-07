package Cache;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class cachesimulator {

    public static void printMenu(){
        System.out.println("*** Cache simulator menu ***");
        System.out.println("type one command:");
        System.out.println("1. cache-read");
        System.out.println("2. cache-write");
        System.out.println("3. cache-flush");
        System.out.println("4. cache-view");
        System.out.println("5. memory-view");
        System.out.println("6. cache-dump");
        System.out.println("7. memory-dump");
        System.out.println("8. quit");
        System.out.println("****************************");
    }


    public static void main(String[] args) {
        //TODO: it might make it easier to put the data structure of ram into the cache and just call the cache in the begining
        Cache cache = null;
        /*
        start, end  = init-ram start end
        in          = take input of command line
        ram         = value of the ram TODO: there might be a problem with making ram separate
         */
        Integer start;
        Integer end;
        String in = null;
        String[] command = null; // = input commands separated by " "

        /*
        preping the ram by:
            1) take the input of how we are going to specify the ram
            2) remove starting whitespace just in case there was some problems entering the data
            3) make a new data set that will take in the input split by " " and remove anything that became ""
            4) ensure correct words and numbers
            5) BE WARY THE SECOND VALUE CAN TAKE IN ANYTHING INCLUDING NEGATIVES
         */

        System.out.println("*** Welcome to the cache simulator ***");
        System.out.println("initialize the Ram:");
        Scanner systemIN = new Scanner(System.in);
        String ramValues = systemIN.nextLine(); // this should be "init-ram 0x00 0xFF"
        // in case there are any whitespace at the front that it might split
        ramValues = removeStartingWhitespace(ramValues);
        String[] initRam = ramValues.split(" ");
        //get rid of all additional whitespace
        Vector<String> temp = new Vector<>();
        for (String s : initRam) {
            if (!(s.equals(""))) {
                s = s.toLowerCase();
                s = s.replace("0x", "");
                temp.add(s);
            }
        }

        try{
            if(temp.contains("init-ram")){
                start = Integer.parseInt(temp.elementAt(1), 16);
                if(start != 0X00){
                    throw new RuntimeException("have to start at 0X00");
                }
                end = Integer.parseInt(temp.elementAt(2), 16);
                cache = new Cache(end);

            }
            else {
                throw new RuntimeException("no init-ram");
            }
        }catch (NumberFormatException e){
            System.out.println("something wasn't a number");
        }

        /*
        beginning the building of the cache
            1) build the ram
                -open the file and read everything in
            3) configure cache make up
            2) make the menu for the input
         */
//1)
        try {
            File inputFile = new File(args[0]);
            Scanner input = new Scanner(inputFile);
            assert cache != null;
            cache.getRam().assignRamWithInput(input);
            input.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
//2)

        System.out.println("configure the cache:");
        Integer cacheSize;
        Integer blockSize;
        Integer associativity;
        Integer replacementPolicy;
        Integer writeHitPolicy;
        Integer writeMissPolicy;
        cacheSize = getCacheSize(systemIN); //todo: might be a good idea make it so someone can end the program
        blockSize = getBlockSize(systemIN);
        associativity = getAssociativity(systemIN);
        replacementPolicy = getReplacementPolicy(systemIN);
        writeHitPolicy = getWriteHitPolicy(systemIN);
        writeMissPolicy = getWriteMissPolicy(systemIN);

        cache.setCache(cacheSize, blockSize, associativity, replacementPolicy,writeHitPolicy,writeMissPolicy);

//3)
        Vector<String> commandsNoWhite = new Vector<>();
        label:
        while (true){
            printMenu();
            in = systemIN.nextLine();
            in = removeStartingWhitespace(in); //in case the input came with some whitespace
            command = in.split(" ");
            for(Integer z = 0; z< command.length; z++){
                if(command[z] != ""){
                    if(command[z].contains("0x") || command[z].contains("0X")){
                        command[z] = command[z].replace("0X", "");
                        command[z] = command[z].replace("0x", "");
                    }
                    commandsNoWhite.add(command[z].toLowerCase());
                }

            }

            switch (commandsNoWhite.elementAt(0)) {
                case "quit":
                    break label;
                case "cache-read":

                    cache.cacheRead(commandsNoWhite.elementAt(1));
                    break;
                case "cache-write":
                    cache.cachewrite(commandsNoWhite.elementAt(1), commandsNoWhite.elementAt(2));
                    break;
                case "cache-flush":
                    cache.flush();
                    break;
                case "cache-view":
                    cache.cache_view();
                    break;
                case "memory-view":
                    cache.memory_view();
                    break;
                case "cache-dump":
                    cache.cache_dump();
                    break;
                case "memory-dump":
                    cache.mem_dump();
                    break;
                default:
                    System.out.println("incorrect input");
                    break;
            }
            commandsNoWhite.clear();
        }

        systemIN.close();

    }


    public static String removeStartingWhitespace(String value){
        /*
            continually remove the first index if it is whitespace
         */
        while (value.startsWith(" ")){
            value = value.substring(1);
        }
        return value;
    }

    /*
    The below snippet of code comes from: https://stackoverflow.com/questions/237159/whats-the-best-way-to-check-if-a-string-represents-an-integer-in-java
     */
    public static Boolean isNumeric(String value){
        try{
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException e){
            return false;
        }
    }

    public static Integer getCacheSize(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("cache size: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);
            if(ret < 8 || ret > 256){
                System.out.println("the cache value: Range = 8-256");
                return getCacheSize(systemIN); //recall the function
            }
        }else{
            System.out.println("Invalid input: is the did you put a number in for cache size? Try again.");
            return getCacheSize(systemIN);
        }
        return ret;
    }
    public static Integer getBlockSize(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("data block size: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);

        }else{
            System.out.println("Invalid input: did you put a number in? Try again.");
            return getBlockSize(systemIN);
        }
        return ret;
    }

    public static Integer getAssociativity(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("associativity: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);
            if(!(ret == 4 || ret == 1 || ret == 2)){
                System.out.println("Associativity can be 1, 2, or 4");
                return getAssociativity(systemIN); //recall the function
            }
        }else{
            System.out.println("Invalid input: is the did you put a number in for Associativity? Try again.");
            return getAssociativity(systemIN);
        }
        return ret;
    }

    public static Integer getReplacementPolicy(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("replacement policy: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);
            if(!(ret == 2 || ret == 1)){
                System.out.println("the replacement policy: Range = 1-2");
                return getReplacementPolicy(systemIN); //recall the function
            }
        }else{
            System.out.println("Invalid input: is the did you put a number in? Try again.");
            return getReplacementPolicy(systemIN);
        }
        return ret;
    }
    public static Integer getWriteHitPolicy(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("write hit policy: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);
            if(!(ret == 2 || ret == 1)){
                System.out.println("write hit policy is between 1 and 2 ");
                return getWriteHitPolicy(systemIN); //recall the function
            }
        }else{
            System.out.println("Invalid input: is the did you put a number? Try again.");
            return getWriteHitPolicy(systemIN);
        }
        return ret;
    }

    public static Integer getWriteMissPolicy(Scanner systemIN){
        String in;
        Integer ret;
        System.out.print("write miss policy: ");
        in = systemIN.nextLine();
        in = in.replace(" ", ""); //get rid of any whitespace they added to this
        if (isNumeric(in)) {
            ret = Integer.parseInt(in);
            if(!(ret == 2 || ret == 1)){
                System.out.println("write miss policy is between 1 and 2 ");
                return getWriteMissPolicy(systemIN); //recall the function
            }
        }else{
            System.out.println("Invalid input: is the did you put a number? Try again.");
            return getWriteMissPolicy(systemIN);
        }
        return ret;
    }




}
