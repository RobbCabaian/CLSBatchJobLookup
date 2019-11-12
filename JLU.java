import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
public class JLU
{
	  private String scheduleTableName;
	  private String datasetTableName;
	  private String[] gatekeeperJobsList;
      private Graph[] JobGraphs;
      
      private Connection DatabaseConnection;
      
      final static String DEFAULT_DIRECTORY = parseStringFromXML("jlu", "defaultDirectory");

      public JLU()
      {
    	  DatabaseConnection = null;
          scheduleTableName = null;
          datasetTableName = null;
          
          gatekeeperJobsList = parseStringFromXML("jlu", "gatekeeperJobs").trim().split("\\s+");
          
          JobGraphs = new DefaultGraph[gatekeeperJobsList.length];
      }
      
      /**
	   * Accessor for the verified table name of the dataset report
	   * @return The name of the dataset report table
	   */
	  public String getDatasetTableName()
	  {
		  return datasetTableName;
	  }

	  /**
	   * Accessor for the verified table name of the schedule report
	   * @return The name of the schedule report table
	   */
	  public String getScheduleTableName()
	  {
		  return scheduleTableName;
	  }
	  
	  /**
	   * An accessor for the String[] that holds all the static Gatekeeper Batch Jobs
	   * @return A String[] containing all Gatekeeper Jobs
	   */
	  public String[] getGatekeeperList()
	  {
		  return gatekeeperJobsList;
	  }
      
     /**
	  * 
	  * @param which An integer that indicates which graph to return. There is one Graph per Job Frequency type. 
	  * @return Returns the indicated GraphStream Graph. 
	  */
	  public Graph getJobFlowGraph(int which)
	  {
		  return JobGraphs[which];
	  }

	/**
	 * Checks whether the given table names are present in the Database.
	 * @paran databaseFilePath The file path of the Database.
	 * @param datasetTable The name of the table that contains the dataset report.
	 * @param scheduleTable The name of the table that contains the schedule report.
	 */
	public boolean checkTableNames(String databaseFilepath, String datasetTable, String scheduleTable) throws SQLException, NullPointerException
	{
	    Statement statement = null;
	    ResultSet scheduleResultSet = null;
	    ResultSet datasetResultSet = null;
	    
	    String testSchedQuery = "SELECT ENV FROM " + datasetTable;
	    String testDatasetQuery = "SELECT JOB FROM " + scheduleTable;
	    
	    boolean datasetExtracted = false;
	    boolean scheduleExtracted = false;
	    
	    try 
	    {
	    	System.out.println("\tEstablishing connection.\n");
	    	
	    	// Loading or registering Oracle JDBC driver class
	    	Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
	        
	    	// Double forward slashes is a must.
	        String dbURL = "jdbc:ucanaccess://" + databaseFilepath; 	
	        DatabaseConnection = DriverManager.getConnection(dbURL); 
	        
	        System.out.println("\tConnection established.\n");
	        
	        statement = DatabaseConnection.createStatement();
	
	        scheduleResultSet = statement.executeQuery(testSchedQuery);
	        datasetResultSet = statement.executeQuery(testDatasetQuery);
	        
	        System.out.println("\tQueries executed.\n");

	        
	        scheduleExtracted = scheduleResultSet != null;
	        datasetExtracted = datasetResultSet != null;
	        
	        if(!scheduleExtracted)
	        	throw new NullPointerException("Invalid Schedule Table Name.");
	        if(!datasetExtracted)
	        	throw new NullPointerException("Invalid Dataset Table Name.");
	    }
	    catch(ClassNotFoundException cnfex) 
	    {
	        System.out.println("Problem in loading or registering MS Access JDBC driver");
	        cnfex.printStackTrace();
	    }
	    
	    return (datasetExtracted && scheduleExtracted);
	}

	/**
	 * Internally sets the table names for the dataset and schedule reports. 
	 * @param datasetTable The verified name of the dataset report table
	 * @param scheduleTable The verified name of the schedule report table
	 */
	public void setTableNames(String datasetTable, String scheduleTable)
	{
		datasetTableName = new String(datasetTable);
		scheduleTableName = new String(scheduleTable);
	}
	
	public void instantiateGatekeeperGraphs(String filepath)
	{
		for(int i = 0; i < gatekeeperJobsList.length; ++i)
	    {
System.out.println("GATEKEEPER: " + gatekeeperJobsList[i]);
	    	JobGraphs[i] = new DefaultGraph(gatekeeperJobsList[i]);
	 	    JobGraphs[i].setStrict(false);
	 	    JobGraphs[i].setAutoCreate(true);
	 	    
	 	    JobGraphs[i].addNode(gatekeeperJobsList[i]).addAttribute("ui.label", gatekeeperJobsList[i]);
	 	    
	 	    Connection connection = null;
		    Statement statement = null;
		    ResultSet resultSet = null;
		    
		    try 
		    {
				Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
				String dbURL = "jdbc:ucanaccess://" + filepath; 	
		          
		        connection = DriverManager.getConnection(dbURL); 	
		
		        statement = connection.createStatement();
		
		        resultSet = statement.executeQuery("SELECT PREDECESSOR, SUCCESSOR"
		        								 + " FROM " + scheduleTableName
		        								 + " WHERE JOB = \'"+gatekeeperJobsList[i]+"\'");
		        
		        while(resultSet.next())
		        {
		        	 String succs[] = resultSet.getString(2).trim().split("\\s+");

		        	 //TODO Possible feature: Let the user indicate whether to expand via Successors, or via Predecessors
		        	 //addPredecessorsToGraph(JobGraphs[i], gatekeeperJobsList[i],  preds); 
		        	 
				     addSuccessorsToGraph(JobGraphs[i], gatekeeperJobsList[i], succs);
		        }
			} 
		    catch (ClassNotFoundException e) 
		    {
				e.printStackTrace();
			} 
		    catch (SQLException e) 
		    {
				e.printStackTrace();
			}
	    }
	}

	  /**
	   * Expands a single Graph with a Gatekeeper Job as its root
	   * @param whichGraph The Graph to fully expand, indicated by its integer position in JobGraphs[]
	   */
	  public void expandGraph(int whichGraph)
	  {
		  Graph G = JobGraphs[whichGraph];
		  Statement statement = null;
	      ResultSet RS = null;
	      ResultSet RS2 = null;
	      
	      
	      /*
	       * 2 Types of Possible Lists:
	       * Queue: Graph will populate itself breadth-first
	       * Stack: Graph will populate itself depth-first
	       */
//	      Queue<String> Queue = new LinkedList<String>();
	      Stack<String> Stack = new Stack<String>();
	      
	      System.out.println("\tExpanding Graph: " + gatekeeperJobsList[whichGraph]);
	      
	      try
	      {
	    	  statement = DatabaseConnection.createStatement();
	    	  RS = statement.executeQuery("SELECT SUCCESSOR"
	    	  							+ " FROM " + scheduleTableName
	    	  							+ " WHERE JOB = \'" + gatekeeperJobsList[whichGraph] + "\'");
	    	  /*
	    	   * Initially load List with first level of successors
	    	   */
	    	  while(RS.next())
	    	  {
	    		  System.out.println("\tResult Set extracted:" + RS.getString(1));
	    		  
	    		  String succs[] = RS.getString(1).trim().split("\\s+");
	    		  
	    		  for(String S : succs)
	    		  {
	    			  System.out.println("\t -->"+S);
//	    			  Queue.add(S);
	    			  Stack.push(S);
	    		  }
	    	  }
    		  //***************************************************************************************
	    	  
	    	  while(/*!Queue.isEmpty()*/!Stack.isEmpty())
	    	  {
	    		  String nextJob = Stack.pop();
//	    		  String nextJob = Queue.remove();
	    		  
	    		  System.out.println("\tFrom List: "+ nextJob);

    			  RS2 = statement.executeQuery("SELECT SUCCESSOR"
 	    		  							+ " FROM " + scheduleTableName
 	    		  							+ " WHERE JOB = \'" + nextJob + "\'");
 	    		  String[] nextSuccs = null;
 	    		 
 	    		  while(RS2.next())	//Ensures RS is not empty
 	    		  {
 	    			  nextSuccs = RS2.getString(1).trim().split("\\s+");
 	    			 
 	    			  System.out.print("\t\tPushed to list: ");
 	    			  for(String S : nextSuccs)
 	    			  {
 	    				  if(/*!Queue.contains(S)*/!Stack.contains(S) && G.getNode(S) == null)
 	    				  {
 	    					  //Queue.add(S);
 	    					  Stack.push(S);
 	    					  System.out.println("\t\t\t"+S);
 	    				  }
 	    			  }
 	    			  System.out.println();
 	    			  addSuccessorsToGraph(G, nextJob, nextSuccs);
 	    		  }
    			  
	    		 }
	    		  
	    	  System.out.println("List empty.");
	      }
	      catch(SQLException sqlex)
	      {
	    	  sqlex.printStackTrace();
	      }
		  
	  }

	  public Job captureJobData(String filepath, String JobName)
	  {
		  Job J = new Job();
		  Statement statement = null;
		  ResultSet resultSet = null; 
		  
		  try
		  {
			  statement = DatabaseConnection.createStatement();
			  
			  String scheduleQuery = parseStringFromXML("jlu", "scheduleSelectClause")  
					  			  + " FROM " + getScheduleTableName() 
					  			  + " WHERE JOB = \'" + JobName + "\'";
			  resultSet = statement.executeQuery(scheduleQuery);
			  
			  System.out.println(JobName + " extracted via SQL!");
			  
			  while(resultSet.next() ) 
			  {
				  String jobName = resultSet.getString(1).trim();
		          String lastRunDate = resultSet.getString(2).trim();
		          String lastRunTime = resultSet.getString(3).trim();
		          String jobFrequency = resultSet.getString(4).trim();
		          String elapTm = resultSet.getString(5).trim();         
		          
		          J.set(Job.JOB_NAME, jobName);
		          J.set(Job.LAST_RUN_DATE, lastRunDate);
		          J.set(Job.LAST_RUN_TIME, lastRunTime);
		          J.set(Job.JOB_FREQUENCY, jobFrequency);
		          J.set(Job.ELAP_TM, elapTm);
		          
		          String datasetQuery = parseStringFromXML("jlu","datasetSelectClause") + "FROM " + getDatasetTableName() + parseStringFromXML("jlu", "datasetWhereClause");
		          ResultSet datasetResultSet = statement.executeQuery(datasetQuery + "\'" + jobName + "\'");
		          
		          while(datasetResultSet.next())
		          {
		        	  String procName = datasetResultSet.getString(1);
			          String procStep = datasetResultSet.getString(2);
			          String pgm = datasetResultSet.getString(3);
			          String dsn = datasetResultSet.getString(4);
			          String fileMode = datasetResultSet.getString(5);
			          
		        	  J.addInformation(procName, procStep, pgm, dsn, fileMode);
		        	  
		        	  System.out.println(J + " extracted!");
		          }
			  }
			  
		  }
		  catch(SQLException sqlex)
	      {
	          sqlex.printStackTrace();
	      }
		  
		  return J; 
	  }

	/**
	   * Capture data from the Microsoft Access Database
	   * @param filename The file name of the Microsoft Access Database
	   *///TODO: Update
	  public String[] scoutJDBCData(String filepath)
	  {
		  String[] JobList = {};
		  ArrayList<String> JobListBuilder = new ArrayList<String>();
		  
		  Statement statement = null;
	      ResultSet JobsResultSet = null;
	      
	      try 
	      {
	    	  System.out.println("\tScouting JDBC Data...");
	          statement = DatabaseConnection.createStatement();
	
	          String scheduleQuery = "SELECT JOB FROM " + getScheduleTableName();
	          JobsResultSet = statement.executeQuery(scheduleQuery);
	
	          while(JobsResultSet.next() ) 
		          JobListBuilder.add(JobsResultSet.getString(1).trim());
	          
	      }
	      catch(SQLException sqlex)
	      {
	          sqlex.printStackTrace();
	      }
	      
	      JobList = JobListBuilder.toArray(JobList);
	      return JobList;
	  }
	  
	  /**
	   * 
	   * @param N The node to add some weight to.
	   * @param weight The weight of the node. This is an integer that indicates the repulsion force with other nodes in the graph. 
	   * A value of 0-1 makes a node less repulsive to other nodes around it, while a value of more than 1 will increase a node's repulsion
	   * with other nodes.
	   */
	  public void addNodeWeight(Node N, double weight)
	  {
		  N.addAttribute("layout.weight", weight);
	  }		

	  public int identifyGraphByGatekeeper(Job jobInQuestion)
	  {
		  System.out.print("Sorting: " + jobInQuestion + "\t");
		  for(int i = 0; i < JobGraphs.length; ++i)
		  {
			  //Already in the Graph
			  if(JobGraphs[i].getNode(jobInQuestion.get(Job.JOB_NAME)) != null)
				  return -1;
			  
			  for(int j = 0; j < jobInQuestion.getPredecessor().size(); ++j)
			  {
				  if(JobGraphs[i].getNode(jobInQuestion.get(Job.JOB_NAME))  != null)
				  {
					  
					  return i;
				  }

			  }
		  }
		  
		  return -1;
	  }

	  /**
       * 
       * @param whichGraph Determines which graph jobName resides in. Jobs are separated into different graphs depending on Job Frequency.
       * @param jobName The Job to add predecessors to.
       * @param predecessorList The list of predecessors to add to a Job.
       * @return Returns a boolean. The boolean is true if the predecessors were successfully added, and false otherwise. 
       */
      private boolean addPredecessorsToGraph(Graph whichGraph, String jobName, String[] predecessorList)
      {
    	  boolean success = false;
	      for(String predecessor : predecessorList)
	      {
			    if(predecessor.isEmpty())
			    {
			    	 success = false;
				     break;
			    }
		        else
		        {
			    	  whichGraph.addEdge(predecessor+"->"+jobName, predecessor, jobName, true);
				      whichGraph.getNode(predecessor).addAttribute("ui.label", predecessor);
				      
				      addNodeWeight(whichGraph.getNode(predecessor), .0001);
				      success = true;
			    }
	     
	       }   
	      return success;
      }
      
      /**
       * 
       * @param whichGraph Determines which graph jobName resides in. Jobs are separated into different graphs depending on Job Frequency.
       * @param jobName The Job to add successors to.
       * @param successorList The list of successors to add to a Job.
       * @return Returns a boolean. The boolean is true if the successors were successfully added, and false otherwise. 
       */
      private boolean addSuccessorsToGraph(Graph whichGraph, String jobName, String[] successorList)
      {
	       boolean success = false;
	       
	       if(successorList == null)
	       {
	    	   System.out.println("\tEmpty successor list");
	    	   return false;
	       }
	       
	       for(String successor : successorList)
	       {
	    	   	 if(successor.isEmpty())
		  	     {
			  	      success = false;
			  	      break;
		  	     }
		  	     else
		  	     {
			  	     if(whichGraph.getEdge(jobName+"->"+successor) == null && whichGraph.getEdge(successor+"->"+jobName)==null)
			  	     {
			  	    	 whichGraph.addEdge(jobName+"->"+successor, jobName, successor, true);
				  	     whichGraph.getNode(successor).addAttribute("ui.label", successor);
				  	     whichGraph.getNode(jobName).addAttribute("ui.label", jobName);
				  	      
				  	     addNodeWeight(whichGraph.getNode(successor), 50);
				  	     success = true;
				  	     System.out.println("\t\tAdded Edge: "+jobName+"->"+successor);
			  	     }
			  	     else
			  	    	 System.out.println("\t\tEdge already exists:"+jobName+"->"+successor);
		  	    	 
		  	     }
	  	    }
	  
	       return success;
       }
      
      /**
       * Function call to extract useful data from the XML configuration file.
       * @param element The name of the element to pull from.
       * @param attribute The name of the element's attribute to pull. 
       * @return Returns the value of the depicted data
       */
      public static String parseStringFromXML(String element, String attribute)
	  {
    	  File inputFile = new File("CLSJobFlowConfig.xml");
	      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder dBuilder;
	      String target = null;
	          
	  	  try{
	  			dBuilder = dbFactory.newDocumentBuilder();
	  			Document doc = dBuilder.parse(inputFile);
	  			doc.getDocumentElement().normalize();
	  	        NodeList nList = doc.getElementsByTagName(element);
	  	        
	  	        for (int temp = 0; temp < nList.getLength(); temp++) 
	  	        {
	  	            org.w3c.dom.Node nNode = nList.item(temp);
	  	            
	  	            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) 
	  	            {
	  	               Element eElement = (Element) nNode;
	  	               
	  	               target = eElement.getElementsByTagName(attribute).item(0).getTextContent();
	  	            }
	  	        }
	  		} 
	  		catch (ParserConfigurationException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (SAXException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (IOException e) 
	  		{
	  			e.printStackTrace();
	  		}
	          
	  		return target;
	  	}

      public static int parseIntFromXML(String element, String attribute)
	  {
	  	  File inputFile = new File("CLSJobFlowConfig.xml");
	      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder dBuilder;
	      int targetInt = 0;
	          
	  	  try{
	  			dBuilder = dbFactory.newDocumentBuilder();
	  			Document doc = dBuilder.parse(inputFile);
	  			doc.getDocumentElement().normalize();
	  	        NodeList nList = doc.getElementsByTagName(element);
	  	        
	  	        for (int temp = 0; temp < nList.getLength(); temp++) 
	  	        {
	  	            org.w3c.dom.Node nNode = nList.item(temp);
	  	            
	  	            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) 
	  	            {
	  	               Element eElement = (Element) nNode;
	  	               
	  	               targetInt = Integer.parseInt(eElement.getElementsByTagName(attribute).item(0).getTextContent());
	  	            }
	  	        }
	  		} 
	  		catch (ParserConfigurationException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (SAXException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (IOException e) 
	  		{
	  			e.printStackTrace();
	  		}
	          
	  		return targetInt;
	  	}
      
      public static double parseDoubleFromXML(String element, String attribute)
	  {
	  	  File inputFile = new File("CLSJobFlowConfig.xml");
	      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder dBuilder;
	      double targetDouble = 0;
	          
	  	  try{
	  			dBuilder = dbFactory.newDocumentBuilder();
	  			Document doc = dBuilder.parse(inputFile);
	  			doc.getDocumentElement().normalize();
	  	        NodeList nList = doc.getElementsByTagName(element);
	  	        
	  	        for (int temp = 0; temp < nList.getLength(); temp++) 
	  	        {
	  	            org.w3c.dom.Node nNode = nList.item(temp);
	  	            
	  	            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) 
	  	            {
	  	               Element eElement = (Element) nNode;
	  	               
	  	               targetDouble = Double.parseDouble(eElement.getElementsByTagName(attribute).item(0).getTextContent());
	  	            }
	  	        }
	  		} 
	  		catch (ParserConfigurationException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (SAXException e) 
	  		{
	  			e.printStackTrace();
	  		} 
	  		catch (IOException e) 
	  		{
	  			e.printStackTrace();
	  		}
	          
	  		return targetDouble;
	  	}
      
}
