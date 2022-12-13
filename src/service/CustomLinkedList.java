package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class CustomLinkedList<T> {

    HashMap<Integer, Integer> historyMap = new HashMap<>();
    LinkedList<Node> historyLinkedList = new LinkedList<>();

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (historyMap.isEmpty()) {
            return tasks;
        } else {
            for (Node node : historyLinkedList) {
                tasks.add(node.task);
            }
        }
        return tasks;
    }

    public void linkLast(Task task) {
        Node newNode = new Node(task);
        int taskId = task.getId();

        if (historyMap.isEmpty() || !historyMap.containsKey(taskId)) {
            historyLinkedList.add(newNode);
        } else {
            historyLinkedList.remove((int) historyMap.get(taskId));
            historyLinkedList.add(newNode);
        }
        historyMap.put(taskId, historyLinkedList.size() - 1);
    }

    public void removeNode(Node node) {
        if (historyMap.containsValue(node)) {
            historyMap.remove(node);
            historyLinkedList.remove(node);
        }
    }
}