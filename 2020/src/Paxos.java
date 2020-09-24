
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
* 我服了，Basic Paxos看了三天了，还是写不出像样的demo，先记录一下进度，以后再说
* */
public class Paxos {

    static Random random = new Random();

    public static void main(String[] args) {
        Net net = new Net();

        Proposer p0 = new Proposer(net, new Proposal(00, 0), 0);
        Proposer p1 = new Proposer(net, new Proposal(10, 1), 1);
        Proposer p2 = new Proposer(net, new Proposal(20, 2), 2);

        new Acceptor(net, 0);
        new Acceptor(net, 1);
        new Acceptor(net, 2);

        Learner learner = new Learner(net);

        p0.start();
        p1.start();
        p2.start();

        try {
            p0.join();
            p1.join();
            p2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println(learner.toString());


    }

    static class Proposer extends Thread {
        Proposal proposal;
        int serverId;
        Net net;

        public Proposer(Net net, Proposal proposal, int serverId) {
            this.proposal = new Proposal(proposal.id, proposal.value);
            this.serverId = serverId;
            this.net = net;
            net.register(this);
            System.out.printf("Proposer_%d -> proposal = %s end\n", serverId, proposal.toString());
        }

        @Override
        public void run() {
            super.run();
            boolean prepare = prepare();
            System.out.printf("Proposer_%d -> prepare %s\n", serverId, prepare ? "成功" : "失败");
            if (!prepare) return;
            System.out.printf("Proposer_%d -> propose start\n", serverId);
            boolean propose = propose();
            if (propose) {
                Learner learner = net.getLearner();
                learner.value = this.proposal.value;
            }
            System.out.printf("Proposer_%d -> propose %s\n", serverId, propose ? "success" : "fail");
        }

        private boolean prepare() {
            System.out.printf("Proposer_%d -> try prepare start\n", serverId);
            Paxos.sleep(1, 3);
            // 1. 广播proposal并等待回复
            Set<Acceptor> allAcceptors = net.getAcceptors();
            int validSize = allAcceptors.size() / 2 + 1;
            // 等待计数器，等待超过半数的返回
            CountDownLatch latch = new CountDownLatch(validSize);
            TreeSet<Proposal> responseProposals = new TreeSet<>(((o1, o2) -> o2.id - o1.id > 0 ? 1 : 0));
            for (Acceptor receiveAcceptor : allAcceptors) {
                new Thread(() -> {
                    Acceptor.Msg msg = receiveAcceptor.prepare(proposal.id);
                    // 是否有已接受的proposal
                    Proposal responseProposal = msg.proposal;
                    if (responseProposal != null) {
                        responseProposals.add(responseProposal);
                        this.proposal = responseProposal;
                    }
                    // 是否响应
                    if (msg.response) {
                        latch.countDown();
                    }
                }).start();
            }
            boolean timeout;
            try {
                timeout = !latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
//                this.proposal.id++;
                return false;
            }
            if (timeout) {
//                this.proposal.id++;
                return false;
            }

            // 2. 接收到超半数的结果，如果存在已被accepted的proposal，用最大的proposal替换自己的proposal，否则prepare步骤完成
            if (responseProposals.size() > 0) {
                Proposal responseProposal = responseProposals.first();
                if (!this.proposal.equals(responseProposal)) {
                    this.proposal = responseProposal;
                    return false;
                }
            }
            return true;
        }

        private boolean propose() {
            Set<Acceptor> allAcceptors = net.getAcceptors();

            int validSize = allAcceptors.size() / 2 + 1;
            CountDownLatch latch = new CountDownLatch(validSize);
            for (Acceptor acceptor : allAcceptors) {
                new Thread(() -> {
                    boolean accept = acceptor.accept(proposal);
                    if (accept) {
                        latch.countDown();
                    }
                }).start();
            }
            boolean timeout;
            try {
                timeout = !latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return !timeout;
        }

    }

    static class Acceptor {

        Proposal acceptedProposal = null;
        Long prepareId = null;
        int serverId;

        public Acceptor(Net net, int serverId) {
            this.serverId = serverId;
            net.register(this);
        }

        public synchronized Msg prepare(long currentId) {
            System.out.printf("Acceptor_%d -> prepare, prepareId = %s, acceptedId = %s\n", serverId, prepareId == null ? "无" : prepareId, acceptedProposal == null ? "无" : acceptedProposal.id);
            if (prepareId == null) {
                prepareId = currentId;
                return new Msg(true, null);
            }
            // 不再接受小于prepareId的proposal
            if (prepareId > currentId) {
                return new Msg(false, null);
            } else {
                // 记录prepare请求中最大的proposal.id
                prepareId = currentId;
            }
            // 之前存在accepted的proposal
            if (acceptedProposal != null) {
                return new Msg(true, acceptedProposal);
            } else {
                return new Msg(true, null);
            }
        }

        public synchronized boolean accept(Proposal proposal) {
            if (prepareId <= proposal.id) {
                this.acceptedProposal = proposal;
                return true;
            } else {
                return false;
            }
        }

        static class Msg {
            public Msg(boolean response, Proposal proposal) {
                this.response = response;
                this.proposal = proposal;
            }

            boolean response;
            Proposal proposal;
        }

    }

    static class Learner {

        int value;

        public Learner(Net net) {
            net.register(this);
        }

        @Override
        public String toString() {
            return "Learner{" +
                    "value=" + value +
                    '}';
        }
    }

    static class Net {
        public Set<Acceptor> acceptors = new HashSet<>();
        public Set<Proposer> proposers = new HashSet<>();
        public Learner learner;

        public void register(Proposer proposer) {
            this.proposers.add(proposer);
        }

        public void register(Acceptor acceptor) {
            this.acceptors.add(acceptor);
        }

        public void register(Learner learner) {
            this.learner = learner;
        }

        public Set<Acceptor> getAcceptors() {
            return new HashSet<>(acceptors);
        }

        public Learner getLearner() {
            return learner;
        }
    }

    enum MessageType {
        Prepare,
        Propose,
        ;
    }

    static class Proposal {

        static Proposal EMPTY = new Proposal(Integer.MIN_VALUE, Integer.MIN_VALUE);

        public long id;

        public int value;

        public Proposal(long id, int value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Proposal{" +
                    "id=" + id +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Proposal proposal = (Proposal) o;
            return id == proposal.id &&
                    value == proposal.value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id) ^ Objects.hashCode(value);
        }
    }

    public static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sleep(int min, int max) {
        int i = random.nextInt(max - min);
        sleep(min + i);
    }

}
