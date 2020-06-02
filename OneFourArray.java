public class OneFourArray {
    public boolean check(int[] array) {
        boolean hasOne = false;
        boolean hasFour = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 1) hasOne = true;
            else if (array[i] == 4) hasFour = true;
            else return false;
        }
        return hasOne && hasFour;
    }
}
