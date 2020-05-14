import java.util.*;

public class ArrayToList {
    public static <T> ArrayList<T> arrayToList(T[] arr) {
        ArrayList<T> lst = new ArrayList<>();
        for (T element : arr){
            lst.add(element);
        }
        return lst;
    }
    public static void main(String[] args) {
        // test with Double
        Double[] dArray = {1.0, 2.0, 3.0, 4.0, 5.0};
        System.out.println(Arrays.toString(dArray));
        ArrayList<Double> dList = arrayToList(dArray);
        System.out.println(dList);
        // test with String
        String[] sArray = {"cat", "dog", "elephant", "unicorn"};
        System.out.println(Arrays.toString(sArray));
        ArrayList<String> sList = arrayToList(sArray);
        System.out.println(sList);
    }
}