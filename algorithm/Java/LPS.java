
// Longest Palindromic Substring
// 最长回文子串（中心扩散法）
// 参考：http://www.ahathinking.com/archives/132.html
public class LPS {

    public static void main(String[] args) {
        String ans = new LPS().solution("bananas");
        System.out.println("ans = " + ans);
    }

    private String solution(String s) {

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
        return s.substring(left, right + 1);
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
