public class TestClass {
    @Test (priority = 2)
    public static void method1() {
        System.out.println("M1");
    }

    @Test
    public static void method2() {
        System.out.println("M2");
    }

    @BeforeSuite
    public static void method3() {
        System.out.println("M3-Before");
    }

    @Test (priority = 10)
    public static void method4() {
        System.out.println("M4");
    }

    @AfterSuite
    public static void method5() {
        System.out.println("M5-After");
    }
}
