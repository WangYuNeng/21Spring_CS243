package submit;

class TestFaintness {
    /**
     * In this method all variables are faint because the final value is never used.
     * Sample out is at src/test/Faintness.out
     */
    void test1() {
        int x = 2;
        int y = x + 2;
        int z = x + y;
        return;
    }

    /**
     * Write your test cases here. Create as many methods as you want.
     * Run the test from root dir using
     * ./run.sh flow.Flow submit.MySolver submit.Faintness submit.TestFaintness
     */
    int foobar (){
      return 5;
    }
    void foo1 (int x) {

    }
    void foo2 (int x) {

    }
    void test2() {
      int x = 1;
      foo1(x);
      x = foobar();
      foo2(x);
    }

    int test4() {
      int x = 0;
      x = x + 1;
      return x;
    }
}
