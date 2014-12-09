package overleg;

import java.util.concurrent.Semaphore;

public class OverlegRuimte {
	
	private static final int NR_WERKPIETEN = 8;
	private static final int NR_VERZAMELPIETEN = 8;
	
	private Thread[] werkpiet;
	private Thread[] verzamelpiet;
	
	private Semaphore werkWacht, verzamelWacht, overleg;
	
	public OverlegRuimte() {
		werkpiet = new Thread[NR_WERKPIETEN];
		verzamelpiet = new Thread[NR_VERZAMELPIETEN];
		
		werkWacht = new Semaphore(NR_WERKPIETEN, true);
		verzamelWacht = new Semaphore(NR_VERZAMELPIETEN, true);
		
		overleg = new Semaphore(0, true);
		
		for(int i = 0; i < NR_WERKPIETEN; i++) {
			werkpiet[i] = new WerkPiet("wp" + i, i);
			werkpiet[i].start();
		}
		for(int i = 0; i < NR_VERZAMELPIETEN; i++) {
			verzamelpiet[i] = new VerzamelPiet("vp" + i, i);
			verzamelpiet[i].start();
		}
		Sinterklaas sint = new Sinterklaas();
		sint.start();
		
	}

	class Sinterklaas extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(true) {
				knapUiltje();
			}
		}
		
		private void knapUiltje() {
			System.out.println("Sinterklaas doet een tukje");
			try {
				Thread.sleep((int)(Math.random() * 10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void startMeeting() {
			System.out.println("Sinterklaas start een overleg");
			try {
				Thread.sleep((int)(Math.random() * 10000));
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
	
	class VerzamelPiet extends Thread {
		
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
				Thread.sleep((int)(Math.random() * 10000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	class WerkPiet extends Thread {
		
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
	
}
