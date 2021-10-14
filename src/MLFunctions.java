import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MLFunctions {

    private DataPane dataPane;
    private ScrollPane scrollPane;
    private CachedRowSet cachedRowSet;

    public void showTable(String tableName, Connection conn, BorderPane rootNode){
        try(Statement stat = conn.createStatement(); ResultSet result = stat.executeQuery("SELECT * FROM " + tableName)){
            RowSetFactory factory = RowSetProvider.newFactory();
            cachedRowSet = factory.createCachedRowSet();
            cachedRowSet.setTableName(tableName);
            cachedRowSet.populate(result);

            dataPane = new DataPane(cachedRowSet);
            dataPane.setAlignment(Pos.CENTER);
            rootNode.setCenter(dataPane);


            ScrollPane scrollPane = new ScrollPane(dataPane);
            //add(scrollPane, BorderLayout.CENTER);
            showNextRow();
        } catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
    }


    private void showNextRow(){
//        try{
//            if (cachedRowSet == null || cachedRowSet.isLast()) return;
//            cachedRowSet.next();
//            dataPane.showRow(cachedRowSet);
//        } catch (SQLException ex){
//            for(Throwable t: ex)
//                t.printStackTrace();
//        }
    }


    public void previousAction(CachedRowSet crs, DataPane dataPane){
//        try {
//            if (crs == null || crs.isFirst()) return;
//            crs.previous();
//            dataPane.showRow(crs);
//
//        } catch (SQLException ex){
//            for (Throwable t: ex)
//                t.printStackTrace();
//        }
    }

    public void nextAction(CachedRowSet crs, DataPane dataPane){
//        try {
//            if (crs == null || crs.isLast()) return;
//            crs.next();
//            dataPane.showRow(crs);
//        } catch (SQLException ex){
//            for (Throwable t: ex)
//                t.printStackTrace();
//        }
    }

    public void delete(){
//        if(cachedRowSet == null) return;
//        new Task<Void>(){
//            @Override
//            protected Void call() throws Exception {
//                cachedRowSet.deleteRow();
//                cachedRowSet.acceptChanges(DatabaseManager.getConnection());
//                if (cachedRowSet.isAfterLast())
//                    if (!cachedRowSet.last()) cachedRowSet = null;
//                return null;
//            }
//            public void done(){
//                dataPane.showRow(cachedRowSet);
//            }
//        };


//        if(cachedRowSet == null) return;
//        new SwingWorker<Void, Void>(){
//            @Override
//            public Void doInBackground() throws SQLException {
//                cachedRowSet.deleteRow();
//                cachedRowSet.acceptChanges(DatabaseManager.getConnection());
//                if (cachedRowSet.isAfterLast())
//                    if (!cachedRowSet.last()) cachedRowSet = null;
//                return null;
//            }
//
//            public void done(){
//                dataPane.showRow(cachedRowSet);
//            }
//        }.execute();
    }

    public void save(){
//        if(cachedRowSet == null) return;
//        new Task<Void>(){
//            @Override
//            protected Void call() throws Exception {
////                dataPane.setRow(cachedRowSet);
//                cachedRowSet.acceptChanges();
//                return null;
//            }
//        };


//        if(cachedRowSet == null)return;
//        new SwingWorker<Void, Void>(){
//            @Override
//            public Void doInBackground() throws SQLException {
//                dataPane.setRow(cachedRowSet);
//                crs.acceptChanges();
//                return null;
//            }
//        }.execute();
    }



}
