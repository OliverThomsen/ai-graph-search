package searchclient;

public class Preprocessing {
    static Integer[][] getReferenceMap(boolean[][] walls, SubGoal subGoal) {
        //instantiate the new map used to reference how far away the agent is for any point on the map
        Integer[][] referenceMap = new Integer[walls.length][walls[0].length];

        PreProcessFrontierBFS frontier = new PreProcessFrontierBFS();
        frontier.add(new PreState(subGoal.row, subGoal.col, 0));

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
                    if (referenceMap[prestate.x()][prestate.y()] == null && !walls[prestate.x()][prestate.y()]
                    && !frontier.contains(prestate)) {
                        frontier.add(prestate);
                    }
                }
            }
        }
        return referenceMap;
    }
}
