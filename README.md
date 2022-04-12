# E2E_SC
This is a technical report for SENS-GEN a highly configurable synthetic data generator that, using input parameters and SENS model, produces an exemplary instance of an Supply Chain Network. 
SENS-GEN enables the generation of Supply Chain data for various industries, e.g., automotive and dairy, determined by the topology and properties of the instantiated output knowledge-graph.

----------------------------------------------------------------------------------------------------------------------------------------------------------------
Installation on a local machine:
--------------------------------

The code is accessible by a pull command of the project from the repository https://github.com/NourRamzy/E2E_SC into an IDE.  
The SENS-GEN code is set as a maven project that contains all the libraries and dependencies required to run the java project. 
The project is linked to a readme file with all the source code. 
The project is available under the DOI 10.5281/zenodo.5675085.

----------------------------------------------------------------------------------------------------------------------------------------------------------------
Resources: 
--------------------------------
In the source code, we create a resources folder, i.e.,src/main/resources where we define the input and the outputs resources for SENS-GEN.

Input:
------
SENS-GEN relies on three input files to run and behave as described:

•SENS Ontology: is the semantic model model
The current version of the ontology is stored in src/main/resources/generator.owl as an OWL file. 
We assign "http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6" as a local prefix for the ontology 

•Parametrization Input: details the input parameters and their corresponding values for SENS-GEN.
In the source code, this file is located in src/main/resources/configurationfile.txt. 
 
•Products  Input: defines  the  products  manufactured  by  the  Supply Chain. 
This file located in src/main/resources/products.txtdetails the


Output:
------
After running the code of SENS-GEN, the output is 

•SENS KG stored in src/main/java/output.ttl.

•SENS-GEN is capable of evaluating the performance of the instantiated KG.
The output values of the benchmarking process show on the IDE console and indicate the performance of the SCin the experimental setup.

----------------------------------------------------------------------------------------------------------------------------------------------------------------
Code Structure: 
--------------------------------

• create_OEM():  this function creates one instance of the class OEM and sets the values for the following properties: hasDeliveryTime,  hasTransportMode,  hasInventory
and the corresponding characteristics of an inventory hasProduct, hasCost, hasQuantity,hasTimeStamp
• create_tiers_nodes(): this function consists of create_Supplier() and create_Customer() methods that generate the SC nodes and corresponding tiers based on the input parameters.
• create_relations():  after  the  execution  of  this  method  nodes  are  connected  via hasUpStreamNode, hasDownStreamNodewhile tiers are linked with hasUpStreamTier,hasDownStreamTier.
• generation(): this function generates the initial values for capacity, inventory, saturation for all nodes.
• create_orders(): we assign orders to customer nodes and corresponding products, delivery times and quantities, i.e.,hasProduct, hasDeliveryTime,hasQuantity
• fulfillDemand(): implements the logic for demand fulfillment described in section 4.1.1.After the execution of this function supply, there is a supply plan specific for each order
• evaluationMetrics()implements the benchmarking and integrated analysis in experimental contexts 
We provide in Evaluation_KPI/folder variousSPARQL-based performance indicators, e.g., utilization
 