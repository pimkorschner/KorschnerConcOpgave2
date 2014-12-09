package overleg;

import java.util.concurrent.Semaphore;

import werknemers.Sinterklaas;
import werknemers.VerzamelPiet;
import werknemers.WerkPiet;

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

}
