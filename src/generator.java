import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;


import io.github.galbiston.geosparql_jena.configuration.GeoSPARQLOperations;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.lang.Math;

//Comment to check if push works (Eric)


public class generator {
	public static int time_big= 10; 
	public static int frequency= 2; 
	
	public static ArrayList<String> listtt=new ArrayList<String>();  
	static String SOURCE = "http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6";
    static String NS = SOURCE + "#";
    static String Scor= "http://purl.org/eis/vocab/scor#";
    public static int S_Tiers; 
    public static int C_Tiers;  
    static ArrayList<Integer> supplier_tiers ;
    static ArrayList<Integer> customer_tiers ;
    public static 	 HashMap<String, ArrayList<Integer>> hash_map ;
	public static HashMap<String, ArrayList<Integer>> hash_map_customer ;
	public static HashMap<String,ArrayList<String>> sc_uniques_final ;
	
	
	private static String Prefix;

	public static BufferedWriter out = null;
	public static BufferedWriter out2 = null;
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		
		Prefix= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"Prefix : <http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#>"; 
		hash_map = new HashMap<String, ArrayList<Integer>>();
		hash_map_customer = new HashMap<String, ArrayList<Integer>>();
		sc_uniques_final = new HashMap<String, ArrayList<String>>();

		File file = new File("C:\\Users\\Ramzy\\Desktop\\datagenerator\\configurationfile.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		out = new BufferedWriter (new FileWriter("C:/Users/Ramzy/Desktop/datagenerator/july_out.ttl"));
		out2 = new BufferedWriter (new FileWriter("C:/Users/Ramzy/Desktop/datagenerator/temp_out.ttl"));
	 OntModel model = ModelFactory.createOntologyModel();
	model.read("C:/Users/Ramzy/Desktop/datagenerator/generator.owl");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		read_parameters(br, model);
        OntClass Supplier_Node = model.getOntClass( NS + "Supplier");
        OntClass Customer_Node = model.getOntClass( NS + "Customer");
        OntClass OEM_Node = model.getOntClass( NS + "OEM" );
        Property inventoryLevel = model.getProperty(NS+"hasInventoryLevel");
        Property hasTransportMode = model.getProperty(NS+"hasTransportMode");
        Individual oem = model.createIndividual( NS+"OEM1", OEM_Node);
        oem.addProperty(inventoryLevel,
  				getRandomValue(100,200));
        Property hasLeadTime=  model.getProperty(NS+"hasLeadTime");
		oem.addProperty(hasLeadTime, "3");
		List<String> transports= new ArrayList<String>(); 
		transports.add("vehicle");
		transports.add("maritime");
		transports.add("air");
		for (int m=0; m<= (int)(3.0 * Math.random()); m++)
		{
			oem.addProperty(hasTransportMode, transports.get(m));
		}
		  Property hasLongitude  = model.getProperty(NS+"hasLongitude");
		  Property hasLatitude   = model.getProperty(NS+"hasLatitude");
		  oem.addProperty(hasLongitude,getRandomValue(70, 75)); 
		  oem.addProperty(hasLatitude, getRandomValue(70,75));
	
        //////////////////////////////////////////create supplier
        Property hasUpStreamTier = model.getProperty(NS+"hasUpStreamTier");
        Property hasUpStreamNode  = model.getProperty(NS+"hasUpStreamNode");
        Property node_oem  = model.getProperty(NS+"hasOEM");
    	
		create_tiers_nodes (hash_map.get("SupplierTier").get(0), hash_map.get("SupplierNodePerTier"), Supplier_Node, reader, model);
        create_relations(hash_map.get("SupplierTier").get(0),hash_map.get("SupplierNodePerTier"), oem, hasUpStreamTier, hasUpStreamNode, node_oem, Supplier_Node, model); 
        //////////////////////////////////////////create oem 
        Property hasDownStreamTier = model.getProperty(NS+"hasDownStreamTier");
        Property hasDownStreamNode  = model.getProperty(NS+"hasDownStreamNode");
        Property oem_node  = model.getProperty(NS+"OEMhasNode");
        ////////////////////////////////////////create customer 
        create_tiers_nodes ( hash_map_customer.get("CustomerTier").get(0), hash_map_customer.get("CustomerNodePerTier"), Customer_Node, reader,model);
        create_relations(hash_map_customer.get("CustomerTier").get(0),hash_map_customer.get("CustomerNodePerTier"),oem,hasDownStreamTier, hasDownStreamNode,oem_node, Customer_Node, model); 
        //replace_byUI(model); 
        OntModel copyOfOntModel  = ModelFactory.createOntologyModel(model.getSpecification()) ; 
	    copyOfOntModel.add( model.getBaseModel() );
	   System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size()); 
	    create_raw_material(model,100,200, null );
        createMaterialFlow(model, null);
		create_capacity(model);
		System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
        /////////////////////////////////////////////////////Evaluation 
	/*  String s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\evaluation queries\\CQ15.rq";
	    List <QuerySolution> l= execute_query(s, model);
	    print_results(l, 1);*/
        /////////////////////////////////////////////////////
	    generation(model); 
	    allocation(model); 
	    allocation_KPI(model,1);
	    //String portfolios = "Select * where {?order :hasQuantity ?q. ?order :hasDeliveryTime ?d.    ?order :hasPortfolio ?p. <<?p :needsNode ?node>>  ?z ?f. }"; 
	   // print_results(execute(Prefix+portfolios,model), 1); 
	    
      /*  optimize_calculate_KPIs (model, null);
        allocation_process_byCustomer(model,true); 
        model.write(out,"Turtle"); 
        allocation_KPI(model,1);
        /////////////////////////////////////////////////////backup model
        /////////////////////////////////////////////////////
        System.out.println("Disruption///////////////////////////////////");
        System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
        model.write(out, "TURTLE");
		HashMap<String,ArrayList<String>> disrupted_nodes= disruption(model, sc_uniques_final,"tier_local", copyOfOntModel); 
		System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
		model.write(out2, "TURTLE");
        optimize_calculate_KPIs (model, disrupted_nodes);
        XSSFWorkbook workbook = new XSSFWorkbook();
	    XSSFSheet sheet = workbook.createSheet("KPIs");
        print_excel(workbook, sheet); 
        allocation_process_byCustomer(model,true); 
        allocation_KPI(model,2);*/
      String adjust= 	Prefix+ "DELETE   \r\n" + 
				"{ ?capacity :hasQuantity \"0\"}\r\n" + 
				"Insert { ?capacity :hasQuantity \"3\"}\r\n" + 
				"where \r\n" + 
				"{ ?capacity :hasQuantity \"0\"}\r\n" ;
      UpdateAction.parseExecute(adjust, model) ;
      model.write(out, "TURTLE");
   //     model.write(out, "RDF/XML");
      
       
      out.close();
	}
	
	private static void allocation(OntModel model) {
	
		String get_oem_leadtime= Prefix+" Select * where { :OEM1 :hasLeadTime ?lt .}"; 
		List<QuerySolution> oem_leadtimes= execute(get_oem_leadtime,model); 
		String oem_leadtime= oem_leadtimes.get(0).get("lt").toString(); 
		
		
		for (int t=0; t<time_big; t++)
		{
			String qq= Prefix+"Select * where { "
					+ "?order :hasDeliveryTime \""+t+"\". "
					+"?order :hasQuantity ?q. "
					+"?order :hasProduct ?p. "
					+ "} "; 
			List<QuerySolution> orders= execute (qq,model);
			print_results(orders,1); 
			System.out.println("Time"+t); 
			maintain_capacity_inventory(t,model); 
			String q= Prefix+"Select * where { "
					//+ "?order a :Order. "
					+ "?order :hasDeliveryTime ?time. "
					+"?order :hasQuantity ?q. "
					+"?order :hasProduct ?p. "
					+ "?customer :makes ?order. "
					+ "?customer :hasPriority ?priority. "
					+ "Filter ((xsd:integer(?time)-xsd:integer("+oem_leadtime+"))= xsd:integer("+t+"))"
					+ "} order by desc (?priority)"; 
			 orders= execute (q,model);
			if (orders.size()==0)
				{
				 q= Prefix+"Select * where { "
						+ "?order :hasDeliveryTime \""+t+"\". "
						+"?order :hasQuantity ?q. "
						+"?order :hasProduct ?p. "
						+ "} "; 
				 orders= execute (q,model);
					for (int j=0; j<orders.size(); j++)
					{
						String order= orders.get(j).get("order").toString();
						update_order_fulfilled(order, "false", model); // order not fulfilled;
						
					}
				}
			else
			{
			for (int o=0; o<orders.size(); o++)
			{
				String order= orders.get(o).get("order").toString();
				String quantity= orders.get(o).get("q").toString();
				String product= orders.get(o).get("p").toString();
			int oem_alloc= check_oem_inventory(orders.get(o), t, model); 
			OntClass portfolio_class = model.getOntClass( NS + "Portfolio");
			 Individual portfolio = model.createIndividual( NS+"Portfolio"+RandomStringUtils.randomAlphanumeric(8), portfolio_class);
			 Individual order_ind= model.getIndividual(order); 
			
			Property hasPortfolio = model.getProperty(NS+"hasPortfolio");
			order_ind.addProperty(hasPortfolio,portfolio);
			String query= Prefix+ "SELECT ?inv ?q ?p\r\n" + 
					"	WHERE {  :OEM1 :hasInventory ?inv. ?inv :hasTimeStamp \""+t+"\". ?inv :hasQuantity ?q.  ?inv :hasPrice ?p.}"; 
			List<QuerySolution> solutions= execute (query,model); 
			String price= solutions.get(0).get("p").toString(); 
			
			if (oem_alloc==0)
			{
				update_order_fulfilled(order, "true", model); // order fulfilled;
				System.out.println("Order"+order+" fulfilled");
				create_order_portfolio(model, "OEM1", portfolio, quantity,t, product,price, order); 
				create_inventory_increase(model,t,"OEM1",10);
			}
			else // need to allocate suppliers with quantity = return from oem check 
			{
				String qq1=(Integer.parseInt(quantity)-oem_alloc) +""; 
				if (!qq1.equals("0")) {
					create_order_portfolio(model, "OEM1", portfolio, qq1,t, product,price, order); 
					}
				boolean flag= allocate(product, oem_alloc, model, t,portfolio,order); 
				System.out.println("Order"+order+flag);
				update_order_fulfilled(order, flag+"", model); // order fulfilled;
				if (flag== false)
				{
					String delete= 			Prefix+ 		"DELETE   \r\n" + 
					"{<"+order +"> :hasPortfolio ?p.  <"+order+"> :hasOriginalQuantity ?q. <"+order+"> :hasTotalOriginalPrice ?price}\r\n" +
					"where \r\n" + 
					"{ <"+order +"> :hasPortfolio ?p}\r\n" ; 
			UpdateAction.parseExecute(delete, model) ;
			
				}
				
			}
			get_protfolio_quantity(model,portfolio.toString().split("#")[1], order ); 
			get_protfolio_price(model,portfolio.toString().split("#")[1], order ); 
			
			}
			}
			create_inventory_increase(model,t,"OEM1", 0); 
		
		}
		// TODO Auto-generated method stub
		
	}
	private static void create_order_portfolio(OntModel model, String node,Individual portfolio,String quantity,  int time, String product, String price, String order) {
		Property needsNode = model.getProperty(NS+"needsNode");
		Property getsProduct = model.getProperty(NS+"getsProduct");
		Property hasTime = model.getProperty(NS+"hasTimeStamp");
		Property hasQuantity = model.getProperty(NS+"hasQuantity");
		Property hasUnitPrice = model.getProperty(NS+"hasUnitPrice");
			Statement S= model.createStatement(portfolio, needsNode,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#"+node)); 
			Resource r = model.createResource(S);
			r.addProperty(getsProduct, product); 
			r.addProperty(hasTime,time+"");
			r.addProperty(hasQuantity,quantity);
			r.addProperty(hasUnitPrice,price);
			
			
		
	}
	
private static void get_protfolio_price(OntModel model, String portfolio, String order)
	
{	
	String get_price= Prefix+ "SELECT * WHERE{ :"+order.split("#")[1]+" :hasPortfolio :"+portfolio+" . <<:"+portfolio+" :needsNode ?node>> :hasQuantity ?q.  <<:"+portfolio+" :needsNode ?node>> :hasUnitPrice ?p.} "; 
	List<QuerySolution> prices= execute(get_price,model);
	float total_price= 0; 
	for (int price=0; price<prices.size(); price++)
	{
		if (prices.get(price).get("?q").toString().contains("float"))
		{
			total_price= total_price+ (Float.parseFloat(prices.get(price).get("?q").toString().split("\\^")[0])*
					Integer.parseInt(prices.get(price).get("?p").toString().split("\\^")[0]));
		}
		else
		{
			total_price= total_price+ (Integer.parseInt(prices.get(price).get("?q").toString().split("\\^")[0])*
					Integer.parseInt(prices.get(price).get("?p").toString().split("\\^")[0]));
		}
		 
	}
	String query= Prefix+ "INSERT { :"+order.split("#")[1]+" :hasTotalOriginalPrice "+total_price+"} WHERE { }"; 
	UpdateAction.parseExecute(query, model) ;
	}
private static void get_protfolio_quantity(OntModel model,String port, String order)

{	
	String get_price= Prefix+ "SELECT * WHERE{ :"+order.split("#")[1]+" :hasPortfolio :"+port+" . << :"+port+" :needsNode ?node>> :hasQuantity ?q. } "; 
	
	List<QuerySolution> prices= execute(get_price,model);
	print_results(prices,1); 
	
	float total_quantity= 0; 
	for (int price=0; price<prices.size(); price++)
	{
		if (prices.get(price).get("?q").toString().contains("float"))
		{
		total_quantity= total_quantity+ (Float.parseFloat(prices.get(price).get("?q").toString().split("\\^")[0])); 
		}
		else
		{
			total_quantity= total_quantity+ (Integer.parseInt(prices.get(price).get("?q").toString()));
		}
	}
	String query= Prefix+ "INSERT { :"+order.split("#")[1]+" :hasOriginalQuantity "+total_quantity+"} WHERE { }"; 
	UpdateAction.parseExecute(query, model) ;
	System.out.print(total_quantity);
	
}
	

	private static void maintain_capacity_inventory(int t, OntModel model) {
		int j=t-1; 
		String test= Prefix+ "Select * "+ "where \r\n" + 
				"{?supplier :hasCapacity ?cap. ?supplier :hasCapacitySaturation ?max.\r\n" + 
				"?cap :hasProduct ?p.\r\n" + 
				"?cap :hasQuantity ?quantity.\r\n" + 
				"?cap :hasPrice ?price.\r\n" + 
				"?cap :hasTimeStamp \""+j+"\"."
				+ "}\r\n" ; 
		List<QuerySolution> ff= execute (test, model); 
	//	print_results(ff,1); 
		Property has_product = model.getProperty(NS+"hasProduct");
		Property time = model.getProperty(NS+"hasTimeStamp");
		OntClass capacity_class = model.getOntClass( NS + "Capacity");
		Property hasCapacity = model.getProperty( NS + "hasCapacity");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
	 for (int i=0; i<ff.size(); i++)
	 {
		// String supplier= ff.get(i).get("supplier").toString().split("#")[1]; 
		 String y=  ff.get(i).get("quantity").toString().split("\\^")[0] ; 
		 
		 Individual capacity = model.createIndividual( NS+"Capacity"+RandomStringUtils.randomAlphanumeric(8), capacity_class);
		 capacity.addProperty(has_product, ff.get(i).get("p").toString()); 
		 capacity.addProperty(time, t+""); 
		 capacity.addProperty(has_quantity,y); 
		 capacity.addProperty(has_price,ff.get(i).get("price").toString()); 
		Individual supplierr = model.getIndividual(ff.get(i).get("supplier").toString());
			supplierr.addProperty(hasCapacity, capacity);
			
	 }
	 test= Prefix+ "Select * "+ "where \r\n" + 
				"{?supplier :hasCapacity ?cap.\r\n" + 
				"?cap :hasProduct ?p.\r\n" + 
				"?cap :hasQuantity ?quantity.\r\n" + 
				"?cap :hasTimeStamp \""+t+"\"."
				+ "}\r\n" ; 
		ff= execute (test, model); 
	//	print_results(ff,1); 
		
		
	}

	private static void update_order_fulfilled(String order, String w, OntModel model) {
		String 	q= Prefix+ 
				/*"DELETE   \r\n" + 
				"{<"+order +"> :isOrderFulfilled ?x. }\r\n" + */
				"Insert {<"+order +"> :isOrderFulfilled '"+w+"' }\r\n" + 
				"where \r\n" + 
				"{ <"+order +"> a :Order}\r\n" ; 
		UpdateAction.parseExecute(q, model) ;
		
	}

	private static int check_oem_inventory(QuerySolution order, int t, OntModel model) {
		String product=order.get("p").toString().split("#")[1];
		int quantity=Integer.parseInt(order.get("q").toString());
		
		String query= Prefix+ "SELECT ?node ?time ?inv ?q \r\n" + 
				"	WHERE { ?node a :OEM. ?node :hasInventory ?inv. ?inv :hasProduct ?p. "
				+ "?inv :hasTimeStamp \""+t+"\". ?inv :hasQuantity ?q. "
				+ "Filter(regex(str(?p),\""+product+"\"))}"; 
		List <QuerySolution> oem_inventory= execute(query,model); 
		int oem_q= Integer.parseInt(oem_inventory.get(0).get("q").toString());
		if (oem_q>= quantity)
		{
			int neww= oem_q- quantity; 
			 query = Prefix+"DELETE   \r\n" + 
					"{ ?inv :hasQuantity ?q. }\r\n" + 
					"Insert {?inv :hasQuantity \""+neww+"\".}\r\n" + 
					"where \r\n" + 
					"{ ?node a :OEM. "
					+ "?node :hasInventory ?inv. "
					+ "?inv :hasProduct ?p. \r\n" + 
					"?inv :hasTimeStamp \""+t+"\". "
					+ "?inv :hasQuantity ?q. }";  
			UpdateAction.parseExecute(query, model) ;
			System.out.println("at time"+t+"OEM was: "+oem_q+"now at"+ (oem_q- quantity)); 
		return 0; 
		}
		else 
		{
				int neww= 0; 
				 query = Prefix+"DELETE   \r\n" + 
						"{ ?inv :hasQuantity ?q. }\r\n" + 
						"Insert {?inv :hasQuantity \""+neww+"\".}\r\n" + 
						"where \r\n" + 
						"{ ?node a :OEM. "
						+ "?node :hasInventory ?inv. "
						+ "?inv :hasProduct ?p. \r\n" + 
						"?inv :hasTimeStamp \""+t+"\". "
						+ "?inv :hasQuantity ?q. }";  
				UpdateAction.parseExecute(query, model) ;
				System.out.println("at time"+t+"OEM was: "+oem_q+"now at"+ (oem_q- quantity)); 
			return quantity-oem_q; 
		}
			
	}

	private static void generation(OntModel model) {
		//create_product(model); 
		try {
			create_read_products(model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		int x= create_orders(model); 
		create_capacity_saturation(model); 
		create_initial_Capacity(model); 
		create_initial_Inventory(model); 
		//create_inventory_increase(model); 
		// TODO Auto-generated method stub
		System.out.println("OrderCount"+ x); 
	}

	 

	private static void create_initial_Capacity(OntModel model) {
		String query= Prefix+ "SELECT DISTINCT ?subject ?product \r\n" + 
				"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. ?subject :manufactures ?product.}"; 
		List<QuerySolution> suppliers= execute (query,model); 
		Property time = model.getProperty(NS+"hasTimeStamp");
		OntClass capacity_class = model.getOntClass( NS + "Capacity");
		Property hasCapacity = model.getProperty( NS + "hasCapacity");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
	
		for (int i=0; i<suppliers.size();i++)
	      {
			Individual capacity = model.createIndividual( NS+"Capacity"+RandomStringUtils.randomAlphanumeric(8), capacity_class);
			Individual supplier = model.getIndividual(suppliers.get(i).get("subject").toString());
			supplier.addProperty(hasCapacity, capacity);
			capacity.addProperty(has_product, suppliers.get(i).get("product").toString()); 
			capacity.addProperty(time, "0"); 
			capacity.addProperty(has_quantity,"0"); 
			capacity.addProperty(has_price,getRandomValue(1,10)); 
			
	      }
	}
	private static String create_inventory_increase(OntModel model, int t, String node, int increase) {
		Property time = model.getProperty(NS+"hasTimeStamp");
		Property hasInventory = model.getProperty(NS +"hasInventory");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
		Individual product = model.getIndividual(NS+"ProductA");
		String query= Prefix+ "SELECT ?inv ?q ?p\r\n" + 
				"	WHERE {  :"+node+" :hasInventory ?inv. ?inv :hasTimeStamp \""+t+"\". ?inv :hasQuantity ?q.  ?inv :hasPrice ?p.}"; 
		List<QuerySolution> solutions= execute (query,model); 
		OntClass inventory_class = model.getOntClass(NS+"Inventory");
		Individual inv = model.createIndividual( NS+"Inventory"+RandomStringUtils.randomAlphanumeric(8), inventory_class);
		Individual supplier = model.getIndividual(NS+node);
		supplier.addProperty(hasInventory, inv);
		inv.addProperty(has_product, product); 
		
		int j=t; 
		j=j+1; 
		inv.addProperty(time,j+""); 
		int f= Integer.parseInt(solutions.get(0).get("q").toString());
		int l=f+increase; 
		System.out.println("Time"+j+" quantity"+l); 
		inv.addProperty(has_quantity, l+""); 
		inv.addProperty(has_price, solutions.get(0).get("p").toString()); 

		return solutions.get(0).get("p").toString(); 
	}
	private static void create_initial_Inventory(OntModel model) {
		String query= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. }"; 
		List<QuerySolution> suppliers= execute (query,model); 
		Property time = model.getProperty(NS+"hasTimeStamp");
		OntClass inventory_class = model.getOntClass(NS+"Inventory");
		Property hasInventory = model.getProperty(NS+"hasInventory");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
		Individual product = model.getIndividual(NS+"ProductA");
		for (int i=0; i<suppliers.size();i++)
	      {
			Individual inv = model.createIndividual( NS+"Inventory"+RandomStringUtils.randomAlphanumeric(8), inventory_class);
			Individual supplier = model.getIndividual(suppliers.get(i).get("subject").toString());
			supplier.addProperty(hasInventory, inv);
			inv.addProperty(has_product, product); 
			inv.addProperty(time, "0"); 
			inv.addProperty(has_quantity,getRandomValue(1,10)); 
			inv.addProperty(has_price,getRandomValue(1,10)); 
			
	      }
		 Individual oem = model.getIndividual( NS+"OEM1");
		 Individual inv = model.createIndividual( NS+"Inventory"+RandomStringUtils.randomAlphanumeric(8), inventory_class);
			inv.addProperty(has_product, product); 
			inv.addProperty(time, "0"); 
			inv.addProperty(has_quantity, "10");
			inv.addProperty(has_price,getRandomValue(10,50));
			oem.addProperty(hasInventory, inv);
	  
	}
	

	private static void create_capacity_saturation(OntModel model) {
		
		String query= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. }"; 
		List<QuerySolution> suppliers= execute (query,model); 
		Property capacity_saturation = model.getProperty(NS+"hasCapacitySaturation");
		for (int i=0; i<suppliers.size();i++)
	      {
			Individual supplier = model.getIndividual(suppliers.get(i).get("subject").toString());
			supplier.addProperty(capacity_saturation, getRandomValue(100,1000));
			//supplier.addProperty(capacity_saturation, 3+"");
	      }
		
	}

	private static void create_product(OntModel model) {
		OntClass product = model.getOntClass( NS + "Product" );
		Individual order_ind = model.createIndividual( NS+"ProductA", product);
	  	
	}

	private static int create_orders(OntModel model ) {
		String query= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Customer. ?subject :belongsToTier :CustomerTier1. ?customer :hasPriority ?p} ORDER  BY desc(?p)  "; 
		List<QuerySolution> customers= execute (query,model); 
		Property makes = model.getProperty(NS+"makes");
		OntClass order = model.getOntClass( NS + "Order" );
		Property time = model.getProperty(NS+"hasDeliveryTime");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property hasPriority = model.getProperty(NS+"hasPriority");
		int order_count=0; 
		for (int i=0; i<customers.size();i++)
	      {
			Individual customer = model.getIndividual(customers.get(i).get("subject").toString());
			int g=i+1; 
			customer.addProperty(hasPriority,g+""); 
			Individual product = model.getIndividual(NS+"ProductA");
			String p= customers.get(i).get("p").toString(); 
			int c=0; 
			if (p.contains("4")|| p.contains("3")) c=3; 
			for (int t=6; t<time_big+6+c; t++)
			{
				//System.out.println("Customer: "+customer+ " time: "+ t+" OrderCount: "+ order_count);
			Individual order_ind = model.createIndividual( NS+"Order"+RandomStringUtils.randomAlphanumeric(8), order);
	        customer.addProperty(makes,order_ind);
	        order_ind.addProperty(time,t+""); 
	        t=t+frequency; 
	        order_ind.addProperty(has_product, product); 
	        order_ind.addProperty(has_quantity, getRandomValue(1,10)); 
	        order_count++; 
	        }
		}
		System.out.println("order count"+ order_count);
		return order_count; 
	}

	private static LinkedHashMap<String, ArrayList<String>> disruption(OntModel model, HashMap<String, ArrayList<String>> sc_uniques_final2, String disruption, OntModel copyOfOntModel) {
		LinkedHashMap<String, ArrayList<String>>  disrupted_nodes= new  LinkedHashMap<String, ArrayList<String>> (); 
		if (disruption.contains("total"))
		{
			disrupted_nodes= total_node_disruption( model,  sc_uniques_final2,  disruption); 
		}
		if (disruption.contains("partial"))
		{}
		if (disruption.contains("tier"))
		{
			disrupted_nodes= tier_disruption( model,  sc_uniques_final2,  disruption);
		}
		if (disruption.contains("material"))
		{
			disrupted_nodes=material_disruption( model,  sc_uniques_final2,  disruption,copyOfOntModel );
		}
	
		return disrupted_nodes;
	}

	private static LinkedHashMap<String, ArrayList<String>> material_disruption(OntModel model,
			HashMap<String, ArrayList<String>> sc_uniques_final2, String disruption, OntModel copyOfOntModel) {
		
		if (disruption.equals("material_local"))
		{
			System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
			model.removeAll();
	        model.add(copyOfOntModel.getBaseModel()); 
	        System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
	        create_raw_material(model,50,100, null);
	        String node= "SupplierNode1.3"; 
	        ArrayList<String> nodes= new ArrayList<String>(); 
	        nodes.add(node); 
	        createMaterialFlow(model,nodes);
	        String kn= "Select ?node ?input ?inq where { << ?node :hasInputComponent ?input>> :hasComponentQuantity ?inq. ?node a :Customer.}"; 
			 List<QuerySolution>knn = execute(Prefix+kn,model); 
	       create_capacity(model);
	        System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size()); 
		}
		if (disruption.equals("material_geo"))
		{
			String get_nodes_loc= "Select ?node ?tier where { ?node :hasLocation ?loc. ?node :belongsToTier ?tier. ?node a :Supplier. Filter (xsd:integer(?loc)<200 && xsd:integer(?loc)>160)} order by desc(?tier)"; 	
			List<QuerySolution>	ll = execute (Prefix+get_nodes_loc, model);
			ArrayList<String> nodes= new ArrayList<String>(); 
	        
			for (int i=0; i<ll.size(); i++)
			{
				nodes.add(ll.get(i).get("?node").toString().split("#")[1]); 
			}
			model.removeAll();
	        model.add(copyOfOntModel.getBaseModel()); 
	        create_raw_material(model,50,100, null);
			createMaterialFlow(model, nodes);
		     create_capacity(model);
		     System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size()); 
		}
		if (disruption.equals("material_tier"))
		{ // to ask 
	        model.removeAll();
	        model.add(copyOfOntModel.getBaseModel()); 
	        create_raw_material(model,1,50, "SupplierNode1.4");
	        createMaterialFlow(model,null);
			create_capacity(model);
			System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size()); 
		}
		
		return null;
	}

	private static LinkedHashMap<String, ArrayList<String>> tier_disruption(OntModel model,
			HashMap<String, ArrayList<String>> sc_uniques_final2, String disruption) {
		LinkedHashMap<String, ArrayList<String>>  disrupted_nodes= new  LinkedHashMap<String, ArrayList<String>> ();
		if (disruption.equals("tier_local"))
		{
		ArrayList <String> temp= new ArrayList<String>(); 
		temp.add(sc_uniques_final.get("Reliability").get(0)); 
		temp.add(sc_uniques_final.get("Reliability").get(3)); 
		 for (int i=0; i<2; i++)
		 {
			 	String node= temp.get(i); 
				String get_tier_group= "Select ?tier ?group where { :"+node+" :belongsToTier ?tier.  :"+node+" :hasGroup ?group. }"; 
				List<QuerySolution>	l = execute (Prefix+get_tier_group, model);
				String tier = l.get(0).get("?tier").toString().split("#")[1];
				String group= l.get(0).get("?group").toString();
				ArrayList<String> info2= new ArrayList<String>(); 
				info2.add(tier); 
				info2.add(group); 
				disrupted_nodes.put(node,info2);
		 }
		 delete_relation (temp.get(0),temp.get(1), model); 
		disrupted_nodes.put("Relation",null);
		}
		if (disruption.equals("tier_geo"))
		{
			String get_nodes_loc= "Select ?node ?node2 ?x ?tier ?tier2 ?group ?group2 where { ?node :hasLocation ?loc. ?node :belongsToTier ?tier ?node :hasGroup ?group. "
					+ "?node ?x ?node2. ?node2 :hasLocation ?loc2. ?node2 :belongsToTier ?tier2. ?node2 :hasGroup ?group2."
					+ "Filter ( regex(str(?x),\"UpStream\") ||  regex(str(?x),\"DownStream\") && xsd:integer(?loc)<200 && xsd:integer(?loc)>100 && xsd:integer(?loc2)<300 && xsd:integer(?loc)>200)}"; 	
			List<QuerySolution>	ll = execute (Prefix+get_nodes_loc, model);
			for (int i=0; i<ll.size(); i++)
			{
				String node= ll.get(i).get("?node").toString().split("#")[1];
				String tier = ll.get(i).get("?tier").toString().split("#")[1];
				String group= ll.get(i).get("?group").toString();
				ArrayList<String> info= new ArrayList<String>(); 
				info.add(tier); 
				info.add(group); 
				disrupted_nodes.put(node,info);
				String node2= ll.get(i).get("?node2").toString().split("#")[1];
				String tier2 = ll.get(i).get("?tier").toString().split("#")[1];
				String group2= ll.get(i).get("?group").toString();
				ArrayList<String> info2= new ArrayList<String>(); 
				info2.add(tier2); 
				info2.add(group2);
				disrupted_nodes.put(node2,info2);
				String relation= ll.get(i).get("x").toString(); 
				if (relation.contains("Up"))
				{
				delete_relation(node, node2, model); 
				}
				else  delete_relation (node2, node, model); 
			}
			disrupted_nodes.put("Relation",null);
		}
		if (disruption.equals("tier_tier"))
		{
			// need to ask 
		}
		return disrupted_nodes;
	}

	private static LinkedHashMap<String, ArrayList<String>> total_node_disruption(OntModel model,HashMap<String, ArrayList<String>> sc_uniques_final2, String disruption) {
		LinkedHashMap<String, ArrayList<String>>  disrupted_nodes= new  LinkedHashMap<String, ArrayList<String>> ();
		if (disruption.equals("total_local"))
		{
		//	String node=  sc_uniques_final2.get("Reliability").get(0);
			String node=  "SupplierNode4.3";
			String get_tier_group= "Select ?tier ?group where { :"+node+" :belongsToTier ?tier.  :"+node+" :hasGroup ?group. }"; 
			List<QuerySolution>	l = execute (Prefix+get_tier_group, model);
			String tier = l.get(0).get("?tier").toString().split("#")[1];
			String group= l.get(0).get("?group").toString();
			ArrayList<String> info2= new ArrayList<String>(); 
			info2.add(tier); 
			info2.add(group); 
			disrupted_nodes.put(node,info2);
			delete_node(node, model);
		}
		if (disruption.equals("total_geo"))
		{
	//String get_nodes_loc= "Select ?node where { ?node :hasLocation ?loc. Filter (xsd:integer(?loc)<200 && xsd:integer(?loc)>100  )}";
			String get_nodes_loc= "Select ?node where { ?node :hasLocation ?loc. Filter (xsd:integer(?loc)<105 && xsd:integer(?loc)>103  )}";
		List<QuerySolution>	ll = execute (Prefix+get_nodes_loc, model);
		for (int i=0; i<ll.size(); i++)
		{
			String node= ll.get(i).get("?node").toString().split("#")[1];
			String get_tier_group= "Select ?tier ?group where { :"+node+" :belongsToTier ?tier.  :"+node+" :hasGroup ?group. }"; 
			List<QuerySolution>	l = execute (Prefix+get_tier_group, model);
			String tier = l.get(0).get("?tier").toString().split("#")[1];
			String group= l.get(0).get("?group").toString();
			ArrayList<String> info2= new ArrayList<String>(); 
			info2.add(tier); 
			info2.add(group); 
			disrupted_nodes.put(node,info2);
			delete_node(node, model);
		}
		}
		if (disruption.equals("total_tier"))
		{
			ArrayList<String> nodes = new ArrayList<String>(); 
			nodes.add("SupplierNode3.2"); nodes.add("SupplierNode1.2"); 
			for (int i=0; i<nodes.size(); i++)
			{
				String node = nodes.get(i); 
				String get_tier_group= "Select ?tier ?group where { :"+node+" :belongsToTier ?tier.  :"+node+" :hasGroup ?group. }"; 
				List<QuerySolution>	l = execute (Prefix+get_tier_group, model);
				String tier = l.get(0).get("?tier").toString().split("#")[1];
				String group= l.get(0).get("?group").toString();
				ArrayList<String> info2= new ArrayList<String>(); 
				info2.add(tier); 
				info2.add(group); 
				disrupted_nodes.put(node,info2);
				delete_node(node, model);
			}
		}
	
		return disrupted_nodes;
	}

	private static void allocation_KPI(OntModel model, int time) {
		String s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\optimization strategies\\FulfillmentPerOrder.rq";
	    List <QuerySolution> l= execute_query(s, model);
	    List<List <QuerySolution>> all= new   ArrayList<List <QuerySolution>> () ; 
	    print_results(l, time); 
	//    all.add(l);
	    s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\optimization strategies\\fullOrderFullfillement.rq";
	    l= execute_query(s, model);
	    print_results(l, time); 
	    all.add(l);
	    
	    s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\optimization strategies\\getFulfillmentCustomer.rq";
	    l= execute_query(s, model);
	    print_results(l, time); 
	    all.add(l);
	    
	    s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\optimization strategies\\utilization.rq";
	    l= execute_query(s,model);
	    print_results(l, time);
	    all.add(l);
	    
	
	}

	private static void createMaterialFlow(OntModel model, ArrayList<String> nodes) {
		 
		Model inferenceModel = JenaUtil.createDefaultModel();
     for (int i=0; i<hash_map.get("SupplierTier").get(0); i++)
     {
    	 inferenceModel= flow_supplier(i, model, nodes, inferenceModel); 
     } 
    	if (nodes!=null)
    	{
     for (int n=0; n< nodes.size(); n++)
		{
		String node= nodes.get(n); 
		String get_tier= "Select ?tier where "+ "{ :"+node+" :belongsToTier ?tier.}\r\n" ;
		List<QuerySolution>  l=   execute(Prefix+get_tier,model); 
		String tier= l.get(0).get("?tier").toString().split("#")[1]; 
		 if (tier.equals("SupplierTier1"))
		 {
			 String q= "Select ?comp ?quantity ?newq where {<< :"+node +" :hasOutputComponent ?comp >> :hasComponentQuantity ?quantity. Bind(xsd:integer(?quantity)/xsd:integer(2) as ?newq)}"; 
			 l= execute(Prefix+q,model); 
			 for (int s=0; s<l.size(); s++)
			 {
     String q1= Prefix+"DELETE   \r\n" + 
				"{ << :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity ?quantity. }\r\n" + 
				"where \r\n" + 
				"{<< :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity ?quantity. }\r\n" ; 
	 UpdateAction.parseExecute(q1, model);
	  String test= "Select ?x ?quantity where "+ "{ << :"+node+" :hasOutputComponent ?x >> :hasComponentQuantity ?quantity.}\r\n" ;
	  System.out.println(model.size()); 

	 q1= Prefix+"Insert {<< :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity "+l.get(s).get("?newq").toString().split("\\^")[0]+" . }\r\n" + 
				"where \r\n" + 
				"{  }\r\n" ; 
	 UpdateAction.parseExecute(q1, model);
		}}}
    	}
    	flow_oem(model); 
    	for (int i=1; i<hash_map_customer.get("CustomerTier").get(0) ; i++)
     {
	   		flow_customer(i, model); }
	 }

	private static void flow_customer(int i, OntModel model) {
		 String kn= "Select ?node ?input ?inq where { << ?node :hasInputComponent ?input>> :hasComponentQuantity ?inq. ?node a :Customer.}"; 
		 List<QuerySolution>knn = execute(Prefix+kn,model); 
   		 String shape111 = "C:\\Users\\Ramzy\\Desktop\\datagenerator\\customer_inout.ttl";   
   	     Model shapeModel111 = JenaUtil.createDefaultModel();
   		   shapeModel111.read(shape111);
   		   Model inferenceModel111 = JenaUtil.createDefaultModel();
   		   inferenceModel111 = RuleUtil.executeRules(model, shapeModel111, 
   		   inferenceModel111, null);
   		   model.add(inferenceModel111); 
   		 String test= "Select ?node ?out ?outq where { <<?node :hasOutputComponent ?out>> :hasComponentQuantity ?outq. ?node a :Customer.} "; 
		 	
   	  List<QuerySolution> nour=   execute(Prefix+test,model); 
   		String  q= "SELECT DISTINCT ?comp (SUM(xsd:integer(?x)) as ?sum) {\r\n" + 
			  		"\r\n" + 
			  		"<<?nodeup :hasOutputComponent ?comp>> :hasComponentQuantity ?x.\r\n" + 
			  		"?nodeup :belongsToTier :CustomerTier"+i+"} \r\n"  
			  		+ "GROUP BY ?comp";   
   		List<QuerySolution>	 l= execute(Prefix+q, model);
		 int h= i+1; 
   		 String  nodess= "Select ?node where { ?node :belongsToTier :CustomerTier"+h+"}";
   		int  nodes_count= Integer.parseInt(execute(Prefix+"Select (count(*) as ?count) where { ?node :belongsToTier :CustomerTier"+h+"}",model).get(0).get("?count").toString().split("\\^")[0]);
   		List<QuerySolution>   lll= execute(Prefix+nodess, model);
   		for (int k=0; k<lll.size(); k++)
   		{

   for (int n1=0; n1<l.size(); n1++)
   {
	   String query = Prefix+
		"Insert {<< <"+lll.get(k).get("?node").toString()+"> :hasInputComponent <"+l.get(n1).get("?comp").toString()+"> >> :hasComponentQuantity "+Integer.parseInt(l.get(n1).get("?sum").toString().split("\\^")[0])/nodes_count+" .}\r\n" + 
					"where \r\n" + 
					"{ }";  
			UpdateAction.parseExecute(query, model) ;
			}
 }
		// TODO Auto-generated method stub
		
	}

	private static void flow_oem(OntModel model) {
		String q= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\so_inout.rq"; 
	     List<QuerySolution> l= execute_query(q, model);
		  for (int n1=0; n1<l.size(); n1++)
		  {
			  String query = Prefix+
						"Insert {<< ?oem :hasInputComponent <"+l.get(n1).get("?comp").toString()+"> >> :hasComponentQuantity "+Integer.parseInt(l.get(n1).get("?sum").toString().split("\\^")[0])+" .}\r\n" + 
						"where \r\n" + 
						"{ ?oem a :OEM}";  
				UpdateAction.parseExecute(query, model) ; 
		  }
		 
		  String shapee = "C:\\Users\\Ramzy\\Desktop\\datagenerator\\oem_inout.ttl";   
	      Model shapeModell = JenaUtil.createDefaultModel();
	 	  shapeModell.read(shapee);
	 	  Model inferenceModell = JenaUtil.createDefaultModel();
	 	  inferenceModell = RuleUtil.executeRules(model, shapeModell, 
	 	  inferenceModell, null);
	 	  model.add(inferenceModell); 
	 	 String tes= "Select  ?out ?outq where { <<:OEM1 :hasOutputComponent ?out>> :hasComponentQuantity ?outq.} "; 
	 	List<QuerySolution> nou=   execute(Prefix+tes,model); 
	 	for (int x=0; x<nou.size(); x++)
	 	{
	 		String query = Prefix+
	 				"Insert {<< ?node :hasInputComponent :"+nou.get(x).get("?out").toString().split("#")[1]+" >> :hasComponentQuantity "+Integer.parseInt(nou.get(x).get("?outq").toString().split("\\^")[0])/hash_map_customer.get("CustomerNodePerTier").get(0)+" .}\r\n" + 
	 				"where \r\n" + 
	 				"{ ?node :belongsToTier :CustomerTier1}"; 
	 		UpdateAction.parseExecute(query, model) ; 
	 	}
	 	
	 	 
		
		// TODO Auto-generated method stub
		
	}

	private static Model flow_supplier(int i, OntModel model, ArrayList<String> nodes, Model inferenceModel) {
		 String kn= "Select ?node ?input ?inq ?out ?outq where { <<?node :hasInputComponent ?input>> :hasComponentQuantity ?inq. }"; 
		 List<QuerySolution>knn = execute(Prefix+kn,model); 
      String shape1 ="C:\\Users\\Ramzy\\Desktop\\datagenerator\\supplier_inout"+3+".ttl";   
      Model shapeModel1 = JenaUtil.createDefaultModel();
	  shapeModel1.read(shape1);
	  Model inferenceModel1 = JenaUtil.createDefaultModel();
	  inferenceModel1 = RuleUtil.executeRules(model, shapeModel1, 
	  inferenceModel1, null);
	  model.add(inferenceModel1); 
	  String test= "Select ?node ?out ?outq where { <<?node :hasOutputComponent ?out>> :hasComponentQuantity ?outq.} "; 
		 	
	  List<QuerySolution> nour=   execute(Prefix+test,model); 
      int t= hash_map.get("SupplierTier").get(0)-i; 
      List<QuerySolution> l= new ArrayList<QuerySolution> (); 
	  if (nodes!=null)
	  {
		for (int n=0; n< nodes.size(); n++)
		{
		String node= nodes.get(n); 
		String get_tier= "Select ?tier where "+ "{ :"+node+" :belongsToTier ?tier.}\r\n" ;
		l=   execute(Prefix+get_tier,model); 
		String tier= l.get(0).get("?tier").toString().split("#")[1]; 
		 if (tier.equals("SupplierTier"+t))
		 {
			 String q= "Select ?comp ?quantity ?newq where {<< :"+node +" :hasOutputComponent ?comp >> :hasComponentQuantity ?quantity. Bind(xsd:integer(?quantity)/xsd:integer(2) as ?newq)}"; 
			 l= execute(Prefix+q,model); 
			 for (int s=0; s<l.size(); s++)
			 {
				 System.out.println(model.size()); 
				 q= Prefix+"DELETE   \r\n" + 
							"{ << :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity ?quantity. }\r\n" + 
							"where \r\n" + 
							"{<< :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity ?quantity. }\r\n" ; 
				 UpdateAction.parseExecute(q, model);
				  test= "Select ?x ?quantity where "+ "{ << :"+node+" :hasOutputComponent ?x >> :hasComponentQuantity ?quantity.}\r\n" ;
				  System.out.println(model.size()); 
			 
				 q= Prefix+"Insert {<< :"+node +" :hasOutputComponent :"+l.get(s).get("?comp").toString().split("#")[1]+">> :hasComponentQuantity "+l.get(s).get("?newq").toString().split("\\^")[0]+" . }\r\n" + 
							"where \r\n" + 
							"{  }\r\n" ; 
				 UpdateAction.parseExecute(q, model);
				 System.out.println(model.size()); 
			 }
			}
		 
		 }

 	  }
	  if (inferenceModel1.size()==inferenceModel.size())
	  {
		  String query = Prefix+
					"Insert {<<  ?node :hasOutputComponent ?comp >> :hasComponentQuantity ?x.}\r\n" + 
					"where \r\n" + 
					"{ <<  ?node :hasInputComponent ?comp >> :hasComponentQuantity ?x. ?node :belongsToTier :SupplierTier"+(hash_map.get("SupplierTier").get(0)-i)+"}";  
			UpdateAction.parseExecute(query, model) ;
	  }
	  if (i!=hash_map.get("SupplierTier").get(0)-1)
	 { 
		 String q= "SELECT DISTINCT ?comp (SUM(xsd:integer(?x)) as ?sum) {\r\n" + 
			  		"\r\n" + 
			  		"<<?nodeup :hasOutputComponent ?comp>> :hasComponentQuantity ?x.\r\n" + 
			  		"?nodeup :belongsToTier :SupplierTier"+(hash_map.get("SupplierTier").get(0)-i)+
			  		"} \r\n"  
			  		+ "GROUP BY ?comp";   
			  l= execute(Prefix+q, model);
			  int node_tier= hash_map.get("SupplierNodePerTier").get(hash_map.get("SupplierTier").get(0)-2-i);
			  String  nodess= "Select ?node where {?node :belongsToTier :SupplierTier"+(hash_map.get("SupplierTier").get(0)-i-1)+"}";  
			  List<QuerySolution>   lll= execute(Prefix+nodess, model);
	  for (int k=0; k<lll.size(); k++)
	  {
		  String node_to= lll.get(k).get("?node").toString().split("#")[1]; 
			  for (int n=0; n<l.size(); n++)
	  {
		  String query = Prefix+
					"Insert {<< :"+node_to+" :hasInputComponent :"+l.get(n).get("?comp").toString().split("#")[1]+" >> :hasComponentQuantity "+Integer.parseInt(l.get(n).get("?sum").toString().split("\\^")[0])/node_tier+" .}\r\n" + 
					"where \r\n" + 
					"{ :"+node_to+" a :Supplier. }";  
			UpdateAction.parseExecute(query, model) ;
	  }
	 }
	 }
	 inferenceModel= inferenceModel1; 
	 return inferenceModel; 
	
}// TODO Auto-generated method stub
		
	

	private static void create_raw_material(OntModel model, int min, int max, String node) {
		// 
		String query_suppl= Prefix+ "SELECT ?subject WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier"+hash_map.get("SupplierTier").get(0)+"} order by desc(?tier)";
		List<QuerySolution> l_suppl= execute (query_suppl, model); 
		OntClass component = model.getOntClass( NS + "Component" );
		Individual compon = model.createIndividual( NS+"Component"+1, component);
		Individual compon2 = model.createIndividual( NS+"Component"+2, component);
     Property has_component = model.getProperty(NS+"hasInputComponent");
     Property comp_quan = model.getProperty(NS+"hasComponentQuantity");
     int val=50;
     int val2=100;
     for (int i=0; i<l_suppl.size(); i++)

{	 Individual suppl = model.getIndividual(l_suppl.get(i).get("subject").toString());

if (l_suppl.get(i).get("subject").toString().split("#")[1].equals(node))
{
	val= min; val2= max; 
}
	Statement S=  model.createStatement(suppl,has_component,compon); 
	Statement S2=  model.createStatement(suppl,has_component,compon2); 
	Resource r = model.createResource(S);
	Resource r2 = model.createResource(S2);
    r.addProperty(comp_quan,val2+"");
    r2.addProperty(comp_quan,val+"");
	
	}
		
	}

	private static void replace_byUI(OntModel model) {
		create_customer_orders(model);
		String s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\testing\\customer_order.rq";
	    execute_query(s, model); 
		//create_orders_products(model);
		s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\testing\\order_product.rq";
		//execute_query(model,s );
		//create_inventory(model);
		s= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\testing\\inventory_product.rq";
		 
		//execute_query(model,s );
	}

	private static  void read_parameters(BufferedReader br, OntModel model) throws NumberFormatException, IOException {
		String st;
		 int count =0; 
		while ((st = br.readLine()) != null)
		  {
			if (st.contains("/////"))
			{count =1; continue; }
			
		  String[] parm = st.split(":");
		  String[] parm_string_values=  parm[1].split(" ");
		  ArrayList<Integer> values= new  ArrayList<Integer> (); 
		  for (int i=1; i< parm_string_values.length; i++)
		  {
			  values.add(Integer.parseInt(parm_string_values[i])); 
			  
		  }
		  if (count ==0)
		  hash_map.put(parm[0],values);
		  if (count ==1)
			  hash_map_customer.put(parm[0],values);
}
		create_read_products (model); 
	
	}
	private static void create_read_products(OntModel model) throws IOException {
		File file = new File("C:\\Users\\Ramzy\\Desktop\\datagenerator\\products.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		OntClass product = model.getOntClass( NS + "Product" );
		Property product_profitability = model.getProperty(NS+"hasProductProfitability");
		Property product_comp_q = model.getProperty(NS+"hasComponentQuantity");
		Property product_comp = model.getProperty(NS+"needsComponent");
		while ((st = br.readLine()) != null)
		  { 
			String[] parm = st.split(" ");
			String s= parm[0]; 
			Individual product_ind = model.createIndividual(NS+s.split(":")[0], product);
			product_ind.addProperty(product_profitability,s.split(":")[1]);
			for (int i=1; i<parm.length; i++)
			{
				String component= parm[i].split(":")[0];
				String quantity= parm[i].split(":")[1];
				Statement S= model.createStatement(product_ind,product_comp,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#"+component)); 
				Resource r = model.createResource(S);
				r.addProperty(product_comp_q,quantity);
			}
		  }
}
		
	
private static void print_excel(XSSFWorkbook workbook,XSSFSheet sheet )
{

	List<String> keys = new ArrayList<String>(sc_uniques_final.keySet());
   
    for (int j=0; j<keys.size(); j++)
{
    	String s= keys.get(j); 
    	Row row = sheet.createRow(j);
    	Cell cell = row.createCell(0);
    	 cell.setCellValue(s);
    for (int i=0; i< sc_uniques_final.get(s).size(); i++)
    {
    	 cell = row.createCell(i+1);
    	 cell.setCellValue(sc_uniques_final.get(s).get(i));
    }
}
    try (FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Ramzy\\Desktop\\datagenerator\\Results.xlsx")) {
        workbook.write(outputStream);
    }
	 catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	private static void optimize_calculate_KPIs(OntModel model, HashMap<String, ArrayList<String>> disrupted_nodes) throws IOException {
		System.out.println("Reliability"); 
		 caculate("Reliability","?node :hasReliability ?metric. \r\n", "?node :hasReliability ?max. \r\n ", "MAX","DESC",  model, disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
	/*	System.out.println("Responsiveness");
		caculate("Responsiveness","?node :hasResponsiveness ?metric. \r\n", "?node :hasResponsiveness ?max. \r\n ", "MAX","DESC", model,disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
		System.out.println("Agility"); 
		caculate("Agility","?node :hasAgility ?metric. \r\n", "?node :hasAgility ?max. \r\n ", "MAX","DESC",model,disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
		System.out.println("AssetManagementEfficiency"); 
		caculate("AssetManagementEfficiency","?node :hasAssetManagementEfficiency ?metric. \r\n", "?node :hasAssetManagementEfficiency ?max. \r\n ", "MAX","DESC",model,disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
		System.out.println("Cost"); 
		caculate("Cost","?node :hasCost ?metric. \r\n", "?node :hasCost ?max. \r\n ", "MIN"," ",  model,disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
		System.out.println("CO2");
		caculate("CO2","?node :hasCO2Balance ?metric. \r\n", "?node :hasCO2Balance ?max. \r\n ", "MIN"," ",model,disrupted_nodes);
		/////////////////////////////////////////////////////////////////////////////////////
		System.out.println("Distance");
		get_shortest_SC(model, disrupted_nodes);*/
		
	}
	private static void caculate(String metricc,String s, String max, String agg, String desc, OntModel model, HashMap<String,ArrayList<String>> disrupted_nodes) {
		model.write(out, "TURTLE");
		String previous= null; 
		String next= null; 
		String q= null; 
		int metric= 0;
		String temp= null; 
		ArrayList<String> uniques = null; 
		ArrayList<String> d_uniques = new ArrayList();
		String get_grou= "Select  ?node ?group ?r where {?node :hasGroup ?group. ?node a :Supplier. ?node :hasResponsiveness ?r. }";
		List<QuerySolution> groupss= execute(Prefix+get_grou,model);
		print_results(groupss, 1); 		
		if (disrupted_nodes== null && uniques== null)// first time 
		{
			compute_regular(s, model, desc, metricc); 
		}
	
		else if (disrupted_nodes.containsKey("Relation")) // go two by two 
		{
			ArrayList<String> disrupted= new ArrayList<String>(); 
			for(Entry<String, ArrayList<String>> listEntry : disrupted_nodes.entrySet()){
				disrupted.add(listEntry.getKey()); 
			}
			uniques= sc_uniques_final.get(metricc); 
			int n=0; 
			       for (int i=0; i<uniques.size()-1; i++)
			       {
			    	   if (disrupted.get(n).equals(uniques.get(i))&& disrupted_nodes.containsKey(disrupted.get(n+1)))
			       {  
			    				String tier= disrupted_nodes.get(uniques.get(i)).get(0); 
								String group= disrupted_nodes.get(uniques.get(i)).get(1);
							//	String tier_n= disrupted_nodes.get(uniques.get(i+1)).get(0);
								String tier_n= disrupted_nodes.get(disrupted.get(n+1)).get(0);
								String group_n= disrupted_nodes.get(disrupted.get(n+1)).get(1);
								if (Integer.parseInt(tier.split("Tier")[1])>Integer.parseInt(tier_n.split("Tier")[1])){
								String a= ""; 
								String s2= s.split(":")[1];
								String s3= s2.split(" ")[0]; 
								if (tier.contains("Supplier")) {a= "?node2 :hasUpStreamNode ?node .";}
								else a= "?node :hasDownStreamNode ?node2 ."; 
								
								q = "Select ?node  ?metric ?node2 ?metric2 ?group ?groupn where {"+
										"?node :belongsToTier :"+tier+ ". "+s+ a+
										"?node2 :hasGroup ?groupn."+ 
										"?node2 :"+s3+" ?metric2."+
										" ?node :hasGroup ?group. Filter(regex(str(?group), \""+group+"\") && regex(str(?groupn),\""+group_n+"\")) }"+ "ORDER  BY "+desc +" (?metric) ?metric2 \r\n"  ; 
							List <QuerySolution> l = execute (Prefix+q, model);
							
							if (l.size()!=0)
							{	
							d_uniques.add(l.get(0).get("?node").toString().split("#")[1]);
							d_uniques.add(l.get(0).get("?node2").toString().split("#")[1]);
							metric= metric+ Integer.parseInt(l.get(0).get("?metric").toString())+Integer.parseInt(l.get(0).get("?metric2").toString());
							n= n+2; 
							i= i+1; 
							}
			       } }
			    	   
			    	   else  if (!uniques.get(i).equals(disrupted.get(n))){
			    	  
			    	   	d_uniques.add(uniques.get(i)); 
			    	   	String get_metric= "Select ?metric ?node where { ?node rdf:type :Node. \r\n "+s+ "Filter (regex(str(?node),\""+uniques.get(i)+"\")).}";
						List <QuerySolution> l = execute (Prefix+get_metric, model);
						if (l.size()!=0)
						{
						metric= metric+ Integer.parseInt(l.get(0).get("?metric").toString());
						}
			    	
			    	   }
			       
		}
			   	metric= metric/(hash_map.get("SupplierTier").get(0)+hash_map_customer.get("CustomerTier").get(0));
				d_uniques.add(metric+""); 	
				sc_uniques_final.put(metricc+"2", d_uniques); 
				}
		
		else {  // node disruption 
					uniques= sc_uniques_final.get(metricc); 
					for (int k=0; k<uniques.size(); k++)
					{
						if (disrupted_nodes.containsKey(uniques.get(k)))
						{
							String tier= disrupted_nodes.get(uniques.get(k)).get(0); 
							String group= disrupted_nodes.get(uniques.get(k)).get(1);
							// add customers 
						q = "Select ?node ?metric ?groupp where {"+
									"?node :belongsToTier :"+tier+ ". "+s+
									" ?node :hasGroup ?groupp. Filter(regex(str(?groupp), \""+group+"\")) }"+ "ORDER  BY "+desc +"(xsd:integer(?metric)) \r\n"  ; 
						List <QuerySolution> l = execute (Prefix+q, model);
						boolean alternative = false; 
							for (int n=0; n<l.size(); n++)
							{
								if (!disrupted_nodes.containsKey(l.get(n).get("?node").toString().split("#")[1])) // node found not disrupted
								{
							previous= l.get(n).get("?node").toString().split("#")[1]; ; 
							System.out.print(previous+ " "); 
							metric= metric+ Integer.parseInt(l.get(n).get("?metric").toString());
							System.out.println(metric);
							d_uniques.add(previous);
							alternative= true; 
							break; 
								}
							}
							if (!alternative) 
							{
							d_uniques.add("x"); //no alternative node chain broken 
							break; 
							}
						}
						else // node in unique not disrupted 
						{
							String get_metric= "Select ?metric ?node where { ?node rdf:type :Node. \r\n "+s+ "Filter (regex(str(?node),\""+uniques.get(k)+"\")).}";
							List <QuerySolution> l = execute (Prefix+get_metric, model);
							if (l.size()!=0)
							{
							metric= metric+ Integer.parseInt(l.get(0).get("?metric").toString());
							d_uniques.add(uniques.get(k)); 
							}
						}
						
					}
					//metric= metric/(hash_map.get("SupplierTier").get(0));
					d_uniques.add(metric+""); 	
					sc_uniques_final.put(metricc+"2", d_uniques); 
				}

	}
		
        
	

	private static void compute_regular(String s, OntModel model, String desc, String metricc) {
		// TODO Auto-generated method stub
		ArrayList<String> uniques= new ArrayList();
		int metric= 0;
		 for(int i=hash_map.get("SupplierTier").get(0); i>0; i--)
			{
				// get group per tier
				String get_group= "Select DISTINCT ?group where {?node :hasGroup ?group. ?node :belongsToTier :SupplierTier"+i+"}";
				List<QuerySolution> groups= execute(Prefix+get_group,model); 
				
				for (int g=0; g<groups.size(); g++) {
			/*	if (i==hash_map.get("SupplierTier").get(0)) temp= " "; 
				else temp= "?node :hasUpStreamNode :"+previous+".\r\n";*/
				String group= groups.get(g).get("group").toString(); 
			String q= 	"Select ?node ?metric {\r\n" + 
				"?node rdf:type :Supplier. "+
				" ?node :hasGroup \""+group+"\". \r\n" + 
				s + 
				"?node :belongsToTier :SupplierTier"+i+".}\r\n" + 
				"ORDER  BY "+desc +"(xsd:integer(?metric)) \r\n"  ;
				List <QuerySolution> l = execute (Prefix+q, model);
				System.out.println("Group:"+group+" "+"Node: "+l.get(0).get("?node").toString().split("#")[1]+ "metric:"+ l.get(0).get("?metric").toString()); 
				metric= metric+ Integer.parseInt(l.get(0).get("?metric").toString());
				uniques.add(l.get(0).get("?node").toString().split("#")[1]); 
				}
			}
			System.out.println("Metric: "+metric);
			uniques.add(metric+""); 
			sc_uniques_final.put(metricc, uniques); 


		
	}

	private static void delete_node(String string, OntModel model) {
		String q= Prefix+"DELETE   \r\n" + 
				"where \r\n" + 
				"{ :"+string +" rdf:type :Node.  :"+string +" ?a ?b. << :"+string+" ?c ?d>> ?g ?f. }\r\n" ;
		/*String q= Prefix+"DELETE   \r\n" + 
		"where " + 
		"{ \r\n  Select * where { :"+string +" rdf:type :Supplier.  :"+string +" ?a ?b. << :"+string+" ?c ?d>> ?g ?f. OPTIONAL{ ?l ?m :"+string+". }  } }\r\n" ; 
	*/	UpdateAction.parseExecute(q, model) ;
		//model.write(out, "TURTLE");
		int x=0; 
	}
	private static void delete_relation(String s1, String s2, OntModel model ) {
		String q= Prefix+"DELETE   where\r\n" + 
				"{ :"+s1 +" ?x :"+s2 +" .}\r\n" ; 
		UpdateAction.parseExecute(q, model) ;
	
	}
	private static void print_dyamic_kpi(List<List<QuerySolution>> all, int time)
	{
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet sheet = workbook.createSheet("KPIs"+time);
		 int size=0; 
		for (int s=0 ; s<all.size(); s++)
		{
			List<QuerySolution> l= all.get(s); 
			for (int i=0; i<l.size(); i++)
			{	
				Row row = sheet.createRow(size+i);
		    	Iterator <String> variables= l.get(i).varNames(); 
		    	int c=0; 
				while (variables.hasNext())
				{ 
				String var= variables.next(); 
				Cell cell = row.createCell(c);
				cell.setCellValue(var); 
				c++; 
				cell = row.createCell(c);
				
				c++; 
				if (l.get(i).get(var).toString().contains("integer")|| l.get(i).get(var).toString().contains("float") || l.get(i).get(var).toString().contains("decimal"))
				{
					cell.setCellValue(l.get(i).get(var).toString().split("\\^")[0]+" "); 
					//System.out.print(l.get(i).get(var).toString().split("\\^")[0]+" ");
				}
				else if (l.get(i).get(var).toString().contains("#"))
					cell.setCellValue(l.get(i).get(var).toString().split("#")[1]+" ");
				//System.out.print(l.get(i).get(var).toString().split("#")[1]+" ");
				else
					cell.setCellValue(l.get(i).get(var).toString()+" ");
					//System.out.print(l.get(i).get(var).toString()+" ");
				
			}
		}
			size= size+l.size(); 
		}
		try (FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Ramzy\\Desktop\\datagenerator\\Results_Dynamic.xlsx")) {
	        workbook.write(outputStream);
	    }
		 catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	private static void print_results(List<QuerySolution> l, int time ) {
		// TODO Auto-generated method stub
		ArrayList<Integer> ll; 
		for (int i=0; i<l.size(); i++)
		{	
			Iterator <String> variables= l.get(i).varNames(); 
			while (variables.hasNext())
			{ 
			String var= variables.next(); 
			if (l.get(i).get(var).toString().contains("integer")|| l.get(i).get(var).toString().contains("float"))
			{
				System.out.print(var+": "+l.get(i).get(var).toString().split("\\^")[0]+" ");
			}
			else if (l.get(i).get(var).toString().contains("#"))
			System.out.print(var+": "+l.get(i).get(var).toString().split("#")[1]+" ");
			else
				System.out.print(var+": "+l.get(i).get(var).toString()+" ");
			
		}
			System.out.println(" ");	
		}
			
		
	}

	
	
	private static void get_shortest_SC(OntModel model, HashMap<String, ArrayList<String>> disrupted_nodes) {
		LinkedHashMap<String, ArrayList<String>> disrupted_nodes_supplier=null;
		LinkedHashMap<String, ArrayList<String>> disrupted_nodes_customer= null; 
		if (disrupted_nodes!= null)
		{
			 disrupted_nodes_supplier= new LinkedHashMap<String, ArrayList<String>> (); 
			 disrupted_nodes_customer= new LinkedHashMap<String, ArrayList<String>> (); 
			

		for(Entry<String, ArrayList<String>> listEntry : disrupted_nodes.entrySet()){
	       if (listEntry.getKey().toString().contains("Supplier"))
	       { 
	    	   disrupted_nodes_supplier.put(listEntry.getKey().toString(),listEntry.getValue()); 
	    }
	       else
	       {
	    	   disrupted_nodes_customer.put(listEntry.getKey().toString(),listEntry.getValue());
	       }
	       TreeMap<String, ArrayList<String>> sorted = new TreeMap<>(disrupted_nodes_supplier); 
	       Set<Entry<String, ArrayList<String>>> mappings = sorted.entrySet();

	       
	    }
		if (disrupted_nodes_supplier.size()==0) disrupted_nodes_supplier=null; 
		if (disrupted_nodes_customer.size()==0)	disrupted_nodes_customer= null; 
		}
		
		model.write(out, "TURTLE");
		ArrayList<String> y= get_shortest_suppliers(model, disrupted_nodes_supplier); 

		ArrayList<String> x=  getshortest_customers(model, disrupted_nodes_customer);
		String metric= "Distance"; 
		int total_distance= Integer.parseInt(y.get(y.size()-1))+ Integer.parseInt(x.get(x.size()-1)); 
		ArrayList<String> nodes = new ArrayList<String> (); 
		for (int i=0; i<y.size()-1; i++)
		{
			nodes.add(y.get(i)); 
		}
		for (int i=0; i<x.size()-1; i++)
		{
			nodes.add(x.get(i)); 
		}
		if (disrupted_nodes!= null)
		{
			 metric= metric+"2"; 
		}
		nodes.add(total_distance+""); 
		sc_uniques_final.put(metric, nodes);
		
	}

	
		
	private static ArrayList<String> getshortest_customers(OntModel model,  HashMap<String, ArrayList<String>>  disrupted_nodes) {
		String q= null ; 
		String next= null; 
		String select=null; 
		int distance=0; 
		ArrayList<String> unique= new ArrayList<String>();
		//ArrayList<String> d_uniques= new ArrayList<String>();  
	
		for(int i=1; i<= hash_map_customer.get("CustomerTier").get(0)-1; i++)
		{
			if (i==1)
				{ next= "?node ";
				select= next+ "?node2 ?diff"; 
				} 
			else select = "?node2 ?diff ?group";
			q=  "select"+select+" {\r\n" + 
					   " \r\n" + 
					   next +" :belongsToTier :CustomerTier"+i+".\r\n" + 
					    next +" :hasDownStreamNode ?node2.\r\n" + 
						  next+ " :hasLocation ?loc.\r\n" + 
					   "  ?node2 :hasLocation ?loc2.\r\n" + 
					   "  BIND (abs(xsd:integer(?loc) - xsd:integer(?loc2)) as ?diff).  } ORDER BY (?diff) \r\n" ;
			
			 List <QuerySolution> l= execute(Prefix+q, model);
			 if (i==1)
					{ 
					  unique.add(l.get(0).get("?node").toString().split("#")[1]);
					}
				 next= ":"+l.get(0).get("?node2").toString().split("#")[1]; 
				 unique.add(l.get(0).get("?node2").toString().split("#")[1]);
				 distance=distance+ Integer.parseInt(l.get(0).get("?diff").toString().split("\\^")[0]); 
		}
		unique.add(distance+""); 
		return unique; 

			}

	private static ArrayList<String> get_shortest_suppliers(OntModel model,  HashMap<String, ArrayList<String>> disrupted_nodes) {
			String q= null ; 
			String next= null; 
			String select=null; 
			int distance=0; 
			ArrayList<String> unique= new ArrayList<String>();
			ArrayList<String> d_uniques= new ArrayList<String>();  
			
			for(int i=hash_map.get("SupplierTier").get(0); i>1; i--)
			{
				if (i==hash_map.get("SupplierTier").get(0))
					{ next= "?node ";
					select= next+ "?node2 ?diff"; 
					} 
				else select = "?node2 ?diff ?group";
				q=   "select "+select+" {\r\n" + 
							   " \r\n" + 
							   next +" :belongsToTier :SupplierTier"+i+".\r\n" + 
							   "?node2 :hasUpStreamNode "+next+".\r\n" + 
							   next+ " :hasLocation ?loc.\r\n" + 
							   "  ?node2 :hasLocation ?loc2.\r\n" + 
							   "  ?node2 :hasGroup ?group.\r\n" + 
							   "  BIND (abs(xsd:integer(?loc) - xsd:integer(?loc2)) as ?diff). } ORDER BY (?diff) \r\n" ;
				
				 List <QuerySolution> l= execute(Prefix+q, model);
					 if (i==hash_map.get("SupplierTier").get(0))
						{ 
						  unique.add(l.get(0).get("?node").toString().split("#")[1]);
						}
					 next= ":"+l.get(0).get("?node2").toString().split("#")[1]; 
					 unique.add(l.get(0).get("?node2").toString().split("#")[1]);
					 distance=distance+ Integer.parseInt(l.get(0).get("?diff").toString().split("\\^")[0]); 
			}
			 
			unique.add(distance+""); 
			return unique;
	}

	private static boolean allocateComponent_supplier(int tofullfil, String component, OntModel model,int t,Individual portfolio, String order) throws IOException {
		 String test= Prefix+ "Select * where"+ 	"{"
					+ "?snode :hasOEM :OEM1. "
					+ "?snode :belongsToTier ?tier.\r\n" + 
					"?snode :hasCapacity ?cap. "
					+ "?cap :hasProduct ?p. "
					+ "?cap :hasQuantity ?quantity. "
					+"?cap :hasTimeStamp ?capacitytime. "
					+ "?snode :hasCapacitySaturation ?saturation. "
					+ "?snode :hasLeadTime ?lt. \r\n" 
					+ "BIND (xsd:integer("+t+") - xsd:integer(?lt) as ?allocationtime).  \r\n" +
					"BIND (xsd:integer(?quantity) + xsd:integer("+ tofullfil+") as ?diff).   \r\n" +
					"FILTER  (regex(str(?tier), \"SupplierTier1\"))}";   
		 List<QuerySolution> ll= execute (Prefix+test, model); 
		String q= Prefix+ "Select * \r\n" + 
				"where \r\n" + 
				"{"
				+ "?snode :hasOEM :OEM1. "
				+ "?snode :belongsToTier ?tier.\r\n" + 
				"?snode :hasCapacity ?cap. "
				+ "?cap :hasProduct ?p. "
				+ "?cap :hasQuantity ?quantity. "
				+ "?cap :hasPrice ?price."
				+"?cap :hasTimeStamp ?capacitytime. "
				+ "?snode :hasCapacitySaturation ?saturation. "
				+ "?snode :hasLeadTime ?lt. \r\n" 
				+ "BIND (xsd:integer("+t+") - xsd:integer(?lt) as ?allocationtime).  \r\n" +
				"BIND (xsd:integer(?quantity) + xsd:integer("+ tofullfil+") as ?diff).   \r\n" +
				"FILTER  (regex(str(?tier), \"SupplierTier1\")"
				+ "&& (xsd:integer(?saturation)>= ?diff) && regex(str(?p),\""+component.split("#")[1]+"\")"
				+ " && (xsd:integer(?allocationtime)= xsd:integer(?capacitytime))).\r\n }"  ;  
		List<QuerySolution> l= execute (Prefix+q, model); 
		if (l.size()>0)
		{
			String allocation_t= l.get(0).get("?capacitytime").toString();  
			System.out.println("Supplier"+ l.get(0).get("snode").toString().split("#")[1]+ "Quantity"+l.get(0).get("diff").toString().split("\\^")[0]+"component"+component+"time: "+ allocation_t); 
			allocate_supplier_product(l.get(0).get("snode").toString().split("#")[1], l.get(0).get("diff").toString().split("\\^")[0], component, model,  allocation_t, t);
			create_order_portfolio(model, l.get(0).get("snode").toString().split("#")[1], portfolio, tofullfil+"",Integer.parseInt(allocation_t),component, l.get(0).get("price").toString(), order); 
			return true;
		}
		else
		{
			return false;
	}
	}


	private static void allocate_supplier_product(String supplier, String y, String component, OntModel model, String allocation_t, int t) {
	String test= Prefix+ "Select * "+ "where \r\n" + 
			"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
			"?cap :hasProduct ?p.\r\n" + 
			"?cap :hasQuantity ?quantity.\r\n" + 
			"?cap :hasTimeStamp \""+allocation_t+"\"."
			+ "}\r\n" ; 
	//print_results(execute (test, model),1); 
 
		String q= Prefix+"DELETE   \r\n" + 
				"{?cap :hasQuantity ?quantity.\r\n }\r\n" + 
				"Insert {?cap :hasQuantity \""+y+"\"}\r\n" + 
				"where \r\n" + 
				"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
				"?cap :hasProduct ?p.\r\n" + 
				"?cap :hasQuantity ?quantity.\r\n" + 
				"?cap :hasTimeStamp \""+allocation_t+"\"."
				+ "}\r\n" ; 
		UpdateAction.parseExecute(q, model) ;
		print_results(execute (test, model),1);
	//	model.write(out,"Turtle");
		System.out.println("Chosen Supplier at time "+ allocation_t);
		propagate_capacity(allocation_t, t, supplier, y, model); 
		 
	}

	private static void propagate_capacity(String allocation_t, int t, String supplier, String y, OntModel model) {
		for (int i=Integer.parseInt(allocation_t); i<=t; i++)
		{
			String q= Prefix+"DELETE   \r\n" + 
					"{?cap :hasQuantity ?quantity.\r\n }\r\n" + 
					"Insert {?cap :hasQuantity \""+y+"\"}\r\n" + 
					"where \r\n" + 
					"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
					"?cap :hasProduct ?p.\r\n" + 
					"?cap :hasQuantity ?quantity.\r\n" + 
					"?cap :hasPrice ?price.\r\n" + 
					"?cap :hasTimeStamp \""+i+"\"."
					+ "}\r\n" ; 
			UpdateAction.parseExecute(q, model) ;
			String test= Prefix+ "Select * "+ "where \r\n" + 
					"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
					"?cap :hasProduct ?p.\r\n" + 
					"?cap :hasQuantity ?quantity.\r\n" + 
					"?cap :hasTimeStamp \""+i+"\"."
					+ "}\r\n" ; 
			print_results(execute (test, model),1); 
		 
		}
	}

	private static void allocateProduct_OEM(int x, String product, OntModel model) {
		String query = Prefix+"DELETE   \r\n" + 
				"{ <<?oem :hasInventoryLevel <"+product+"> >> :hasProductQuantity ?quantity. }\r\n" + 
				"Insert {<<?oem :hasInventoryLevel <"+product+"> >> :hasProductQuantity "+x+".}\r\n" + 
				"where \r\n" + 
				"{<<?oem :hasInventoryLevel <"+product+"> >> :hasProductQuantity ?quantity. }";  
		UpdateAction.parseExecute(query, model) ;
		
	}

	private static void create_capacity(OntModel model) {
		String query_supplier= Prefix+ "SELECT ?subject WHERE { ?subject a :Supplier.}";
		List<QuerySolution> l_suppliers= execute (query_supplier,model); 
		 Property capacity = model.getProperty(NS+"hasCurrentCapacity");
     Property capacity_max = model.getProperty(NS+"hasMaximumCapacity");
     Property comp_quantity = model.getProperty(NS+"hasComponentQuantity");
for (int i=0; i<l_suppliers.size(); i++)
{
	String supp=  l_suppliers.get(i).get("subject").toString().split("#")[1]; 
	String get_component= Prefix+ "Select ?comp ?quantity  where {<< :"+supp +" :hasOutputComponent ?comp >> :hasComponentQuantity ?quantity.}"; 
	 
	List<QuerySolution> l_components= execute (get_component,model);
	Individual supplier = model.getIndividual(l_suppliers.get(i).get("subject").toString());
	 
	for (int j=0; j<l_components.size();j++)
	{
		
	Statement S= model.createStatement(supplier,capacity,model.createResource((l_components.get(j).get("comp").toString()))); 
	Resource r = model.createResource(S);
	r.addProperty(comp_quantity,0+"");
	Statement S2=  model.createStatement(supplier,capacity_max,model.createResource(l_components.get(j).get("comp").toString())); 
	Resource r2 = model.createResource(S2);
    r2.addProperty(comp_quantity,l_components.get(j).get("quantity"));    
	}
	if (supp.split("\\.")[1].contains("1"))// can be removed this is jsut to add more compoenents
	{
		if (supp.split("\\.")[0].contains("1")|| supp.split("\\.")[0].contains("2"))
		{
			
		
		Statement S= model.createStatement(supplier,capacity,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component11")); 
		Resource r = model.createResource(S);
		r.addProperty(comp_quantity,0+"");
		Statement S2=  model.createStatement(supplier,capacity_max,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component11")); 
		Resource r2 = model.createResource(S2);
	    r2.addProperty(comp_quantity,50+"");    
	     S= model.createStatement(supplier,capacity,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component9")); 
		 r = model.createResource(S);
		r.addProperty(comp_quantity,0+"");
		 S2=  model.createStatement(supplier,capacity_max,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component9")); 
		 r2 = model.createResource(S2);
	    r2.addProperty(comp_quantity,50+"");    
		
		}
		else
		{
		Statement S= model.createStatement(supplier,capacity,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component10")); 
		Resource r = model.createResource(S);
		r.addProperty(comp_quantity,0+"");
		Statement S2=  model.createStatement(supplier,capacity_max,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component10")); 
		Resource r2 = model.createResource(S2);
	    r2.addProperty(comp_quantity,100+"");    
	    S= model.createStatement(supplier,capacity,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component12")); 
		 r = model.createResource(S);
		r.addProperty(comp_quantity,0+"");
		 S2=  model.createStatement(supplier,capacity_max,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component12")); 
		 r2 = model.createResource(S2);
	    r2.addProperty(comp_quantity,100+"");    
		}
		
	}
}		
//int f=0; 
}

	private static void create_inventory(OntModel model) {
		// TODO Auto-generated method stub
		String query= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Product }"; 
		List<QuerySolution> l= execute (query,model); 
  Property inventoryLevel = model.getProperty(NS+"hasInventoryLevel");
Individual oem = model.getIndividual( NS+"OEM1");

for (int i=0; i<l.size(); i++)
{
	Statement S=  model.createStatement(oem,inventoryLevel,l.get(i).get("subject")); 
    Resource r = model.createResource(S);
    Property product_quantity = model.getProperty(NS+"hasProductQuantity");
    r.addProperty(product_quantity,getRandomValue(1,20));
}		
}

	private static void allocation_process_byCustomer(OntModel model, boolean combine) throws IOException {
		
	/*	String prioritization= "C:\\Users\\Ramzy\\Desktop\\datagenerator\\optimization strategies\\prioritizeCustomer.rq";		
		List<QuerySolution> l_customers= execute_query (prioritization, model);
		for (int i=0; i<l_customers.size(); i++)
		{
			String customer= l_customers.get(i).get("customer").toString();
			String order_customer_query = "Select ?customer ?order\r\n {\r\n<"  + customer + "> :makes ?order. }\r\n" ;
			List<QuerySolution> l_order_customer= execute (Prefix+ order_customer_query,model);
			for (int j=0; j<l_order_customer.size(); j++)
			{
				String order= l_order_customer.get(j).get("order").toString();
				String product_order_qery= "Select ?product ?quantity {\r\n << <"+order+"> :containsProduct ?product>> :hasProductQuantity ?quantity."
						+ "?product :hasProductProfitability ?profitability. \r\n } ORDER BY DESC(?profitability) ";
				List<QuerySolution> l_product_order= execute (Prefix+ product_order_qery,model);
				String order_fulfillement= ""; 
				for (int k=0; k< l_product_order.size(); k++)
				{
					String product= l_product_order.get(k).get("?product").toString(); 
					int quantity= Integer.parseInt(l_product_order.get(k).get("?quantity").toString().split("\\^")[0]);
					System.out.println(customer.split("#")[1]+ " "+ order.split("#")[1] + " "+ product.split("#")[1] + " "+ quantity); 
					 boolean full= allocate(product, quantity, model);
					order_fulfillement= order_fulfillement+" "+full; 
					fulfill_order_product(order, product, full,model);
				}
				String temp= null; 
				int result = order_fulfillement.split("false",-1).length - 1;
				    String[] splited = order_fulfillement.split(" ");
				    Arrays.sort(splited);
				    System.out.println(Arrays.toString(splited));
				    int max = 0;
				    int count= 1;
				    String word = splited[0];
				    String curr = splited[0];
				    for(int i1 = 1; i1<splited.length; i1++){
				        if(splited[i1].equals(curr)){
				            count++;
				        }
				        else{
				            count =1;
				            curr = splited[i1];
				        }
				        if(max<count){
				            max = count;
				            word = splited[i1];
				        }
				    }
				    System.out.println(max + " x " + word);
				    String w= ""; 
				    if (max==5)// product per order
				    {  if (word.equals("false")) w=word;
				    else w="true";}
				    else 
				    {w= "partial";}
				    
				String 	q= Prefix+ 
						"Insert {<"+order +"> :isOrderFulfilled '"+w+"' }\r\n" + 
						"where \r\n" + 
						"{ <"+order +"> a :Order}\r\n" ; 
				UpdateAction.parseExecute(q, model) ;
				
			}
		}*/
	}


	private static boolean allocate(String product, int product_quantity, OntModel model, int t, Individual portfolio, String order) {

		String get_needed_components= "Select ?component ?quantity where { << :"+product.split("#")[1]+" :needsComponent ?component >> :hasComponentQuantity ?quantity}"; 
		List<QuerySolution> l_component_product= execute (Prefix+ get_needed_components,model);
		boolean success= true; 
		for (int c=0; c< l_component_product.size(); c++)
		{
			String component= l_component_product.get(c).get("component").toString(); 
			int quantity= product_quantity* Integer.parseInt(l_component_product.get(c).get("quantity").toString().split("\\^")[0]); 
			try {
				System.out.println("Needed Component "+ component.split("#")[1] + "In quantity "+ quantity);
				success= success && allocateComponent_supplier(quantity, component, model, t,portfolio, order);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return success; 
	}


	private static void fulfillorder( String order, String product, OntModel model, int fulfilled) {
		String q= Prefix+
				"Insert {<< <"+order +"> :containsProduct <"+product+"> >> :hasFulfilledProductQuantity "+fulfilled+"  }\r\n" + 
				"where \r\n" + 
				"{ <"+order+"> a :Order. }\r\n" ; 
		UpdateAction.parseExecute(q, model) ; 
	}

	private static int check_oem(String product,OntModel model) {
		String querystring = Prefix + "Select ?inventory \r\n where { << ?oem :hasInventoryLevel <"+ product+"> >> :hasProductQuantity ?inventory. }\r\n" ;
		List<QuerySolution> l=  execute (querystring,model); 
		int inventory= Integer.parseInt(l.get(0).get("inventory").toString().split("\\^")[0]); 
		return inventory;
	}

	private static List<QuerySolution> execute(String query, OntModel model )
	{
		
		Query query2 = QueryFactory.create(query); 
		QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
		ResultSet results = qe2.execSelect();
		List<QuerySolution> l= new ArrayList<QuerySolution> (); 
		while (results.hasNext())
		{l.add(results.next()); }
		return l;
		
	}
	private static void create_orders_products(OntModel model)
	{
		String query= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Order }"; 
		List<QuerySolution> l_orders= execute (query,model); 
		System.out.println("Total number of orders is: "+ l_orders.size()); 
		String query_product= Prefix+ "SELECT ?subject \r\n" + 
				"	WHERE { ?subject a :Product }"; 
		List<QuerySolution> l_products= execute (query_product,model); 
		System.out.println("Number of Products per Order is: "+ l_products.size());
		Property order_product = model.getProperty(NS+"containsProduct");
		Property product_quantity = model.getProperty(NS+"hasProductQuantity");
		
		for (int i=0; i<l_orders.size(); i++)
		{
			Individual order = model.getIndividual(l_orders.get(i).get("subject").toString());
			for (int j=0; j<l_products.size();j++)
			
			{
			Statement S=  model.createStatement(order,order_product,l_products.get(j).get("subject")); 
		    Resource r = model.createResource(S);
		    r.addProperty(product_quantity,getRandomValue(1,20));
		   // r.addProperty(product_quantity,2+"");
			}
		}	
	}
private static void create_customer_orders(OntModel model)
{
	String query= Prefix+ "SELECT ?subject \r\n" + 
			"	WHERE { ?subject a :Customer. ?subject :belongsToTier :CustomerTier1. }"; 
	List<QuerySolution> l= execute (query,model); 
	System.out.println("Total number of Customers is: "+ l.size());
	Property makes = model.getProperty(NS+"makes");
	OntClass order = model.getOntClass( NS + "Order" );
	for (int i=0; i<l.size();i++)
      {
		Individual customer = model.getIndividual(l.get(i).get("subject").toString());
		for (int j=0; j<4; j++)
		{
		Individual order_ind = model.createIndividual( NS+"Order"+RandomStringUtils.randomAlphanumeric(8), order);
        customer.addProperty(makes,order_ind);
       }
	}
}




	private static List<QuerySolution> execute_query(String s, OntModel model) {
	File path = new File(s);
	Query query = QueryFactory.read(path.getAbsolutePath());
	QueryExecution qe2 = QueryExecutionFactory.create(query, model);
	ResultSet results = qe2.execSelect();
	List<QuerySolution> l = new ArrayList<QuerySolution>();
	while (results.hasNext())
		l.add(results.next());
	
	return l; 
		
	}


	private static void create_relations(int tiers, ArrayList<Integer>tiers_array ,Individual oem, Property p, Property Node_node, Property node_oem, OntClass Node, OntModel model) {
		
        for (int i=1; i<tiers; i++)
        {
        	Individual t = model.getIndividual(NS+ Node.getLocalName()+"Tier"+ (i));
      		Individual tup = model.getIndividual(NS+Node.getLocalName()+"Tier"+ (i+1)); 		
      		t.addProperty(p, tup); 
        	for (int j=1; j<=tiers_array.get(i-1); j++)
        	{
        		
        		Individual n = model.getIndividual(NS+Node.getLocalName()+"Node"+j+"."+(i));
        		for (int l=1; l<= tiers_array.get(i); l++ )
        		{ 
        			Individual nup = model.getIndividual(NS+Node.getLocalName()+"Node"+l+"."+(i+1)); 		
                	n.addProperty(Node_node, nup);
                	if (i==1 && Node.getLocalName().contains("Supplier"))
             
                	{
                		n.addProperty(node_oem, oem); 
                	}
                	if (i==1 && Node.getLocalName().contains("Customer"))
                        
                	{
                		oem.addProperty(node_oem, n); 
                	}
                }
        	}
        }
	}
	private static void create_tiers_nodes(int tiers, ArrayList<Integer>tiers_array ,  OntClass Node, BufferedReader reader, OntModel model) throws IOException {
		  Property p_node  = model.getProperty(NS+"belongsToTier");
		  Property leadtime  = model.getProperty(NS+"hasLeadTime");
		  Property manufactures  = model.getProperty(NS+"manufactures");
		  Property hasLongitude  = model.getProperty(NS+"hasLongitude");
		  Property hasLatitude   = model.getProperty(NS+"hasLatitude");
		  Property hasTransportMode  = model.getProperty(NS+"hasTransportMode");
		for (int i=1; i<=tiers; i++)
        {
			OntClass tier = model.getOntClass( NS+"Tier" );
        	Individual t = model.createIndividual(NS+Node.getLocalName()+"Tier"+ i, tier );
        	 
        	for (int j= 1; j<=tiers_array.get(i-1); j++)
        	{
        		
        		Individual n = model.createIndividual( NS+Node.getLocalName()+"Node"+ j+"."+ i, Node);
        		n.addProperty(p_node, t); 
        		n.addProperty(hasLongitude,getRandomValue(70, 75)); 
        		n.addProperty(hasLatitude, getRandomValue(70,75));
        		List<String> transports= new ArrayList<String>(); 
    			transports.add("vehicle");
    			transports.add("maritime");
    			transports.add("air");
        		for (int m=0; m<= (int)(3.0 * Math.random()); m++)
        		{
        			n.addProperty(hasTransportMode, transports.get((int)(3.0 * Math.random())));
        		}
        		if (j==1 ||j ==3) {
        			n.addProperty(leadtime, 2+""); 
        		n.addProperty(manufactures, "Product"+1);
        		}
        		else
        		{
        			n.addProperty(leadtime, 3+""); 
            		n.addProperty(manufactures, "Product"+2);
        		}
        			
        		
    		//	n.addProperty(manufactures, "Component"+getRandomValue(1,3));
        		List<QuerySolution> l= get_data_property(Node.getLocalName(), model); 
            	for(int k=0 ; k< l.size(); k++)
    	      	{
            		String property= l.get(k).get("subject").toString().split("#")[1]; 
            		if (Node.getLocalName().contains("Supplier") && hash_map.get(property) != null)
        			{
            			
            			ArrayList <Integer> temp= hash_map.get(property); 
            			if (property.contains("Group")) {
            				//n.addProperty(model.getProperty(l.get(k).get("subject").toString()),getRandomValue(1,temp.get(i-1)));
            				int c=1; 
            				if (j>2&&j<=5)c=2;
            				if (j>=5)c=3; 
            				float nss= (i+c); 
            				
            				int f=Math.round(nss); 
            				n.addProperty(model.getProperty(l.get(k).get("subject").toString()),f+"");
            			}
            			else
        		    	{	//n.addProperty(model.getProperty(l.get(k).get("subject").toString()), getRandomValue(temp.get(0),temp.get(1)));
            				int f=temp.get(0)+j; 
            				n.addProperty(model.getProperty(l.get(k).get("subject").toString()),f+"") ;
        		    	}
        		    	
            					
        		    }
            		if (Node.getLocalName().contains("Customer") && hash_map_customer.get(property) != null)
        			{
            			ArrayList <Integer> temp= hash_map_customer.get(property); 
        		    	n.addProperty(model.getProperty( l.get(k).get("subject").toString()),
        	      				getRandomValue(temp.get(0),temp.get(1)));
        		    }
    			}	
            
        	}
        }
		
	}
	private static List<QuerySolution> get_data_property(String node, OntModel model) {
		 String querystring = Prefix + 
		"SELECT Distinct ?subject \r\n" + 
		"	WHERE { ?subject rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* :"+ node+
		"}"; 
		 	Query query2 = QueryFactory.create(querystring); 
			QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
			ResultSet results = qe2.execSelect();
			List<QuerySolution> l= new ArrayList<QuerySolution>();
			while(results.hasNext())
			{
	       		  l.add(results.next());
	        }	 
	      				  
	    return l; 
	}
	private static String getRandomValue(int i, int j) {
		Random r = new Random();
		int R = r.nextInt(j - i) + i;

		return String.valueOf(R);
		
	}

}
