import java.util.*;

class Fruit {
    private float weight;

    public Fruit() {
        weight = 0.0f;
    }

    public Fruit(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }
}

class Apple extends Fruit {
    public Apple() {
        super(1.0f);
    }

    public Apple(float weight) {
        super(weight);
    }
 }

class Orange extends Fruit {
    public Orange() {
        super(1.5f);
    }

    public Orange(float weight) {
        super(weight);
    }
 }

class Box<T extends Fruit> {
    private ArrayList<T> box;

    public Box() {
        box = new ArrayList<>();
    }

    public Box(int size) {
        box = new ArrayList<>(size);
    }

    public Box(T[] fruits) {
        box = new ArrayList<>();
        for (T element : fruits) {
            box.add(element);
        }
    }

    public void addFruit(T fruit) {
        box.add(fruit);
    }

    public float getWeight() {
        float result = 0.0f;
        for (T element : box) {
            result += element.getWeight();
        }
        return result;
    }

    public boolean compare(Box other) {
        return this.getWeight() == other.getWeight();
    }

    public Box<T> sprinkle() {
        Box<T> newBox = new Box<>(box.size());
        for (T element : box) {
            newBox.addFruit(element);
        }
        box.clear();
        return newBox;
    }

    public int boxSize() {
        return box.size();
    }
}

public class FruitsInBoxes {
    public static void main(String[] args) {
        Apple[] apples = new Apple[10];
        for (int i = 0; i < 10; i++) {
            apples[i] = new Apple();
        }

        Orange[] oranges = new Orange[10];
        for (int i = 0; i < 10; i++) {
            oranges[i] = new Orange();
        }

        Box<Apple> appleBox = new Box<>(apples);
        Box<Orange> orangeBox = new Box<>(oranges);

        System.out.println("Compare 10 apples to 10 oranges: " + appleBox.compare(orangeBox));

        for (int i = 0; i < 5; i++) {
            appleBox.addFruit(new Apple());
        }

        System.out.println("Compare 15 apples to 10 oranges: " + appleBox.compare(orangeBox));

        Box<Apple> anotherAppleBox = appleBox.sprinkle();

        System.out.printf("Number of fruits in the old box: %d, number of fruits in the new box: %d\n",
            appleBox.boxSize(), anotherAppleBox.boxSize());
    }
}