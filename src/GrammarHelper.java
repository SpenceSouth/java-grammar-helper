import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Spence on 2/19/2015.
 */
public class GrammarHelper {
    public static void main(String args[]){

        //Decs
        Grammar grammar = new Grammar();

        //Import grammar from file
        String raw = readInFile(args[0]);
        String[] split = raw.split("\n");

        for(String s : split){
            grammar.addRule(s);
        }

        System.out.println(grammar.toString());

        System.out.println("\n\nFirsts:");

        for(FirstSet fs : grammar.getFirsts()){
            System.out.println(fs.toString());
        }

        System.out.println("\n\nFollows:");

        for(FollowSet fs : grammar.getFollows()){
            System.out.println(fs.toString());
        }
        
        grammar.isLL1();

    }


    public static String readInFile(String args){

        String output = "";
        File file = new File(args);
        FileReader fr = null;
        BufferedReader br = null;
        String temp = "";

        try{
            fr = new FileReader(file);
            br = new BufferedReader(fr);
        }
        catch(FileNotFoundException fnfe){
            System.out.println("File not found");
        }

        while(temp != null){

            try {
                temp = br.readLine();
                if(temp == null){
                    break;
                }
                output += temp + "\n";
            }
            catch(Exception ioe){
                temp = null;
            }
        }

        return output;

    }
}

class Grammar{

    //Decs
    ArrayList<Rule> rules = new ArrayList<Rule>();


    public Grammar(){

    }

    public void addRule(String s){
        String[] splitRule = s.split(" -> ");
        rules.add(new Rule(splitRule[0], splitRule[1]));
    }

    public String toString(){
        String result = "";

        for(Rule rule : rules){
            result += rule.toString() + "\n";
        }

        return result;
    }

    public boolean isNonTerminal(String s){
        for(Rule rule : rules){
            if(s.equals(rule.getTransition())){
                return true;
            }
        }

        return false;
    }

    public ArrayList<FirstSet> getFirsts(){

        ArrayList<FirstSet> fs = new ArrayList<FirstSet>();

        try {
            for (Rule rule : rules) {
                fs.add(new FirstSet(rule.getTransition(), getFirsts(rule.getTransition())));
            }
        }
        catch(StackOverflowError sofe){
            System.out.println("This is not LL(1) parseable");
        }

        return fs;
    }

    private ArrayList<String> getFirsts(String t){

        Rule current = null;
        ArrayList<String> f = new ArrayList<String>();
        HashSet<String> hs = new HashSet<String>();

        //Find the transition we want to work with
        for(Rule rule : rules){
            if(rule.getTransition().equals(t)){
                current = rule;
            }
        }

        if(current == null){
            return f;
        }

        //System.out.println("Working with " + current.getTransition());

        //Go through each partition and add terminals to the firstset
        for(int i = 0; i < current.getProductionArray().size(); i++){

            int empty = 0;

            for(int j = 0; j < current.getProductionArray().get(i).length(); j++){

                //If terminal, add it then break,
                if(!isNonTerminal(current.getProductionArray().get(i).charAt(j) + "")){
//                    f.add(current.getProductionArray().get(i).charAt(j) + "");
                    hs.add(current.getProductionArray().get(i).charAt(j) + "");

                    break;

                }
                else{
                    //Check the first of the nonterminal
                    //f.addAll(getFirsts(current.getProductionArray().get(i).charAt(j) + ""));

                    for(String s : getFirsts(current.getProductionArray().get(i).charAt(j) + "")){
                        /*if(!f.contains(s)){
                            f.add(s);
                        }*/
                        if(!hs.contains(s)){
                            hs.add(s);
                        }
                    }

                    //If the nonterminal has an empty string proceed to the next item
                    if((getFirsts(current.getProductionArray().get(i).charAt(j) + "").contains("@"))){
                        //Remove @ from hs
                        hs.remove("@");
                        empty++;
                    }
                    else{
                        break;
                    }
                }

                if(empty == current.getProductionArray().get(i).length()){
                    hs.add("@");
                }

            }

        }

        f.addAll(hs);
        return f;
    }

    public ArrayList<FollowSet> getFollows(){

        int count = 0;

        //Generate followset for each non-terminal and do pass 1
        ArrayList<FollowSet> fs = new ArrayList<FollowSet>();
        for(Rule rule : rules){
            FollowSet temp = new FollowSet(rule.getTransition());
            temp.firsts.addAll(getFollowsPass1(rule.getTransition()));
            if(count++ == 0){
                temp.firsts.add("$");
            }
            fs.add(temp);
        }

        //Do pass 2
        getFollowsPass2(fs);

        return fs;
    }

    /**Gets the follow set for a defined transition */
    private HashSet<String> getFollowsPass1(String s){

        Rule current = null;
        HashSet<String> hs = new HashSet<String>();


        //Go through each rule
        for(int k = 0; k < rules.size(); k++) {

            current = rules.get(k);

            //Look at the partitions of each transition rule
            for (int i = 0; i < current.getProductionArray().size(); i++) {

                //Go through each partition character by character looking at the follows
                for (int j = 0; j < current.getProductionArray().get(i).length(); j++) {
                    String p = current.getProductionArray().get(i);

                    //If it is a terminal then we can move on. We are not looking for the follows of terminals.
                    if (!isNonTerminal(p.charAt(j) + "")) {
                        continue;
                    }
                    //If is is a transition then we need to record what follows it.
                    else if (isNonTerminal(p.charAt(j) + "") && (j != p.length() - 1)) {

                        //Check to see what comes after the transition
                        if (isNonTerminal(p.charAt(j + 1) + "")) {

                            if(s.equals(p.charAt(j) + "")) {

                                int count = 1;

                                ArrayList<String> temp = getFirsts(p.charAt(j + 1) + "");
                                temp.remove("@");
                                hs.addAll(temp);

                                //TODO: Something needs to happen here to address transitions that can go away
                                //TODO: Needs to check and see that there is one more spot in the string to look ahead at
                                while(getFirsts(p.charAt(j + count) + "").contains("@") && ((j + count) < current.getProductionArray().get(i).length())){

                                    if(j+count == p.length()-1) break;

                                    temp = getFirsts(p.charAt(j + ++count) + "");
                                    temp.remove("@");
                                    hs.addAll(temp);

                                }


                            }

                            //TODO: This statement may not be necessary or may even be problematic
                            /*if (!(getFirsts(p.charAt(j + 1) + "")).contains("@")) {
                                break;
                            }*/

                        }
                        else {
                            if(s.equals(p.charAt(j) + "")) {
                                //System.out.println(s + " equals " + p.charAt(j) + ". Next character is " + p.charAt(j+1));
                                hs.add(p.charAt(j + 1) + "");
                            }
                            else{
                                //System.out.println(s + " did not equal " + p.charAt(j));
                            }
                            //TODO: Suspect follow issue is here
                            //break;
                        }

                    }

                }
            }
        }


        return hs;
    }

    private void getFollowsPass2(ArrayList<FollowSet> fs){

        //Remember to loop around to keep checking each follows until nothing changes
        boolean modified;
        String partition;
        Rule current;

        do{

            modified = false;

            for(int i = 0; i < rules.size(); i++){
                current = rules.get(i);

                //Gets the follow set for the current rule
                FollowSet f = fs.get(i);

                //Look at the partitions of each rule
                for(int j = 0; j < current.getProductionArray().size(); j++){

                    partition = current.getProductionArray().get(j);

                    //Go through each string backwards
                    for(int k = partition.length()-1; k > -1; k--){

                        String endChar = partition.charAt(k) + "";

                        //If the string ends with a terminal then we can move on
                        if(!isNonTerminal(partition.charAt(k) + "")){
                            break;
                        }
                        //If not then it is a transition.
                        else{

                            //What follows the current rule must follow the transition at the end of that partition.

                            //Find the transition we want to work with
                            FollowSet set = null;

                            for(FollowSet temp : fs){
                                if(temp.nonTerminal.equals(endChar)){
                                    set = temp;
                                }
                            }

                            //Add any follows from f to set.  Record if any modifications were made.
                            if(set.add(f)){
                                modified = true;
                            }

                            //If the current terminal can go away continue, else break
                            if(!getFirsts(endChar).contains("@")){
                                break;
                            }


                        }

                    }

                }

            }

        } while(modified);


    }
    
    public void isLL1(){
        
        //Get the first and followset
        ArrayList<FirstSet> firstSet = getFirsts();
        ArrayList<FollowSet> followSet = getFollows();
        
        //Check each partition of each rule
        for(int i = 0; i < rules.size(); i++){
            
            Rule current = rules.get(i);
            
            //Look at each partition
            for(int j = 0; j < current.getProductionArray().size(); j++){
                
                
                
            }
            
        }
        
    }
    
    private ArrayList<String> getFirstOfPartition(String partition){
        
        ArrayList<String> result = new ArrayList<String>();
        HashSet<String> hs = new HashSet<String>();
        boolean empty = true;
        
        for(int i = 0; i < partition.length(); i++){
            if(!isNonTerminal(partition.charAt(i) + "")){
                hs.add(partition.charAt(i) + "");
                empty = false;
                break;
            }
            //Dealing with nonterminals
            else{

                ArrayList<String> temp = getFirsts(partition.charAt(i) + "");
                temp.remove("@");
                hs.addAll(temp);
                
                //Get the first of the nonterminal
                if(!getFirsts(partition.charAt(i) + "").contains("@")){
                    //It can go away so we need to check the next character as well
                    
                }
                else{
                    empty = false;
                    break;
                }
                
            }
        }
        
        if(empty){
            hs.add("@");
        }

        result.addAll(hs);
        
        return result;
    }

}

class Rule{

    //Decs
    String transition = null;
    ArrayList<String> productions = new ArrayList<String>();

    public Rule(){

    }

    public Rule(String transition, ArrayList<String> productions){
        this.transition = transition;
        this.productions = productions;
    }

    public Rule(String transition, String production){
        this.transition = transition;
        String[] split = production.split(" \\| ");

        for(String s : split){
            productions.add(s);
        }
    }

    public String getTransition(){
        return transition;
    }

    public String getProductions(){
        return productions.toString();
    }

    public ArrayList<String> getProductionArray(){
        return productions;
    }

    public String toString(){

        String t = "";

        for(int i = 0; i < productions.size(); i++){
            t += productions.get(i);

            if(i != productions.size()-1){
                t += " | ";
            }
        }

        return transition + " -> " + t;
    }

}

class FirstSet{

    //Dec
    String nonTerminal;
    ArrayList<String> firsts = new ArrayList<String>();

    public FirstSet(){

    }

    public FirstSet(String s){
        nonTerminal = s;
    }

    public FirstSet(String s, ArrayList<String> firsts){
        nonTerminal = s;
        this.firsts = firsts;
    }

    public boolean add(String s){
        if(!firsts.contains(s)){
            firsts.add(s);
            return true;
        }
        return false;
    }

    public boolean contains(String s){
        return firsts.contains(s);
    }

    public String toString(){
        return nonTerminal + " = " + firsts;
    }

}

class FollowSet extends FirstSet{

    public FollowSet(){

    }

    public FollowSet(String s){
        nonTerminal = s;
    }

    public boolean add(FollowSet fs){

        boolean result = false;
        ArrayList<String> copySet = fs.firsts;

        for(String str : copySet){
            if(!this.firsts.contains(str)){
                firsts.add(str);
                result = true;
            }
        }

        return result;
    }

}
