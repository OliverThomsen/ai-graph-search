package searchclient;

import java.util.ArrayList;

public class PreState{
    private int x = 0;
    private int y = 0;
    private int g = 0;
    private int hash = 0;


    public PreState(int x, int y, int g){
        this.x = x;
        this.y = y;
        this.g = g;
    }

    public ArrayList<PreState> getExpandedPreStates(){
         ArrayList<PreState> expandedprestates = new ArrayList<>();
         if (this.y >= 0 && this.x >= 0) {
             expandedprestates.add(new PreState(this.x - 1, this.y, this.g + 1));
             expandedprestates.add(new PreState(this.x + 1, this.y, this.g + 1));
             expandedprestates.add(new PreState(this.x, this.y - 1, this.g + 1));
             expandedprestates.add(new PreState(this.x, this.y + 1, this.g + 1));
         }
         return expandedprestates;

    }

    public int g() {
        return this.g;
    }

    public int y() {
        return this.y;
    }

    public int x() {
        return this.x;
    }

    @Override
    public boolean equals(Object other){
        if (this == other) return true;

        if (other == null || (this.getClass() != other.getClass())){
            return false;
        }

        PreState preState = (PreState) other;
        return (this.x == preState.x && this.y == preState.y);
    }

    @Override
    public int hashCode(){
        int result = 0;

        result = 31*result + x;
        result = 31*result + y;

        return result;


    }
}