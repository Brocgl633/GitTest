package cs586;
import java.util.Scanner; 

public class cs586 {
	public static void main(String[] args)
	    { // partial driver
	          GasPump_1 gp1 = new GasPump_1();

	          System.out.print("                          GasPump-1");
	          System.out.print("\n");
	          System.out.print("                  MENU of Operations");
	          System.out.print("\n");
	          System.out.print("          0. Activate(int)");
	          System.out.print("\n");
	          System.out.print("          1. Start()");
	          System.out.print("\n");
	          System.out.print("          2. PayCredit");
	          System.out.print("\n");
	          System.out.print("          3. Reject()");
	          System.out.print("\n");
	          System.out.print("          4. Cancel()");
	          System.out.print("\n");
	          System.out.print("          5. Approved()");
	          System.out.print("\n");
	          System.out.print("          6. PayCash(int)");
	          System.out.print("\n");
	          System.out.print("          7. StartPump()");
	          System.out.print("\n");
	          System.out.print("          8. Pump()");
	          System.out.print("\n");
	          System.out.print("          9. StopPump()");
	          System.out.print("\n");
	          System.out.print("          q. Quit the program");
	          System.out.print("\n");

	          System.out.print("  Please make a note of these operations");
	          System.out.print("\n");
	          System.out.print("           GasPump-1 Execution");
	          System.out.print("\n");
	          char ch='1';
	          Scanner sc = new Scanner(System.in); 
	      while (ch !='q')
	      {
	          System.out.print("  Select Operation: ");
	          System.out.print("\n");
	          System.out.print("0-Activate,1-Start,2-PayCredit,3-Reject,                                                          4-Cancel,5-Approved,6-PayCash,7-StartPump, 8-Pump, 9-StopPump, q-quit");
	          System.out.print("\n");
	          ch=sc.next().charAt(0); 
	          switch (ch)
	          {
	        case '0':
	        { //Activate()
	                 System.out.print("  Operation:  Activate(int a)");
	                 System.out.print("\n");
	                 System.out.print("  Enter value of the parameter a:");
	                 System.out.print("\n");
	                 int a = sc.nextInt();
	                 gp1.Activate(a);
	                 break;
	             }

	        case '1':
	        { //Start
	                System.out.print("  Operation:  Start()");
	                System.out.print("\n");
	                gp1.Start();
	                break;
	            }

	        case '2':
	        { //PayCredit
	                System.out.print("  Operation:  PayCredit()");
	                System.out.print("\n");
	                gp1.PayCredit();
	                break;
	            }

	        case '3':
	        { //Reject
	                System.out.print("  Operation:  Reject()");
	                System.out.print("\n");
	                gp1.Reject();
	                break;
	            }

	        case '4':
	        { //Cancel
	                System.out.print("  Operation:  Cancel()");
	                System.out.print("\n");
	                gp1.Cancel();
	                break;
	            }

	        case '5':
	        { //Approved
	                System.out.print("  Operation:  Approved()");
	                System.out.print("\n");
	                gp1.Approved();
	                break;
	            }

	        case '6':
	        { //PayCash
	                System.out.print("  Operation:  PayCash(int c)");
	                System.out.print("\n");
	                System.out.print("  Enter value of the parameter c:");
	                System.out.print("\n");
	                int c =  sc.nextInt();
	                gp1.PayCash(c);
	                break;
	               }

	        case '7':
	        { //StartPump
	                System.out.print("  Operation:  StartPump()");
	                System.out.print("\n");
	                gp1.StartPump();
	                break;
	            }


	        case '8':
	        { //Pump
	                System.out.print("  Operation: Pump()");
	                System.out.print("\n");
	                gp1.Pump();
	                break;
	            }

	        case '9':
	        { //StopPump
	                System.out.print("  Operation:  StopPump()");
	                System.out.print("\n");
	                gp1.StopPump();
	                break;
	            }
	      }; // endswitch

	   }; //endwhile
   }
}