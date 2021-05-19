package searchclient;

public class Conflict {
    private Integer conflictAgent;
    private int coordinatesOfConflictRow;
    private int coordinateOfConflictCol;
    private boolean isStationary;

    public Conflict(Integer conflictAgent, int coordinatesOfConflictRow, int coordinateOfConflictCol, boolean isStationary){
        this.conflictAgent = conflictAgent;
        this.coordinatesOfConflictRow = coordinatesOfConflictRow;
        this.coordinateOfConflictCol = coordinateOfConflictCol;
        this.isStationary = isStationary;
    }

    public int[] getCoordinatesOfConflict() {
        int[] coordinatesOfConflict = new int[2];
        coordinatesOfConflict[0] = coordinatesOfConflictRow;
        coordinatesOfConflict[1] = coordinateOfConflictCol;
        return coordinatesOfConflict;
    }

    public Integer getConflictAgent() {
        return conflictAgent;
    }

    public boolean isStationary() {
        return isStationary;
    }
}
