package me.sameersuri.backingtrack.automata;

import java.util.Arrays;

public class AutomatonGrid {
    private int[] grid;
    private int ruleset;

    public AutomatonGrid(int size, int ruleset) {
        this(generateGrid(size), ruleset);
    }

    private static int[] generateGrid(int size) {
        int[] grid = new int[size];
        for(int i = 0; i < size; i++) {
            if(Math.random() > 0.5) {
                grid[i] = 1;
            }
        }
        return grid;
    }

    public AutomatonGrid(int[] grid, int ruleset) {
        this.grid = grid;
        this.ruleset = ruleset;
        System.out.println("Seed: " + Arrays.toString(grid));
        System.out.println("Ruleset: " + ruleset);
    }

    public int[] getGrid() {
        return Arrays.copyOf(grid, grid.length);
    }

    public int getSize() {
        return grid.length;
    }

    public int getRuleset() {
        return ruleset;
    }

    public void iterate() {
        int[] newGrid = new int[grid.length];

        for(int i = 0; i < grid.length; i++) {
            int leftCell = i - 1;
            int rightCell = i + 1;
            if (i == 0) {
                leftCell = grid.length - 1;
            }
            if (i == grid.length - 1) {
                rightCell = 0;
            }

            int index = 4 * grid[leftCell] + 2 * grid[i] + grid[rightCell];
            int newState = (ruleset >> index) & 0b1;

            newGrid[i] = newState;
        }

        grid = newGrid;
    }
}
