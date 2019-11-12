import java.util.ArrayList;

public class Job implements Comparable<Job>
{ 
	 /*
	  * JobDetails[5] positioning
	  */
	 final static int JOB_NAME = 0;
	 final static int LAST_RUN_DATE = 1;
	 final static int LAST_RUN_TIME = 2;
	 final static int JOB_FREQUENCY = 3;
	 final static int ELAP_TM = 4;
	 
	 final static int STORED_STRINGS = 10;
	 final static int STORED_LISTS = 5;
	 
	 private ArrayList<String> ProcName;
	 private ArrayList<String> ProcStep;
	 private ArrayList<String> ProgramName;
	 private ArrayList<String> DSNName;
	 private ArrayList<String> FileMode;
	 
	 private ArrayList<String> SuccessorList;
	 private ArrayList<String> PredecessorList;
	 
	 private String[] JobDetails;
 
	 public Job()
	 {
		  JobDetails = new String[STORED_STRINGS];
		  
		  ProcName = new ArrayList<String>();
		  ProcStep = new ArrayList<String>();
		  ProgramName = new ArrayList<String>();
		  DSNName = new ArrayList<String>();
		  FileMode = new ArrayList<String>();
		  SuccessorList = new ArrayList<String>();
		  PredecessorList = new ArrayList<String>();
	 }
 
	 /**
	  * 
	  * @param jobName The name of this Job. Used as a primary identifier of a Job.
		 public Job(String jobName)
	 {
		  JobDetails = new String[STORED_STRINGS];
		  JobDetails[JOB_NAME] = jobName; 
	 }
	   */

	 /**
	  * 
	  * @param outputCode An integer code that indicates the position of the String in question.
	  * @return A piece of information from JobDetailes[] in form of a String. 
	  */
	 public String get(int outputCode)
	 {
		 String output = "";
		 try
		 {
			   output = JobDetails[outputCode];
		 }
	     catch(ArrayIndexOutOfBoundsException AIOOBE)
		 {
			   System.out.println("<Job>::get(int x): x does not exist. Range: 0 - "+(JobDetails.length-1));
		 }
		 return output;
	 }
	 
	 /**
	  * 
	  * @param where An integer code that indicates which position value will take in JobDetails[].
	  * @param value The String to add to JobDetails[].
	  */
	 public void set(int where, String value)
	 {
		 try
		 {
			   JobDetails[where] = value;
		 }
		 catch(ArrayIndexOutOfBoundsException AIOOBE)
		 {
			   System.out.println("<Job>::s(int x, String S): x does not exist. Range: 0 - "+(JobDetails.length-1));
		 }
	 }
	 
	 /**
	  * 
	  * @param row The row to pull information from. 
	  * @return Returns a String array. The String array is a row of information about a Job.
	  */
	 public String[] getInformation(int row)
	 {
		 String[] info = new String[STORED_LISTS];
		 info[0] = ProcName.get(row);
		 info[1] = ProcStep.get(row);
		 info[2] = ProgramName.get(row);
		 info[3] = DSNName.get(row);
		 info[4] = FileMode.get(row);
		 return info;
	 }

	/**
	  * 
	  * @param procName The procedure name of the Job.
	  * @param procStep The procedure step within the procedure name.
	  * @param pgm The program that is fired by the procedure step.
	  * @param DSN The data source name the program in the procedure step uses.
	  * @param fileMode The file mode indicates whether this procedure uses an input, or creates an output.
	  * @return Returns a boolean. The boolean is true if all parameters were added successfully, and false otherwise.
	  */
	 public boolean addInformation(String procName, String procStep, String pgm, String DSN, String fileMode)
	 {
		  return ProcName.add(procName) && ProcStep.add(procStep) && ProgramName.add(pgm) && DSNName.add(DSN) && FileMode.add(fileMode);
	 }
	 
	 /**
	  * 
	  * @return Returns a String ArrayList of all successors.
	  */
	 public ArrayList<String> getSuccessor()
	 {
		 return SuccessorList;
	 }
	 
	 /**
	  * 
	  * @return Returns a String ArrayList of all predecessors.
	  */
	 public ArrayList<String> getPredecessor()
	 {
		  return PredecessorList;
	 }
	 
	 /**
	  * 
	  * @param pred The job name of the job which precedes this Job.
	  * @return Returns a boolean. The boolean is true if the predecessor was added successfully, and returns a false otherwise.
	  */
	 public boolean addPredecessor(String pred)
	 {
		  return PredecessorList.add(pred);
	 }
	 
	 /**
	  * 
	  * @param succ The job name of the job which succeeds this Job.
	  * @return Returns a boolean. The boolean is true if the predecessor was added successfully, and returns a false otherwise.
	  */
	 public boolean addSuccessor(String succ)
	 {
		  return SuccessorList.add(succ);
	 }

	/**
	  * 
	  * @param succList A list of successors to add into a job's predecessors, in the form of an array of Strings.
	  */
	 public void addSuccessor(String[] succList)
	 {
	  for(String succ : succList)
	   addSuccessor(succ);
	 }
	 
	 /**
	  * 
	  * @param predList A list of predecessors to add into a job's predecessors, in the form of an array of Strings.
	  */
	 public void addPredecessor(String[] predList)
	 {
		 for(String pred: predList)
			addPredecessor(pred); 
	 }
	 
	 /**
	  * 
	  * @return Returns an integer value. The integer is the number of different procedure names and procedure steps under a single job name.
	  */
	 public int numberOfVariations()
	 {
		  return ProcName.size();
	 }
	 
	 @Override
	 public String toString()
	 {
		  return JobDetails[JOB_NAME];
	 }
	 
	 @Override
	 public boolean equals(Object o)
	 {
		 if(o == this)
			 return true;
			 
		 if(! (o instanceof Job))
			 return false;
			  
		 Job J = (Job)o;
			  
		 boolean predEqual = this.getPredecessor().equals(J.getPredecessor());
		 boolean succEqual = this.getSuccessor().equals(J.getSuccessor());
		 boolean detailsEqual = this.JobDetails.equals(J.JobDetails);
		  
		 return  predEqual && succEqual && detailsEqual;
	 }
	 
	 @Override
	 public int compareTo(Job o) 
	 {
		  return (this.JobDetails[JOB_NAME].compareTo(((Job)o).JobDetails[JOB_NAME]));
	 }
 
}

