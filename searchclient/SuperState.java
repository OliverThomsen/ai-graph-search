package searchclient;

import java.util.ArrayList;

public interface SuperState {
    ArrayList<SuperState> getExpandedStates();
}
