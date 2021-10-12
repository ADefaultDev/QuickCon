import javafx.scene.control.ScrollPane;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MLFunctions {
    private CachedRowSet cachedRowSet;
    private ScrollPane scrollPane;
    private DataPane dataPane;

    public void showTable(String tableName, Connection conn){
        try(Statement stat = conn.createStatement(); ResultSet result =
                stat.executeQuery("SELECT * FROM " + tableName)){
            RowSetFactory factory = RowSetProvider.newFactory();
            cachedRowSet = factory.createCachedRowSet();
            cachedRowSet.setTableName(tableName);
            cachedRowSet.populate(result);


            dataPane = new DataPane(cachedRowSet);
            ScrollPane scrollPane = new ScrollPane(dataPane);
            //add(scrollPane, BorderLayout.CENTER);
            showNextRow();
        }
        catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
    }


    private void showNextRow(){
        try{
            if(cachedRowSet==null || cachedRowSet.isLast()) return;
            cachedRowSet.next();
            dataPane.showRow(cachedRowSet);
        }
        catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }


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
        /*if(crs == null) return;
        new SwingWorker<Void, Void>(){
            @Override
            public Void doInBackground() throws SQLException {
                crs.deleteRow();
                crs.acceptChanges(conn);
                if (crs.isAfterLast())
                    if (!crs.last()) crs = null;
                return null;
            }

            public void done(){
                dataPanel.showRow(crs);
            }
        }.execute();*/
    }

    public static void save(){
        /*if(crs==null)return;
        new SwingWorker<Void, Void>(){
            @Override
            public Void doInBackground() throws SQLException {
                dataPanel.setRow(crs);
                crs.acceptChanges();
                return null;
            }
        }.execute();*/
    }



}
