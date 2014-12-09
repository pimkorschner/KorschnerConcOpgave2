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
		super.run();
		while(true) {
			verzamel();
			
			//piet meld zich hier bij de sint. 
			
		}
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
