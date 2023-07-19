/* Code for COMP103 - 2021T2, Assignment 4
 * Name: Annie Cho
 * Username: choanni
 * ID: 300575457
 */

/**
 * Implements a decision tree that asks a user yes/no questions to determine a decision.
 * Eg, asks about properties of an animal to determine the type of animal.
 * 
 * A decision tree is a tree in which all the internal nodes have a question, 
 * The answer to the question determines which way the program will
 *  proceed down the tree.  
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 *
 * The decision tree may be a predermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 *
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree. 
 *
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Color;
import java.io.FileWriter;

public class DecisionTree {
    public DTNode theTree;    // root of the decision tree;
    public List<String> textFile = new ArrayList<String>();
    
    public float TOP = 20;
    public float BOTTOM = 600;
    public float LEFT = 50;
    public float BOXHEIGHT = 15;
    public float BOXLENGTH = 100;
    
    /**
     * Setup the GUI and make a sample tree
     */
    public static void main(String[] args){
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI(){
        UI.addButton("Load Tree", ()->{loadTree(UIFileChooser.open("File with a Decision Tree"));});
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", this::saveTree);  // for completion
        UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", ()->{loadTree("sample-animal-tree.txt");});
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    
    /**  
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     * 
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     *  (The indentation string will be a string of space characters)
     */
    public void printTree(){
        UI.clearText();
        if(theTree != null){
            UI.println(theTree.getText() + "?");
            helper(theTree.getYes(), "yes", "   ");
            helper(theTree.getNo(), "no", "   ");
        }
    }
    
    /**
     * Recursive helper method for printTree()
     */
    public void helper(DTNode node, String yn, String indent){
        UI.println(indent + yn + ": " + node.getText() + "?");
        if(!node.isAnswer()){
            helper(node.getYes(), "yes", indent + "   ");
            helper(node.getNo(), "no", indent + "   ");
        }
    }

    
    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */
    public void runTree() {
        String answer = UI.askString(theTree.getText()); // asks the first question
        DTNode current = theTree.getYes();               // initialised
        if(answer.equals("yes") || answer.equals("y")){current = theTree.getYes();}
        else if(answer.equals("no") || answer.equals("n")){current = theTree.getNo();}
        while(!current.isAnswer() || current.getMoreNodes()){
            if (current.getMoreNodes() == false){
                answer = UI.askString(current.getText());
                if (answer.equals("yes") || answer.equals("y")){current = current.getYes();}
                else if(answer.equals("no") || answer.equals("n")){current = current.getNo();}
            }
            else{
                UI.println(current.getText());
                List<String> possibleAnswersString = current.getStringList();
                List<DTNode> possibleAnswers = current.getList();
                UI.println("Your possible answers are: ");
                for(int i=0; i<current.getChildrenCount(); i++){
                    // for each children in the arraylist
                    UI.print(possibleAnswers.get(i).getText() + " ");
                }
                UI.println();
                answer = UI.askString("Enter your answer here: ");
                int index = possibleAnswersString.indexOf(answer); // finds the index of the users answer in the list
                current = possibleAnswers.get(index);              // goes to the brown node
                current = current.getOther();
            }
        }
        UI.println("The answer is: " + current.getText());
    }

    
    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     *  until it finally gets to a leaf node. 
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     *  - asks the user what the decision should have been,
     *  - asks for a question to distinguish the right decision from the wrong one
     *  - changes the text in the node to be the question
     *  - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree() {
        String answer = UI.askString(theTree.getText());        // asks the first question
        DTNode current = theTree.getYes();                      // initialised
        if(answer.equals("yes") || answer.equals("y")) {current = theTree.getYes();}
        else if(answer.equals("no") || answer.equals("n")) {current = theTree.getNo();}
        
        // will go through the tree as long as the current node has more to ask
        while(!current.isAnswer() || current.getMoreNodes()){
            if(current.getMoreNodes() == false){
                answer = UI.askString(current.getText());
                if (answer.equals("yes") || answer.equals("y")){ current = current.getYes();} 
                else if(answer.equals("no") || answer.equals("n")) {current = current.getNo();}
            }
            else{ // it has multiple children
                UI.println(current.getText());
                List<String> possibleAnswersString = current.getStringList();
                List<DTNode> possibleAnswers = current.getList();
                UI.println("Your possible answers are: ");
                for(int i=0; i<current.getChildrenCount(); i++){
                    // for each children in the arraylist
                    UI.print(possibleAnswers.get(i).getText() + " ");
                }
                UI.println();
                answer = UI.askString("Enter your answer here: ");
                int index = possibleAnswersString.indexOf(answer); // finds the index of the users answer in the list
                current = possibleAnswers.get(index);              // goes to the brown node
                current = current.getOther();
            }
        }
        
        
        // once we reach a node without any children
        answer = UI.askString("Is the answer " + current.getText() + "?");
        
        if(answer.equals("yes") || answer.equals("y")){ UI.println("Yay! Thank you.");} 
        else if(answer.equals("no") || answer.equals("n")){
            int num = UI.askInt("How many branches do you want to add? (One or more)");
            if(num <= 2){
                String newAnswer = UI.askString("Ah, so what should the answer be?"); // dog
                answer = UI.askString("Tell me a property that is true for " + newAnswer + " but not " + current.getText() + "? ");
                UI.println("Thank you! I will now update my decision tree.");
                String oldAnswer = current.getText();   // saves the old text/leaf of the node
                current.setText(answer);                // sets the text of the node to be a question/property
                current.setChildren(new DTNode(newAnswer), new DTNode(oldAnswer));
            }
            else{
                String property = UI.askString("Please tell me the property. (eg. Colour? Pattern?) "); 
                for(int i=0; i<num; i++){
                    String animal = UI.askString("What is an answer that I can add? (eg. Cat? Dog? Pig?) ");
                    String branch = UI.askString("What is a branch can I add to the property? (eg. Brown, orange, blue) ");
                    current.setMoreNode(new DTNode(branch, new DTNode(animal))); // adds into the nodes arraylist
                }               
                current.setText(property); // sets it to color
                UI.println("Thank you! I will now update my decision tree.");
            }
        }
    }
    
    
    /**
     * Saves the decision tree into a file that can then be loaded back in using loadTree() method
     */
    public void saveTree(){
        textFile.clear(); // clears all the old strings every time user wants to save
        String filename = UI.askString("What do you want to name the .txt file? (Do not include .txt in name.) " );
        savingTree(theTree);
        try{
            FileWriter writer = new FileWriter(filename + ".txt"); 
            for(String line: textFile){
              writer.write(line + System. lineSeparator());
            }
            writer.close();
            UI.println(filename + ".txt has been saved!");
        }
        catch (java.io.IOException ioe){ioe.printStackTrace();}
    }
    
    
    /**
     * 
     * Recursive method to save the tree
     */
    public void savingTree(DTNode node){
        if(node.isAnswer() && node.getMoreNodes() == false){  // if it is a leaf node and it also does not have any extra nodes
            textFile.add("Answer: " + node.getText());
        }
        else if (node.getMoreNodes() == true){  // if it does have extra nodes
            textFile.add("Question: " + node.getText());
            List<DTNode> possibleAnswers = node.getList();
            for(DTNode newNode: possibleAnswers){
                savingTree(newNode.getOther());
            }
        }
        else{
            textFile.add("Question: " + node.getText());
            savingTree(node.getYes());
            savingTree(node.getNo());
        }
    }
    
    /**
     * Draws the tree into a graph
     */
    public void drawTree(){
        UI.setFontSize(12);
        UI.setColor(Color.black);
        drawingTree(theTree, LEFT, TOP, BOTTOM, 1, LEFT + BOXLENGTH, (BOTTOM-TOP)/2);
    }
    
    /**
     * Recursive method for drawing the tree, only works with yes/no nodes
     */
    public void drawingTree(DTNode node, float x, float topY, float botY, float count, float prevX, float prevY){
        float boxSize = botY - topY;
        float midY = topY + boxSize/2;
        if(count != 1){UI.drawLine(prevX, prevY, x, midY+BOXHEIGHT/2);}
        node.draw(x, midY);
        if(!node.isAnswer()){
            count++;
            drawingTree(node.getYes(), x+150, topY, midY, count, x+BOXLENGTH/2, midY+BOXHEIGHT/2);
            drawingTree(node.getNo(),  x+150, midY, botY, count, x+BOXLENGTH/2, midY+BOXHEIGHT/2);
        }
    }
    
    /**
     * Trying a recursive method for drawing the tree that works with all nodes
     * Spoiler: it doesn't really work
     */
    public void drawingTree2(DTNode node, float x, float topY, float botY, float count, float prevX, float prevY){
        float boxSize = botY - topY;
        float split = node.getChildrenCount(); // how many portions i need
        float mid = boxSize/split;             // one (end line) portion
        float midY = topY + mid;               // if it is cut in two

        if(count != 1){UI.drawLine(prevX, prevY, x, midY+BOXHEIGHT/2);}
        node.draw(x, midY);
        
        if(!node.isAnswer() || node.getMoreNodes() == true){  // if it has any children
            count++;
            if(node.getMoreNodes() == true){                  // if it has multiple children in the arraylist
                List<DTNode> listOfNodes = node.getList();    // grabs all of the children
                for(int i=1; i<node.getChildrenCount(); i++){
                    drawingTree2(listOfNodes.get(i).getOther(), x+150, 
                                topY+i*boxSize/split, mid+i*boxSize/split, 
                                count, x+BOXLENGTH/2, midY+BOXHEIGHT/2);
                }
            }
            else{
                drawingTree2(node.getYes(), x+150, topY, midY, count, x+BOXLENGTH/2, midY+BOXHEIGHT/2);
                drawingTree2(node.getNo(),  x+150, midY, botY, count, x+BOXLENGTH/2, midY+BOXHEIGHT/2);
            }
        }
    }

    // You will need to define methods for the Completion and Challenge parts.

    // Written for you

    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTree (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;
    }
}
