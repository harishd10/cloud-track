package hope.it.works.rainfall;

public class FindLongitude {
	
	
	public static void main(String [] args) {
		float llon = 71;
		float dimx = 481;
		
		float min = 90 + 220 * llon / dimx;
		float max = 90 + 380 * llon / dimx;
		System.out.println(min + " " + max);
		
		for(int i = 122; i <= 150;i += 5) {
			float ll = i - 90;
			float x = ll * dimx / llon;
			System.out.print(x + ",");
		}
		System.out.println();
		for(int i = 122; i <= 150;i += 5) {
			System.out.print("r'$"+ i + "^\\circ$'"+ ",");
		}
		System.out.println();
	}
}
