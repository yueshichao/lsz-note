package com.lsz.algorithm;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 树的遍历
 */
public class TreeTraversalTest {

    private static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    @Test
    public void preOrderTest() {
        // 构造测试数据
        TreeNode root = new TreeNode(1);
        TreeNode node2 = new TreeNode(2);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);
        TreeNode node5 = new TreeNode(5);
        TreeNode node6 = new TreeNode(6);
        root.left = node2;
        root.right = node3;
        root.left.left = node4;
        root.left.right = node5;
        root.right.right = node6;


        List<Integer> ans = preorderTraversal(root);
        System.out.println("ans = " + ans);

        ans = postorderTraversal(root);
        System.out.println("ans = " + ans);

    }

    // 非递归的前序遍历
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode node = root;
        while (node != null || !stack.isEmpty()) {
            // 先根，再左子树，记忆根结点，便于右子树遍历
            while (node != null) {
                ans.add(node.val);
                stack.offer(node);
                node = node.left;
            }
            // 回到根结点，找到右子树遍历
            node = stack.pollLast();
            node = node.right;
        }
        return ans;
    }

    // 非递归的后序遍历
    public List<Integer> postorderTraversal(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        TreeNode node = root;
        TreeNode prev = null;
        Deque<TreeNode> stack = new ArrayDeque<>();
        while (node != null || !stack.isEmpty()) {
            // 访问完左子树
            while (node != null) {
                stack.offer(node);
                node = node.left;
            }
            // 拿出上一个根结点
            node = stack.pollLast();
            // 如果该结点存在右子树且右子树未被访问过
            if (node.right != null && prev != node.right) {
                stack.offer(node);
                node = node.right;
            } else {// 左右子树访问完了，访问根结点
                ans.add(node.val);
                prev = node;
                node = null;// 下一个循环访问栈中的上一层根结点
            }
        }
        return ans;
    }


}
