import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

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
        
        if(grammar.isLL1()){
            System.out.println("Grammar is LL1");
        }
        else{
            //System.out.println("Grammar is not LL1");
        }

        if(args.length < 2){
            System.exit(5);
        }

        grammar.createTable();

        String parse = readInFile(args[1]);

        grammar.parseString(parse);

    }


    public static String readInFile(String args){

        try {

            String output = "";
            File file = new File(args);
            FileReader fr = null;
            BufferedReader br = null;
            String temp = "";

            try {
                fr = new FileReader(file);
                br = new BufferedReader(fr);
            } catch (FileNotFoundException fnfe) {
                System.out.println("File not found");
            }

            while (temp != null) {

                try {
                    temp = br.readLine();
                    if (temp == null) {
                        break;
                    }
                    output += temp + "\n";
                } catch (Exception ioe) {
                    temp = null;
                }
            }

            return output;
        }
        catch (Exception ex){
            return null;
        }

    }
}

class Grammar{

    //Decs
    ArrayList<Rule> rules = new ArrayList<Rule>();
    String[][] table = new String[10][10];


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
                                    //System.out.println(p.charAt(j) + " is looking at " + p.charAt(j+count));
                                    
                                    if(isNonTerminal(p.charAt(j + ++count) + "")){
                                        temp = getFirsts(p.charAt(j + count) + "");
                                        temp.remove("@");
                                        hs.addAll(temp);                                        
                                    }
                                    else{
                                        hs.add(p.charAt(j+count) + "");
                                    }

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
    
    public boolean isLL1(){
        
        //Get the first and followset
        ArrayList<FirstSet> firstSet = getFirsts();
        ArrayList<FollowSet> followSet = getFollows();

        System.out.println("\n");
        
        //Check each partition of each rule
        for(int i = 0; i < rules.size(); i++){
            
            Rule current = rules.get(i);

            if(current.getProductionArray().size() < 2){
                continue;
            }
            else {

                HashSet<String> items = new HashSet<String>();

                //Look at each partition
                for (int j = 0; j < current.getProductionArray().size(); j++) {

                    //We need to compare the first terminal value, or set of values, to each partition
                   // System.out.print(current.getTransition() + " -> ");
                   // System.out.println(getFirstOfPartition(current.getProductionArray().get(j)));

                    for(String str : getFirstOfPartition(current.getProductionArray().get(j))){

                        if(!items.contains(str)){
                            items.add(str);
                        }
                        else{
                            System.out.println("Not LL1 parseable: " + current.getTransition());
                            return false;
                        }
                    }

                    //Handle the empty string
                    if(items.contains("@")){
                        //System.out.println("Need to check the intersect of the follow of " + current.getTransition());
                        //System.out.println(getFollowsOf(current.getTransition()));

                        for(String str : getFollowsOf(current.getTransition())) {
                            if (!items.contains(str)) {
                                items.add(str);
                            } else {
                                System.out.println("Not LL1 parseable: " + current.getTransition());
                                return false;
                            }
                        }
                    }

                }

                //System.out.println(current.getTransition() + " -> " + items);
            }
            
        }

        return true;
    }

    public ArrayList<String> getFollowsOf(String key){
        ArrayList<FollowSet> temp = getFollows();

        for(FollowSet f : temp){
            if(f.nonTerminal.equals(key)){
                return f.firsts;
            }
        }

        return  null;
    }

    public ArrayList<String> getFirstOf(String key){
        ArrayList<FirstSet> temp = getFirsts();

        for(FirstSet f : temp){
            if(f.nonTerminal.equals(key)){
                return f.firsts;
            }
        }

        return  null;
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

    public void printRules(){
        for(Rule rule : rules){
            System.out.println(rule);
        }
    }

    public String createTable(){

        //Terminals need to be stored on table[0][x>0] and transitions need to be stored on [x>0][0]
        ArrayList<String> terminals = new ArrayList<String>();
        ArrayList<String> nonterminals = new ArrayList<String>();

        for(Rule rule : rules){
            nonterminals.add(rule.getTransition());
        }

        for(Rule rule : rules){
            for(String str : rule.getProductionArray()){
                for(int i = 0; i < str.length(); i++) {
                    if (!terminals.contains(str.charAt(i) + "") && !nonterminals.contains(str.charAt(i) + "")) {
                        terminals.add(str.charAt(i) + "");
                    }
                }

            }
        }

        terminals.add("$");
        terminals.remove("@");

        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){

                //Add terminals to the table
                if(i == 0 && j > 0 && j < terminals.size()+1){
                    table[i][j] = terminals.get(j-1);
                }

                //Add nonterminals to the table
                else if(j == 0 && i > 0 && i < nonterminals.size()+1){
                    table[i][j] = nonterminals.get(i-1);
                }

                else{
                    table[i][j] = "";
                }



            }
        }

        //Add items into the correct spot in the array[][]
        for(String transition : nonterminals){
            //Get partitions that go along with those terminals
            ArrayList<String> p = getRules(transition);
            //For each partition belonging to the transition find the terminal that would lead to it.
            ArrayList<String> f = getFirstOf(transition);

            //Look at each rule to identify the first that would lead to it then update that slot in the table
            for(String part : p){
                ArrayList<String> partition_first = getFirstOfPartition(part);

                //We want to place the partition in the table on Transition X Terminal where ever partition_first contains a terminal in f
                for(String fir : f){
                    if(fir.equals("@") && partition_first.contains(fir)){
                        //Get the follow of the current Transition
                        ArrayList<String> fo = getFollowsOf(transition);
                        for(String followItem : fo){
                            int x = searchColumn(transition, table);    //Searches for the transition
                            int y = searchRow(followItem, table);       //Searches for the terminal
                            table[x][y] = part;
                        }
                    }
                    else if (partition_first.contains(fir)){
                        //System.out.println(transition + " -> " + part + " is moved to on " + fir);
                        int x = searchColumn(transition, table);    //Searches for the transition
                        int y = searchRow(fir, table);       //Searches for the terminal
                        table[x][y] = part;
                        //System.out.println("Should be inserted at " + x + "," + y);
                    }
                }
            }


        }


        //Print this for testing
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){

                System.out.print(table[i][j] + "\t");

            }

            System.out.println();
        }

        return null;
    }

    public ArrayList<String> getRules(String key){
        for(Rule rule : rules){
            if(rule.getTransition().equals(key)){
                return rule.getProductionArray();
            }
        }

        return null;
    }

    private int searchRow(String key, String[][] array){
        for(int i = 1; i < 10; i++){
            //System.out.println("Looking at row " + i);
            if(array[0][i].equals(key)){
                return i;
            }
        }

        return 0;
    }

    private int searchColumn(String key, String[][] array){
        for(int i = 1; i < 10; i++){
            //System.out.println("Looking at column " + i);
            if(array[i][0].equals(key)){
                return i;
            }
        }

        return 0;
    }

    public void parseString(String parse){

        System.out.println("Parsing String");
        System.out.println("--------------\n");

        Stack<String> stack = new Stack<String>();
        stack.push("$");
        stack.push(rules.get(0).getTransition());

        parse = parse.replace("\n","") + "$";

        System.out.println(stack + "\t\t" + parse);

        while(!stack.peek().equals("$") && !parse.substring(0,1).equals("$")){

            //Lookup
            String lookup;
            lookup = lookupInTable(stack.peek(), parse.substring(0,1), table);
            //System.out.println("Lookup is " + lookup);

            if(lookup.equals("")){
                System.out.println("LL1 could not parse the string");
                System.out.println(stack.peek() + " | " + parse.substring(0, 1));
                System.exit(2);
            }

            if(stack.peek().equals(lookup.substring(0,1))){
                System.out.println(stack + "\t==\t" + parse);
                stack.pop();
                parse = parse.substring(1, parse.length());
                System.out.println(stack + "\t\t" + parse);
                continue;
            }

            stack.pop();

            for(int i = lookup.length()-1; i > -1; i--){
                stack.push(lookup.substring(i,i+1));
            }
            System.out.println(stack + "\t\t" + parse);

        }


        System.out.println(stack + "\t==\t" + parse);
        System.out.println("ACCEPT");
    }

    public String lookupInTable(String transition, String terminal, String array[][]){
        int y = searchRow(terminal, array);
        int x = searchColumn(transition, array);
        return array[x][y];
    }

    public String[][] getTable(){
        return table;
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
