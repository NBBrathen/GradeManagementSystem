package ManagementSystem;


public class MyPriorityQueue {
    Node head;

    // Inner Class - Node
    class Node {
        Node next;
        Object data;        // Choose any datatype

        Node(Object data) {
            this.data = data;
            next = null;
        }

        public Node getNext() { return next;}
        public Object getData() { return data;}

        public void setNext(Node next) { this.next = next; }
        public void setData(Object data) { this.data = data;}
    }


    public MyPriorityQueue() { this.head = null; }

    public void enqueue(Object data) {
        Node newNode = new Node(data);

        if (head == null || compare(newNode.getData(), head.getData()) > 0) {
            newNode.setNext(head);
            head = newNode;
            return;
        }

        Node current = head;
        while (current.getNext() != null && compare(newNode.getData(), current.getNext().getData()) <= 0) {
            current = current.getNext();
        }

        newNode.setNext(current.getNext());
        current.setNext(newNode);
    }

    public Object dequeue(){
        if (head == null) {
            return null;
        }

        Node temp = head;
        head = head.getNext();
        return temp.getData();
    }

    public Object peek() {
        if (head == null){
            return null;
        }

        return head.getData();
    }

    private int compare(Object o1, Object o2){
        if (o1 instanceof StudentGrade && o2 instanceof StudentGrade){
            StudentGrade s1 = (StudentGrade) o1;
            StudentGrade s2 = (StudentGrade) o2;

            return Float.compare(s1.getGrade(), s2.getGrade());
        }
        return 0;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        int count = 0;
        Node current = head;
        while (current != null) {
            count++;
            current = current.getNext();
        }
        return count;
    }
}
