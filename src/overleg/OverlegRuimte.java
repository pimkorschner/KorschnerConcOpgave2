package overleg;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * afkortingen: 
 * 	- wp = werkpiet
 *  - wpz = zwarte werkpiet
 *  
 *  - vp = verzamelpiet
 * 
 * @author Pim
 *
 */
public class OverlegRuimte {
	private Random random;

	private static final int ZWART = 1;
	private static final int BLAUW = 2;
	private static final int ROOD = 3;
	private static final int GROEN = 4;

	private static final int NR_WERKPIETEN = 8;
	private static final int NR_VERZAMELPIETEN = 8;

	private int wpzRij = 0;
	private int wpRij = 0;
	private int vpRij = 0;

	private Thread[] werkpiet;
	private Thread[] verzamelpiet;

	private Semaphore verzamelOverleg, werkOverleg, verzamelOverlegWpz;
	
	private Semaphore meldSintWp, meldSintVp, meldSintWpz;

	private Semaphore mutexWpz, mutexWp, mutexVp;
	
	private Semaphore slaap;
	
	private boolean overlegBezig = false;

	public OverlegRuimte() {
		random = new Random();
		
		slaap = new Semaphore(0);

		meldSintWp = new Semaphore(0, true);
		meldSintVp = new Semaphore(0, true);
		meldSintWpz = new Semaphore(0, true);
		
		mutexWpz = new Semaphore(1);
		mutexWp = new Semaphore(1);
		mutexVp = new Semaphore(1);

		werkpiet = new Thread[NR_WERKPIETEN];
		verzamelpiet = new Thread[NR_VERZAMELPIETEN];

		verzamelOverleg = new Semaphore(0, true); //niet meteen een overleg bezig
		werkOverleg = new Semaphore(0, true); // niet meteen een overleg bezig
		verzamelOverlegWpz = new Semaphore(0, true); //1 voor de zwarte piet, zodat deze terug aan het werk gezet kan worden zodra er een werkoverleg is

		werkpiet[0] = new WerkPiet("wp" + 0, 0, ZWART); // om zeker te weten dat
														// er in ieder geval 1
														// zwarte piet is.
		werkpiet[0].start();
		for (int i = 1; i < NR_WERKPIETEN; i++) {
			werkpiet[i] = new WerkPiet("wp" + i, i, random.nextInt(4) + 1);
			werkpiet[i].start();
		}
		 for(int i = 0; i < NR_VERZAMELPIETEN; i++) {
		 verzamelpiet[i] = new VerzamelPiet("vp" + i, i, random.nextInt(4)+1);
		 verzamelpiet[i].start();
		 }
		Sinterklaas sint = new Sinterklaas();
		sint.start();

	}

	class Sinterklaas extends Thread {
		@Override
		public void run() {
			super.run();
			System.out.println("De sint is alive!");
			while (true) {
				try {
					slaap.acquire();
					System.out.println("De sint is busy!");
					mutexVp.acquire();
					mutexWpz.acquire();
					if(vpRij >= 3 && wpzRij == 1) {
						overlegBezig = true;
						int verzamelPietenInMeeting = vpRij;
						
						meldSintVp.acquire(vpRij);
						meldSintWpz.acquire(1);
						
						//de wachtrij weer naar 0 zetten zodat nieuwe verzamelpieten bij het overleg kunnen komen.
						vpRij = 0;
						
						mutexVp.release();
						mutexWpz.release();
						
						/*
						 * werkpieten weer aan het werk zetten.
						 */
						mutexWp.acquire();
						meldSintWp.acquire(wpRij);
						werkOverleg.release(wpRij);
						wpRij = 0;
						mutexWp.release();
						
						verzamelOverleg();
						
						mutexWpz.acquire();
						verzamelOverleg.release(verzamelPietenInMeeting); //verzamelpieten weer wegsturen
						verzamelOverlegWpz.release(1); //zwarte piet weer aan het werk
						
						//De wachtrij voor zwarte werkpieten naar 0 zodat zwarte werkpieten weer aan kunnen sluiten
						wpzRij = 0;
						mutexWpz.release();
						overlegBezig = false;
					} else {
						mutexVp.release(); //om zeker te zijn dat er gereleased wordt
						mutexWpz.release();
					}
					
					mutexWp.acquire();
					if(meldSintWp.tryAcquire(3)) { //zodra er 3 werkpieten aanwezig zijn start de werkpiet meeting
						overlegBezig = true;
						
						//zwarte piet weer aan het werk zetten.
						mutexWpz.acquire();
						if(wpzRij == 1) {
							meldSintWpz.acquire(1);
							verzamelOverlegWpz.release(1);
							wpzRij = 0;
						}
						mutexWpz.release();
						
						System.out.println("3 werkpieten beschikbaar");
						werkOverleg();
						werkOverleg.release(3);
						
						/*Dit is er zodat er niet meer dan 3 werkpieten gaan wachten op een meeting*/
//						mutexWp.acquire();
						wpRij = 0;
						mutexWp.release();
						overlegBezig = false;
					} else {
						mutexWp.release();
					}
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		}

		private void verzamelOverleg() {
			
			System.out.println("Sinterklaas start een VERZAMELoverleg +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			try {
				Thread.sleep((int) (Math.random() * 10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Verzameloverleg afgelopen +++++++++++++++++++++++++++++++++++++++++++++++---------------------");
		}

		private void werkOverleg() {
			System.out.println("Sinterklaas start een WERKoverleg _____________________________________________");
			try {
				Thread.sleep((int) (Math.random() * 10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("werkoverleg afgelopen ____________________________=========================");
		}

	}

	class WerkPiet extends Thread {

		private int id;
		private int kleur;

		public WerkPiet(String name, int id, int kleur) {
			super(name);
			this.id = id;
			this.kleur = kleur;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (true) {
				try {
					werk();
					if(!overlegBezig) {
						if (kleur == ZWART && wpzRij < 1) {
							// zwarte piet, dus proberen in de rij voor zwarte
							// pieten te komen
							mutexWpz.acquire();
							if (wpzRij < 1) {
								wpzRij++;
								mutexWpz.release();
								
								System.out.println(getName() + " staat in de zwarte rij");
								meldSintWpz.release(1);
//								werkpietZwart.acquire();
								
								/**
								 * Hier de sint wakker maken.
								 */
								slaap.release();
								verzamelOverlegWpz.acquire();
								System.out.println(getName() + " gaat weer aan het werk");
//								sintDutje.acquire();
							} else {
								mutexWpz.release();
							}
						} else {
							// gekleurde pieten gaan hier naartoe
							//overige zwarte pieten ook, die knakkers zijn slim dus gaan wachten op een andere meeting i.p.v. werken
							mutexWp.acquire();
							if (wpRij < 3) {
								// er is nog ruimte in de rij voor werkpieten
								wpRij++;
								mutexWp.release();
								
								System.out.println(getName() + " staat in de kleur rij");
								meldSintWp.release(1);
								/**
								 * Hier de sint wakker maken. 
								 */
								slaap.release();
								werkOverleg.acquire();
								System.out.println(getName() + " gaat weer aan het werk");
							} else {
								mutexWp.release();
							}

						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		private void werk() {
			try {
//				System.out.println(getName() + " is aan het werk en is kleur: " + kleur);
				Thread.sleep((int) (Math.random() * 10000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	class VerzamelPiet extends Thread {

		private int id;
		private int kleur;

		public VerzamelPiet(String name, int id, int kleur) {
			// TODO Auto-generated constructor stub
			super(name);
			this.id = id;
			this.kleur = kleur;
		}

		@Override
		public void run() {
			super.run();
			while (true) {
				verzamel();
				try {
					mutexVp.acquire();
					vpRij++;
					mutexVp.release();
					
					System.out.println(getName() + " staat in de rij voor het verzamel overleg");
					meldSintVp.release(1);
					
					/**
					 * Hier de sint wakker maken. 
					 */
					slaap.release();
					verzamelOverleg.acquire();
					System.out.println(getName() + " gaat weer verzamelen");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		private void verzamel() {
//			System.out.println(getName() + " is aan het verzamelen");
			try {
				Thread.sleep((int) (Math.random() * 10000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
