/*
 * 最长上升子序列
 * */
#include <stdio.h>
#include <iostream>

using namespace std;
int n;

int main() {

    while (cin >> n) {
        int nums[n];
        for (int i = 0; i < n; ++i) {
            scanf("%d", &nums[i]);
        }
        int dp[n];
        fill(dp, dp + n, 1);
        int maxDp = dp[0];
        for (int i = 1; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                // j   i
                if (nums[i] > nums[j] && dp[j] >= dp[i]) {
                    dp[i] = dp[j] + 1;
                }
            }
            if (dp[i] > maxDp) {
                maxDp = dp[i];
            }
        }
        printf("%d\n", maxDp);
    }

    return 0;
}
/*
8
10 9 2 5 3 7 101 18
 * */