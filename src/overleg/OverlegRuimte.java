package overleg;

import java.util.concurrent.Semaphore;

public class OverlegRuimte {
	
	private static final int ZWART = 1;
	private static final int BLAUW = 2;
	private static final int ROOD = 3;
	private static final int GROEN = 4;
	
	private static final int NR_WERKPIETEN = 8;
	private static final int NR_VERZAMELPIETEN = 8;
	
	private Thread[] werkpiet;
	private Thread[] verzamelpiet;
	
	private Semaphore werkpietZwart, werkWacht, verzamelWacht, overleg, verzamelOverleg, werkOverleg, slapendeSint;
	
	public OverlegRuimte() {
		werkpiet = new Thread[NR_WERKPIETEN];
		verzamelpiet = new Thread[NR_VERZAMELPIETEN];
		
		werkpietZwart = new Semaphore(0, true); //geen zwarte piet aanwezig
		
		slapendeSint = new Semaphore(1, true); //sint begint slapend
		
		werkWacht = new Semaphore(NR_WERKPIETEN, true);
		verzamelWacht = new Semaphore(NR_VERZAMELPIETEN, true);
		
		overleg = new Semaphore(0, true);
		verzamelOverleg = new Semaphore(0, true); //niet meteen een overleg bezig
		werkOverleg = new Semaphore(0, true); //niet meteen een overleg bezig
		
		werkpiet[0] = new WerkPiet("wp" + 1, 1, ZWART); //om zeker te weten dat er in ieder geval 1 zwarte piet is.
		for(int i = 1; i < NR_WERKPIETEN; i++) {
			werkpiet[i] = new WerkPiet("wp" + i, i, (int)(Math.random()*4+1));
			werkpiet[i].start();
		}
		for(int i = 0; i < NR_VERZAMELPIETEN; i++) {
			verzamelpiet[i] = new VerzamelPiet("vp" + i, i, (int)(Math.random()*4 + 1));
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
				System.out.println("werkwacht is: " + werkWacht.getQueueLength());
				werkWacht.release();
				System.out.println("na release werkwacht: " + werkWacht.getQueueLength());
				
				System.out.println("verzamelwacht is: " + verzamelWacht.getQueueLength());
				verzamelWacht.release(3);
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
			//prioriteit over een werkOverleg, als dit begint dan stuur de werkpieten weer aan het werk
		}
		
		private void werkOverleg() {
			
		}
		
	}
	
	class VerzamelPiet extends Thread {
		
		private int id;
		private int kleur;
		
		public VerzamelPiet(String name, int id, int kleur) {
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
				try {
					verzamelWacht.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
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
		private int kleur;
		
		public WerkPiet(String name, int id, int kleur) {
			super(name);
			this.id = id;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(true) {
				werk();

				//hier melden bij de sint, als er al een overleg gaande is dan meteen weer werk()
				try {
					werkWacht.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
