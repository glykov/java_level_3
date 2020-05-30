import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
/**
 * Почему-то не удалось синхронизировать с помощью CountDownLatch - какая-нибудь из машин
 * все время стартовала до срока
 * пришлось подбирать количество вызовов barrier.await() эмпирически
 */
public class MainClass {
    public static final int CARS_COUNT = 4;
    public static void main(String[] args) {
        ExecutorService exec = Executors.newFixedThreadPool(CARS_COUNT);
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        CyclicBarrier barrier = new CyclicBarrier(CARS_COUNT + 1); // синхронизация машин и потока main
        //CountDownLatch startLatch = new CountDownLatch(1);  // попытка синхронизации main с машинами
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), barrier/*, startLatch*/);
        }
        for (int i = 0; i < cars.length; i++) {
            exec.execute(cars[i]);
        }
        try {
            // ждем когда машины подготовятся
            barrier.await();
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            //startLatch.countDown();
            // машины едут
            barrier.await();
            // машины доехали, можно печатать "Важное объявление"
            barrier.await();
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exec.shutdown();
        }

    }
}
class Car implements Runnable {
    static Object monitor = new Object();
    static boolean winner = false;
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private CyclicBarrier barrier;
    //private CountDownLatch latch;
    private int speed;
    private String name;
    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed, CyclicBarrier barrier/*, latch*/) {
        this.race = race;
        this.speed = speed;
        this.barrier = barrier;
        //this.latch = latch;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }
    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            barrier.await();
            barrier.await();
            //latch.await();
            for (int i = 0; i < race.getStages().size(); i++) {
                race.getStages().get(i).go(this);
            }
            synchronized (monitor) {
                if (!winner) {
                    winner = true;
                    System.out.println(name + " ПОБЕДИЛ!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}

class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
// устанавливаем семафор на тоннель
// т.к. он позволяет ограничить количество потоков,
// одновременно получающих доступ к методам класса
// более, чем 1 потоком
class Tunnel extends Stage {
    private Semaphore semaphore;
    public Tunnel() {
        this.semaphore = new Semaphore(MainClass.CARS_COUNT / 2);
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            try {
                if (!semaphore.tryAcquire()) {
                    System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                    semaphore.acquire();
                }
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                semaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() { return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}