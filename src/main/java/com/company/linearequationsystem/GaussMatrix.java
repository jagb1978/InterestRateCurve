package com.company.linearequationsystem;

/**
 * This class implements the algorithm to solve a
 * system of linear equations using Gaussian elimination
 *
 */
public class GaussMatrix {
    private double[] solution;
    private double[] constants;
    private int numberOfEquations;
    private double[][] coefficientMatrix;

    public GaussMatrix(int equations) {
        this.numberOfEquations = equations;
        this.coefficientMatrix = new double[equations][equations];
        this.constants = new double[equations];
        this.solution = new double[equations];
    }

    public void findSolution() {
        this.eliminate();
        this.solve();
    }

    private void eliminate() {
        int rowCounter;
        for (rowCounter = 0; rowCounter < this.numberOfEquations; rowCounter++) {   // i -> row ; matrix order decreases during elimination

            double largestCoefficientCurrentColumn = Math.abs(this.coefficientMatrix[rowCounter][rowCounter]);
            int index = rowCounter; //largest index
            int columnCounter; // columnCounter

            for (columnCounter = rowCounter + 1; columnCounter < this.numberOfEquations; columnCounter++) {
                if (Math.abs(this.coefficientMatrix[columnCounter][rowCounter]) > largestCoefficientCurrentColumn) {
                    largestCoefficientCurrentColumn = this.coefficientMatrix[columnCounter][rowCounter];
                    index = columnCounter;
                }
            }

            this.swapRows(rowCounter, index);  // swapping i-th row to index-th row
            int k;
            for (k = rowCounter + 1; k < this.numberOfEquations; k++) {
                double factor = this.coefficientMatrix[k][rowCounter] / this.coefficientMatrix[rowCounter][rowCounter]; // processing column wise
                int l;
                for (l = rowCounter; l < this.numberOfEquations; l++) {
                    this.coefficientMatrix[k][l] -= factor * this.coefficientMatrix[rowCounter][l];
                }
                this.constants[k] -= factor * this.constants[rowCounter];  // processing constants
            }
        }
    }

    private void solve() {
        for (int i = this.numberOfEquations - 1; i >= 0; i--) {
            this.solution[i] = this.constants[i];   // creates a copy
            for (int j = this.numberOfEquations - 1; j > i; j--) {
                this.solution[i] -= this.coefficientMatrix[i][j] * solution[j];
            }
            this.solution[i] /= this.coefficientMatrix[i][i];
        }
    }

    private void swapRows(int row1, int row2) {
        double temp;
        for (int j = 0; j < this.numberOfEquations; j++) {   // swapping coefficient rows
            temp = this.coefficientMatrix[row1][j];
            this.coefficientMatrix[row1][j] = this.coefficientMatrix[row2][j];
            this.coefficientMatrix[row2][j] = temp;
        }
        temp = this.constants[row1];   // swapping constants vectors
        this.constants[row1] = this.constants[row2];
        this.constants[row2] = temp;
    }

    /**
     * Getters and Setters
     */
    public double[] getSolution() {
        return solution;
    }

    public double[] getConstants() {
        return constants;
    }

    public double[][] getCoefficientMatrix() {
        return coefficientMatrix;
    }

    public int getNumberOfEquations() {
        return numberOfEquations;
    }

    public void setCoefficientMatrix(double[][] coefficientMatrix) {
        this.coefficientMatrix = coefficientMatrix;
    }

    public void setConstants(double[] constants) {
        this.constants = constants;
    }
}





