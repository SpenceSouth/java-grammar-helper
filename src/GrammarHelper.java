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

    public ArrayList<String> getFirsts(){



        return null;
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
