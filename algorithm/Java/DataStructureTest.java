package com.lsz.algorithm;

import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 基本的数据结构：堆、（双端）队列、栈
 */
public class DataStructureTest {

    @Test
    public void headTest() {
        Random random = new Random();
        List<Integer> randomCases = Stream.generate(() -> random.nextInt(10000)).limit(100).collect(Collectors.toList());
        Heap heap = new Heap();
        for (Integer randomCase : randomCases) {
            heap.offer(randomCase);
        }
        while (heap.size() > 0) {
            int poll = heap.poll();
            System.out.println("poll = " + poll);
        }
    }

    private static class Heap {
        int[] data = new int[2];
        int size = 0;

        public void offer(int e) {
            resize();
            data[size++] = e;
            siftUp(size - 1);
        }

        private void resize() {
            if (size >= data.length) {
                int[] newData = new int[data.length * 2];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
        }

        // 小根堆，小的往上提
        private void siftUp(int index) {
            // 0,1,2,3
            // 1 -> 0, 2 -> 0, 3 -> 1
            int parentIndex = (index - 1) / 2;
            while (index > parentIndex && data[index] < data[parentIndex]) {
                swap(index, parentIndex);
                index = parentIndex;
                parentIndex = (index - 1) / 2;
            }
        }

        private void swap(int i, int j) {
            int tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }

        public int poll() {
            if (size == 0) throw new RuntimeException("堆空！");
            // 返回堆顶元素
            int e = data[0];
            // 最后一个元素提到堆顶，进行siftDown
            data[0] = data[size - 1];
            size--;
            siftDown(0);
            return e;
        }

        // 小根堆，大的往下沉，父结点应该是三结点最小的
        private void siftDown(int index) {
            while (index * 2 + 1 < size) {
                int left = index * 2 + 1;
                int right = index * 2 + 2;
                int next = index;
                if (data[left] < data[index]) {
                    next = left;
                }
                if (right < size && data[right] < data[index] && data[right] < data[left]) {
                    next = right;
                }
                if (index == next) break;
                swap(index, next);
                index = next;
            }
        }

        public int size() {
            return size;
        }

    }


    @Test
    public void dequeTest() {
        Deque deque = new Deque();
        deque.offer(2);
        deque.offerFirst(1);
        deque.offer(3);
        deque.offer(1000);

        int pollLast = deque.pollLast();
        System.out.println("pollLast 1000? ---> " + pollLast);

        while (deque.size() > 0) {
            int poll = deque.poll();
            System.out.println("poll = " + poll);
        }


    }

    private static class Deque extends Queue {

        public void offerFirst(int e) {
            resize();
            head = (head - 1 + data.length) % data.length;
            data[head] = e;
        }

        public int pollLast() {
            tail = (tail - 1 + data.length) % data.length;
            return data[tail];
        }

    }

    @Test
    public void queueTest() {
        Queue queue = new Queue();
        for (int i = 0; i < 100; i++) {
            queue.offer(i);
        }

        while (queue.size() > 0) {
            int poll = queue.poll();
            System.out.println("poll = " + poll);
        }

    }


    private static class Queue {
        // head = tail表示队空，tail + 1 = head表示队满
        // 指向下一个要出队的位置
        int head = 0;
        // 指向下一个要入队的位置
        int tail = 0;
        int[] data = new int[2];

        public void offer(int e) {
            resize();
            data[tail] = e;
            tail = (tail + 1) % data.length;
        }

        protected void resize() {
            if ((tail + 1) % data.length == head) {
                // 考虑队列原先形式可能为[tail, 0, 0, head]，
                // 如使用数组拷贝，[tail, 0, 0, head, 0, 0, 0, 0]，并不符合期望
                // 故将队列重新从0开始排进新数组
                int[] newData = new int[data.length * 2];
                int size = size();
                for (int i = 0; i < size; i++) {
                    newData[i] = data[head];
                    head = (head + 1) % data.length;
                }
                data = newData;
                head = 0;
                tail = size;
            }
        }

        public int poll() {
            if (head == tail) throw new RuntimeException("队空");
            int e = data[head];
            head = (head + 1) % data.length;
            return e;
        }

        public int size() {
            return (tail + data.length - head) % data.length;
        }


    }

    @Test
    public void stackTest() {
        Stack stack = new Stack();
        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }

        while (stack.size() > 0) {
            int pop = stack.pop();
            System.out.println("pop = " + pop);
        }
    }

    private static class Stack {

        int top = 0;
        int[] data = new int[2];

        public void push(int e) {
            resize();
            data[top++] = e;
        }

        private void resize() {
            if (top >= data.length) {
                int[] newData = new int[data.length * 2];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
        }

        public int pop() {
            return data[--top];
        }

        public int size() {
            return top;
        }

    }


}
