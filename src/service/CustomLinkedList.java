package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

/*
    Это самое странное и не понятное задание что пока мне встретилось.
    Я неплохо понял теорию, поигрался с мапами и сетами в песочнице, но
    совершенно не понял что я делаю в этом задании. Очень надеюсь что
    сделал то, что нужно.
    Так и не понял почему надо в remove(int id) в InMemoryHistoryManager
    обязательно передавать id, а потом в removeNode(Node node) передавать именно ноду.
    Работоспособность проверял ассертами в Main, строка 204
 */
public class CustomLinkedList<T> {

    HashMap<Integer, Node> historyMap = new HashMap<>();

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (historyMap.isEmpty()) {
            return tasks;
        } else {
            for (Node node : historyMap.values()) {
                tasks.add(node.task);
            }
        }
        return tasks;
    }

    public void linkLast(Task task) {
        Node newNode = new Node(task);
        Integer nodeKey = task.getId();

        if (historyMap.isEmpty()) {
            historyMap.put(nodeKey, newNode);
        } else if (!historyMap.containsKey(nodeKey)) {
            historyMap.put(nodeKey, newNode);
        } else {
            historyMap.remove(nodeKey);
            historyMap.put(nodeKey, newNode);
        }
    }

    public void removeNode(Node node) {
        if (historyMap.containsValue(node)) {
            historyMap.remove(node);
        }
    }
}