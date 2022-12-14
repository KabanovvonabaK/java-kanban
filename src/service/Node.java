package service;

import model.Task;

public class Node {

    Task task;
    Node prev;
    Node next;

    public Node(Task task, Node prev, Node next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    @Override
    public boolean equals(Object obj) {
        Node node = (Node) obj;
        Task taskToCompare = node.task;
        return task.equals(taskToCompare);
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }
}