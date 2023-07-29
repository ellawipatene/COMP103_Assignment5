// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 5
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

// Does not work for ( - ( * 2 4 ) ( / 2 4 ) )
// sqrt does not work

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/** 
 * Calculator for Cambridge-Polish Notation expressions
 * (see the description in the assignment page)
 * User can type in an expression (in CPN) and the program
 * will compute and print out the value of the expression.
 * The template provides the method to read an expression and turn it into a tree.
 * You have to write the method to evaluate an expression tree.
 *  and also check and report certain kinds of invalid expressions
 */

public class CPNCalculator{
    public final double PI = Math.PI; 
    public final double E = Math.E; 
    public Stack<ExpElem> brackets = new Stack<ExpElem>();  // makes sure that there is an equal amount of opening and closing brackets

    /**
     * Setup GUI then run the calculator
     */
    public static void main(String[] args){
        CPNCalculator calc = new CPNCalculator();
        calc.setupGUI();
        calc.runCalculator();
    }

    /** Setup the gui */
    public void setupGUI(){
        UI.addButton("Clear", UI::clearText); 
        UI.addButton("Quit", UI::quit); 
        UI.setDivider(1.0);
    }

    /**
     * Run the calculator:
     * loop forever:  (a REPL - Read Eval Print Loop)
     *  - read an expression,
     *  - evaluate the expression,
     *  - print out the value
     * Invalid expressions could cause errors when reading or evaluating
     * The try-catch prevents these errors from crashing the program - 
     *  the error is caught, and a message printed, then the loop continues.
     */
    public void runCalculator(){
        UI.println("Enter expressions in pre-order format with spaces");
        UI.println("eg   ( * ( + 4 5 8 3 -10 ) 7 ( / 6 4 ) 18 )");
        while (true){
            UI.println();
            try {
                GTNode<ExpElem> expr = readExpr();
                if(!brackets.isEmpty()){UI.println("Sorry, odd amount of brackets.");}
                else{
                    double value = evaluate(expr);
                    UI.println(" -> " + value);
                }
            }catch(Exception e){UI.println("Something went wrong! "+e);}
        }
    }

    /**
     * Evaluate an expression and return the value
     * Returns Double.NaN if the expression is invalid in some way.
     * If the node is a number
     *  => just return the value of the number
     * or it is a named constant
     *  => return the appropriate value
     * or it is an operator node with children
     *  => evaluate all the children and then apply the operator.
     */
    public double evaluate(GTNode<ExpElem> expr){
        if (expr==null){
            return Double.NaN;
        }

        /*# YOUR CODE HERE */
        ExpElem element = expr.getItem();
        String operator = element.operator;
        int childLength = expr.numberOfChildren();
        
        if(operator.equals("#")){ return element.value; } // if the element is a number
        else if(operator.equals("PI")){ return PI;} // returns 3.14...
        else if(operator.equals("E")){return E;} // expodential value
        else if(operator.equals("+")){
            double answer = 0;
            for (int i = 0; i < childLength; i++){  // plus all of the children together
                answer += evaluate(expr.getChild(i)); 
                if( i < childLength -1){printExpr("+", evaluate(expr.getChild(i)), false);}
                else{printExpr("+", evaluate(expr.getChild(i)), true);}
            }
            return answer;
        }else if(operator.equals("-")){ // minus all of the children from the first child
            double answer = evaluate(expr.getChild(0));
            printExpr("-", evaluate(expr.getChild(0)), false);
            for (int i = 1; i < childLength; i++){ 
                answer -= evaluate(expr.getChild(i)); 
                if( i < childLength -1){printExpr("-", evaluate(expr.getChild(i)), false);}
                else{printExpr("-", evaluate(expr.getChild(i)), true);}
            }
            return answer;
        }else if(operator.equals("*")){ // times all of the children together 
            double answer = 1;
            for (int i = 0; i < childLength; i++){ 
                answer = answer * evaluate(expr.getChild(i)); 
                if( i < childLength -1){printExpr("*", evaluate(expr.getChild(i)), false);}
                else{printExpr("*", evaluate(expr.getChild(i)), true);}
            }
            return answer;
        }else if(operator.equals("/")){ // divide the first child by the rest
            double answer = evaluate(expr.getChild(0));
            printExpr("/", evaluate(expr.getChild(0)), false);
            for (int i = 1; i < childLength; i++){  // start the loop at the 2nd child
                answer = answer / evaluate(expr.getChild(i));
                if( i == 1){
                    UI.print("(");
                    printExpr(" * ", evaluate(expr.getChild(i)), false); 
                }else if (i == childLength -1){
                    printExpr("*", evaluate(expr.getChild(i)), true);
                    UI.println(")"); 
                }else{
                    printExpr(" * ", evaluate(expr.getChild(i)), false); 
                }
            }
            return answer;
        }else if(operator.equals("sqrt")){  // squareroot the opperant
            if(childLength == 1){
                UI.print("sqrt"); 
                printExpr(" sqrt ", evaluate(expr.getChild(0)), true); 
                return Math.sqrt(evaluate(expr.getChild(0)));
            }
            else{
                UI.println("Too many operands for sqrt, please only enter one."); 
                return Double.NaN;
            }
        }else if(operator.equals("log")){
            if(childLength == 1) { // one opperant = 1og base 10
                UI.print("log10");
                printExpr(" log ", evaluate(expr.getChild(0)), true);
                return Math.log10(evaluate(expr.getChild(0))); 
            }
            else if(childLength == 2){ // two operants = log base operant 1
                UI.print("log");
                printExpr(String.valueOf(evaluate(expr.getChild(0))), evaluate(expr.getChild(1)), false);
                return Math.log10(evaluate(expr.getChild(0))) / Math.log10(evaluate(expr.getChild(1))); }
            else{  // if childLength > 2
                UI.println("Too many operands for log."); 
                return Double.NaN;
            }
        }else if(operator.equals("ln")){ // log to the base e
            if(childLength == 1){
                UI.print("ln");
                printExpr(" ln ", evaluate(expr.getChild(0)), true);
                return Math.log(evaluate(expr.getChild(0)));
            } 
            else {
                UI.println("Too many operands for ln, please only enter one."); 
                return Double.NaN;
            }
        }else if(operator.equals("^")){ // to the power of
            if(childLength == 2){
                UI.print("^");
                printExpr(" ^ ", evaluate(expr.getChild(0)), true);
                return Math.pow(evaluate(expr.getChild(0)), evaluate(expr.getChild(1)));}
            else {
                UI.println("For '^' please enter two operands."); 
                return Double.NaN;
            }
        }else if(operator.equals("sin")){
            UI.print("sin");
            printExpr("sin", evaluate(expr.getChild(0)), true);
            return Math.sin(evaluate(expr.getChild(0)));
        }else if(operator.equals("cos")){
            UI.print("cos");
            printExpr("cos", evaluate(expr.getChild(0)), true);
            return Math.cos(evaluate(expr.getChild(0)));
        }else if(operator.equals("tan")){
            UI.print("tan");
            printExpr("tan", evaluate(expr.getChild(0)), true);
            return Math.tan(evaluate(expr.getChild(0))); 
        }else if(operator.equals("dist")){ // distance between two points
            if(childLength == 4){ 
                double total_pow = Math.pow(evaluate(expr.getChild(0)) - evaluate(expr.getChild(2)), 2) + 
                                   Math.pow(evaluate(expr.getChild(1)) - evaluate(expr.getChild(3)), 2);
                return Math.sqrt(total_pow); 
            }else if (childLength == 6){ 
                double total_pow = Math.pow(evaluate(expr.getChild(0)) - evaluate(expr.getChild(3)), 2) +
                                   Math.pow(evaluate(expr.getChild(1)) - evaluate(expr.getChild(4)), 2) +
                                   Math.pow(evaluate(expr.getChild(2)) - evaluate(expr.getChild(5)), 2);
                return Math.sqrt(total_pow);
            }
            else{
                UI.println("Invalid number of operants for 'dist'. Please enter either 4 or 6.");
                return Double.NaN;
            }
        }else if(operator.equals("avg")){
            if(childLength > 0){ // does not divide by 0
                double answer = 0;
                for (int i = 0; i < childLength; i++){ answer += evaluate(expr.getChild(i)); }
                return answer/childLength;
            }else{
               UI.println("'avg' must have atleast one operant."); 
               return Double.NaN; 
            }
        }else{
            UI.println(operator + " is not a valid opperator"); 
            return Double.NaN;
        }
    }
    
    /** Prints the expression in a readable manner 
       Does not work perfectly
    */ 
    public void printExpr(String operator, double value, boolean end){
        if(!end){UI.print(value + operator);}
        else{UI.print(value);}
    }

    /** 
     * Reads an expression from the user and constructs the tree.
     */ 
    public GTNode<ExpElem> readExpr(){
        String expr = UI.askString("expr:");
        return readExpr(new Scanner(expr));   // the recursive reading method
    }

    /**
     * Recursive helper method.
     * Uses the hasNext(String pattern) method for the Scanner to peek at next token
     */
    public GTNode<ExpElem> readExpr(Scanner sc){
        if (sc.hasNextDouble()) {                     // next token is a number: return a new node
            return new GTNode<ExpElem>(new ExpElem(sc.nextDouble()));
        }
        else if (sc.hasNext("\\(")) {                 // next token is an opening bracket
            brackets.push(new ExpElem(sc.next()));    // read and add opening bracket to stack
            ExpElem opElem = new ExpElem(sc.next());  // read the operator
            GTNode<ExpElem> node = new GTNode<ExpElem>(opElem);  // make the node, with the operator in it.
            int counter = 0;                          // counter to make sure there are no empty bracke
            while (! sc.hasNext("\\)")){              // loop until the closing ')'
                GTNode<ExpElem> child = readExpr(sc); // read each operand/argument
                node.addChild(child);                 // and add as a child of the node
                counter++; 
            }
            if(counter == 0){
                UI.println("Error, empty brackets."); // unable to get it to print :( 
                return null; 
            }
            sc.next();                                // read and throw away the closing ')'
            brackets.pop();                           // removes the matching bracket.
            return node;
        }
        else {                                        // next token must be a named constant (PI or E)
                                                      // make a token with the name as the "operator"
            return new GTNode<ExpElem>(new ExpElem(sc.next()));
        }
    }

}

