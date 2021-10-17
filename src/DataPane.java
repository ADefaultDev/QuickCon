import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * defines better version of GridPane
 * using to show databases table values
 * and to change them
 */

public class DataPane extends GridPane {

    private ArrayList<String> columnNames;
    private ArrayList<Object> data;
    private CachedRowSet cachedRowSet;

    DataPane(CachedRowSet rowSet) throws SQLException {
        super();
        columnNames = new ArrayList<>();
        data = new ArrayList<>();
        this.cachedRowSet = rowSet;
        ResultSetMetaData metaData = rowSet.getMetaData();
        System.out.println("Count of columns in a table " + metaData.getTableName(1).toUpperCase() + ": " + metaData.getColumnCount());
        int countOfColumn = metaData.getColumnCount() + 1 ;
        for (int i = 1; i < countOfColumn; i++)
            columnNames.add(metaData.getColumnName(i)); // adding name of columns in array

        while (rowSet.next()) {
            ArrayList<Object> row = new ArrayList<>();
            for (int i = 1; i < countOfColumn; i++)
                row.add(rowSet.getObject(i).toString()); // filling a row with values
            data.add(row); // adding a filled row to an array
        }
        this.showRow();

    }

    private void demo() throws SQLException{
        System.out.println(cachedRowSet.size());
        cachedRowSet.last();
        System.out.println( cachedRowSet.getString(2));

    }


    private void showRow(){

        //create a table
        this.setVgap(10); // The height of the vertical gaps between rows.
        this.setHgap(40); // The width of the horizontal gaps between columns.
        this.add(new Label(""),0,0);

        for (int i=0; i < columnNames.size(); i++)
            this.add(new Label(columnNames.get(i)),i+1,0); // label for columns
        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> dt = new ArrayList<>((Collection<?>) data.get(i));
            CheckBox checkBox = new CheckBox("Del");
            checkBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if(aBoolean) {
                    try {
                        demo();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            this.add(checkBox,0,i+1);
            for (int j = 0; j < columnNames.size(); j++)
                this.add(new TextField((String) dt.get(j)), j + 1, 1 + i);
        }
    }


}
