
package twopassassembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TwoPassAssembler {
    
    protected ArrayList<String> inp = new ArrayList();
    protected HashMap<String,Integer> opTable = new HashMap();
    protected HashMap<String,Integer> symTable = new HashMap();
    protected ArrayList<String> intermediate = new ArrayList();
    protected ArrayList<String> instOperand = new ArrayList();
    protected ArrayList<String> object = new ArrayList();
    protected ArrayList<String> objectCode = new ArrayList();
    protected ArrayList<String> addObject = new ArrayList();
    private Scanner scan;
    public static int LOCCTR=0;
    protected static int startAddress;
    protected static int endAddress;
    protected static int fileLength;
    protected String objectName;
    protected static int objectStart;
    
    public void initializeOpTable() throws FileNotFoundException{
        scan = new Scanner(new File("./src/twopassassembler/opTable.txt"));
        while(scan.hasNextLine()){
            String temp = scan.nextLine();
            String[] xy = temp.split("\\s+");
            opTable.put(xy[0], Integer.parseInt(xy[1],16));
            //StringTokenizer x = new StringTokenizer();
        }
    }
    
    /*public void initializeSymTable() throws FileNotFoundException{
        scan = new Scanner(new File("./src/twopassassembler/symTable.txt"));
        while(scan.hasNextLine()){
            String temp = scan.nextLine();
            symTable.put(temp.split("\\s+")[0], Integer.parseInt(temp.split("\\s+")[1]));
            //StringTokenizer x = new StringTokenizer();
        }
    }*/
    
    public void writeIntermediate() throws FileNotFoundException, IOException{
        FileWriter x = new FileWriter("./src/twopassassembler/intermediate.txt");
        for(int i=0;i<intermediate.size();i++){
        x.write(intermediate.get(i) + '\n');
        }
        x.close();
    }
    
    public void writeSymTable() throws IOException{
        FileWriter x = new FileWriter("./src/twopassassembler/symbolTable.txt");
        for(String key : symTable.keySet() ){
        x.write(key +"\t"+Integer.toHexString(symTable.get(key)) + "\n");
        }
        x.close();
    }
    
    
    public void loadInput() throws FileNotFoundException{
        scan = new Scanner(new File("./src/twopassassembler/input4.txt"));
        while(scan.hasNextLine()){
            inp.add(scan.nextLine());
        }
    }
    
    public void pass1(){
        boolean first = false;
        for(int i=0;i<inp.size();i++){
            if(inp.get(i).charAt(0) == '.'){continue;}
            
            String[] temp = inp.get(i).split("\\s+");
            //System.out.println("String : " + inp.get(i));
            String label,instruction,operand;
            if(temp.length == 3){
                if(temp[0].length()==0){label="7amada";}else{label = temp[0];}
                instruction = temp[1];
                operand = temp[2];
            }
            else if(temp.length==2){
                label = "7amada";
                instruction = temp[0];
                operand = temp[1];
            }
            else if(temp.length == 1){
                label = "7amada";
                instruction = temp[0];
                operand = "7amada";
            }
            else{System.out.println("Error ya gooz 7meer");return;}
            
            if(instruction.length()==0){instruction=operand;operand="7amada";}
            
            //System.out.println("Loop"+i+"  label: "+label + "  instruction: "+instruction+"  operand: "+operand);
            
            
            int oldLOCCTR;

                if(!first){startAddress = LOCCTR;first=true;}
                endAddress = LOCCTR;
                
                if(operand!="7amada"){instOperand.add(instruction + " " + operand + " " + LOCCTR);}
                else{instOperand.add(instruction + " " + "Empty" + " " + LOCCTR);}
            
            
            if(instruction.equals("START")){
                LOCCTR = Integer.parseInt(operand, 16);
                startAddress = LOCCTR;
                objectStart = LOCCTR;
                objectName = label;
                intermediate.add(Integer.toHexString(LOCCTR) + "\t" + inp.get(i));
                continue;
            }
            else if(instruction.equals("END")){
                intermediate.add(inp.get(i));
                return;
            }
            else{
                if(label.equals("7amada")){}
                else{
                    if(symTable.containsKey(label)){System.out.println("Error, Duplicate Label" + label + "found    LOCCTR: " + Integer.toHexString(LOCCTR));}
                    else{
                        symTable.put(label, LOCCTR);
                    }
                }
                
                oldLOCCTR = LOCCTR;
                
                if(opTable.containsKey(instruction)){LOCCTR+=3;}
                else if(instruction.equals("WORD")){LOCCTR+=3;}
                else if(instruction.equals("RESW")){LOCCTR+= (3*(Integer.parseInt(operand)));}
                else if(instruction.equals("RESB")){;LOCCTR+= Integer.parseInt(operand);}
                else if(instruction.equals("BYTE")){
                    //Count # of chars, add the number to the LOCCTR;
                    if(operand.charAt(0) == 'C'){LOCCTR += (operand.length()-3);}
                    else{LOCCTR += 1;}
                }
                else{System.out.println("Error, invalid operation code"+  instruction  + "LOCCTR: " + Integer.toHexString(LOCCTR));return;}
                
            }
                  
            intermediate.add(Integer.toHexString(oldLOCCTR) + "\t" + inp.get(i));
        }
        
        fileLength = LOCCTR - startAddress;
    }
    
    
    
    protected void setObjectCode(){
        for(int i=0;i<instOperand.size();i++){
            String temp[] = instOperand.get(i).split("\\s+");
            String instruction = temp[0];
            String operand=temp[1];
            String code = "";
            int loc = Integer.parseInt(temp[2]);
            
            if(instruction.equals("START") || instruction.equals("END") || instruction.equals("RESW") || instruction.equals("RESB")){
                objectCode.add(code);
            }
            else{
                if(opTable.containsKey(instruction)){
                    code = Integer.toHexString(opTable.get(instruction));
                    String address;
                    if(symTable.containsKey(operand)){
                        code = code + Integer.toHexString(symTable.get(operand));
                        objectCode.add(code);
                    }
                    else if(operand.charAt(operand.length()-1) == 'X' && operand.charAt(operand.length()-2) == ','){
                        address = Integer.toHexString(32768 + symTable.get(operand.substring(0,operand.length()-2)));
                        code = code + address;
                        objectCode.add(code);
                    }
                    else{System.out.println("Unkown opernad! " + operand + "  Instruction : " + instruction);
                        while(code.length()<6){code = code + "0";}
                        objectCode.add(code);
                    }
                }
                else if(instruction.equals("WORD")){
                    code = operand;
                    while(code.length()<6){code = "0" + code;}
                    objectCode.add(code);
                }
                else if(instruction.equals("BYTE")){
                    if(operand.charAt(0) == 'C'){
                        String sub = operand.substring(2,operand.length()-1);
                        for(int k=0;k<sub.length();k++){
                            code = code + Integer.toHexString((int) sub.charAt(k));
                        }
                        objectCode.add(code);
                    }
                    else{
                        code = operand.substring(2, operand.length()-1);
                        objectCode.add(code);
                    }
                }
                else{System.out.println("Error! Uknown instruction" + instruction);return;}
            }
            addObject.add(code + " " + loc);
        }
    }
    
    
    public void writeListing() throws IOException{
        FileWriter x = new FileWriter("./src/twopassassembler/listing.txt");
        for(int i=0;i<intermediate.size();i++){
            x.write(intermediate.get(i) + "\t" + objectCode.get(i) + "\n");
        }
        x.close();
    }
    
    
    
    public void pass2(){
        String sAddress = Integer.toHexString(startAddress);
        while(sAddress.length()<6){sAddress = "0" + sAddress;}
        String len = Integer.toHexString(endAddress - startAddress);
        while(len.length()<6){len = "0" + len;}
        object.add("H "+ objectName + " " + sAddress + " " + len);
        
        String temp="";
        boolean flag=false;
        int floc=startAddress,lloc=0;
        int counter=0;
        for(int i=1;i<addObject.size()-1;i++){
            String t[] = addObject.get(i).split("\\s+");
            String obj = t[0];
            int ctr = Integer.parseInt(t[1]);
            if(obj.equals("")){
                counter=0;
                try{lloc = ctr-1;}catch(Exception e){System.out.println(e);}
                if(temp.length()!=0){
                object.add("T " + Integer.toHexString(floc) + " " + (Integer.toHexString(lloc-floc)) + temp);
                try{floc = ctr-1;}catch(Exception e){System.out.println(e);}
                }
                temp="";                
            }
            else if(counter==10){
                counter=0;
                try{lloc = ctr-1;}catch(Exception e){System.out.println(e);}
                if(temp.length()!=0){
                object.add("T " + Integer.toHexString(floc) + " " + (Integer.toHexString(lloc-floc)) + temp);
                }
                try{floc = ctr-1;}catch(Exception e){System.out.println(e);}
                temp="";
                i--;
            }
            else{
                temp = temp + " " + obj;
                counter++;
            }
        }
        
        
        
        object.add("E " + sAddress);
       
    }
    
    public void writeObject() throws IOException{
        FileWriter x = new FileWriter("./src/twopassassembler/objectCode.txt");
        for(int i=0;i<object.size();i++){
            x.write(object.get(i) + "\n");
        }
        x.close();
    }
    
    
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
       TwoPassAssembler x = new TwoPassAssembler();
       x.loadInput();
       x.initializeOpTable();
       x.pass1();
       x.writeIntermediate();
       x.writeSymTable();
       x.setObjectCode();
       x.writeListing();
       x.pass2();
       x.writeObject();
    }
    
}
