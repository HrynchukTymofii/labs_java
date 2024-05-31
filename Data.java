package lection2.tictak;

public class Data {
    private int state = 1;

    public synchronized void Tic() {
        while (state != 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.print("Tic-");
        state = 2;
        notifyAll();
    }

    public synchronized void Tak() {
        while (state != 2) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.print("Tak");
        state = 3;
        notifyAll();
    }

    public synchronized void Toy() {
        while (state != 3) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("-Toy");
        state = 1;
        notifyAll();
    }

}

/*
public class Data {
    private int state = 1;
    public int getState() {return state;}

    public synchronized void Tic() {
        System.out.print("Tic-");
        state = 2;
    }

    public synchronized void Tak() {
        System.out.print("Tak");
        state = 3;
    }

    public synchronized void Toy() {
        System.out.println("-Toy");
        state = 1;
    }

}*/
