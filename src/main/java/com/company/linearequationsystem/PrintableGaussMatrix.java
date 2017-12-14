package com.company.linearequationsystem;

import java.util.Scanner;

/**
 * PrintableGaussMatrix extends GaussMatrix in order
 * to solve a system of linear equation that is specified through
 * a terminal application
 *
 * @author Jose Gonzalez
 */
public class PrintableGaussMatrix extends GaussMatrix {
    private Scanner scanner;

    public PrintableGaussMatrix(int equations, Scanner scanner) {
        super(equations);
        this.scanner = scanner;
    }

    /**
     * Method used in the terminal application to input the elements
     * in the coefficient matrix
     */
    public void fillMatrix() {
        for (int i = 0; i < this.getNumberOfEquations(); i++) {
            System.out.println("Enter the co-efficients of unknowns and constant term for Equation " + (i + 1) + " :");

            for (int j = 0; j < this.getNumberOfEquations(); j++) {
                System.out.print("Enter Co-efficient " + (j + 1) + " : ");
                this.getCoefficientMatrix()[i][j] = this.scanner.nextDouble();
            }

            System.out.print("Enter Constant Term : ");
            this.getConstants()[i] = scanner.nextDouble();
        }
    }

    /**
     * Prints the final solution
     */
    public void printSolution() {
        StringBuilder solutionString = new StringBuilder();
        solutionString.append("\nSolution Set is : ");

        for (int i = 0; i < this.getNumberOfEquations(); i++) {
            solutionString.append((char) ('A' + i) + " = " + this.getSolution()[i]);
        }

        System.out.println(solutionString.toString());
    }

    /**
     * Iterated through the coefficient matrix and prints
     * in the terminal
     */
    public void printMatrix() {
        StringBuilder matrixString = new StringBuilder();

        for (int i = 0; i < this.getNumberOfEquations(); i++) {
            for (int j = 0; j < this.getNumberOfEquations(); j++) {

                matrixString.append(this.getCoefficientMatrix()[i][j] >= 0 ?
                        " +" + this.getCoefficientMatrix()[i][j] + ((char) ('A' + j)) + " "
                        : " " + this.getCoefficientMatrix()[i][j] + ((char) ('A' + j)) + " "
                );
            }
            matrixString.append(" = " + this.getConstants()[i] +"\n");
        }

        System.out.println(matrixString.toString());
    }
}
