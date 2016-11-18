package General;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexionBD {

	public static void main(String[] args) {
        Connection con = null;
        Statement stat = null;

        try {
        	
       
        	
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://192.168.1.210/followup_2016","root", "Backside360");

            stat = con.createStatement();
            // query de prueba
            String strQuery = "SELECT * FROM inq_asignada_estados limit 1";
            String strQuery2 = "INSERT INTO inq_asignada_estados (estado) VALUES ('brenda')";
            
            int result2 = CasoDePrueba.ExecuteUpdateQuery(strQuery2);           
                   
            
            ResultSet rs = stat.executeQuery(strQuery);
            
            rs.next();
            String strValorObtenido = rs.getString("estado");
            System.out.println(strValorObtenido);
       
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
            
        } finally {
        	
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
	

}
