package searchclient;

import java.util.*;

public interface Frontier
{
    void add(AgentState state);
    AgentState pop();
    boolean isEmpty();
    int size();
    boolean contains(AgentState state);
}

class FrontierBestFirst
        implements Frontier {
    private Heuristic heuristic;
    private final HashSet<AgentState> set = new HashSet<>(65536);
    private final PriorityQueue<AgentState> priorityQueue;


    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        priorityQueue = new PriorityQueue<>(65536, heuristic);
    }

    @Override
    public void add(AgentState state)
    {
        priorityQueue.add(state);
        this.set.add(state);
    }

    @Override
    public AgentState pop()
    {
        AgentState state = this.priorityQueue.poll();
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
    public boolean contains(AgentState state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}

    class PreProcessFrontierBFS
    {
        private final ArrayDeque<PreState> queue = new ArrayDeque<>(65536);



        public void add(PreState prestate)
        {
            this.queue.addLast(prestate);

        }


        public PreState pop()
        {
            PreState prestate = this.queue.pollFirst();
            return prestate;
        }


        public boolean isEmpty()
        {
            return this.queue.isEmpty();
        }


        public int size()
        {
            return this.queue.size();
        }

    }


