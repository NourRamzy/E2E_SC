import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;


public class generator {

	public static int time_big= 100;

	static String SOURCE = "http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6";
    static String NS = SOURCE + "#";
    public static HashMap<String, ArrayList<Integer>> hash_map ;
	public static HashMap<String, ArrayList<Integer>> hash_map_customer ;
	public static HashMap<String,ArrayList<String>> sc_uniques_final ;
	
	
	private static String Prefix;

	public static BufferedWriter out = null;

	//@SuppressWarnings({"resource", "deprecation"})
	public static void main(String[] args) throws IOException {
		
		//Prefix-sources
		Prefix= """
				PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r
				PREFIX owl: <http://www.w3.org/2002/07/owl#>\r
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r
				PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r
				Prefix : <http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#>""";
		hash_map = new HashMap<>();
		hash_map_customer = new HashMap<>();
		sc_uniques_final = new HashMap<>();

		//Resources
		File file = new File("src/resources/configurationfile.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		out = new BufferedWriter (new FileWriter("src/out1.ttl"));
		OntModel model = ModelFactory.createOntologyModel();
		InputStream generator = FileManager.get().open( "src/resources/generator.owl" );

		model.read(generator, null);
		read_parameters(br, model);
        OntClass Supplier_Node = model.getOntClass( NS + "Supplier");
        OntClass Customer_Node = model.getOntClass( NS + "Customer");

		//OEM properties
        OntClass OEM_Node = model.getOntClass( NS + "OEM" );
		Property inventoryLevel = model.getProperty(NS+"hasInventoryLevel");
        Individual oem = model.createIndividual( NS+"OEM1", OEM_Node);
        oem.addProperty(inventoryLevel, getRandomValue(100,200));
        Property hasLeadTime=  model.getProperty(NS+"hasLeadTime");
		oem.addProperty(hasLeadTime, "3");
		Property hasLongitude = model.getProperty(NS+"hasLongitude");
		Property hasLatitude = model.getProperty(NS+"hasLatitude");
		oem.addProperty(hasLongitude,getRandomValue(70, 75));
		oem.addProperty(hasLatitude, getRandomValue(70,75));
		Property hasTransportMode = model.getProperty(NS+"hasTransportMode");
		List<String> transports= new ArrayList<>();
		transports.add("vehicle");
		transports.add("maritime");
		transports.add("air");

		//Random # transport modes for OEM
		for (int m=0; m<= (int)(3.0 * Math.random()); m++)
		{
			oem.addProperty(hasTransportMode, transports.get(m));
		}

        //create supplier
        Property hasUpStreamTier = model.getProperty(NS+"hasUpStreamTier");
        Property hasUpStreamNode  = model.getProperty(NS+"hasUpStreamNode");
        Property node_oem  = model.getProperty(NS+"hasOEM");

		create_tiers_nodes (hash_map.get("SupplierTier").get(0), hash_map.get("SupplierNodePerTier"), Supplier_Node, model);
        create_relations(hash_map.get("SupplierTier").get(0),hash_map.get("SupplierNodePerTier"), oem, hasUpStreamTier, hasUpStreamNode, node_oem, Supplier_Node, model);

		//create customer
        Property hasDownStreamTier = model.getProperty(NS+"hasDownStreamTier");
        Property hasDownStreamNode  = model.getProperty(NS+"hasDownStreamNode");
        Property oem_node  = model.getProperty(NS+"OEMhasNode");

        create_tiers_nodes ( hash_map_customer.get("CustomerTier").get(0),hash_map_customer.get("CustomerNodePerTier"), Customer_Node, model);
        create_relations(hash_map_customer.get("CustomerTier").get(0),hash_map_customer.get("CustomerNodePerTier"),oem,hasDownStreamTier, hasDownStreamNode,oem_node, Customer_Node, model);

	    create_raw_material(model);
		//create_capacity(model);

        //Generate initial values for capacity, inventory, saturation
	    generation(model);

		//Allocation function, supply plan generation
	    allocation(model);

		//Allocation evaluation
	    allocation_KPI(model);

      String adjust= Prefix+ "DELETE   \r\n" +
				"{ ?capacity :hasQuantity \"0\"}\r\n" + 
				"Insert { ?capacity :hasQuantity \"3\"}\r\n" + 
				"where \r\n" + 
				"{ ?capacity :hasQuantity \"0\"}\r\n" ;
      UpdateAction.parseExecute(adjust, model) ;
      model.write(out, "TURTLE");

      out.close();
	}
	
	private static void allocation(OntModel model) {

		//Capacity requirement allocation
		String get_oem_leadtime= Prefix+" Select * where { :OEM1 :hasLeadTime ?lt .}"; 
		List<QuerySolution> oem_leadtimes= execute(get_oem_leadtime,model); 
		String oem_leadtime= oem_leadtimes.get(0).get("lt").toString();


		for (int t=0; t<time_big; t++)
		{
			//Updating capacity inventory
			maintain_capacity_inventory(t, model);
			//Get orders with delivery time DT and OEM lead time LT at t= DT-LT
			String getOrders= Prefix+"Select * where { "
					+"?order :hasDeliveryTime ?time. "
					+"?order :hasQuantity ?q. "
					+"?order :hasProduct ?p. "
					+"?customer :makes ?order. "
					+"?customer :hasPriority ?priority. "
					+"Filter ((xsd:integer(?time)-xsd:integer("+oem_leadtime+"))= xsd:integer("+t+"))"
					+"} order by desc (?priority)";
			List<QuerySolution> orders= execute (getOrders,model);
			 print_results(orders);

			if (orders.size()==0)
				{
				 getOrders= Prefix+"Select * where { "
						+ "?order :hasDeliveryTime \""+t+"\". "
						+"?order :hasQuantity ?q. "
						+"?order :hasProduct ?p. "
						+ "} "; 
				 orders= execute (getOrders,model);
					for (QuerySolution querySolution : orders) {
						String order = querySolution.get("order").toString();
						update_order_fulfilled(order, "false", model); // order not fulfilled;

					}
				}
			//Start allocation
			else
			{
				for (QuerySolution querySolution : orders) {
					String order = querySolution.get("order").toString();
					String quantity = querySolution.get("q").toString();
					String product = querySolution.get("p").toString();
					int oem_alloc = check_oem_inventory(querySolution, t, model);
					OntClass portfolio_class = model.getOntClass(NS + "Portfolio");
					Individual supplyPlan = model.createIndividual(NS + "Portfolio" + RandomStringUtils.randomAlphanumeric(8), portfolio_class);
					Individual order_ind = model.getIndividual(order);

					Property hasSupplyPlan = model.getProperty(NS + "hasPortfolio");
					order_ind.addProperty(hasSupplyPlan, supplyPlan);

					//Allocate at OEM
					String query = Prefix + "SELECT ?inv ?q ?p\r\n" +
							"	WHERE {  :OEM1 :hasInventory ?inv. ?inv :hasTimeStamp \"" + t + "\". ?inv :hasQuantity ?q.  ?inv :hasPrice ?p.}";
					List<QuerySolution> solutions = execute(query, model);
					String price = solutions.get(0).get("p").toString();

					//If successful -> allocation from OEM inventory, else -> check supplier for material and then production
					if (oem_alloc == 0) {
						update_order_fulfilled(order, "true", model);
						System.out.println("Order" + order + " fulfilled at OEM");
						create_supply_plan(model, "OEM1", supplyPlan, quantity, t, product, price, order);
						create_inventory_increase(model, t, 10);
					} else
					{
						String remainder = (Integer.parseInt(quantity) - oem_alloc) + "";
						if (!remainder.equals("0")) {
							create_supply_plan(model, "OEM1", supplyPlan, remainder, t, product, price, order);
						}
						boolean success = allocate(product, oem_alloc, model, t, supplyPlan, order);
						update_order_fulfilled(order, success + "", model);
						if (!success) {
							String delete = Prefix + "DELETE   \r\n" +
									"{<" + order + "> :hasPortfolio ?p.  <" + order + "> :hasOriginalQuantity ?q. <" + order + "> :hasTotalOriginalPrice ?price}\r\n" +
									"where \r\n" +
									"{ <" + order + "> :hasPortfolio ?p}\r\n";
							UpdateAction.parseExecute(delete, model);

						}

					}
					get_supply_plan_quantity(model, supplyPlan.toString().split("#")[1], order);
					get_portfolio_price(model, supplyPlan.toString().split("#")[1], order);

				}
			}
			create_inventory_increase(model,t, 3);
		
		}
		
	}

	private static void create_supply_plan(OntModel model, String node, Individual portfolio, String quantity, int time, String product, String price, String order) {

		//Introducing portfolio properties
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
	
	private static void get_portfolio_price(OntModel model, String portfolio, String order) {

		//Check supplier prices
		String get_price= Prefix+ "SELECT * WHERE{ :"+order.split("#")[1]+" :hasPortfolio :"+portfolio+" . <<:"+portfolio+" :needsNode ?node>> :hasQuantity ?q.  <<:"+portfolio+" :needsNode ?node>> :hasUnitPrice ?p.} ";
		List<QuerySolution> prices= execute(get_price,model);
		float total_price= 0;
		for (QuerySolution querySolution : prices) {
			if (querySolution.get("?q").toString().contains("float")) {
				total_price = total_price + (Float.parseFloat(querySolution.get("?q").toString().split("\\^")[0]) *
					Integer.parseInt(querySolution.get("?p").toString().split("\\^")[0]));
			} else {
				total_price = total_price + (Integer.parseInt(querySolution.get("?q").toString().split("\\^")[0]) *
					Integer.parseInt(querySolution.get("?p").toString().split("\\^")[0]));
				}

		}
		String query= Prefix+ "INSERT { :"+order.split("#")[1]+" :hasTotalOriginalPrice "+total_price+"} WHERE { }";
		UpdateAction.parseExecute(query, model) ;
	}

	private static void get_supply_plan_quantity(OntModel model, String port, String order) {

		//Checking order quantity against supply quantity
		String get_price= Prefix+ "SELECT * WHERE{ :"+order.split("#")[1]+" :hasPortfolio :"+port+" . << :"+port+" :needsNode ?node>> :hasQuantity ?q. } ";
	
		List<QuerySolution> prices= execute(get_price,model);
		print_results(prices);
	
		float total_quantity= 0;
		for (QuerySolution querySolution : prices) {
			if (querySolution.get("?q").toString().contains("float")) {
				total_quantity = total_quantity + (Float.parseFloat(querySolution.get("?q").toString().split("\\^")[0]));
			} else {
				total_quantity = total_quantity + (Integer.parseInt(querySolution.get("?q").toString()));
			}
		}
		String query= Prefix+ "INSERT { :"+order.split("#")[1]+" :hasOriginalQuantity "+total_quantity+"} WHERE { }";
		UpdateAction.parseExecute(query, model) ;
		System.out.print(total_quantity);
	
	}

	private static void maintain_capacity_inventory(int t, OntModel model) {

		//Update capacity inventory
		int j=t-1;
		String test= Prefix+ "Select * "+ "where \r\n" +
				"{?supplier :hasCapacity ?cap. ?supplier :hasCapacitySaturation ?max.\r\n" +
				"?cap :hasProduct ?p.\r\n" +
				"?cap :hasQuantity ?quantity.\r\n" +
				"?cap :hasPrice ?price.\r\n" +
				"?cap :hasTimeStamp \""+j+"\"."
				+ "}\r\n" ;
		List<QuerySolution> ff= execute (test, model);
		Property has_product = model.getProperty(NS+"hasProduct");
		Property time = model.getProperty(NS+"hasTimeStamp");
		OntClass capacity_class = model.getOntClass( NS + "Capacity");
		Property hasCapacity = model.getProperty( NS + "hasCapacity");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
		for (QuerySolution querySolution : ff) {
			String y = querySolution.get("quantity").toString().split("\\^")[0];

			Individual capacity = model.createIndividual(NS + "Capacity" + RandomStringUtils.randomAlphanumeric(8), capacity_class);
			capacity.addProperty(has_product, querySolution.get("p").toString());
			capacity.addProperty(time, t + "");
			capacity.addProperty(has_quantity, y);
			capacity.addProperty(has_price, querySolution.get("price").toString());
			Individual supplier = model.getIndividual(querySolution.get("supplier").toString());
			supplier.addProperty(hasCapacity, capacity);

		}
		
	}

	private static void update_order_fulfilled(String order, String w, OntModel model) {

		//Check Order completion
		String 	q= Prefix+ "Insert {<"+order +"> :isOrderFulfilled '"+w+"' }\r\n"
				+"where \r\n"
				+"{ <"+order +"> a :Order}\r\n" ;
		UpdateAction.parseExecute(q, model) ;
		
	}

	private static int check_oem_inventory(QuerySolution order, int t, OntModel model) {

		//Check manufacturer inventory
		String product=order.get("p").toString().split("#")[1];
		int quantity = Integer.parseInt(order.get("q").toString());
		
		String query= Prefix+ "SELECT ?node ?time ?inv ?q \r\n"
				+"	WHERE { ?node a :OEM. ?node :hasInventory ?inv. ?inv :hasProduct ?p. "
				+"?inv :hasTimeStamp \""+t+"\". ?inv :hasQuantity ?q. "
				+"Filter(regex(str(?p),\""+product+"\"))}";
		List <QuerySolution> oem_inventory= execute(query,model);
		int oem_q= Integer.parseInt(oem_inventory.get(0).get("q").toString());

		//TODO
		//if -> OEM inventory >= needed quantity, then delete query, else -> ?
		if (oem_q>= quantity)
		{
			int n= oem_q-quantity;
			 query = Prefix+ "DELETE   \r\n"
					 +"{ ?inv :hasQuantity ?q. }\r\n"
					 +"Insert {?inv :hasQuantity \""+n+"\".}\r\n"
					 +"where \r\n"
					 +"{ ?node a :OEM. "
					 +"?node :hasInventory ?inv. "
					 +"?inv :hasProduct ?p. \r\n"
					 +"?inv :hasTimeStamp \""+t+"\". "
					 +"?inv :hasQuantity ?q. }";
			UpdateAction.parseExecute(query, model) ;

		return 0; 
		}
		else {
				int m= 0;
				 query = Prefix+"DELETE   \r\n"
						 +"{ ?inv :hasQuantity ?q. }\r\n"
						 +"Insert {?inv :hasQuantity \""+m+"\".}\r\n"
						 +"where \r\n"
						 +"{ ?node a :OEM. "
						 +"?node :hasInventory ?inv. "
						 +"?inv :hasProduct ?p. \r\n"
						 +"?inv :hasTimeStamp \""+t+"\". "
						 +"?inv :hasQuantity ?q. }";
				UpdateAction.parseExecute(query, model) ;
				System.out.println("at time"+t+"OEM was: "+oem_q+"now at"+ (oem_q-quantity));
			return quantity-oem_q; 
		}
			
	}

	private static void generation(OntModel model) {


		try {
			create_read_products(model);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		int x= create_orders(model); 
		create_capacity_saturation(model); 
		create_initial_Capacity(model); 
		create_initial_Inventory(model);
		System.out.println("OrderCount"+ x); 

	}

	private static void create_initial_Capacity(OntModel model) {

		//Check supplier material properties (capacity, quantity, price)
		String query= Prefix+ "SELECT DISTINCT ?subject ?product \r\n"
				+"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. ?subject :manufactures ?product.}";
		List<QuerySolution> suppliers= execute (query,model);

		OntClass capacity_class = model.getOntClass( NS + "Capacity");

		Property time = model.getProperty(NS+"hasTimeStamp");
		Property hasCapacity = model.getProperty( NS + "hasCapacity");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");

		suppliers.forEach(querySolution -> {
			Individual capacity = model.createIndividual(NS + "Capacity" + RandomStringUtils.randomAlphanumeric(8), capacity_class);
			Individual supplier = model.getIndividual(querySolution.get("subject").toString());
			supplier.addProperty(hasCapacity, capacity);
			capacity.addProperty(has_product, querySolution.get("product").toString());
			capacity.addProperty(time, "0");
			capacity.addProperty(has_quantity, "0");
			capacity.addProperty(has_price, getRandomValue(1, 10));
		});
	}

	private static String create_inventory_increase(OntModel model, int t, int increase) {

		//Replaces used materials
		Property time = model.getProperty(NS+"hasTimeStamp");
		Property hasInventory = model.getProperty(NS +"hasInventory");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");

		Individual product = model.getIndividual(NS+"ProductA");
		String query= Prefix+ "SELECT ?inv ?q ?p\r\n"
				+"	WHERE {  :"
				+"OEM1"
				+" :hasInventory ?inv. ?inv :hasTimeStamp \""
				+t
				+"\". ?inv :hasQuantity ?q.  ?inv :hasPrice ?p.}";
		List<QuerySolution> solutions= execute (query,model);

		OntClass inventory_class = model.getOntClass(NS+"Inventory");
		Individual inv = model.createIndividual( NS+"Inventory"+RandomStringUtils.randomAlphanumeric(8), inventory_class);
		Individual supplier = model.getIndividual(NS+ "OEM1");
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

		//Check supplier inventory
		String query= Prefix+ "SELECT ?subject \r\n"
				+"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. }";
		List<QuerySolution> suppliers= execute (query,model);

		OntClass inventory_class = model.getOntClass(NS+"Inventory");

		Property time = model.getProperty(NS+"hasTimeStamp");
		Property hasInventory = model.getProperty(NS+"hasInventory");
		Property has_product = model.getProperty(NS+"hasProduct");
		Property has_quantity = model.getProperty(NS+"hasQuantity");
		Property has_price = model.getProperty(NS+"hasPrice");
		Individual product = model.getIndividual(NS+"ProductA");

		suppliers.forEach(querySolution -> {
			Individual inv = model.createIndividual(NS + "Inventory" + RandomStringUtils.randomAlphanumeric(8), inventory_class);
			Individual supplier = model.getIndividual(querySolution.get("subject").toString());
			supplier.addProperty(hasInventory, inv);
			inv.addProperty(has_product, product);
			inv.addProperty(time, "0");
			inv.addProperty(has_quantity, getRandomValue(1, 10));
			inv.addProperty(has_price, getRandomValue(1, 10));
		});

		 Individual oem = model.getIndividual( NS+"OEM1");
		 Individual inv = model.createIndividual( NS+"Inventory"+RandomStringUtils.randomAlphanumeric(8), inventory_class);
			inv.addProperty(has_product, product); 
			inv.addProperty(time, "0"); 
			inv.addProperty(has_quantity, "10");
			inv.addProperty(has_price,60+"");
			oem.addProperty(hasInventory, inv);
	  
	}

	private static void create_capacity_saturation(OntModel model) {

		//Set capacity saturation for each supplier
		String query= Prefix+ "SELECT ?subject \r\n"
				+"	WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier1. }";
		List<QuerySolution> suppliers= execute (query,model); 
		Property capacity_saturation = model.getProperty(NS+"hasCapacitySaturation");
		suppliers.stream().map(querySolution -> model.getIndividual(querySolution.get("subject").toString())).forEach(supplier -> supplier.addProperty(capacity_saturation, 3000 + ""));
		
	}

	private static int create_orders(OntModel model ) {

		//Check suppliers and creates orders for needed materials
		String query= Prefix+ "SELECT ?subject \r\n"
				+"	WHERE { ?subject a :Customer. ?subject :belongsToTier :CustomerTier1. } ORDER  BY desc(?p)  ";
		List<QuerySolution> customers= execute (query,model);

		OntClass order = model.getOntClass( NS + "Order" );

		Property makes = model.getProperty(NS+"makes");
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

			  for (int t=6; t<time_big+6; t++)
			{
				for (int f=0;f<1; f++)
				{

			Individual order_ind = model.createIndividual( NS+"Order"+RandomStringUtils.randomAlphanumeric(8), order);
	        customer.addProperty(makes,order_ind);
	        order_ind.addProperty(time,t+"");
	        order_ind.addProperty(has_product, product);
	        order_ind.addProperty(has_quantity, getRandomValue(1,10));
	        order_count++;
	        }
			}
			System.out.println("order count"+ order_count);

		}
		System.out.println("order count"+ order_count);
		return order_count; 
	}

	private static void allocation_KPI(OntModel model) {

		//Check if allocation requirements are met?
		String s= "optimization strategies/FulfillmentPerOrder.rq";
	    List <QuerySolution> l= execute_query(s, model);
		print_results(l);

	    s= "optimization strategies/fullOrderFullfillement.rq";
	    l= execute_query(s, model);
	    print_results(l);

	    s= "optimization strategies/utilization.rq";
	    l= execute_query(s,model);
	    print_results(l);

	}

	private static void create_raw_material(OntModel model) {

		//Check if supplier has needed materials?
		String query_supplier= Prefix+ "SELECT ?subject WHERE { ?subject a :Supplier. ?subject :belongsToTier :SupplierTier"
				+hash_map.get("SupplierTier").get(0)
				+"} order by desc(?tier)";

		List<QuerySolution> l_suppl= execute (query_supplier, model);
		OntClass component = model.getOntClass( NS + "Component" );
		Individual comp = model.createIndividual( NS+"Component"+1, component);
		Individual comp2 = model.createIndividual( NS+"Component"+2, component);
     	Property has_component = model.getProperty(NS+"hasInputComponent");
     	Property component_quantity = model.getProperty(NS+"hasComponentQuantity");

     	int val=50;
     	int val2=100;
			for (QuerySolution querySolution : l_suppl) {
				Individual suppl = model.getIndividual(querySolution.get("subject").toString());

			if (querySolution.get("subject").toString().split("#")[1].equals(null)) {
				val = 100;
				val2 = 200;
			}

			Statement s = model.createStatement(suppl, has_component, comp);
			Statement s2 = model.createStatement(suppl, has_component, comp2);
			Resource r = model.createResource(s);
			Resource r2 = model.createResource(s2);
			r.addProperty(component_quantity, val2 + "");
			r2.addProperty(component_quantity, val + "");

		}
		
	}

	private static void read_parameters(BufferedReader br, OntModel model) throws NumberFormatException, IOException {

		String st;
		int count =0;

		while ((st = br.readLine()) != null)
		  {
			if (st.contains("/////")){
				count =1; continue;
			}
			
		  String[] param = st.split(":");
		  String[] param_string_values=  param[1].split(" ");
		  ArrayList<Integer> values= new ArrayList<>();
		  for (int i=1; i< param_string_values.length; i++)
		  {
			  values.add(Integer.parseInt(param_string_values[i]));
			  
		  }
		  if (count ==0)
		  hash_map.put(param[0],values);
		  if (count ==1)
			  hash_map_customer.put(param[0],values);
	}
		create_read_products (model); 
	
	}

	private static void create_read_products(OntModel model) throws IOException {

		//Check product properties (quantity, profitability, type)
		File file = new File("src/resources/products.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));

		OntClass product = model.getOntClass( NS + "Product" );
		Property product_profitability = model.getProperty(NS+"hasProductProfitability");
		Property product_comp_q = model.getProperty(NS+"hasComponentQuantity");
		Property product_comp = model.getProperty(NS+"needsComponent");

		br.lines().map(st -> st.split(" ")).forEach(param -> {
			String s = param[0];
			Individual product_ind = model.createIndividual(NS + s.split(":")[0], product);
			product_ind.addProperty(product_profitability, s.split(":")[1]);
			IntStream.range(1, param.length).forEach(i -> {
				String component = param[i].split(":")[0];
				String quantity = param[i].split(":")[1];
				Statement S = model.createStatement(product_ind, product_comp, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#" + component));
				Resource r = model.createResource(S);
				r.addProperty(product_comp_q, quantity);
			});
		});
}

	private static void print_results(List<QuerySolution> l) {

		//Output results
		for (QuerySolution querySolution : l) {
			Iterator<String> variables = querySolution.varNames();
			while (variables.hasNext()) {
				String var = variables.next();
				if (querySolution.get(var).toString().contains("integer") || querySolution.get(var).toString().contains("float")) {
					System.out.print(var + ": " + querySolution.get(var).toString().split("\\^")[0] + " ");
				} else if (querySolution.get(var).toString().contains("#"))
					System.out.print(var + ": " + querySolution.get(var).toString().split("#")[1] + " ");
				else
					System.out.print(var + ": " + querySolution.get(var).toString() + " ");

			}
			System.out.println(" ");
		}

	}

	private static boolean allocateComponent_supplier(int toFulfill, String component, OntModel model,int t,Individual portfolio, String order) throws IOException {

		//Allocate supplier components?
		//TODO rename string to what?
		String test= Prefix+ "Select * where"
				 	+"{"
					+"?snode :hasOEM :OEM1. "
					+"?snode :belongsToTier ?tier.\r\n"
				 	+"?snode :hasCapacity ?cap. "
					+"?cap :hasProduct ?p. "
					+"?cap :hasQuantity ?quantity. "
					+"?cap :hasTimeStamp ?capacitytime. "
					+"?snode :hasCapacitySaturation ?saturation. "
					+"?snode :hasLeadTime ?lt. \r\n"
					+"BIND (xsd:integer("+t+") - xsd:integer(?lt) as ?allocationtime).  \r\n"
				 	+"BIND (xsd:integer(?quantity) + xsd:integer("+ toFulfill+") as ?diff).   \r\n"
				 	+"FILTER  ( regex(str(?tier), \"SupplierTier1\") && regex(str(?p),\""+component.split("#")[1]+"\"))}";
		 List<QuerySolution> ll= execute (Prefix+test, model);

		//TODO rename string
		String q= Prefix+ "Select * \r\n"
				+"where \r\n"
				+"{"
				+"?snode :hasOEM :OEM1. "
				+"?snode :belongsToTier ?tier.\r\n"
				+"?snode :hasCapacity ?cap. "
				+"?cap :hasProduct ?p. "
				+"?cap :hasQuantity ?quantity. "
				+"?cap :hasPrice ?price."
				+"?cap :hasTimeStamp ?capacitytime. "
				+"?snode :hasCapacitySaturation ?saturation. "
				+"?snode :hasLeadTime ?lt. \r\n"
				+"BIND (xsd:integer("+t+") - xsd:integer(?lt) as ?allocationtime).  \r\n"
				+"BIND (xsd:integer(?quantity) + xsd:integer("+ toFulfill+") as ?diff).   \r\n"
				+"FILTER  (regex(str(?tier), \"SupplierTier1\")"
				+"&& (xsd:integer(?saturation)>= ?diff) && regex(str(?p),\""+component.split("#")[1]+"\")"
				+" && (xsd:integer(?allocationtime)= xsd:integer(?capacitytime))).\r\n }"  ;
		List<QuerySolution> l= execute (Prefix+q, model);

		if (l.size()>0)
		{
			String allocation_t= l.get(0).get("?capacitytime").toString();  
			System.out.println("Supplier"+ l.get(0).get("snode").toString().split("#")[1]+ "Quantity"+l.get(0).get("diff").toString().split("\\^")[0]+"component"+component+"time: "+ allocation_t); 
			allocate_supplier_product(l.get(0).get("snode").toString().split("#")[1], l.get(0).get("diff").toString().split("\\^")[0], model,  allocation_t, t);
			create_supply_plan(model, l.get(0).get("snode").toString().split("#")[1], portfolio, toFulfill+"",Integer.parseInt(allocation_t),component, l.get(0).get("price").toString(), order);
			return true;
		}
		else
		{
			return false;
		}
	}

	private static void allocate_supplier_product(String supplier, String y, OntModel model, String allocation_t, int t) {

		//Allocate required products at supplier
		//TODO rename string
		String test= Prefix+ "Select * "
				+"where \r\n"
				+"{:"+supplier+" :hasCapacity ?cap.\r\n"
				+"?cap :hasProduct ?p.\r\n"
				+"?cap :hasQuantity ?quantity.\r\n"
				+"?cap :hasTimeStamp \""+allocation_t+"\"."
				+"}\r\n" ;

		String quantity= Prefix+"DELETE   \r\n"
				+"{?cap :hasQuantity ?quantity.\r\n }\r\n"
				+"Insert {?cap :hasQuantity \""+y+"\"}\r\n"
				+"where \r\n"
				+"{:"+supplier+" :hasCapacity ?cap.\r\n"
				+"?cap :hasProduct ?p.\r\n"
				+"?cap :hasQuantity ?quantity.\r\n"
				+"?cap :hasTimeStamp \""+allocation_t+"\"."
				+"}\r\n" ;

		UpdateAction.parseExecute(quantity, model) ;
		print_results(execute (test, model));
		System.out.println("Chosen Supplier at time "+ allocation_t);
		propagate_capacity(allocation_t, t, supplier, y, model); 
		 
	}

	private static void propagate_capacity(String allocation_t, int t, String supplier, String y, OntModel model) {

		//Not sure
		IntStream.rangeClosed(Integer.parseInt(allocation_t), t).forEach(i -> {

			//TODO rename string
			String test = Prefix + "Select * "
					+ "where \r\n"
					+ "{:" + supplier + " :hasCapacity ?cap.\r\n"
					+ "?cap :hasProduct ?p.\r\n"
					+ "?cap :hasQuantity ?quantity.\r\n"
					+ "?cap :hasTimeStamp \"" + i + "\"."
					+ "}\r\n";
			print_results(execute(test, model));

			String quantity = Prefix + "DELETE   \r\n"
					+ "{?cap :hasQuantity ?quantity.\r\n }\r\n"
					+ "Insert {?cap :hasQuantity \"" + y + "\"}\r\n"
					+ "where \r\n"
					+ "{:" + supplier + " :hasCapacity ?cap.\r\n"
					+ "?cap :hasProduct ?p.\r\n"
					+ "?cap :hasQuantity ?quantity.\r\n"
					+ "?cap :hasPrice ?price.\r\n"
					+ "?cap :hasTimeStamp \"" + i + "\"."
					+ "}\r\n";
			UpdateAction.parseExecute(quantity, model);
			print_results(execute(test, model));
		});
	}

	/*
	private static void create_capacity(OntModel model) {

		String query_supplier= Prefix+ "SELECT ?subject WHERE { ?subject a :Supplier.}";
		List<QuerySolution> l_suppliers= execute (query_supplier,model); 
		Property capacity = model.getProperty(NS+"hasCurrentCapacity");
     	Property capacity_max = model.getProperty(NS+"hasMaximumCapacity");
     	Property comp_quantity = model.getProperty(NS+"hasComponentQuantity");

			for (QuerySolution l_supplier : l_suppliers) {
				String supp = l_supplier.get("subject").toString().split("#")[1];
				String get_component = Prefix + "Select ?comp ?quantity  where {<< :" + supp + " :hasOutputComponent ?comp >> :hasComponentQuantity ?quantity.}";

				List<QuerySolution> l_components = execute(get_component, model);
				Individual supplier = model.getIndividual(l_supplier.get("subject").toString());

			for (QuerySolution l_component : l_components) {

				Statement S = model.createStatement(supplier, capacity, model.createResource((l_component.get("comp").toString())));
				Resource r = model.createResource(S);
				r.addProperty(comp_quantity, 0 + "");
				Statement S2 = model.createStatement(supplier, capacity_max, model.createResource(l_component.get("comp").toString()));
				Resource r2 = model.createResource(S2);
				r2.addProperty(comp_quantity, l_component.get("quantity"));
			}
			if (supp.split("\\.")[1].contains("1"))// can be removed this is just to add more components
			{
				if (supp.split("\\.")[0].contains("1") || supp.split("\\.")[0].contains("2")) {


					Statement S = model.createStatement(supplier, capacity, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component11"));
					Resource r = model.createResource(S);
					r.addProperty(comp_quantity, 0 + "");
					Statement S2 = model.createStatement(supplier, capacity_max, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component11"));
					Resource r2 = model.createResource(S2);
					r2.addProperty(comp_quantity, 50 + "");
					S = model.createStatement(supplier, capacity, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component9"));
					r = model.createResource(S);
					r.addProperty(comp_quantity, 0 + "");
					S2 = model.createStatement(supplier, capacity_max, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component9"));
					r2 = model.createResource(S2);
					r2.addProperty(comp_quantity, 50 + "");

				} else {
					Statement S = model.createStatement(supplier, capacity, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component10"));
					Resource r = model.createResource(S);
					r.addProperty(comp_quantity, 0 + "");
					Statement S2 = model.createStatement(supplier, capacity_max, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component10"));
					Resource r2 = model.createResource(S2);
					r2.addProperty(comp_quantity, 100 + "");
					S = model.createStatement(supplier, capacity, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component12"));
					r = model.createResource(S);
					r.addProperty(comp_quantity, 0 + "");
					S2 = model.createStatement(supplier, capacity_max, model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#Component12"));
					r2 = model.createResource(S2);
					r2.addProperty(comp_quantity, 100 + "");
				}

			}
		}
	}

	 */

	private static boolean allocate(String product, int product_quantity, OntModel model, int t, Individual portfolio, String order) {

		//Check for suppliers for needed components in production
		String get_needed_components= "Select ?component ?quantity where { << :"+product.split("#")[1]+" :needsComponent ?component >> :hasComponentQuantity ?quantity}"; 
		List<QuerySolution> l_component_product= execute (Prefix+ get_needed_components,model);
		boolean success= true;

		for (QuerySolution querySolution : l_component_product) {
			String component = querySolution.get("component").toString();
			int quantity = product_quantity * Integer.parseInt(querySolution.get("quantity").toString().split("\\^")[0]);
			try {
				System.out.println("Needed Component " + component.split("#")[1] + "In quantity " + quantity);
				success = success && allocateComponent_supplier(quantity, component, model, t, portfolio, order);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success; 
	}

	private static List<QuerySolution> execute(String query, OntModel model )
	{
		return getQuerySolutions(query, model);
	}

	private static List<QuerySolution> getQuerySolutions(String query, OntModel model) {

		Query query2 = QueryFactory.create(query);
		return getQuerySolutions(model, query2);
	}

	private static List<QuerySolution> getQuerySolutions(OntModel model, Query query2) {

		QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
		ResultSet results = qe2.execSelect();
		List<QuerySolution> l= new ArrayList<>();
		while (results.hasNext())
		{l.add(results.next()); }
		return l;
	}

	private static List<QuerySolution> execute_query(String s, OntModel model) {
	File path = new File(s);
	Query query = QueryFactory.read(path.getAbsolutePath());
		return getQuerySolutions(model, query);

	}

	private static void create_relations(int tiers, ArrayList<Integer>tiers_array ,Individual oem, Property p, Property Node_node, Property node_oem, OntClass Node, OntModel model) {

		//
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

	private static void create_tiers_nodes(int tiers, ArrayList<Integer> tiers_array, OntClass Node, OntModel model) {

		//
		Property p_node = model.getProperty(NS+"belongsToTier");
		Property leadtime = model.getProperty(NS+"hasLeadTime");
		Property manufactures = model.getProperty(NS+"manufactures");
		Property hasLongitude = model.getProperty(NS+"hasLongitude");
		Property hasLatitude = model.getProperty(NS+"hasLatitude");
		Property hasTransportMode = model.getProperty(NS+"hasTransportMode");

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
        		List<String> transports= new ArrayList<>();
    			transports.add("vehicle");
    			transports.add("maritime");
    			transports.add("air");

				IntStream.rangeClosed(0, (int) (3.0 * Math.random())).forEach(m -> n.addProperty(hasTransportMode, transports.get((int) (3.0 * Math.random()))));
        		if (j==1 ||j ==3) {
        			n.addProperty(leadtime, 2+"");
        		n.addProperty(manufactures, "Product"+1);
        		}
        		else
        		{
        			n.addProperty(leadtime, 3+"");
            		n.addProperty(manufactures, "Product"+2);
        		}

        		List<QuerySolution> l= get_data_property(Node.getLocalName(), model);
				for (QuerySolution querySolution : l) {
					String property = querySolution.get("subject").toString().split("#")[1];
					if (Node.getLocalName().contains("Supplier") && hash_map.get(property) != null) {

						ArrayList<Integer> temp = hash_map.get(property);
						if (property.contains("Group")) {

							int c = 1;
							if (j > 2 && j <= 5) c = 2;
							if (j >= 5) c = 3;
							float nss = (i + c);

							int f = Math.round(nss);
							n.addProperty(model.getProperty(querySolution.get("subject").toString()), f + "");
						} else {
							int f = temp.get(0) + j;
							n.addProperty(model.getProperty(querySolution.get("subject").toString()), f + "");
						}

					}
					if (Node.getLocalName().contains("Customer") && hash_map_customer.get(property) != null) {
						ArrayList<Integer> temp = hash_map_customer.get(property);
						n.addProperty(model.getProperty(querySolution.get("subject").toString()),
								getRandomValue(temp.get(0), temp.get(1)));
					}
				}
            
        	}
        }
		
	}

	private static List<QuerySolution> get_data_property(String node, OntModel model) {

		//Rename string?
		 String querystring = Prefix +"SELECT Distinct ?subject \r\n"
				 +"	WHERE { ?subject rdfs:domain/(owl:unionOf/rdf:rest*/rdf:first)* :"
				 +node
				 +"}";
		return getQuerySolutions(querystring, model);
	}

	private static String getRandomValue(int i, int j) {

		//Why?
		Random r = new Random();
		int R = r.nextInt(j - i) + i;

		return String.valueOf(R);
		
	}

}
