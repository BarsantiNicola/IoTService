package statistics;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings({"unused", "deprecation"})
public class Statistics implements Serializable{

    private final List<Statistic> statistics;

    public Statistics(){ statistics = new ArrayList<>(); }

    public Statistics(List<Statistic> statistics ){
        this.statistics = statistics;
    }

    //  function to add iteratively statistics. It is not important the order in which statistic instances
    //  will be added to the class, they will always be showed ordered by their date parameter
    public void addStatistic(Statistic statistic){
        for( Statistic s: statistics )
            if( s.getX().after(statistic.getX())) {
                statistics.add(statistics.indexOf(s), statistic);
                break;
            }
    }

    //  gives back all the stored statistic instances
    public List<Statistic> getStatistics(){ return statistics; }

    //  TODO to be removed only for testing purpouse
    public static List<Statistic> buildTestEnvironment(){
        return new Statistics( Arrays.asList(
                new Statistic( new Date(2012, Calendar.JULY, 15), "0" ),
                new Statistic( new Date(2012, Calendar.JULY, 18),"2000000"))).getStatistics();
//                new Statistic( new Date(2012, Calendar.JULY, 23),6000000),
//                new Statistic( new Date(2012, Calendar.AUGUST, 1),10000000),
//                new Statistic( new Date(2012, Calendar.AUGUST, 11),21000000),
//                new Statistic( new Date(2012, Calendar.AUGUST, 23),50000000),
//                new Statistic( new Date(2012, Calendar.AUGUST, 31),75000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 4),100000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 10),125000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 13),150000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 16),175000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 18),200000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 21),225000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 24),250000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 26),275000000),
//                new Statistic( new Date(2012, Calendar.SEPTEMBER, 28), 302000000 ))


    }

}
