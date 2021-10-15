import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class MLFunctions {

    private DataPane dataPane;
    private ScrollPane scrollPane;
    private CachedRowSet cachedRowSet;

    void showTable(String tableName, Connection conn, BorderPane rootNode){
        try(Statement stat = conn.createStatement(); ResultSet result = stat.executeQuery("SELECT * FROM " + tableName)){
            RowSetFactory factory = RowSetProvider.newFactory();
            cachedRowSet = factory.createCachedRowSet();
            cachedRowSet.setTableName(tableName);
            cachedRowSet.populate(result);

            dataPane = new DataPane(cachedRowSet);


            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(dataPane);
            dataPane.setPadding(new Insets(3,3,3,3));
            rootNode.setCenter(scrollPane);

        } catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
    }


    void delete(){
        System.out.println("Delete method started");
        if(cachedRowSet == null){
            System.out.println("No rowSet selected");
            return;
        }
        try {
            new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    cachedRowSet.last();
                    cachedRowSet.deleteRow();
                    cachedRowSet.acceptChanges(DatabaseManager.getConnection());
                    if (cachedRowSet.isAfterLast())
                        if (!cachedRowSet.last()) cachedRowSet = null;
                    return null;
                }

                public void done() {
                    //Do nothing ??????????????
                    System.out.println("Done");

                }
            }.call();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
/*    public void save(){
        if(cachedRowSet == null) return;
        new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                dataPane.setRow(cachedRowSet);
                cachedRowSet.acceptChanges();
                return null;
            }
        };


        if(cachedRowSet == null)return;
        new SwingWorker<Void, Void>(){
            @Override
            public Void doInBackground() throws SQLException {
                dataPane.setRow(cachedRowSet);
                crs.acceptChanges();
                return null;
            }
        }.execute();
    }*/



}
