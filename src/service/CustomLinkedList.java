package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomLinkedList {

    HashMap<Integer, Node> historyMap = new HashMap<>();
    Node head;
    Node tail;

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!historyMap.isEmpty()) {
            Node node = head;
            while (node != null) {
                tasks.add(node.task);
                node = node.next;
            }
        }
        return tasks;
    }

    public void linkLast(Task task) {
        int taskId = task.getId();
        Node last = tail;
        Node newNode = new Node(task, last, null);
        tail = newNode;

        if (last == null) {
            head = newNode;
        } else {
            last.next = newNode;
        }
        if (historyMap.containsKey(taskId)) {
            removeNode(taskId);
        }
        historyMap.put(taskId, newNode);
    }

    public void removeNode(int id) {
        if (historyMap.containsKey(id)) {
            Node nodeActual = historyMap.get(id);
            Node nodePrev = nodeActual.prev;
            Node nodeNext = nodeActual.next;

            if (nodePrev == null) {
                head = nodeNext;
            } else {
                nodePrev.next = nodeNext;
            }

            if (nodeNext == null) {
                tail = nodePrev;
            } else {
                nodeNext.prev = nodePrev;
            }
        }
    }
}