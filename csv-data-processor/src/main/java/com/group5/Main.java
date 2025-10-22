package com.group5;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome to the example branch!");
        System.out.println("This line was added in the example branch!");
        System.out.println("To commit this new code to the main branch, open a Pull Request (PR)");

        exampleClass example = new exampleClass();
        example.printMessage();

    }
}