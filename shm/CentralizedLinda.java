package linda.shm;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    /**Permet un acces concurrent en lecture et en ecriture de l'ensemble de la hashmap
     * Les tuples sont ranges selon les classes de leurs composants
     * Les cles sont des tuples contenant les superclasses
     * Ex : [1,"1"] -> [Int,String], [1,[true,2],"component"] -> [Int,Tuple,String]
     **/
    private static Map<Tuple,CopyOnWriteArrayList<Tuple>> tupleSpaceMap = new ConcurrentHashMap<Tuple,CopyOnWriteArrayList<Tuple>>();
   
    /**Permet un acces concurrent en lecture et en ecriture de l'ensemble de la hashmap
     * Les events sont ranges dans une hashmap dont les cles sont eventMode.mode
     */
     private static Map<eventMode,CopyOnWriteArrayList<Tuple>> taskMap = new ConcurrentHashMap<eventMode,CopyOnWriteArrayList<Tuple>>(); 
     private Callback callbackEvent;
    
    public CentralizedLinda() {
    }

    /** Permet de savoir si l'espace de tuples contient le template
     * 
     * @param template
     * @return Boolean
     */
    public boolean spaceContains(Tuple template) {
       return tupleSpaceMap.containsKey(template);
    }
        
    /** Retourne un tuple correspondant au template
     * 
     * @param template
     * @return Tuple 
     */
    public Tuple getMatchingTuple (Tuple template) {
        CopyOnWriteArrayList<Tuple> tupleList = tupleSpaceMap.get(template);
        Iterator<Tuple> iter = tupleList.iterator();
        Tuple matchingTuple = null;
        boolean iterate = true;
        while(iter.hasNext() & iterate) {
            Tuple t = iter.next();
            if (template.contains(t)) {
                matchingTuple = t;
                iterate = false;
            } 
        }
        return matchingTuple;
    }

    /** Retourne le template superclasse corresponant au tuple
     * 
     * @param t
     * @return Tuple
     */
    public Tuple getMatchingTemplate(Tuple t) {

        Iterator<Serializable> iter = t.iterator();
        Serializable[] components = new Serializable[t.size()];
        int s = 0;

        while(iter.hasNext()) {
          Serializable c = iter.next();
             if(c.getClass() == Class.class) {
                components[s] = c;
                } else {              
                    components[s] = c.getClass();
                }
                s++;        
        }
        return new Tuple(components);
    }

    /** Retourne l'indice du tuple dans la liste s'il correspont au template
     *  Retourne -1 sinon
     * 
     * @param tupleList
     * @param template
     * @return Int
     */
    public int containsTuple(CopyOnWriteArrayList<Tuple> tupleList,Tuple template) {   
        Iterator<Tuple> iter = tupleList.iterator();
        int s = 0;
        while(iter.hasNext()) {
            Tuple t = iter.next();
            if(template.contains(t)) {
                return s;           
            }
            s++;
        }
        return -1;
    }

    /** Les fonctions suivantes sont decrites dans l'interface Linda */
    
    @Override
    public void write(Tuple t) {

        Tuple template = getMatchingTemplate(t);
        CopyOnWriteArrayList<Tuple> tupleList;
        synchronized(this) {
            if(!tupleSpaceMap.containsKey(template)) {             
                tupleList = new CopyOnWriteArrayList<Tuple>();
                tupleSpaceMap.put(template,tupleList);  
            }
        }
        tupleList = tupleSpaceMap.get(template);
        tupleList.add(t);     
        synchronized(this) {        
            if(!taskMap.isEmpty()) {              
                if(taskMap.containsKey(eventMode.TAKE) && !taskMap.get(eventMode.TAKE).isEmpty()) {                 
                    tupleList = taskMap.get(eventMode.TAKE);
                    int i = containsTuple(tupleList, getMatchingTemplate(t));                   
                    if(i >= 0) {
                    eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, tupleList.get(i), callbackEvent);
                    taskMap.get(eventMode.TAKE).remove(i);
                    if(taskMap.get(eventMode.TAKE).size() == 0){
                        taskMap.remove(eventMode.TAKE);
                    }
                    }
                } else if(taskMap.containsKey(eventMode.READ) && !taskMap.get(eventMode.READ).isEmpty()) {
                    tupleList = taskMap.get(eventMode.READ);
                    int i = containsTuple(tupleList, getMatchingTemplate(t));   
                    if(i >= 0) {
                    eventRegister(eventMode.READ,eventTiming.IMMEDIATE, tupleList.get(i), callbackEvent);
                    taskMap.get(eventMode.READ).remove(i);
                    if(taskMap.get(eventMode.READ).size() == 0) {
                        taskMap.remove(eventMode.READ);
                    }
                    }
                }
            }         
            notifyAll();    
        }     
      }
           

    @Override
    public Tuple take(Tuple template) {

        Tuple keyTemplate = getMatchingTemplate(template);
        while(!tupleSpaceMap.containsKey(keyTemplate)) {
        synchronized(this) {
          try{           
            wait();
          } catch (InterruptedException e){
                e.printStackTrace();
          }
        }
       }       
      CopyOnWriteArrayList<Tuple> tupleList = tupleSpaceMap.get(keyTemplate);
      int templateIndex = containsTuple(tupleList, template);        
      while(templateIndex < 0) {
            synchronized(this) {
                try {                    
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }                
            }
        }    
        Tuple t = tupleList.get(templateIndex);
        tupleList.remove(templateIndex);       
        return t;
    }
 
    @Override
    public Tuple read(Tuple template) {

        Tuple keyTemplate = getMatchingTemplate(template);
        while(!tupleSpaceMap.containsKey(keyTemplate)) {
        synchronized(this) {
          try{
            wait();
          } catch (InterruptedException e){
                e.printStackTrace();
           }
          }
        }
        CopyOnWriteArrayList<Tuple> tupleList = tupleSpaceMap.get(keyTemplate);
        while(containsTuple(tupleList, template) < 0) {
            synchronized(this) {
                try {  
                    wait();
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }               
            }
        }  
        Tuple t = tupleList.get(containsTuple(tupleList, template));      
        return t;      
    }

    @Override 
    public Tuple tryTake(linda.Tuple template) {
        Tuple keyTemplate = getMatchingTemplate(template);
        if(tupleSpaceMap.containsKey(keyTemplate)) {
          if (containsTuple(tupleSpaceMap.get(keyTemplate), template) >= 0) {
               return take(template); 
          } else {
               return null;
          }
        } else {
            return null;
        }
    }

    @Override 
    public Tuple tryRead(linda.Tuple template) {
        Tuple keyTemplate = getMatchingTemplate(template);
        if(tupleSpaceMap.containsKey(keyTemplate)) {
            if(containsTuple(tupleSpaceMap.get(keyTemplate), template) >= 0) {
                return read(template); 
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        Tuple keyTemplate = getMatchingTemplate(template);
        if(tupleSpaceMap.containsKey(keyTemplate)) {
        List<Tuple> matchingTuples = new CopyOnWriteArrayList<Tuple>();
        //System.out.println(tupleSpaceMap.get(getMatchingTemplate(template)));
        while(containsTuple(tupleSpaceMap.get(keyTemplate), template) >= 0) {
           matchingTuples.add(take(template));  
        }
           return matchingTuples;
        } else {
           return null;
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        Tuple keyTemplate = getMatchingTemplate(template);
        if(tupleSpaceMap.containsKey(keyTemplate)) {
            List<Tuple> matchingTuples = new CopyOnWriteArrayList<Tuple>();
            CopyOnWriteArrayList<Tuple> copyTupleList = new CopyOnWriteArrayList<Tuple>(tupleSpaceMap.get(keyTemplate));
            Iterator<Tuple> iter = copyTupleList.iterator();
            while((containsTuple(copyTupleList, template) >= 0) & iter.hasNext()) {
                int i = containsTuple(copyTupleList, template);
                matchingTuples.add(copyTupleList.get(i));  
                copyTupleList.remove(i);
                iter.next();
            }          
                return matchingTuples;
            } else {
                return null;    
            } 
    }
           
    @Override
    public void eventRegister(eventMode mode,eventTiming timing,Tuple template, Callback callback) {

        callbackEvent = callback;
        switch (mode) {
           case TAKE:
                 if(timing == eventTiming.IMMEDIATE) {
                    if(!(tryRead(template) == null) ) {                      
                        callback.call(take(template));                    
                    } else {                     
                        eventRegister(eventMode.TAKE,eventTiming.FUTURE, template, callback);                     
                    }
                } else {
                    if(taskMap.containsKey(eventMode.TAKE)) {
                        CopyOnWriteArrayList<Tuple>taskList = taskMap.get(eventMode.TAKE);     
                        taskList.add(template);
                        taskMap.put(mode,taskList);   
                    } else {
                        CopyOnWriteArrayList<Tuple> taskList = new CopyOnWriteArrayList<Tuple>();
                        taskList.add(template);  
                        taskMap.put(eventMode.TAKE,taskList);               
                    }
                }            
            break;
           case READ:   
                 if(timing == eventTiming.IMMEDIATE) {
                    System.out.println("Callback immediate");
                    if(!(tryRead(template) == null)) {
                         System.out.println("Callback");
                         callback.call(read(template));
                    } else {
                         System.out.print("Callback");
                         eventRegister(eventMode.READ,eventTiming.FUTURE, template, callback);
                    }
                 } else {
                    System.out.println("Callback future");
                    if(taskMap.containsKey(eventMode.READ)) {
                        CopyOnWriteArrayList<Tuple> taskList = taskMap.get(eventMode.READ);
                        taskList.add(template);
                        taskMap.put(eventMode.READ,taskList);                                          
                    } else {
                        CopyOnWriteArrayList<Tuple> taskList = new CopyOnWriteArrayList<Tuple>(); 
                        taskList.add(template);
                        taskMap.put(eventMode.READ,taskList);
                    }
                } 
          break;
         default: {
             System.out.println("Unknown event mode : " + mode);
         }
        }
    }

    /**Affichage de l'espace de tuples et de l'eventRegister
     * 
     */
    @Override
    public void debug(String prefix) {
        System.out.println("Espace de tuples : " + tupleSpaceMap + "\n");
        System.out.println("Event register: " + taskMap + "\n");
    }
}
