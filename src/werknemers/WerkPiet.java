package werknemers;

public class WerkPiet extends Thread {
	
	private int id;
	
	public WerkPiet(String name, int id) {
		super(name);
		this.id = id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(true) {
			werk();
			
			//hier melden bij de sint
		}
	}
	
	private void werk() {
		try {
			System.out.println("werkpiet " + id + "is aan het werk");
			Thread.sleep((int)(Math.random() * 1000));
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
}
