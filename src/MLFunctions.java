import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class MLFunctions {


    public static void previousAction(CachedRowSet crs, DataPane dataPane){
        try{
            if(crs==null || crs.isFirst()) return;
            crs.previous();
            dataPane.showRow(crs);

        }
        catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }

    public static void nextAction(CachedRowSet crs, DataPane dataPane){
        try{
            if(crs==null || crs.isLast()) return;
            crs.next();
            dataPane.showRow(crs);
        }
        catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }

    public static void delete(){
        System.out.println("delete demo");
    }

    public static void save(){
        System.out.println("save demo");
    }


}
