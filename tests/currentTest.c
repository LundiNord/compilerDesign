int main() {
  int a = 5;
  int b = 10;
  a += b;
  b = (a * 2) - 4;
  int c;
  c = b / a;
  int d = 0xF;
  d *= 2;
  int e;
  e = -d + (c * 3);
  a = e % 4;
  b -= a;
  c = (b + a) * (d - e);
  int f = c / (a + 1);
  return f;
}
