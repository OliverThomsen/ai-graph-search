package searchclient;

public class SuperState {
    public int[] agentRows;
    public int[] agentCols;
    public char[][] boxes;

    public SuperState(int[] rows, int[] cols, char[][] boxes){
        this.agentRows = rows;
        this.agentCols = cols;
        this.boxes = boxes;
    }

    public SuperState(int rows, int cols, char[][] boxes){
        this.agentRows[0] = rows;
        this.agentCols[0] = cols;
        this.boxes = boxes;
    }

    public SuperState(AgentState state, Action action) {
        this.agentRows[0] = state.row;
        this.agentCols[0] = state.col;
        this.boxes = state.boxes;
        char box;
        switch (action.type)
        {
            case NoOp:
                break;

            case Move:
                this.agentRows[0] += action.agentRowDelta;
                this.agentCols[0] += action.agentColDelta;
                break;

            case Push:
                this.agentRows[0] += action.agentRowDelta;
                this.agentCols[0] += action.agentColDelta;
                int prevBoxRow = this.agentRows[0];
                int prevBoxCol = this.agentCols[0];
                int destBoxRow = prevBoxRow + action.boxRowDelta;
                int destBoxCol = prevBoxCol + action.boxColDelta;
                box = this.boxes[prevBoxRow][prevBoxCol];
                this.boxes[prevBoxRow][prevBoxCol] = 0;
                this.boxes[destBoxRow][destBoxCol] = box;
                break;

            case Pull:
                prevBoxRow = this.agentRows[0] - action.boxRowDelta;
                prevBoxCol = this.agentCols[0] - action.boxColDelta;
                destBoxRow = this.agentRows[0];
                destBoxCol = this.agentCols[0];
                this.agentRows[0] += action.agentRowDelta;
                this.agentCols[0] += action.agentColDelta;
                box = this.boxes[prevBoxRow][prevBoxCol];
                this.boxes[prevBoxRow][prevBoxCol] = 0;
                this.boxes[destBoxRow][destBoxCol] = box;
        }

    }



    public SuperState(State state, Action[] jointAction){
        int numAgents = state.agentRows.length;
        this.boxes = state.boxes;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            this.agentRows[numAgents] = state.agentRows[numAgents];
            this.agentCols[numAgents] = state.agentCols[numAgents];
            char box;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    int prevBoxRow = this.agentRows[agent];
                    int prevBoxCol = this.agentCols[agent];
                    int destBoxRow = this.agentRows[agent] + action.boxRowDelta;
                    int destBoxCol = this.agentCols[agent] + action.boxColDelta;
                    box = this.boxes[prevBoxRow][prevBoxCol];
                    this.boxes[prevBoxRow][prevBoxCol] = 0;
                    this.boxes[destBoxRow][destBoxCol] = box;
                    break;

                case Pull:
                    prevBoxRow = this.agentRows[agent] - action.boxRowDelta;
                    prevBoxCol = this.agentCols[agent] - action.boxColDelta;
                    destBoxRow = this.agentRows[agent];
                    destBoxCol = this.agentCols[agent];
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    box = this.boxes[prevBoxRow][prevBoxCol];
                    this.boxes[prevBoxRow][prevBoxCol] = 0;
                    this.boxes[destBoxRow][destBoxCol] = box;
            }
        }
    }
}
