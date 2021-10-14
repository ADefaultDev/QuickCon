import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javax.sql.RowSet;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * defines better version of GridPane
 * using to show databases table values
 * and to change them
 */

public class DataPane extends GridPane {

    private ArrayList<TextField> fields;

    DataPane(RowSet rowSet) throws SQLException {
        super();

        ArrayList<String> columnNames = new ArrayList<>();
        ArrayList<Object> data = new ArrayList<>();

        ResultSetMetaData metaData = rowSet.getMetaData();
        System.out.println("Count of columns in a table " + metaData.getTableName(1).toUpperCase() + ": " + metaData.getColumnCount());
        int countOfColumn = metaData.getColumnCount() + 1 ; // WHY + 1???
        for (int i = 1; i < countOfColumn; i++) {  // i STARTs at 1
            columnNames.add(metaData.getColumnName(i)); // adding name of columns in array
        }
        System.out.println(columnNames);
        while (rowSet.next()) {
            ArrayList<Object> row = new ArrayList<>();
            for (int i = 1; i < countOfColumn; i++ ) {
                row.add(rowSet.getObject(i)); // filling a row with values
            }
            data.add(row); // adding a filled row to an array
        }
        System.out.println(data);

        //THERE SHOULD BE A TABLE
        fields = new ArrayList<TextField>();
        TextField tf = new TextField("Text1");
        fields.add(tf);
        for (int i=0; i < fields.size(); i++){
            this.add(new Label("First"),0,0);
            this.add(fields.get(i),1,0);
        }
    }

    public void showRow(ResultSet rs){
        try{
            if(rs==null) return;
            for(int i = 1; i <= fields.size(); i++){
                String field = rs.getString(i);
                TextField tb = fields.get(i-1);
                tb.setText(field);
            }
        }
        catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
    }
}
