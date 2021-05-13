package searchclient;

public class Preprocessing {
    private PreProcessFrontierBFS frontier = new PreProcessFrontierBFS();
    private Integer[][] referenceMap;
    private boolean reachedEndOfMap = false;

    public Preprocessing(State initialState) {
        //instantiate the new map used to reference how far away the agent is for any point on the map
        referenceMap = new Integer[initialState.walls.length][initialState.walls[0].length];

        frontier.add(new PreState(initialState.agentRows.get(0), initialState.agentCols.get(0), 0));

        while (!frontier.isEmpty()) {

            // choose a leaf node and remove it from the frontier
            // pop returns all leaf nodes from the frontier
            PreState currentPreState = frontier.pop();

            // add the node to the explored set
            // this adds all the leif nodes the explored set
            referenceMap[currentPreState.x()][currentPreState.y()] = currentPreState.g();

            // expand the chosen node, adding the resulting nodes to the frontier
            // This expands all leif nodes, and for each node
            // their corresponding new state is added to the frontier of not already explored
            for (PreState prestate : currentPreState.getExpandedPreStates()) {
                // only if not in the frontier or explored set
                if (prestate.x() >= 0 && prestate.y() >= 0 &&
                        prestate.x() < referenceMap.length && prestate.y() < referenceMap[0].length) {
                    if (referenceMap[prestate.x()][prestate.y()] == null && !initialState.walls[prestate.x()][prestate.y()]
                    && !frontier.contains(prestate)) {
                        frontier.add(prestate);
                    }
                }
            }


        }
    }

    public Integer[][] getReferenceMap() {
//        for (int i = 0; i < referenceMap.length; i++) {
//            System.err.println("");
//            for (int j = 0; j < referenceMap[0].length; j++) {
//                System.err.print("[" + referenceMap[i][j] + "]");
//            }
//        }
        return referenceMap;
    }
}
