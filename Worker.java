package lection2.tictak;

public class Worker extends Thread{

	private int id;
	private final Data data;
	
	public Worker(int id, Data d){
		this.id = id;
		data = d;
		this.start();
	}

	@Override
	public void run(){
		for (int i=0;i<5;i++){
			if(id==1) data.Tic();
			else if (id == 2) data.Tak();
			else data.Toy();
		}
	}

	/*
	@Override
	public void run(){
		super.run();
		for (int i=0;i<5;i++){
			synchronized (this.data){
				if(this.data.getState()==1) data.Tic();
				else if (this.data.getState() == 2) data.Tak();
				else data.Toy();
			}
		}
	}
	*/
	
}
