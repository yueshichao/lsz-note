/*
 * 最大连续子序列和
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
        dp[0] = nums[0];
        int out = dp[0];
        for (int i = 1; i < n; ++i) {
            if (dp[i - 1] < 0) {
                dp[i] = nums[i];
            } else {
                dp[i] = dp[i - 1] + nums[i];
            }
            if (dp[i] > out) {
                out = dp[i];
            }
        }
        printf("out = %d\n", out);
    }

    return 0;
}
/*
3
5 -1 2

9
-2 1 -3 4 -1 2 1 -5 4
 * */