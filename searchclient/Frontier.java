package searchclient;

import java.util.*;

public interface Frontier
{
    void add(SuperState state);
    SuperState pop();
    boolean isEmpty();
    int size();
    boolean contains(SuperState state);
    String getName();

    Map<Integer, SubGoal> getSubGoals();
}

class FrontierBestFirst implements Frontier {
    private Heuristic heuristic;
    private final HashSet<SuperState> set = new HashSet<>(65536);
    private final PriorityQueue<SuperState> priorityQueue;


    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        priorityQueue = new PriorityQueue<>(65536, heuristic);
    }

    public Map<Integer, SubGoal> getSubGoals() {
        return this.heuristic.getSubGoals();
    }

    @Override
    public void add(SuperState state)
    {
        priorityQueue.add(state);
        this.set.add(state);
    }

    @Override
    public SuperState pop()
    {
        SuperState state = this.priorityQueue.poll();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty() {
        return this.priorityQueue.isEmpty();
    }

    @Override
    public int size() {
        return this.priorityQueue.size();
    }

    @Override
    public boolean contains(SuperState state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}

class PreProcessFrontierBFS {
    private final ArrayDeque<PreState> queue = new ArrayDeque<>(65536);
    private final HashSet<PreState> set = new HashSet<>(65536);



    public void add(PreState prestate)
    {
        this.queue.addLast(prestate);
        this.set.add(prestate);
    }


    public PreState pop()
    {
        PreState prestate = this.queue.pollFirst();
        this.set.remove(prestate);
        return prestate;
    }


    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    public boolean contains(PreState preState) {return this.set.contains(preState);}


    public int size()
    {
        return this.queue.size();
    }

}


