import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Diagnostico {

	private final String DATAFILE = "data/disease_data.data";
	private Connection conn;
	private boolean creado=false;
	private boolean conectado=false;

	private void showMenu() {

		int option = -1;
		do {
			System.out.println("Bienvenido a sistema de diagnostico\n");
			System.out.println("Selecciona una opcion:\n");
			System.out.println("\t1. Creacion de base de datos y carga de datos.");
			System.out.println("\t2. Realizar diagnostico.");
			System.out.println("\t3. Listar sintomas de una enfermedad.");
			System.out.println("\t4. Listar enfermedades y sus codigos asociados.");
			System.out.println("\t5. Listar sintomas existentes en la BD y su tipo semantico.");
			System.out.println("\t6. Mostrar estadisticas de la base de datos.");
			System.out.println("\t7. Salir.");
			try {
				option = readInt();
				switch (option) {
				case 1:
					crearBD();
					break;
				case 2:
					realizarDiagnostico();
					break;
				case 3:
					listarSintomasEnfermedad();
					break;
				case 4:
					listarEnfermedadesYCodigosAsociados();
					break;
				case 5:
					listarSintomasYTiposSemanticos();
					break;
				case 6:
					mostrarEstadisticasBD();
					break;
				case 7:
					exit();
					break;
				}
			} catch (Exception e) {
				System.err.println("Opcion introducida no valida!");
			}
		} while (option != 7);
		exit();
	}// de showMenu

	private void exit() {
		try {
			conn.close();// cerramos la conexion
			System.out.println("Saliendo.. hasta otra!");
			System.exit(0);
		} catch (SQLException e) {
			System.err.println("Error en SQL: " + e.getMessage());
			e.printStackTrace();
		}// de try-catch

	}// de exit

	private void conectar() {
		try {
			if(!conectado){// nos aseguramos si antes ya estamos conectados de antes
				Class.forName("com.mysql.jdbc.Driver");
				String serverAddress = "localhost:3306";
				String user = "bddx";
				String pass = "bddx_pwd";
				String url = "jdbc:mysql://" + serverAddress + "/";
				conn = DriverManager.getConnection(url, user, pass);
				conn.setAutoCommit(true);
				System.out.println("Conectado a la base de datos!");
			}// de try
			conectado=true;
		}catch(Exception e){
			System.err.println("Error al conectar BD:" + e.getMessage());
			e.printStackTrace();
		} // de try-catch
	}// de conectar

	private void crearBD() {
		conectar();
		try {
			if(!creado){

				String baseDeDatos = "CREATE DATABASE diagnostico";

				String symptom = "CREATE TABLE `symptom` ("
						+ "`cui` varchar(25) NOT NULL,"
						+ "`name` varchar(255) DEFAULT NULL,"
						+  " `st` varchar(45) DEFAULT NULL,"
						+ "PRIMARY KEY (`cui`)"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1; "; //creacion de la tabla symptom


				String source= "CREATE TABLE `source` ("
						+ " `source_id` int(11) NOT NULL AUTO_INCREMENT,"
						+ " `name` varchar(255) DEFAULT NULL,"
						+ "PRIMARY KEY (`source_id`)"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1;"; //creacion de la tabla source


				String code = "CREATE TABLE `code` ("
						+ "`code_id` varchar(255) NOT NULL,"
						+ " `source_id` int(11) NOT NULL,"
						+ "PRIMARY KEY (`code_id`),"
						+ " KEY `source_id_idx` (`source_id`),"
						+ "CONSTRAINT `fk_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`) ON DELETE NO ACTION ON UPDATE NO ACTION"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1;"; //creacion de la tabla code


				String disease= "CREATE TABLE `disease` ("
						+ "`disease_id` int(11) NOT NULL AUTO_INCREMENT,"
						+ "`name` varchar(255) DEFAULT NULL,"
						+ "PRIMARY KEY (`disease_id`)"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1;"; //creacion de la tabla disease 


				String disease_code = "CREATE TABLE `disease_code` ("
						+ " `disease_id` int(11) NOT NULL,"
						+ "`code` varchar(255) NOT NULL,"
						+ " `source_id` int(11) NOT NULL,"
						+ "PRIMARY KEY (`disease_id`,`code`,`source_id`),"
						+ " KEY `fk_code_idx` (`code`),"
						+ " KEY `fk_source_id_idx` (`source_id`),"
						+ "CONSTRAINT `fko_code` FOREIGN KEY (`code`) REFERENCES `code` (`code_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,"
						+ " CONSTRAINT `fko_disease_id` FOREIGN KEY (`disease_id`) REFERENCES `disease` (`disease_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,"
						+ " CONSTRAINT `fko_source_id` FOREIGN KEY (`source_id`) REFERENCES `code` (`source_id`) ON DELETE NO ACTION ON UPDATE NO ACTION"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1;"; //creacion de la tabla disease_code

				String disease_symptom="CREATE TABLE `disease_symptom` ("
						+ "`disease_id` int(11) NOT NULL,"
						+ "`symptom_id` varchar(25) NOT NULL,"
						+ "PRIMARY KEY (`disease_id`,`symptom_id`),"
						+ " KEY `symptom_id_idx` (`symptom_id`),"
						+ "CONSTRAINT `fk_disease_id` FOREIGN KEY (`disease_id`) REFERENCES `disease` (`disease_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,"
						+ " CONSTRAINT `fk_symptom_id` FOREIGN KEY (`symptom_id`) REFERENCES `symptom` (`cui`) ON DELETE NO ACTION ON UPDATE NO ACTION"
						+ ")ENGINE=InnoDB DEFAULT CHARSET=latin1;"; //creacion de la tabla disease_symptom 

				//Ejecutamos los PreparedStatements para crear la base de datos y las tablas 

				PreparedStatement pst= conn.prepareStatement(baseDeDatos);
				pst.execute();
				conn= DriverManager.getConnection("jdbc:mysql://" +  "localhost:3306" + "/"+ "diagnostico" + "?allowMultiQueries=true", "bddx","bddx_pwd");// nos conectamos a diagnostico
				pst= conn.prepareStatement(symptom);
				pst.execute();
				pst= conn.prepareStatement(source);
				pst.execute();
				pst= conn.prepareStatement(code);
				pst.execute();
				pst= conn.prepareStatement(disease);
				pst.execute();
				pst= conn.prepareStatement(disease_code);
				pst.execute();
				pst= conn.prepareStatement(disease_symptom);
				pst.execute(); 

				conn.setAutoCommit(false);

				//Declaracion de los String para insertar los datos
				String disease_data = "INSERT INTO disease(name) VALUES (?);";
				String source_data = "INSERT INTO source(name) SELECT ? FROM dual WHERE NOT EXISTS (SELECT * FROM diagnostico.source WHERE name=?)";
				String sim_data = "INSERT INTO symptom(cui,name,st) SELECT ?,?,?  FROM dual WHERE NOT EXISTS (SELECT * FROM diagnostico.symptom WHERE cui=?);";
				String code_data = "INSERT INTO code(code_id,source_id)  VALUES (?,(SELECT source_id FROM diagnostico.source WHERE name=?));";
				String disease_code_data = "INSERT INTO disease_code (disease_id, code, source_id) VALUES ((SELECT disease_id FROM diagnostico.disease WHERE name=?),?,(SELECT source_id FROM diagnostico.source WHERE name=?));";
				String disease_symp = "INSERT INTO disease_symptom(disease_id,symptom_id) VALUES ((SELECT disease_id FROM diagnostico.disease WHERE name=?),?);";

				//Introducimos los datos en un ArrayList para poder separarlos e introducirlos en las tablas
				LinkedList<String> datos = readData();
				Iterator<String> it = datos.iterator();
				ArrayList<String> arr= new ArrayList<String>();
				while( it.hasNext()){
					arr.add(it.next());
				}// de while


				for (int i=0; i<arr.size(); i++){  // separacion de cada enfermedad

					String[] div1 = arr.get(i).split("=");   // division de nombre de enfermedad y codigo del resto 
					String[] div2 = div1[0].split(":"); // segunda division por nombre de enfermedad y codigo de enfermedad

					// Introducimos los datos de las enfermedades y le asignamos un codigo
					PreparedStatement pst1 = conn.prepareStatement(disease_data);
					pst1.setString(1, div2[0]);
					pst1.executeUpdate(); 

					String[] div3 = div2[1].split(";");  // division de cada uno de los codigos con sus dos valores

					for(int j=0; j<div3.length;j++){
						String[] div4 = div3[j].split("@");  // division del nombre del codigo y su lenguaje 


						//Introducimos los datos en la tabla source y le asignamos un codigo
						pst1 = conn.prepareStatement(source_data);
						pst1.setString(1,div4[1]);
						pst1.setString(2, div4[1]);
						pst1.executeUpdate();

						//Introducimos los datos en la tabla code 
						pst1 = conn.prepareStatement(code_data);
						pst1.setString(1,div4[0]);
						pst1.setString(2, div4[1]);
						pst1.executeUpdate();

						//Introducimos los datos en la tabla disease_code
						pst1 = conn.prepareStatement(disease_code_data);
						pst1.setString(1,div2[0]);
						pst1.setString(2, div4[0]);
						pst1.setString(3, div4[1]);
						pst1.executeUpdate();
					}// de for 

					String[] div5= div1[1].split(";"); // division de los distintos sintomas

					for(int k=0; k<div5.length; k++){
						String[]div6 = div5[k].split(":");

						//Introducimos los datos en la tabla symptom
						pst1 = conn.prepareStatement(sim_data);
						pst1.setString(1, div6[1]);
						pst1.setString(2, div6[0]);
						pst1.setString(3, div6[2]);
						pst1.setString(4, div6[1]);
						pst1.executeUpdate();

						//Introducimos los datos en la tabla disease_symptom
						pst1=conn.prepareStatement(disease_symp);
						pst1.setString(1,div2[0]);
						pst1.setString(2, div6[1]);
						pst1.executeUpdate();
					}// de for
					pst1.close();
				}// de for 

				pst.close();
				//Cierre de la transaccion 
				conn.commit();
				conn.setAutoCommit(true);
				creado=true;
			}// de if
			else{
				System.out.println("Base ya creada, puede realizar cualquier otra operacion");
				showMenu();
			}// de else 
		} catch (SQLException e) {
			System.err.println("Error SQL:" + e.getMessage());
		} catch (Exception e){
			System.err.println("Error al crear la BD:" + e.getMessage());
			e.printStackTrace();
		}// de try-catch
	}// de crearBD

	private void realizarDiagnostico() {
		try {
			if (conn.isClosed()){ conectar(); } // Comprobacion de conexion a la base de datos

			// Se imprimen los sintomas con su ID correspondiente
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM diagnostico.symptom;");
			while (rs.next()) { System.out.println("ID: " + rs.getString(1) + "   Sintoma: " + rs.getString(2));} // de while

			System.out.println("Por favor, seleccione el ID de los sÌntomas del paciente (para seleccion multiple separelos con comas");

			// Se leen los ID que inserta el usuario separados por comas
			String sintomas="";
			sintomas += readString();

			// Se separa el String recibido con comas como separador
			String[] sint = sintomas.split(",");

			// Se imprime error y se sale si no se escribe nada
			if (sint[0] == "") {
				System.err.println("No se ha recibido ningun ID.");
				return;
			}// de if 

			//Vamos creando la query conforma va introduciendo el cliente los datos

			String busqueda="SELECT name fROM disease WHERE disease_id IN(";
			for( int i=0; i< sint.length-1; i++){
				busqueda+="SELECT disease_id FROM disease_symptom WHERE symptom_id="+ '\"'+ sint[i]+ '\"'+" AND disease_id IN (";
			}// de for 
			busqueda+="SELECT disease_id FROM disease_symptom WHERE symptom_id="+'\"'+sint[sint.length-1]+'\"';
			for( int i=0; i< sint.length; i++){
				busqueda+= ")";
			}// de for 

			// Ejecutamos la query una vez todos los codigos de los sintomas introducidos 
			rs = st.executeQuery(busqueda );
			ArrayList <String> enf = new ArrayList <String> (); // sintomas de la enfermedad
			while (rs.next()){
				String name = rs.getString("name");
				enf.add(name); 
			}// de while 
			if (enf.size()==0) {
				System.out.println("Los sintomas introducidos no correspodnden con ninguna enfermedad");
				return;
			}// de if 
			System.out.println("las enfermedades que corresponden a los sintomas son: ");
			System.out.println ("NOMBRE ENFERMEDAD");
			for (int i = 0; i < enf.size(); i++){
				System.out.println (enf.get(i)); 
			}// de for 
			System.out.println("\n ");

		} catch (Exception e) {
			System.out.println("Ha surgido un error. Mas informacion: "+ e.getMessage());
		}// de catch
	}// de realizarDiagnostico

	private void listarSintomasEnfermedad() {

		try {
			if (conn.isClosed()){     conectar();  }// Comprobacion de conexion a la base de datos

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM disease");
			ArrayList <String> diseases = new ArrayList <String> ();// enfermedades en la base de datos 
			while (rs.next()){
				int id = rs.getInt("disease_id");
				String name = rs.getString("name");
				diseases.add(id + " " + name);
			}// de while 
			System.out.println("Las enfermedades en la base de datos son:" + "\n");
			for (int i = 0; i < diseases.size(); i++){
				System.out.println (diseases.get(i));// imprime todas las enfermedades
			}// de for 

			System.out.println("Introduce el ID de la enfermedad cuyos sintomas quiera consultar:" + "\n");
			int opcion=readInt();// integer para poder hacer el select del ID de la enfermedad utilizada por el cliente 
			if (opcion > 11 || opcion<=0){ // si el cliente introduce un ID que no es válido 
				System.out.println("El ID de la enfermedad no es valido; los ID validos comprenden los valores de 1 a 11.");
				listarSintomasEnfermedad(); //vuelve a empezar el metodo
			}// de if 

			else{
				rs = st.executeQuery("SELECT symptom.name FROM disease_symptom, symptom "
						+ "WHERE disease_symptom.symptom_id = symptom.cui AND disease_id = " + opcion);
				ArrayList <String> symptoms = new ArrayList <String> (); // sintomas de la enfermedad
				while (rs.next()){
					String name = rs.getString("name");
					symptoms.add(name); 
				}// de while 

				System.out.println("Los sintomas de la enfermedad escogida son : ");
				for (int i = 0; i < symptoms.size(); i++){
					System.out.println (symptoms.get(i)); 
				}// de for 
				System.out.println ('\n');
			}//de else
			//Cierre del Statement y el ResultSet
			st.close();
			rs.close();
		} catch (SQLException e) {
			System.err.println("Error SQL:" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error en la lectura por teclado");
			e.printStackTrace();
		}// de try-catch
	}// de listarSintomas

	private void listarEnfermedadesYCodigosAsociados() {

		System.out.println("Listado de enfermedades y codigos asociados a las mismas" + "\n");

		try {
			if (conn.isClosed()){     conectar();  } // Comprobacion de conexion a la base de datos

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT disease.name, code, source.name FROM disease, disease_code, source "
					+ "WHERE disease_code.disease_id= disease.disease_id AND disease_code.source_id= source.source_id  "
					+ "ORDER BY disease.name ASC");

			//Creacion de los ArrayLists donde se van a guardar los resultados del ResultSet
			ArrayList <String> nombre = new ArrayList <String> ();
			ArrayList <String> codigo = new ArrayList <String> ();
			ArrayList <String> nombre_s = new ArrayList <String> ();
			while (rs.next()){
				String name = rs.getString("disease.name");
				String code = rs.getString("code");
				String source_id = rs.getString("source.name");
				nombre.add(name);
				codigo.add(code);
				nombre_s.add(source_id);
			}// de while

			System.out.printf ("%-30s%-25s%-20s\n","NOMBRE ENFERMEDAD", "CODIGO", "TIPO CODIGO");
			System.out.println ('\n'); 

			//Se imprimen por pantalla los ArrayLists
			for (int i = 0; i <nombre.size(); i++){
				System.out.printf ("%-30s%-25s%-20s\n",nombre.get(i), codigo.get(i), nombre_s.get(i));
			}// de for 
			System.out.println ('\n');   

			//Cierre del Statement y el ResultSet
			st.close();
			rs.close();

		} catch (SQLException e) {
			System.err.println("Error SQL:" + e.getMessage());
			e.printStackTrace();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}// de try-catch

	}// de listarEnfermedadesYCodigosAsociados

	private void listarSintomasYTiposSemanticos() {

		System.out.println("Listado de sintomas y sus tipos sematicos" + "\n");

		try {
			if (conn.isClosed()){     conectar();  } // Comprobacion de conexion a la base de datos

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT name,st FROM symptom ORDER BY name ASC");

			//Creacion de los ArrayLists donde se van a guardar los resultados del ResultSet
			ArrayList <String> name = new ArrayList <String> ();
			ArrayList <String> sty = new ArrayList <String> ();
			while (rs.next()){
				String n = rs.getString("name");
				String tui = rs.getString("st");
				name.add (n); 
				sty.add (tui); }// de while

			//Se imprimen por pantalla los ArrayLists
			System.out.printf ("%-35s%-35s\n","NOMBRE SINTOMA","CODIGO SEMANTICO");
			for (int i = 0; i < name.size();i++){
				System.out.printf ("%-35s%-35s\n",name.get(i), sty.get(i));
			}// de for 
			System.out.println ('\n');

			//Cierre del Statement y el ResultSet
			st.close();
			rs.close();

		} catch (SQLException e) {
			System.err.println("Error SQL:" + e.getMessage());
			e.printStackTrace();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}// de try-catch
	}// de listarSintomasYTiposSemanticos


	private void mostrarEstadisticasBD() {

		System.out.println("Estadisticos de la base de datos" + "\n");

		try {
			if (conn.isClosed()){     conectar();  } // Comprobacion de conexion a la base de datos

			// Creacion de variables auxiliares que van a ser usadas para imprimir por pantalla los resultados de las Querys
			int auxI = 0;
			String auxS = "";

			//Primera query: contar las enfermedades de la tabla diagnostico
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM diagnostico.disease");
			while(rs.next()){ auxI= rs.getInt(1); }// de while
			System.out.println("Hay un total de " + auxI + " enfermedades");auxI=0;

			//Segunda query: contar los sintomas que hay en la base de datos 
			rs = st.executeQuery("SELECT COUNT(*) FROM diagnostico.symptom;");
			while(rs.next()){ auxS= rs.getString(1); }// de while
			System.out.println("Hay un total de " + auxS + " sintomas");auxS= "";


			//Tercera query: la enfermedad con mayor numero de sintomas
			rs = st.executeQuery("SELECT disease.name, COUNT(*) FROM disease_symptom, disease, symptom"
					+ " WHERE disease_symptom.symptom_id = symptom.cui AND disease.disease_id = disease_symptom.disease_id "
					+ "GROUP BY disease.name "
					+ "ORDER BY COUNT(*) DESC LIMIT 1;");
			while(rs.next()){ auxS= rs.getString(1);
			auxI = rs.getInt(2);} // de while
			System.out.println("La enfermedad con mas sintomas es " + auxS + " la cual tiene " + auxI + " sintomas");auxS= "";auxI=0;

			//Cuarta query: la enfermedad con menor numero de sintomas, hay 2 comprobadas por Workbench por lo que el límite es 2
			rs = st.executeQuery("SELECT disease.name, COUNT(*) FROM disease_symptom, disease, symptom"
					+ " WHERE disease_symptom.symptom_id = symptom.cui AND disease.disease_id = disease_symptom.disease_id "
					+ "GROUP BY disease.name "
					+ "ORDER BY COUNT(*) ASC LIMIT 2;");
			while(rs.next()){  auxS =  auxS + rs.getString(1) + " y ";
			auxI = rs.getInt(2);} // de while
			System.out.println("Las enfermedades con menos sintomas son " + auxS +" las cuales tienen " + auxI + " sintomas"); auxS= ""; auxI=0;

			//Quinta query: numero medio de sintomas por enfermedad -> Prueba con group by
			rs = st.executeQuery("SELECT AVG (total) FROM (SELECT disease_symptom.disease_id, COUNT(symptom_id) AS total "
					+ "FROM disease_symptom, disease, symptom "
					+ "WHERE disease_symptom.symptom_id = symptom.cui AND disease.disease_id = disease_symptom.disease_id "
					+ "GROUP BY disease_symptom.disease_id)AS media ");
			while(rs.next()){ auxI= rs.getInt(1); }// de while
			System.out.println("El numero medio de sintomas : " + auxI); auxI=0;

			//Sexta query: la enfermedad con mayor numero de sintomas
			rs = st.executeQuery("SELECT distinct st FROM symptom;"); // No se tienen en cuenta cuando aparecen repetidos, solo los distintos
			while(rs.next()){     auxS= auxS + ", " + rs.getString(1);  }// DE WHILE
			System.out.println("Tipos de semantic_type en la base de datos: " + auxS); auxS="";

			//Septima query: numero de sintomas por tipo semantico
			rs = st.executeQuery("SELECT st, COUNT(name) FROM symptom GROUP BY st");
			ArrayList<String> seman_type= new ArrayList<String>();
			ArrayList<Integer> seman_num= new ArrayList<Integer>();
			while(rs.next()){
				auxS = rs.getString(1);
				auxI= rs.getInt(2);
				seman_type.add(auxS);
				seman_num.add(auxI);
			}// de while 
			System.out.println("Numero de sintomas por cada semantic_type : " );
			System.out.printf ("%-35s%-35s\n","Tipo semantico","Numero de sintomas");
			for( int i= 0; i<seman_type.size();i++){
				System.out.printf("%-35s%-35s\n",seman_type.get(i),seman_num.get(i));
			}// de for 

			//Cierre del Statement y el ResultSet
			st.close();
			rs.close();
		} catch (SQLException e) {
			System.err.println("Error SQL:" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}// de try-catch
	}// de mostrarEstadisticasBD

	/**
	 * MÔøΩtodo para leer nÔøΩmeros enteros de teclado.
	 * 
	 * @return Devuelve el nÔøΩmero leÔøΩdo.
	 * @throws Exception
	 *             Puede lanzar excepciÔøΩn.
	 */
	private int readInt() throws Exception {
		try {
			System.out.print("> ");
			return Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
		} catch (Exception e) {
			throw new Exception("Not number");
		}// de try-catch
	}// de readInt

	/**
	 * MÔøΩtodo para leer cadenas de teclado.
	 * 
	 * @return Devuelve la cadena leÔøΩda.
	 * @throws Exception
	 *             Puede lanzar excepciÔøΩn.
	 */
	private String readString() throws Exception {
		try {
			System.out.print("> ");
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception e) {
			throw new Exception("Error reading line");
		}// de try-catch
	}// de readString

	/**
	 * MÔøΩtodo para leer el fichero que contiene los datos.
	 * 
	 * @return Devuelve una lista de String con el contenido.
	 * @throws Exception
	 *             Puede lanzar excepciÔøΩn.
	 */
	private LinkedList<String> readData() throws Exception {
		LinkedList<String> data = new LinkedList<String>();
		BufferedReader bL = new BufferedReader(new FileReader(DATAFILE));
		while (bL.ready()) {
			data.add(bL.readLine());
		}// de while
		bL.close();
		return data;
	}// de readData

	public static void main(String args[]) {
		new Diagnostico().showMenu();
	}// de main
}// de Diagnostico