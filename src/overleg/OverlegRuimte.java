package overleg;

import java.util.Random;
import java.util.concurrent.Semaphore;

import sun.org.mozilla.javascript.internal.ast.TryStatement;

public class OverlegRuimte {
	private Random random;

	private static final int ZWART = 1;
	private static final int BLAUW = 2;
	private static final int ROOD = 3;
	private static final int GROEN = 4;

	private static final int NR_WERKPIETEN = 8;
	private static final int NR_VERZAMELPIETEN = 8;

	private static final int WERKPIETENRIJ = 3;
	private static final int VERZAMELPIETENRIJ = 8;

	private int werkPietZwartRij = 0;
	private int werkPietenInRij = 0;
	private int verzamelPietenInRij = 0;

	private Thread[] werkpiet;
	private Thread[] verzamelpiet;

	private Semaphore werkpietZwart, werkWacht, verzamelWacht, overleg,
			verzamelOverleg, werkOverleg, sintDutje, verzamelOverlegZwart;
	
	private Semaphore meldSintWerk, meldSintVerzamel, meldSintWerkZwart;

	private Semaphore mutexZwart, mutexWerk, mutexVerzamel;
	
	private boolean overlegBezig = false;

	public OverlegRuimte() {
		random = new Random();

		meldSintWerk = new Semaphore(0, true);
		meldSintVerzamel = new Semaphore(0, true);
		meldSintWerkZwart = new Semaphore(0, true);
		
		mutexZwart = new Semaphore(1);
		mutexWerk = new Semaphore(1);
		mutexVerzamel = new Semaphore(1);

		werkpiet = new Thread[NR_WERKPIETEN];
		verzamelpiet = new Thread[NR_VERZAMELPIETEN];

		werkpietZwart = new Semaphore(1, true); // geen zwarte piet aanwezig

		sintDutje = new Semaphore(0, true); // sint begint slapend

		werkWacht = new Semaphore(3, true); // deze kan 3 lang zijn
		verzamelWacht = new Semaphore(NR_VERZAMELPIETEN, true);

		verzamelOverleg = new Semaphore(0, true); //niet meteen een overleg bezig
		werkOverleg = new Semaphore(0, true); // niet meteen een overleg bezig
		verzamelOverlegZwart = new Semaphore(0, true); //1 voor de zwarte piet, zodat deze terug aan het werk gezet kan worden zodra er een werkoverleg is

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
			// TODO Auto-generated method stub
			super.run();
			System.out.println("De sint is alive!");
			while (true) {
				// psuedoshit
				// als er 3 verzamelpieten en 1 werkpiet is dan: verzameloverleg
				// werkpieten gebeuren eerst doen, dus: als er geen
				// verzamelpieten zijn of 3 werkpieten (met of zonder zwarte)
				// dan begin een werkoverleg

				try {
					mutexVerzamel.acquire();
					mutexZwart.acquire();
					if(verzamelPietenInRij >= 3 && werkPietZwartRij == 1) {
						overlegBezig = true;
						int verzamelPietenInMeeting = verzamelPietenInRij;
						
						meldSintVerzamel.acquire(verzamelPietenInRij);
						meldSintWerkZwart.acquire(1);
						
						//de wachtrij weer naar 0 zetten zodat nieuwe verzamelpieten bij het overleg kunnen komen.
						verzamelPietenInRij = 0;
						
						mutexVerzamel.release();
						mutexZwart.release();
						
						/*
						 * Voor dat het overleg begint eerst de werkpieten weer aan het werk zetten. 
						 */
						mutexWerk.acquire();
						meldSintWerk.acquire(werkPietenInRij);
						werkOverleg.release(werkPietenInRij);
						werkPietenInRij = 0;
						mutexWerk.release();
						
						verzamelOverleg();
						
						mutexZwart.acquire();
						verzamelOverleg.release(verzamelPietenInMeeting); //verzamelpieten weer wegsturen
						verzamelOverlegZwart.release(1); //zwarte piet weer aan het werk
						
						//De wachtrij voor zwarte werkpieten naar 0 zodat zwarte werkpieten weer aan kunnen sluiten
						werkPietZwartRij = 0;
						mutexZwart.release();
						overlegBezig = false;
					} else {
						mutexVerzamel.release(); //om zeker te zijn dat er gereleased wordt
						mutexZwart.release();
					}
					
					if(meldSintWerk.tryAcquire(3)) { //zodra er 3 werkpieten aanwezig zijn start de werkpiet meeting
						overlegBezig = true;
						
						//zwarte piet weer aan het werk zetten.
						mutexZwart.acquire();
						if(werkPietZwartRij == 1) {
							meldSintWerkZwart.acquire(1);
							verzamelOverlegZwart.release(1);
							werkPietZwartRij = 0;
						}
						mutexZwart.release();
						
						System.out.println("3 werkpieten beschikbaar");
						werkOverleg();
						werkOverleg.release(3);
						
						/*Dit is er zodat er niet meer dan 3 werkpieten gaan wachten op een meeting*/
						mutexWerk.acquire();
						werkPietenInRij = 0;
						mutexWerk.release();
						overlegBezig = false;
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
						if (kleur == ZWART && werkPietZwartRij < 1) {
							// zwarte piet, dus proberen in de rij voor zwarte
							// pieten te komen
							mutexZwart.acquire();
							if (werkPietZwartRij < 1) {
								werkPietZwartRij++;
								mutexZwart.release();
								
								System.out.println(getName() + " staat in de zwarte rij");
								meldSintWerkZwart.release(1);
//								werkpietZwart.acquire();
								verzamelOverlegZwart.acquire();
								System.out.println(getName() + " gaat weer aan het werk");
//								sintDutje.acquire();
							} else {
								mutexZwart.release();
							}
						} else {
							// gekleurde pieten gaan hier naartoe
							//overige zwarte pieten ook, die knakkers zijn slim dus gaan wachten op een andere meeting i.p.v. werken
							mutexWerk.acquire();
							if (werkPietenInRij < 3) {
								// er is nog ruimte in de rij voor werkpieten
								werkPietenInRij++;
								mutexWerk.release();
								
								System.out.println(getName() + " staat in de kleur rij");
								meldSintWerk.release(1);
								werkOverleg.acquire();
								System.out.println(getName() + " gaat weer aan het werk");
							} else {
								mutexWerk.release();
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
		}

		@Override
		public void run() {
			super.run();
			while (true) {
				verzamel();
				try {
					mutexVerzamel.acquire();
					verzamelPietenInRij++;
					mutexVerzamel.release();
					
					System.out.println(getName() + " staat in de rij voor het verzamel overleg");
					meldSintVerzamel.release(1);
					
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
