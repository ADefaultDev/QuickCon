import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * defines better version of GridPane
 * using to show databases table values
 * and to change them
 */

public class DataPane extends GridPane {
    private ArrayList<TextField> fields;

    DataPane() {
        super();
        fields = new ArrayList<TextField>();
        TextField tf = new TextField("dol");
        fields.add(tf);
        for (int i=0;i<fields.size();i++){
            this.add(new Label("First"),0,0);
            this.add(fields.get(i),1,0);
        }
    }

    public void showRow(ResultSet rs){
        try{
            if(rs==null)return;
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
