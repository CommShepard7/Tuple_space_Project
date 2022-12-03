package linda.test;
import linda.Linda;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Callback;



public class RemoteTest {

    public static class RemoteCallBack implements Callback {

        @Override
        public void call(Tuple t) {

            System.out.println("CALLBACK MADE ON " + t);

        }
    }


    public static void main(String[] a) {
        final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaRemoteSpace");
        Callback c = new RemoteCallBack();
        Tuple motif = new Tuple(4, 5);
        new Thread() {
            public void run () {
                System.out.println("Writing :" + motif);
                linda.write(motif);
                linda.write(new Tuple(1,"hey"));
                linda.write(new Tuple(1,"hey"));
                linda.write(new Tuple("hey"));
                linda.write(new Tuple("here"));
                //Collection <Tuple> t = linda.takeAll(new Tuple(Integer.class,Integer.class));
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, new Tuple(Integer.class,Boolean.class), c);
                linda.write(new Tuple(10,"Hey"));
                linda.write(new linda.Tuple("hello"));
                linda.debug("");
            }
                //System.out.println(t);
                //linda.debug("(1)");      
        }.start();

        new Thread() {
            public void run () {  
                linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, new Tuple(Integer.class,String.class), c);
                linda.debug("");
            }
                //System.out.println(t);
                //linda.debug("(1)");      
        }.start();


        new Thread() {
            public void run () {
                try {
                Thread.sleep(10000);
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("Writing :" + motif);
                linda.write(motif);
                linda.write(new Tuple(1,"hey"));
                linda.write(new Tuple(1,true));
                linda.write(new Tuple("hey"));
                linda.write(new Tuple("here"));
                /*Collection <Tuple> t = linda.takeAll(new Tuple(Integer.class,Integer.class));
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, new Tuple(Integer.class,String.class), c);
                linda.write(new Tuple(1,"hey"));
                linda.write(new linda.Tuple("hello"));
                linda.debug("");*/
            }      
        }.start();


    }   
}
