import java.io.IOException;
import java.sql.SQLException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.layout.HierarchicalLayout;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.DefaultMouseManager;
import org.graphstream.ui.view.util.MouseManager;

public class OperationsManualUI 
{
	private JLU JobLookupUnit;
	private String file;
 
	public OperationsManualUI()
	{
		JobLookupUnit = new JLU();
	}

	/**
	 * Deploys the Main Menu window.
	 */
	public void deployMainMenuUI()
	{
		int MENU_DIMENSION_WIDTH = JLU.parseIntFromXML("operationsManual", "mainMenuWidth");
		int MENU_DIMENSION_LENGTH = JLU.parseIntFromXML("operationsManual", "mainMenuLength");
		int menuRows = JLU.parseIntFromXML("operationsManual", "mainMenuRows");
		int menuCols = JLU.parseIntFromXML("operationsManual", "mainMenuColumns");
		
		final JFrame Frame = new JFrame("CLS Batch Job Flow");
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel MidPanel = new JPanel(new GridLayout(menuRows, menuCols));
		final JTextField FileName = new JTextField("Please load a Database");
		   
		final JButton LoadFileButton = new JButton("Load Database");
		final JButton GraphButton = new JButton("Job Graphs");
		final JButton JobSearchButton = new JButton("Job Search");
		JButton HelpButton = new JButton("Help");
		
		GraphButton.setEnabled(false); 
		JobSearchButton.setEnabled(false);
		   
		LoadFileButton.addActionListener(new ActionListener()
		{
				JFileChooser JFC = null;
				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					try
				    {
						FileName.setText("Loading file...");
						
					    JFC = new JFileChooser(JLU.DEFAULT_DIRECTORY);
					    JFC.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Access Databases", "accdb")); /*JFC.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Excel Spreadsheets", "xlsx"));*/
					    JFC.showOpenDialog(Frame);
					    
					    file = JFC.getSelectedFile().getPath();
					    
					    deployTableNameInput(Frame, FileName, JFC.getSelectedFile().getName());
					    
					    GraphButton.setEnabled(true);
					    JobSearchButton.setEnabled(true);
				    }
				    catch(NullPointerException NPE)
				    {
				    	System.out.println("Deploy Startup Window: No file selected.");
				    	JOptionPane.showMessageDialog(Frame, "No file selected!");
				    }	
					
					
				}
		   }													);
		   
		   GraphButton.addActionListener(new ActionListener()
		   {
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					try 
					{
						  JobLookupUnit.instantiateGatekeeperGraphs(file);
						  deployGraphWindow();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					catch(NullPointerException NPE)
					{
						JOptionPane.showMessageDialog(Frame, "No file selected!");
					}
				}
	   
		   }													);
		   
		   JobSearchButton.addActionListener(new ActionListener()
		   {
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					try 
					{
						deployJobInfoWindow(file);
					} 
					catch (NullPointerException e) 
					{
						JOptionPane.showMessageDialog(Frame, "No file selected!");
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
	   
		   }													);
		   
		   HelpButton.addActionListener(new ActionListener()
		   {
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					// TODO Auto-generated method stub
				}
	   
		   }												);
		   
		   FileName.setEditable(false);
		   
		   MidPanel.add(LoadFileButton);
		   MidPanel.add(GraphButton);
		   MidPanel.add(JobSearchButton);
		   MidPanel.add(HelpButton);
		   
		   Panel.add(FileName, BorderLayout.SOUTH);
		   Panel.add(MidPanel, BorderLayout.CENTER);
		   
		   /*
		    * Main Menu Border Visual Padding
		    */
		   Panel.add(new JPanel(), BorderLayout.NORTH);
		   Panel.add(new JPanel(), BorderLayout.WEST);
		   Panel.add(new JPanel(), BorderLayout.EAST);
		   
		   Frame.add(Panel);
		   
		   Frame.setPreferredSize(new Dimension(MENU_DIMENSION_WIDTH, MENU_DIMENSION_LENGTH));
		   Frame.setResizable(false);
		   Frame.pack();
		   Frame.setVisible(true);
		   Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 }
	
	/**
	 * A window that asks the user to specify the exact names of the schedule and dataset tables in the selected Database.
	 */
	public void deployTableNameInput(final JFrame ParentFrame, final JTextField TextFieldToUpdate, final String fileName)
	{
		int tableRows = JLU.parseIntFromXML("operationsManual","tableNameRows");
		int tableCols = JLU.parseIntFromXML("operationsManual","tableNameColumns");
		int textFieldLength = JLU.parseIntFromXML("operationsManual","tableNameTextFieldLength");
		int textFieldWidth = JLU.parseIntFromXML("operationsManual","tableNameTextFieldHeight");
		
		ParentFrame.setVisible(false);
		
		final JFrame Frame = new JFrame("Table names for: " + fileName);
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel MidPanel = new JPanel(new GridLayout(tableRows, tableCols));
		JLabel ScheduleLabel = new JLabel("Schedule Report Table Name:");
		JLabel DatasetLabel = new JLabel("Dataset Report Table Name:");
		final JTextField ScheduleTextArea = new JTextField();
		final JTextField DatasetTextArea = new JTextField();
		final JButton OkButton = new JButton("OK");
		
		ScheduleTextArea.setPreferredSize(new Dimension(textFieldLength ,textFieldWidth));
		DatasetTextArea.setPreferredSize(new Dimension(textFieldLength, textFieldWidth));
		
		OkButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				boolean valid = false;
				
				try 
				{
					System.out.println("Checking table names\n");
					valid = JobLookupUnit.checkTableNames(file, DatasetTextArea.getText(), ScheduleTextArea.getText());
				} 
				catch (SQLException SQLE) 
				{
					JOptionPane.showMessageDialog(OkButton, "Table names not found! Try again.");
				}
				catch(NullPointerException NPE)
				{ 
					JOptionPane.showMessageDialog(OkButton, "Table names not found! Try again.");
				}
				if(valid)
				{
					JobLookupUnit.setTableNames(DatasetTextArea.getText(), ScheduleTextArea.getText());
					
					JOptionPane.showMessageDialog(Frame, "Table names accepted.");
					Frame.setVisible(false);
					ParentFrame.setVisible(true);
					
					TextFieldToUpdate.setText(fileName);
				}
				
			}
		});
		
		Action pressOk = new AbstractAction() 
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				OkButton.doClick();
			}
			
		};
		
		OkButton.getActionMap().put("pressOk", pressOk);		
		OkButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "pressOk");		
		
		
		Frame.setPreferredSize(new Dimension(450,125));
		
		MidPanel.add(DatasetLabel);
		MidPanel.add(DatasetTextArea);
		MidPanel.add(ScheduleLabel);
		MidPanel.add(ScheduleTextArea);
		
		Panel.add(new JPanel(), BorderLayout.NORTH);
		Panel.add(OkButton, BorderLayout.SOUTH);
		Panel.add(new JPanel(), BorderLayout.EAST);
		Panel.add(new JPanel(), BorderLayout.WEST);
		Panel.add(MidPanel, BorderLayout.CENTER);
		
		Frame.add(Panel);
		
		Frame.pack();
		Frame.setVisible(true);
	}
	 
	 /**
	  * Deploys the Job Search window.
	  * @param windowName The name of the Frame, which includes the Excel file's name.
	  */
	 public void deployJobInfoWindow(String windowName) throws IOException, NullPointerException
	 {
		  int FRAME_WIDTH = JLU.parseIntFromXML("operationsManual", "jobMenuWidth");
		  int FRAME_LENGTH = JLU.parseIntFromXML("operationsManual", "jobMenuLength");
		  int jobRows = JLU.parseIntFromXML("operationsManual", "jobMenuRows");
		  int jobCols = JLU.parseIntFromXML("operationsManual", "jobMenuColumns");
		    
		  final JFrame JobInfoFrame = new JFrame("Job Search: "+windowName);
		  
		  final JPanel Panel = new JPanel();
		  JPanel MainPanel = new JPanel(new BorderLayout());
		  JPanel GridPanel = new JPanel(new BorderLayout());
		  JPanel TopPanel = new JPanel(new GridLayout(jobRows, jobCols));
		  
		  JLabel JobNameLabel = new JLabel("Job Name:");
		  
		  String[] jobNamesArray =  JobLookupUnit.scoutJDBCData(file);
		  final JComboBox<String> JobNameDropdown = new JComboBox<String>(jobNamesArray); 
		  
		  final JButton SearchButton = new JButton("Search!");
		  
		  final JTextField JobNameText = new JTextField();
		  final JTextField Frequency = new JTextField();
		  final JTextField LastRunDate = new JTextField();
		  final JTextField LastRunTime = new JTextField();
		  final JTextField ElapTime = new JTextField();
		  
		  Frequency.setEditable(false); LastRunDate.setEditable(false); LastRunTime.setEditable(false); ElapTime.setEditable(false);
		  
		  JobInfoFrame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_LENGTH));
		  JobNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		  JobNameText.setToolTipText("Enter Job Name manually and select Search!");
		  
		  SearchButton.addActionListener(new ActionListener() 
		  {
			  int HEADER_ROW = 1;
			  int JOB_NAME_COLUMN = 1;
			
			  @Override
			  public void actionPerformed(ActionEvent e)
			  {
				  Panel.removeAll();
			    
			      try 
			      {
			    	  /*
			    	   * If the JTextField is empty, it searches for the selected Job in the ComboBox
			    	   * Conditional Assignment Syntax: (value = if this is true ? use this value : otherwise use this)
			    	   */
			    	  Job jobInQuestion = JobNameText.getText().isEmpty() ? 
			    			  JobLookupUnit.captureJobData(file, JobNameDropdown.getItemAt(JobNameDropdown.getSelectedIndex()))
			    			  : JobLookupUnit.captureJobData(file, JobNameText.getText());
				     
				       Frequency.setText(jobInQuestion.get(Job.JOB_FREQUENCY));
				       LastRunDate.setText(jobInQuestion.get(Job.LAST_RUN_DATE));
				       LastRunTime.setText(jobInQuestion.get(Job.LAST_RUN_TIME));
				       ElapTime.setText(jobInQuestion.get(Job.ELAP_TM));
				     
				       Panel.add(new JLabel("PROCNAME"));
				       Panel.add(new JLabel("PROCSTEP"));
				       Panel.add(new JLabel("PGM"));
				       Panel.add(new JLabel("DSN NAME"));
				       Panel.add(new JLabel("FILE MODE"));
				     
				       for(int i = 0; i<jobInQuestion.numberOfVariations(); ++i) 
				       {
					         String[] additionalInfo = jobInQuestion.getInformation(i);
					       
					         for(String S: additionalInfo)
					         {
						          JTextField newLabel = new JTextField(S);
						          Panel.add(newLabel);
					         }
				       
				       }
				       Panel.setLayout(new GridLayout(jobInQuestion.numberOfVariations() + HEADER_ROW, Job.STORED_LISTS + JOB_NAME_COLUMN));
			     
			      }
			      catch(NullPointerException NPE)
			      {
			    	  System.out.println("Job Search Window: NullPointerException on JLU::getJob()");
			    	  JOptionPane.showMessageDialog(JobInfoFrame, "No file selected!");
			    	  throw NPE;
			      }
			    
			      JobInfoFrame.revalidate();
			      JobInfoFrame.repaint();
			}
			   
		   }             									);
		 	  
		  Action pressSearch = new AbstractAction()
		  {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				SearchButton.doClick();
			}
	  
		  };
		  
		  SearchButton.getActionMap().put("pressSearch", pressSearch);
		  SearchButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "pressSearch");
		  
		  JobNameDropdown.addActionListener(new ActionListener() 
		  {
			  @Override
			  public void actionPerformed(ActionEvent arg0) 
			  {
				  JobNameText.setText("");
			  }
		  });
		  JScrollPane ScrollPane = new JScrollPane(Panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		  
		  GridPanel.add(ScrollPane, BorderLayout.CENTER);
		  
		  MainPanel.add(GridPanel, BorderLayout.CENTER);
		  
		  TopPanel.add(JobNameLabel);
		  TopPanel.add(JobNameDropdown);
		  TopPanel.add(JobNameText);
		  TopPanel.add(SearchButton);
		  TopPanel.add(new JLabel("FREQUENCY:"));
		  TopPanel.add(new JLabel("LAST RUN DATE:"));
		  TopPanel.add(new JLabel("LAST RUN TIME"));
		  TopPanel.add(new JLabel("ELAPTM:"));
		  TopPanel.add(Frequency);
		  TopPanel.add(LastRunDate);
		  TopPanel.add(LastRunTime);
		  TopPanel.add(ElapTime);
		  
		  MainPanel.add(TopPanel, BorderLayout.NORTH);
		  
		  JobInfoFrame.add(MainPanel);
		  JobInfoFrame.pack();
		  JobInfoFrame.setVisible(true);
	 }
	 
	 /**
	  * Deploys the window where Graphs can be drawn per Gatekeeper Job
	  * @throws IOException
	  * @throws NullPointerException
	  */
	 public void deployGraphWindow() throws IOException, NullPointerException
	 {
		  int DIMENSION_WIDTH = JLU.parseIntFromXML("operationsManual", "graphMenuLength");
		  int DIMENSION_LENGTH = JLU.parseIntFromXML("operationsManual", "graphMenuWidth");
		 
		  final JFrame GraphFrame = new JFrame("Job Flow Graphs");
		  GraphFrame.setPreferredSize(new Dimension(DIMENSION_WIDTH, DIMENSION_LENGTH));
		  final JPanel Panel = new JPanel(new BorderLayout());
		  final JComboBox<String> ComboBox = new JComboBox<String>(JobLookupUnit.getGatekeeperList());
		  JButton Button = new JButton("OK");
		  
		  System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		  
		  for(int i = 0; i < JobLookupUnit.getGatekeeperList().length; ++i)
			  JobLookupUnit.getJobFlowGraph(i).addAttribute("ui.stylesheet", "node{fill-color:green; size-mode:fit; stroke-mode:plain; shape:freeplane;size:15px,15px; text-size:20;} "
			  		+ "node.pinned{fill-color:yellow;}"
			  		+ "edge{shape:cubic-curve; }");
		  
		  
		  Button.addActionListener(new ActionListener() 
		  {
			   @Override
			   public void actionPerformed(ActionEvent e) 
			   {
				    final Graph G = JobLookupUnit.getJobFlowGraph(ComboBox.getSelectedIndex());
				    JobLookupUnit.expandGraph(ComboBox.getSelectedIndex());//TODO Debug
				    
				    final Viewer V = new Viewer(G, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
				    V.enableAutoLayout(new HierarchicalLayout());
				    V.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
				    final ViewPanel VP = V.addDefaultView(false); 
				    
				    int centerX = JLU.parseIntFromXML("operationsManual", "graphViewCenterX");
				    int centerY = JLU.parseIntFromXML("operationsManual", "graphViewCenterY");
				    int centerZ = JLU.parseIntFromXML("operationsManual", "graphViewCenterZ");
				    
				    VP.getCamera().setViewCenter(centerX, centerY, centerZ);
				    VP.getCamera().setViewPercent(2);
			   
				    MouseManager MM = new DefaultMouseManager() 
				    {
					     Point3 originPoint;
					     
					     @Override
					     public void mousePressed(MouseEvent event)
					     {	
						      super.mousePressed(event);
						      
						      curElement = view.findNodeOrSpriteAt(event.getX(), event.getY());
						      if(curElement != null)
						      {
						    	  switch(event.getButton())
						    	  {
							    	  case 1:
							    	  {
							    		  Node N = G.getNode(curElement.getId());
							    		  GraphicNode GN = V.getGraphicGraph().getNode(N.getId());
							    		  GN.removeAttribute("ui.class");
							    	  }
							    	  case 3:	//TODO: Right click actions
							    	  {
							    		  
							    	  }
						    	  }
						    	  
						      }
						      else
						    	  originPoint = VP.getCamera().transformPxToGu(event.getX(), event.getY());
					     }
				     
					     @Override
					     public void mouseReleased(MouseEvent event)
					     {
						      super.mouseReleased(event);
						      curElement = view.findNodeOrSpriteAt(event.getX(), event.getY());
						      
						      if(curElement != null)
						      {
						    	  Node N = G.getNode(curElement.getId());
						    	  GraphicNode GN = V.getGraphicGraph().getNode(N.getId());
						    	  
						    	  switch(event.getButton())
						    	  {
							    	  case 1:
							    	  {
									       N.addAttribute("layout.frozen");
									       N.addAttribute("x", GN.getX());
									       N.addAttribute("y", GN.getY());
									       GN.addAttribute("ui.class", "pinned");
							    	  }
							    	  case 3:
							    	  {
							    		  
							    	  }
						    	  }
							       
						      }
					     }
				     
					     @Override
					     public void mouseDragged(MouseEvent event)
					     {
					    	 super.mouseDragged(event);
					    	 
					         if(curElement == null)
					         {
						          Point3 difference = VP.getCamera().transformPxToGu(event.getX(), event.getY());
						          Point3 center = VP.getCamera().getViewCenter();
						          VP.getCamera().setViewCenter(center.x - (difference.x - originPoint.x), center.y - (difference.y - originPoint.y), center.z);
					         }
					     
					     }
			    											};
		    
		    VP.addMouseWheelListener(new MouseWheelListener()
		    {
		    	//10% zoom/pan per mouse scroll
		    	double ZOOM_COEFFICIENT = JLU.parseDoubleFromXML("operationsManual", "graphZoomCoefficient"); 
		    	
		    	@Override
		    	public void mouseWheelMoved(MouseWheelEvent event)
		    	{
		    		double zoom = VP.getCamera().getViewPercent() + (event.getWheelRotation()*(ZOOM_COEFFICIENT));
		    		
		    		if(zoom > 0)
		    			VP.getCamera().setViewPercent(zoom);
		    	}
		     
		    }													);
		    
		    VP.setMouseManager(MM);
		    
		    JScrollPane ScrollPane = new JScrollPane(VP, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		    
		    try
		    {
			     BorderLayout layout = (BorderLayout) Panel.getLayout();
			     Panel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
		    }
		    catch(NullPointerException NPE)
		    {
		    	System.out.println("\tGraph Window: Nothing to remove.");
		    }
		    Panel.add(ScrollPane, BorderLayout.CENTER); 
		    
		    Panel.revalidate();
		    Panel.repaint();
		   }
		   
		  }														);
		  
		  
		  Panel.add(ComboBox, BorderLayout.NORTH);
		  Panel.add(Button, BorderLayout.SOUTH);
		   
		  
		  GraphFrame.add(Panel);
		  GraphFrame.pack();
		  GraphFrame.setVisible(true);
	 }
	    
	 public static void main(String[] args)
	 {
		 OperationsManualUI OMUI = new OperationsManualUI();
		   
		 OMUI.deployMainMenuUI();
	 }
}
