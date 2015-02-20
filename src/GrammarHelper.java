import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

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

        for(Rule rule : rules){
            fs.add(new FirstSet(rule.getTransition(), getFirsts(rule.getTransition())));
        }

        return fs;
    }

    private ArrayList<String> getFirsts(String t){

        Rule current = null;
        ArrayList<String> f = new ArrayList<String>();
        ArrayList<Integer> empty = new ArrayList<Integer>();

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
            for(int j = 0; j < current.getProductionArray().get(i).length(); j++){

                //If terminal, add it then break,
                if(!isNonTerminal(current.getProductionArray().get(i).charAt(j) + "")){
                    f.add(current.getProductionArray().get(i).charAt(j) + "");
                    empty.add(1);

                    break;

                }
                else{
                    //Check the first of the nonterminal
                    //f.addAll(getFirsts(current.getProductionArray().get(i).charAt(j) + ""));

                    for(String s : getFirsts(current.getProductionArray().get(i).charAt(j) + "")){
                        if(!f.contains(s)){
                            f.add(s);
                        }
                    }

                    //If the nonterminal has an empty string proceed to the next item
                    if((getFirsts(current.getProductionArray().get(i).charAt(j) + "").contains("@"))){
                        empty.add(0);
                    }
                    else{
                        //empty.add(1);
                        break;
                    }
                }

            }
        }

        return f;
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

    public void addFirst(String s){
        if(!firsts.contains(s)){
            firsts.add(s);
        }
    }

    public boolean contains(String s){
        return firsts.contains(s);
    }

    public String toString(){
        return nonTerminal + " = " + firsts;
    }

}
