package General;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CasoDePrueba {
	
	public static WebDriver Instance;
	
	
	/* CP01
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Guardar los logs de ejecucion en la tabla 'Engee_LogEjecuciones'
	 * Detalle: Genera un registro en la tabla 'Engee_LogEjecuciones' con el id de la ejecucion y el texto a ingresar al log.
	 * Devuelve: Void
	 * Modificaciones:
	 */
	public static void guardarLogEjecucion(Connection conConexion, int intIdEjecucion, String strDetalleLog){
		String strQuery="INSERT INTO Engee_LogEjecuciones(intIdEjecucion,datFecha,strDetalle) VALUES ("+intIdEjecucion+",GETDATE(),'" + strDetalleLog + "')";
		try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			int count=stConsulta.executeUpdate();
			stConsulta.close();
			System.out.println(strDetalleLog);
		}
        catch (Exception e) {
			System.out.println("No se pudo grabar el log en la tabla 'Engee_LogEjecuciones'");
			e.printStackTrace();
		}
	}
	
	
	/* CP02
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Genera un string con la fecha del dia (o fecha + hora) con el formato "AAAAMMDD" (o "AAAAMMDDhhmmss").
	 * Detalle: Si el parametro strFecha es "Hora", genera el string con la fecha y la hora del momento. Sino, solo la fecha.
	 * Devuelve: String
	 * Modificaciones:
	 */
	public static String obtenerFecha(String strTipo) {
		// Obtiene la fecha del dia
		Calendar fecha = Calendar.getInstance();
    	int anio = fecha.get(Calendar.YEAR);
    	int mes = fecha.get(Calendar.MONTH) + 1;
    	int dia = fecha.get(Calendar.DAY_OF_MONTH);
    	// Concatena en formato "AAAAMMDD"
    	String strFecha= Integer.toString(anio)+ Integer.toString(mes)+ Integer.toString(dia);
    	// Si el parametro es "Hora", agrega la hora del momento en formato "hhmmss".
		if (strTipo=="Hora") {
	       	int hora =fecha.get(Calendar.HOUR_OF_DAY);
	    	int minutos = fecha.get(Calendar.MINUTE);
	    	int segundos = fecha.get(Calendar.SECOND);
	    	strFecha= strFecha + "_"+Integer.toString(hora)+Integer.toString(minutos)+Integer.toString(segundos);
		}		
		return strFecha;
	}
	

	/* CP03
	* Autor: Brenda Rodríguez
	* Creacion: 04.04.2016
	* Objetivo: Realiza la conexion de Eclipse con la base de datos SQL Server
	* Detalle: Es necesario configurar la solapa "Arguments" del "Run Configurations" con lo siguiente: (-Djava.library.path="\C:\sqljdbc_4.0\enu\auth\x64")
	* 		   Ademas se debe agregar el sqljdbc.jar a las librerias del proyecto
	* Devuelve: Connection
	* Modificaciones: 
	*/
	public static Connection abrirBaseDeDatos(Connection conConexion){
		try {
			String url = "jdbc:sqlserver://BARCELONA\\SQL2012;databaseName=QA_AUTOMATIZACION;integratedSecurity=true";
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			conConexion= DriverManager.getConnection(url);
			guardarLogEjecucion(conConexion, 0, "Conexion realizada correctamente");
		} catch (ClassNotFoundException ex) {
			System.out.println("WARN: Error 1 en la conexión con la BD: "+ex.getMessage());
			guardarLogEjecucion(conConexion, 0, "WARN: Error ClassNotFoundException en la conexión con la BD: "+ex.getMessage() );
			conConexion=null;			
		} catch (SQLException ex) {
			System.out.println("WARN: Error 2 en la conexión con la BD: "+ex.getMessage());
			guardarLogEjecucion(conConexion, 0, "WARN: Error SQLException en la conexión con la BD: "+ex.getMessage() );
			conConexion=null;			
		}
		catch (Exception ex) {
			System.out.println("WARN: Error Exception en la conexión con la BD: "+ex.getMessage());
			guardarLogEjecucion(conConexion, 0, "WARN: Error 3 en la conexión con la BD: "+ex.getMessage() );
			conConexion=null;
		}			
		return conConexion;
	}	

	
	/* CP04
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Cierra la conexion con la base de datos
	 * Detalle: -
	 * Devuelve: Void
	 * Modificaciones:
	 */
	public static void cerrarBaseDatos(Connection conConexion,int intIdEjecucion){
        try {
        	// Se cierra la base de datos correctamente
			guardarLogEjecucion(conConexion, intIdEjecucion, "Se cierra la conexión con la BD.");
			conConexion.close();
		} 
        // No se logra cerrar la base de datos
        catch (SQLException e) {
			guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: Error al intentar cerrar la conexión.");
		}
	}
	
	
	/* CP05
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Crea un nuevo registro en la tabla Engee_Ejecuciones
	 * Detalle: Un idEjecucion equivale a la ejecucion de una caso de prueba dentro de un lote de ejecucion.
	 * Devuelve: Void
	 * Modificaciones:
	 */
	public static void crearIdEjecucion(Connection conConexion,int intIdEjecucion,int intLoteEjecucion,String strNombreCasoPrueba){
		String strQuery="INSERT INTO Engee_Ejecuciones(intIdLoteEjecucion,strCasoPrueba,strResultado) VALUES ("+intLoteEjecucion + ",'"+ strNombreCasoPrueba +"' , 'WARN')";
		try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			int count=stConsulta.executeUpdate();
			stConsulta.close();
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: Error al grabar en la tabla 'Engee_Ejecuciones'");
			e.printStackTrace();
		}
	}
	
	
	/* CP06
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Obtiene un valor de la tabla Engee_Constantes
	 * Detalle: Recupera una constante en base al proyecto (intIdProyecto), ambiente (intIdAmbiente) y tipo de constante solicitada (strConstanteBuscada).
	 * Devuelve: String
	 * Modificaciones:
	 */
	public static String obtenerConstante (Connection conConexion,int intIdAmbiente,int intIdProyecto,String strConstanteBuscada){
		String strConstante=null;
		String strQuery="select strValorConstante from Engee_Constantes where intIdAmbiente="+intIdAmbiente+" and intIdProyecto=" + intIdProyecto + " and strDescripcionConstante='"+strConstanteBuscada + "'";
        try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			ResultSet rs=stConsulta.executeQuery();
			while (rs.next()) {
				strConstante=rs.getString("strValorConstante");
				guardarLogEjecucion(conConexion,0,"Constante obtenida con exito: " + strConstante);
			}
			rs.close();
			stConsulta.close();
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexion,0,"WARN: Error al leer la tabla 'Engee_Constantes: No se pudo obtener el valor de la constante: "+strConstanteBuscada+"'");
			e.printStackTrace();
		}
		return strConstante;
	}

	
	/* CP07
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 04.04.2016
	 * Objetivo: Recuera el ultimo idEjecucion de la tabla Engee_Ejecuciones
	 * Detalle: -
	 * Devuelve: Integer
	 * Modificaciones:
	 */
	public static int recuperarUltimoIdEjecucion(Connection conConexion){
		int intIdEjecucion=0;
		String strQuery="select top 1 intIdEjecucion from Engee_Ejecuciones order by 1 desc";
        try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			ResultSet rs=stConsulta.executeQuery();
			while (rs.next()) {
				intIdEjecucion=rs.getInt("intIdEjecucion");
			}
			rs.close();
			stConsulta.close();
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: Error al leer la tabla 'Engee_Ejecuciones': No se pudo obtener el ultimo IdEjecucion");
			e.printStackTrace();
		}
		return intIdEjecucion;
	}
	
	
	/* CP08
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 05.04.2016
	 * Objetivo: Actualizar el estado de la ejecucion (caso de prueba) en base a sus validaciones
	 * Detalle: La ejecucion vigente quedara con estado igual a lo pasado en el string strResultadoEjecucion
	 * Devuelve: Void
	 * Modificaciones:
	 */
	public static void actualizarEstadoEjecucion(Connection conConexion,int intIdEjecucion,String strResultadoEjecucion){
		String strQuery="update Engee_Ejecuciones set strResultado='" + strResultadoEjecucion + "' where intIdEjecucion=" + intIdEjecucion;
        try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			int count=stConsulta.executeUpdate();
			stConsulta.close();
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: Error al grabar en la tabla 'Engee_Ejecuciones': No se pudo actualizar el estado de la ejecucion " + intIdEjecucion + " a " + strResultadoEjecucion);
		}
	}
	
	
	/* CP09
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 05.04.2016
	 * Objetivo: Realizar una captura de la pantalla vigente.
	 * Detalle: Los archivos los crea en el directorio "strDireccionPantallas"
	 * Devuelve: Void
	 * Modificaciones: strNombreCasoPrueba.substring(0,6)
	 */ 
	public static void capturarPantalla(Connection conConexion,WebDriver Driver,int intIdEjecucion,String strDireccionPantallas,String strNombreCasoPrueba){	
		
		try {			
			File scrFile = ((TakesScreenshot)Driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(strDireccionPantallas+strNombreCasoPrueba.substring(0, 6)+"_"+CasoDePrueba.obtenerFecha("Hora")+".png"));
			guardarLogEjecucion(conConexion, intIdEjecucion, "Pantalla capturada.");
		} catch (IOException e1) {
			// Error en la captura de pantalla
			guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: No se pudo obtener la pantalla.");
			e1.printStackTrace();
		}	
	}
	
	
	/* CP10
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 05.04.2016
	 * Objetivo: Crea el lote de ejecucion (un lote de ejecucion siempre son 1 o mas ejecuciones (casos de prueba)).
	 * Detalle: Inserta un registro en la tabla Engee_LoteEjecuciones
	 * Devuelve: Void
	 * Modificaciones:
	 */
	public static void crearLoteEjecucion(Connection conConexion,int intIdModulo,String strUsuarioConectado){
		String strQuery="INSERT INTO Engee_LoteEjecuciones(datFecha,intIdModulo,strUsuario,strResultado) VALUES (getdate(),"+intIdModulo + " , '"+strUsuarioConectado+"' , 'WARN')";
		try {
			PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
			int count=stConsulta.executeUpdate();
			stConsulta.close();
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexion, 0, "WARN: Error al grabar en la tabla 'Engee_LoteEjecuciones'");
			e.printStackTrace();
		}
	}
	

	/* CP11
	 * Autor: Leopoldo Bernasconi
	 * Creacion: 05.04.2016
	 * Objetivo: Recupera el ultimo lote de ejecucion creado.
	 * Detalle: Esto lo hace para asignar a las nuevas ejecuciones este nuevo id.
	 * Devuelve: Integer
	 * Modificaciones:
	 */
		public static int recuperarUltimoLoteEjecucion(Connection conConexion){
			int intLoteEjecucion=0;
			String strQuery="select top 1 intidLoteEjecucion from Engee_LoteEjecuciones order by 1 desc";
	        try {
				PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
				ResultSet rs=stConsulta.executeQuery();
				while (rs.next()) {
					intLoteEjecucion=rs.getInt("intidLoteEjecucion");
				}
				rs.close();
				stConsulta.close();
			}
	        catch (Exception e) {
				guardarLogEjecucion(conConexion, intLoteEjecucion, "WARN: Error al leer la tabla 'intidLoteEjecucion': No se pudo obtener el ultimo Lote de ejecucion");
				e.printStackTrace();
			}
			return intLoteEjecucion;
		}
	
	
		/* CP12
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 05.04.2016
		 * Objetivo: Analiza el resultado de las ejecuciones (Para luego establecer el resultado del la ejecucion del lote)
		 * Detalle: Si una ejecucion resulto "FAIL", todo el lote sera "FAIL". Sino, sera "PASS"
		 * Devuelve: Void
		 * Modificaciones:
		 * - lBernasconi (15.07.2016): Contempla la posibilidad de "Warnings" durante la ejecucion
		 */
		public static void analizarLoteEjecucion(String strResultadoLote, Connection conConexion,int intLoteEjecucion){
			String strQuery="update Engee_LoteEjecuciones set strResultado='" + strResultadoLote + "' where intidLoteEjecucion="+intLoteEjecucion;
			try {
				PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
				int count=stConsulta.executeUpdate();
				stConsulta.close();
			}
	        catch (Exception e) {
				guardarLogEjecucion(conConexion, 0, "WARN: Error al grabar en la tabla 'Engee_LoteEjecuciones': No se pudo actualizar el caso de prueba de la ejecucion ");
				e.printStackTrace();
			}
		}
	
		
		/* CP13
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 05.04.2016
		 * Objetivo: Chequear que un dato recuperado de la base de datos sea correcto (no nulo).
		 * Detalle: Si no pudo recuperar algun dato, la ejecucion resultara "WARN"-
		 * Devuelve: Boolean
		 * Modificaciones:
		 */
		public static boolean chequearDatoRecuperado(Connection conConexion,boolean booValidaCasoPrueba,int intIdEjecucion, String strDatoRecuperado,String strNombreCasoPrueba){
			// Solo se ejecuta si no existen errores previos
			if (booValidaCasoPrueba==true) {
				if (strDatoRecuperado.equals(null)) {
					guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: No se pudo recuperar ningun dato de la base de datos. Valor actual: NULL");
					return false;
				}	
			} 
			return booValidaCasoPrueba;
		}
		
		
		/* CP14
		* Autor: Brenda Rodríguez
		* Creacion: 05.04.2016
		* Objetivo: Crea un directorio con el numero de lote de ejecucion
		* Detalle: Crea un directorio por cada lote de ejecución.		   
		* Devuelve: Void
		* Modificaciones: -	
		*/
		public static void crearDirectorio(Connection conConexion,int intLoteEjecucion,String strDireccionPantallas) {
			
			try {
				File directorio = new File(strDireccionPantallas+intLoteEjecucion+"\\");
				directorio.mkdir();
				guardarLogEjecucion(conConexion, 0, "Se creo el directorio " + intLoteEjecucion + " en " + strDireccionPantallas);
			} catch (Exception e) {
				// Error en la creacion del directorio
				guardarLogEjecucion(conConexion, 0, "Error al crear el directorio: " + intLoteEjecucion);
				}
		}
	
	
		/* CP15
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 05.04.2016
		 * Objetivo: Crear una archivo .txt donde grabar los logs de las ejecuciones.
		 * Detalle: -
		 * Devuelve: String
		 * Modificaciones:
		 */
		public static String crearArchivoLogs(Connection conConexion,int intLoteEjecucion,String strDireccionLogs){
			// Crea el fichero
			String strFichero=strDireccionLogs+intLoteEjecucion+".txt";
			File fichero = new File (strFichero);
			try {
				fichero.createNewFile();
				guardarLogEjecucion(conConexion, 0, "El archivo de logs " + intLoteEjecucion + " se ha creado correctamente");
			} catch (IOException ioe) {
				// Error en la creacion
				guardarLogEjecucion(conConexion, 0, "No ha podido ser creado el fichero " + intLoteEjecucion);
				ioe.printStackTrace();
			}
			return strFichero;			
		}
	
	
		/* CP16
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 05.04.2016
		 * Objetivo: Valida el resultado del caso de prueba ejecutado (en base a validaciones y acciones del @Test)
		 * Detalle: Para evaluar el caso, utiliza el parametro booValidaCasoPrueba.
		 * Devuelve: Void
		 * Modificaciones: 
		 * - lBernasconi (15.07.2016): Se contempla el valor de las 2 variables (booCasoPass y booCasoFail) para determinar su resultado.
		 */
		public static String validarCasoDePrueba(Connection conConexion,String strResultadoCaso ,int intIdEjecucion,String strNombreCasoPrueba){
			// Establece un valor (PASS / FAIL / WARN) para el caso de prueba analizado			
			if (strResultadoCaso=="PASS") {
				strResultadoCaso="PASS";
			} else {
				if (strResultadoCaso=="FAIL") {
					strResultadoCaso="FAIL";
				} else {
					strResultadoCaso="WARN";
				}
			}
			actualizarEstadoEjecucion(conConexion, intIdEjecucion, strResultadoCaso);
			//guardarLogEjecucion(conConexion, intIdEjecucion, "Fin del caso de prueba  " + strNombreCasoPrueba); /*strNombreCasoPrueba.substring(0, 6))*/
			return strResultadoCaso;
		}
	
		
		/* CP17						 							REVISAR!!!!!! HUBO CAMBIOS
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 05.04.2016
		 * Objetivo: Actualiza el nombre del caso de prueba en la tabla Engee_Ejecuciones
		 * Detalle: 
		 * Devuelve: Void
		 * Modificaciones:
		 */	
		public static void actualizarCasoPruebaEjecuciones(Connection conConexion,int intIdEjecucion,String strPrefijoCasoPrueba){
			String strQuery="update Engee_Ejecuciones set strCasoPrueba='"+strPrefijoCasoPrueba+"' where intIdEjecucion="+intIdEjecucion;
	        try {
				PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
				int count=stConsulta.executeUpdate();
				stConsulta.close();
			}
	        catch (Exception e) {
				guardarLogEjecucion(conConexion, intIdEjecucion, "WARN: Error al grabar en la tabla 'Engee_Ejecuciones': No se pudo actualizar el caso de prueba de la ejecucion " + intIdEjecucion + " a " + strPrefijoCasoPrueba);
				e.printStackTrace();
			}
		}

		
		/* CP18
		 * Autor: Brenda Rodriguez
		 * Creacion: 04.04.2016
		 * Objetivo: Inicializa una instancia de navegador para utilizar en las pruebas.
		 * Detalle: Al momento de realizar pruebas multinavegador, incluir un parametro que reciba el navegador a utilizar
		 * Valor: Void
		 * Modificaciones:
		 */
		 public static WebDriver inicializarNavegador(Connection conConexion){
			   	try {
			   	Instance = new FirefoxDriver();
			} catch (Exception e) {
				guardarLogEjecucion(conConexion, 0, "Error al instanciar el navegador");
			}
			       
			       return Instance;
			   }

	    
		/* CP19
		 * Autor: Brenda Rodriguez
		 * Creacion: 04.04.2016
		 * Objetivo: Cierra la instancia de navegador abierta para realizar las pruebas.
		 * Detalle: -
		 * Valor: Void
		 * Modificaciones:
		 */
	    public static void finalizarNevagador() { 
	    	Instance.close(); 
	    	}
	    
	    		    
		/* CP20
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 06.04.2016
		 * Objetivo: Poner el test como "Fail" en los reportes de jUnit
		 * Detalle: -
		 * Valor: Void
		 * Modificaciones:
		 * lBernasconi (19.07.2016): Se modifico el parametro que recibe a String
		 */
		public static void validarEjecucionCasoPrueba(String strResultadoCaso ){
			// Chequea las validaciones realizadas durante la ejecucion del caso de prueba
			if (strResultadoCaso=="FAIL") {		
				fail();
			}	
		}			
		
		

		/* CP21
		 * Autor: Brenda Rodriguez
		 * Creacion: 06.04.2016
		 * Objetivo: Poner el lote de ejecucion como "Fail" en la tabla dbo.Engee_LoteEjecuciones
		 * Detalle: Si por lo menos, un caso de prueba esta como "Fail", todo el lote sera "Fail"
		 * Valor: boolean
		 * Modificaciones: 
		 * - lBernasconi (07.04.2016): Se pasa el metodo a "boolean"
		 * - lBernasconi (15.07.2016): Analiza el valor de las variables strResultadoCaso y strResultadoLote para determinar 
		 * 							   el resultado del lote de ejecucion (el metodo pasa a "String").
		 */
		public static String validarResultadoLote(Connection conConexion, String strResultadoCaso, String strResultadoLote) {
			// Asigna un valor (PASS / FAIL / WARN) en base al valor de los variables del caso de prueba
			System.out.println("Lote: " + strResultadoLote);
			System.out.println("Caso: " + strResultadoCaso);
			if (strResultadoLote=="FAIL") {
			} else {
				if (strResultadoLote=="WARN") {
					if (strResultadoCaso=="FAIL") {
						strResultadoLote="FAIL";
					}
				} else {
					if (strResultadoCaso=="FAIL") {
						strResultadoLote="FAIL";
					} else {
						if (strResultadoCaso=="WARN") {
							strResultadoLote="WARN";
						}
					}
				}
			}
			return strResultadoLote;			
		}	
		
		
		/* CP22
		 * Autor: Brenda Rodriguez
		 * Creacion: 07.04.2016
		 * Objetivo: Modificar constante password en BD 
		 * Valor: Void
		 * Detalle: Modificar la password en la bd para el cp FU0034
		 * Modificaciones: 
		 */
			
	
		public static void modificarContraseñaBD(Connection conConexion, int intIdEjecucion, String strPassword) {
			
			String strQuery="update dbo.Engee_Constantes set strValorConstante = '"+
					         strPassword + "' where strDescripcionConstante = 'Password_Automatizacion'";
			//System.out.println(strQuery);
			try {
				PreparedStatement stConsulta=conConexion.prepareStatement(strQuery);
				int count=stConsulta.executeUpdate();
				stConsulta.close();
				
			} catch (Exception e) {
				guardarLogEjecucion(conConexion, intIdEjecucion, "WARN : Error al actualizar password en la tabla Engee_Contastes");
				e.printStackTrace();
			}
			
		}	
		
		
	
		/* CP23
		 * Autor: Brenda Rodriguez
		 * Creacion: 19.04.2016
		 * Objetivo: Connection DB mysql
		 * Valor: Void
		 * Detalle: Realizar la conección de la base de datos para mysql
		 * Modificaciones: 
		 */
			
		
		public static Connection conexion;
		
		public static Connection conectarMySql(String usuario, String password) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conexion = DriverManager.getConnection("jdbc:mysql://192.168.1.210/followup_2016",usuario, password);
				
			} catch (Exception e) {
				System.out.println(e);				
				
			}
			return conexion;
		}
		
		
		/* CP24
		 * Autor: Brenda Rodriguez
		 * Creacion: 19.04.2016
		 * Objetivo: Close connection DB mysql
		 * Valor: Void
		 * Detalle: Cerrar conección a la bd mysql. 
		 * Modificaciones: 
		 * */
		 
		
		public static void desconectarMySql(Connection conectarMySql) {			
			try {
		           conexion.close();
		       } catch (Exception e) {
		           System.out.println(e);
		       }

		}
		
		
		
		/* CP25 - No vigente
		 * Autor: Brenda Rodríguez	
		 * Creacion: 19.04.2016
		 * Objetivo: Ejecuta query de tipo select pasada como parámetro
		 * Valor: ResultSet
		 * Detalle: 
		 * Modificaciones:  
		 * */
		 		
		
		public static ResultSet sqlQuerySelect (String strQuery) throws SQLException {
			Statement stat = null;
			
			 try {
			       stat = conexion.createStatement();
			   } catch (SQLException e) {
			       e.printStackTrace();
			   }
			   return stat.executeQuery(strQuery);			
		}
	
		
		/* CP26- No vigente
		 * Autor: Brenda Rodríguez	
		 * Creacion: 22.04.2016
		 * Objetivo: Ejecuta query tipo INSERT, UPDATE o DELETE.
		 * Valor: Int
		 * Detalle: -
		 * Modificaciones:  
		 * */
				
		public static int ExecuteUpdateQuery(String strQuery) throws SQLException {
			   Statement stmt= null;
			   try {
			       stmt = conexion.createStatement();
			   } catch (SQLException e) {
			       e.printStackTrace();
			   }
			   return stmt.executeUpdate(strQuery);
		}
		
		
		/* CP27
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 18.07.2016
		 * Objetivo: Recibe una query, la ejecuta en mySQL y devuelve un Integer
		 * Valor: Int
		 * Detalle: Las querys deben estar preparadas para dar como resultado un unico valor (Integer)
		 * Modificaciones:  
		 * */
		public static int RecuperarInteger(Connection conConexionMySQL,int intIdEjecucion,String strQuery){
			int intValorDevuelto=0;
			ResultSet rs=null;
        try {
        	Statement st = conConexionMySQL.createStatement();
        	rs = st.executeQuery(strQuery);
        	
        	while (rs.next())
        	{
        		intValorDevuelto=rs.getInt(1);
        	}
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexionMySQL,intIdEjecucion, "WARN: No se pudo ejecutar la query enviada");
			e.printStackTrace();
		}
        return intValorDevuelto;
		}

		
		/* CP28
		 * Autor: Leopoldo Bernasconi
		 * Creacion: 18.07.2016
		 * Objetivo: Recibe una query, la ejecuta en mySQL y devuelve un String
		 * Valor: String
		 * Detalle: Las querys deben estar preparadas para dar como resultado un unico valor (String)
		 * Modificaciones:  
		 * */
		public static String RecuperarString(Connection conConexionMySQL,int intIdEjecucion,String strQuery){
			String strValorDevuelto=null;
			ResultSet rs=null;
        try {
        	Statement st = conConexionMySQL.createStatement();
        	rs = st.executeQuery(strQuery);
        	while (rs.next())
        	{
        		strValorDevuelto=rs.getString(1);
        	}
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexionMySQL,intIdEjecucion, "WARN: No se pudo ejecutar la query -" + strQuery + "-.");
			e.printStackTrace();
		}
        return strValorDevuelto;
		}
		
		
		/* CP28
		 * Autor: Bren
		 * Creacion: 01.09.2016
		 * Objetivo: Recibe una query, la ejecuta en mySQL y devuelve una lista de string
		 * Valor: String
		 * Detalle: 
		 * Modificaciones:  
		 * */
		public static ArrayList RecuperarArray(Connection conConexionMySQL,int intIdEjecucion,String strQuery){
			ArrayList lista=new ArrayList();
			//int arrValorDevuelto = 0;
			ResultSet rs=null;
        try {
        	Statement st = conConexionMySQL.createStatement();
        	rs = st.executeQuery(strQuery);
        	while (rs.next())
        	{        		
        		String nombre= rs.getString("nombre");
        		String k=new String(nombre);  //
        		lista.add(k);   //se agrega el objeto a esta lista
        		
        	}
        	System.out.println(lista);
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexionMySQL,intIdEjecucion, "WARN: No se pudo ejecutar la query -" + strQuery + "-.");
			e.printStackTrace();
		}
        return lista;
		}
		
		
		/* CP29
		 * Autor: Bren
		 * Creacion: 01.09.2016
		 * Objetivo: Recibe una query, la ejecuta en mySQL y devuelve una lista de int
		 * Valor: int	
		 * Detalle: 
		 * Modificaciones:  
		 * */
		public static ArrayList RecuperarArrayInt(Connection conConexionMySQL,int intIdEjecucion,String strQuery){
			ArrayList lista=new ArrayList();
			//int arrValorDevuelto = 0;
			ResultSet rs=null;
        try {
        	Statement st = conConexionMySQL.createStatement();
        	rs = st.executeQuery(strQuery);
        	while (rs.next())
        	{        		
        		Integer nombre= rs.getInt("legajo");
        		Integer k=new Integer(nombre);  //
        		lista.add(k);   //se agrega el objeto a esta lista
        		
        	}
        	System.out.println(lista);
		}
        catch (Exception e) {
			guardarLogEjecucion(conConexionMySQL,intIdEjecucion, "WARN: No se pudo ejecutar la query -" + strQuery + "-.");
			e.printStackTrace();
		}
        return lista;
		}
		
			
}
