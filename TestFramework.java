import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;

public class TestFramework {
    public static void main(String[] args) {
        start(TestClass.class);
        System.out.println("\nAnother test:");
        start("TestClass");
    }

    public static void start(Class cls){
        try {
            doTest(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start(String name) {
        try {
            Class cls = Class.forName(name);
            doTest(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doTest(Class c) throws Exception {
        ArrayList<Method> methods = new ArrayList<>();

        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                methods.add(m);
            }
        }

        methods.sort(new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getAnnotation(Test.class).priority() - o2.getAnnotation(Test.class).priority();
            }
        });

        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(BeforeSuite.class)) {
                if (methods.size() > 0 && methods.get(0).isAnnotationPresent(BeforeSuite.class))
                    throw new RuntimeException("More than one @BeforeSuit");
                methods.add(0, m);
            }
            if (m.isAnnotationPresent(AfterSuite.class)) {
                if (methods.size() > 0 && methods.get(methods.size() - 1).isAnnotationPresent(BeforeSuite.class))
                    throw new RuntimeException("More than one @AfterSuit");
                methods.add(m);
            }
        }
        for (int i = 0; i < methods.size(); i++) {
            methods.get(i).invoke(null);
        }
    }
}
