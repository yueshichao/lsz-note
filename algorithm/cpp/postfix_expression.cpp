/*
 * 本算法为中缀表达式转后缀表达式(不包括括号运算符)，并且计算后缀表达式的例子
 * 中缀表达式更适合人阅读
 * 后缀表达式更适合机器运算
 * 比如：中缀 1 + 4 / 2 -> 后缀 1 4 2 / +
 * 程序计算后缀表达式，只需要不停将操作数压栈，遇到运算符出栈计算再压栈即可
 * 中缀转后缀的过程：
 * 0. 需要一个容器保存后缀表达式，还需要一个保存运算符的栈
 * 1. 遇到操作数直接加到后缀表达式中
 * 2. 遇到运算符，比较优先级
 *  2.1 若栈为空，进栈
 *  2.2 若栈不为空，与栈顶比较，比栈顶大，则将运算符压栈，否则将栈顶弹出到后缀表达式中，不断重复操作2
 *
 * 感性认知：这里栈就是起一个管理优先级的作用，让优先级大的先进入到后缀表达式，这样可以在计算时，先被计算
 * */
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <stack>
#include <vector>

using namespace std;

int opPriority(char i);

double calculate(vector<string> v);

bool isOp(string s);

double calculate(double a, double b, char c);

int main() {
    string s;
    stack<char> op;
    vector<string> v;
    while (cin >> s) {
        string numStr;
        for (auto it = s.begin(); it != s.end(); it++) {
            int singleNum = *it - '0';
            if (singleNum >= 0 && singleNum <= 9) {
                numStr += *it;
            } else {
                v.push_back(numStr);
                numStr.clear();
                if (op.empty()) {
                    op.push(*it);
                } else {
                    // 栈内存在优先级更高的，让它先出栈，进表达式
                    while (!op.empty() && opPriority(*it) <= opPriority(op.top())) {
                        v.push_back(string(1, op.top()));
                        op.pop();
                    }
                    op.push(*it);
                }
            }
        }
        if (!numStr.empty()) {
            v.push_back(numStr);
        }
        while (!op.empty()) {
            v.push_back(string(1, op.top()));
            op.pop();
        }
        cout << "后缀表达式：";
        for (auto it = v.begin(); it != v.end(); it++) {
            cout << *it << " ";
        }
        cout << endl;
        // 计算后缀表达式
        double res = calculate(v);
        cout << "后缀表达式值：" << res << endl;
        v.clear();
    }
    return 0;
}

double calculate(vector<string> v) {
    stack<double> s;
    for (auto it = v.begin(); it != v.end(); it++) {
        string e = *it;
        if (isOp(e)) {
            double b = s.top();
            s.pop();
            double a = s.top();
            s.pop();
            double res = calculate(a, b, e[0]);
            s.push(res);
        } else {
            int num = atoi(e.c_str());
            s.push(num);
        }
    }
    return s.top();
}

double calculate(double a, double b, char c) {
    switch (c) {
        case '+':
            return a + b;
        case '-':
            return a - b;
        case '*':
            return a * b;
        case '/':
            return a / b;
    }
    return 0;
}

bool isOp(string s) {
    return s.length() > 0 && opPriority(s[0]) > 0;
}

int opPriority(char op) {
    switch (op) {
        case '+':
        case '-':
            return 1;
        case '*':
        case '/':
            return 2;
    }
    return 0;
}