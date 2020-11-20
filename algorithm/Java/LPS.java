
// Longest Palindromic Substring
// 最长回文子串
public class LPS {

    // 方法签名
    interface LpsSolution {
        int solution(String s);
    }

    public static void main(String[] args) {
        List<LpsSolution> solutions = Stream.of(new CenterSpread(), new DP()).collect(Collectors.toList());

        for (LpsSolution solution : solutions) {
            int ans = solution.solution("bananas");
            System.out.println("ans = " + ans);
        }
    }

    // 1. 中心扩散法(http://www.ahathinking.com/archives/132.html)
    static class CenterSpread implements LpsSolution {

        public int solution(String s) {

            char[] chars = s.toCharArray();

            int maxLen = 1, maxIndex = 0;
            for (int i = 0; i < chars.length; i++) {
                int len = centerSpread(chars, i);
                if (len > maxLen) {
                    maxLen = len;
                    maxIndex = i;
                }
            }
            int left, right;
            int radius = maxLen / 2;
            if (maxLen % 2 == 0) {
                // a b a a b
                left = maxIndex - radius + 1;
                right = maxIndex + radius;
            } else {
                // a b a
                left = maxIndex - radius;
                right = maxIndex + radius;
            }
//            return s.substring(left, right + 1);
            return right - left + 1;
        }

        private int centerSpread(char[] chars, int i) {
            int l = i, r = i;
            int len1 = 0;
            // 奇数
            while (l >= 0 && r < chars.length && chars[l--] == chars[r++]) len1++;
            len1 = (len1 - 1) * 2 + 1;

            // 偶数
            l = i;
            r = i + 1;
            int len2 = 0;
            while (l >= 0 && r < chars.length && chars[l--] == chars[r++]) len2++;
            len2 = len2 * 2;

            return Math.max(len1, len2);
        }
    }

    // 2. 动态规划法(https://labuladong.gitbook.io/algo/dong-tai-gui-hua-xi-lie/1.1-dong-tai-gui-hua-ji-ben-ji-qiao/zhuang-tai-ya-suo-ji-qiao)
    static class DP implements LpsSolution {
        public int solution(String s) {
            char[] chars = s.toCharArray();
            int n = chars.length;
            // dp 数组全部初始化为 0
            int[][] dp = new int[n][n];
            // base case
            for (int i = 0; i < n; i++)
                dp[i][i] = 1;
            // 反着遍历保证正确的状态转移
            for (int i = n - 2; i >= 0; i--) {
                for (int j = i + 1; j < n; j++) {
                    // 状态转移方程
                    if (chars[i] == chars[j])
                        dp[i][j] = dp[i + 1][j - 1] + 2;
                    else
                        dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
            // 整个 s 的最长回文子串长度
            return dp[0][n - 1];
        }
    }

}
