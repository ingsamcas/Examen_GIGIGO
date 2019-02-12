
package eclipseapps.mobility.parkeame.backendservice;

public class InvocationExample
{
    public static void main( String[] args )
    {
        DemoService.initApplication();

        DemoService demoService = DemoService.getInstance();
        // invoke methods of you service
        //Object result = demoService.yourMethod();
        //System.out.println( result );
    }
}
