package werknemers;

public class VerzamelPiet extends Thread {
	
	private int id;
	
	public VerzamelPiet(String name, int id) {
		// TODO Auto-generated constructor stub
		super(name);
		this.id = id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
	
	private void verzamel() {
		System.out.println("Verzamelpiet " + id + " is aan het verzamelen");
		try {
			Thread.sleep((int)(Math.random() * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
