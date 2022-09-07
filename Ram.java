package Cache;

import java.util.Arrays;
import java.util.Scanner;

public class Ram {
    private String[] ram;
    Ram(Integer end){
        this.ram = new String[end+1];
        zeroRam();
        System.out.println("RAM successfully initialized!");
    }


    private void zeroRam(){
        for(int i = 0; i < ram.length; i++) {
            ram[i] = "00";
        }
    }

    public void assignRamWithInput(Scanner input){
        int index = 0;
        while (input.hasNextLine() && index < ram.length){
            ram[index] = input.next();
            index += 1;
        }
    }
    @Override
    public String toString(){
        return Arrays.toString(ram);
    }

    public void setRamIndex(Integer number, String tg){
        ram[number] = tg;
    }
    public String[] getBlockOfRam(Integer blockSize, Integer startingIndex){
        String[] ret = new String[blockSize];
        for(Integer i = 0; i < blockSize; i++){
            ret[i] = ram[startingIndex + i];
        }


        return ret;
    }

    public void assignRamWithBlockOfData(String[] value, Integer startingIndex){
        for(Integer i =0; i< value.length; i++){
            ram[startingIndex + i] = value[i];
        }
    }

    public Integer length(){
        return this.ram.length;
    }

    public String atIndex(Integer value){
        return ram[value];
    }







}
