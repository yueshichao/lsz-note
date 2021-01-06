

wait、notify实现生产者消费者
```java
// wait() notify() 方法
public class Main {

    private static final Deque<Integer> q = new ArrayDeque<>();
    static final Random r = new Random();

    public static void main(String[] args) {
        consumer();
        producer();
    }

    private static void producer() {
        new Thread(() -> {
            synchronized (q) {
                System.out.println("producer");
                while (true) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int bound = r.nextInt(5);
                    for (int i = 0; i < bound; i++) {
                        q.offer(r.nextInt(10));
                    }
                    q.notify();
                    System.out.println("producer notify");
                    try {
                        System.out.println("producer wait");
                        q.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static void consumer() {
        new Thread(() -> {
            synchronized (q) {
                System.out.println("consumer");
                while (true) {
                    try {
                        System.out.println("consumer wait");
                        q.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (!q.isEmpty()) {
                        System.out.println("q.poll() = " + q.poll());
                    }
                    System.out.println("consumer notify");
                    q.notify();
                }
            }
        }).start();
    }

}

```