/**
 * 1. Создать три потока, каждый из которых выводит определенную букву (A, B и C) 5 раз (порядок – ABСABСABС). Используйте wait/notify/notifyAll.
 * Написал свою реализацию, т.к. переделать класс WaitNotifyClass из методички - очень простое задание
 * просто добавить функцию 
 * public void printС() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'С') {
                        mon.wait();
                    }
                    System.out.print("С");
                    currentLetter = 'A';
                    mon.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
 * И, соответсвенно создать 3 поток в main
 * Thread t3 = new Thread(() -> {w.printC();});
 */
// Класс для печати любого символа в консоль, также служит монитором для синхронизации потоков
class CharPrinter {
	private volatile char currentLetter = 'A';
	
	public synchronized void printLetter(char letter) {
		while (letter != currentLetter) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.print(letter);
		if (currentLetter == 'A') currentLetter = 'B';
		else if (currentLetter == 'B') currentLetter = 'C';
		else if (currentLetter == 'C') currentLetter = 'A';
		notifyAll();
	}
}
// класс для печати конкретного символо на конкретном принетере в многопотоке, для чего реализует интерфейс Runnable
class PrintLetter implements Runnable {
	private char letterToPrint;
	private CharPrinter printer;
	
	public PrintLetter(char letter, CharPrinter cp) {
		letterToPrint = letter;
		printer = cp;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < 5; i++) {
			printer.printLetter(letterToPrint);
		}
	}
}
// Класс, создаеющий из запускающий потоки печати
public class PrintLetterTest {
	public static void main(String[] args) {
		CharPrinter printer = new CharPrinter();
		PrintLetter printA = new PrintLetter('A', printer);
		PrintLetter printB = new PrintLetter('B', printer);
		PrintLetter printC = new PrintLetter('C', printer);
		
		Thread t1 = new Thread(printA);
		Thread t2 = new Thread(printB);
		Thread t3 = new Thread(printC);
		
		t1.start();
		t2.start();
		t3.start();
	}
}
// Полученный вывод: ABCABCABCABCABC
