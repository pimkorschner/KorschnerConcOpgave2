package werknemers;

public class Sinterklaas extends Thread {
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
	
	private void knapUiltje() {
		System.out.println("Sinterklaas doet een tukje");
		try {
			Thread.sleep((int)(Math.random() * 1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startMeeting() {
		System.out.println("Sinterklaas start een overleg");
		try {
			Thread.sleep((int)(Math.random() * 1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void verzamelOverleg() {
		
	}
	
	private void werkOverleg() {
		
	}
	
}
