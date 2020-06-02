public class LastFourArray {
    public int[] lastFourArray(int[] array) {
        int[] result;
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 4)
                index = i + 1;      // points at the next element after 4
        }
        if (index != -1) {
            result = new int[array.length - index];
            System.arraycopy(array, index, result, 0, array.length - index);
            return result;
        }
        else {
            throw new RuntimeException("Number 4 is not found");
        }
    }
    public static void main(String[] args) {
//        LastFourArray lfa = new LastFourArray();
//        int[] test01 = {1, 2, 4, 4, 2, 3, 4, 1, 7, 4};
//        int[] test02 = {1, 2, 3};
//        System.out.println("Initial array: " + Arrays.toString(test02));
//        System.out.println("Result: " + Arrays.toString(lfa.lastFourArray(test02)));
    }
}
