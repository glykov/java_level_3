import java.util.*;

public class SwapMethod {
    public static <T> void swapArray(T[] a) {
        for (int i = 0; i < (a.length - 1); i++) {
            T temp = a[i];
            a[i] = a[i + 1];
            a[i + 1] = temp;
        }
    }
    public static void main(String[] args){
        // testing Integers
        Integer[] iArray = {1, 2, 3, 4, 5};
        System.out.println(Arrays.toString(iArray));
        swapArray(iArray);
        System.out.println(Arrays.toString(iArray));
        // testing with Strings
        String[] sArray = {"I", "learn", "Java"};
        System.out.println(Arrays.toString(sArray));
        swapArray(sArray);
        System.out.println(Arrays.toString(sArray));
    }
}