int main() {
    int x = 2147483646;
    x /= 2 * 3 * 3 * 11 * 31 * 151 * 331; // Prime factorization reversed
    return x; // Should be 7
}
