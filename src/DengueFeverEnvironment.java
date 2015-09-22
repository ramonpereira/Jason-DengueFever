// Environment code
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DengueFeverEnvironment extends Environment {
  
    private DengueModel model;
    private DengueView view;
    
    public static final int GRID_SIZE = 10;
  
    public enum Move {
        UP, DOWN, RIGHT, LEFT
    }

    private Map<String, String> agents = new HashMap<String, String>();
    private Logger logger = Logger.getLogger("DengueFeverSetup.mas2j"+DengueFeverEnvironment.class.getName());
    
    Term                    clear    = Literal.parseLiteral("clear_waterspot");
    Term                    lay_eggs = Literal.parseLiteral("lay_eggs");
    Term                    mate     = Literal.parseLiteral("mate");
    Term                    up       = Literal.parseLiteral("move(up)");
    Term                    down     = Literal.parseLiteral("move(down)");
    Term                    right    = Literal.parseLiteral("move(right)");
    Term                    left     = Literal.parseLiteral("move(left)");
    Term                    random   = Literal.parseLiteral("move(random)");
    
    Map<String, Integer> sickPeople = new ConcurrentHashMap<String, Integer>();
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
      model = new DengueModel();
      view  = new DengueView(model);
      model.setView(view);
      for (int id = 0; id < model.getNbOfAgs(); id++)
      {
        Location l = model.getAgPos(id);
        if(id < 5){
          addPercept("person"+(id + 1), Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
        } else if (id >= 5 && id <= 9 ){
          addPercept("inspector"+(id - 4), Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
        } else {
          addPercept("mosquito"+(id - 9), Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));
        }
      }
    }

    public void sickPeopleMaintenance(){
    	for(Map.Entry<String,Integer> e: sickPeople.entrySet()){
    		if(e.getValue() == 0)
    			sickPeople.remove(e.getKey());
    		else 
    			e.setValue(e.getValue()-1);
    	}
    }
    
    @Override
    public boolean executeAction(String agName, Structure action) {
    	try {
	        // parse that file
	        jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileInputStream("/Users/ramonfragapereira/Workspace/Jason-DengueFever/DengueFeverSetup.mas2j"));
	        MAS2JProject project;
			project = parser.mas();
			 List<String> names = new ArrayList<String>();
		     // get the names from the project
		     for (AgentParameters ap : project.getAgents()) {
		    	 System.out.println(ap.toString());
		    	 
		    	 System.out.println("Name: " + ap.getAgName());
		    	 System.out.println("Class: " + ap.getAgArchClasses());
		     }
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
      int agId = -1;
      try {
        // Get agent id
        if(agName.contains("person")) {
          agId = Integer.parseInt(agName.substring(6)) - 1;
          Thread.sleep(400);
        } else if(agName.contains("inspector")){
          agId = 5 + Integer.parseInt(agName.substring(9)) - 1;
          Thread.sleep(800);
        } else if(agName.contains("mosquito")) {
          agId = 10 + Integer.parseInt(agName.substring(8)) - 1;
          Thread.sleep(1000);
        } else {
          return false;
        }
        updatePercepts(agName, agId);
        // Clear
        if (action.equals(clear)) {
          Location l = model.getAgPos(agId);
          if (model.hasObject(DengueModel.CLEAN_WATERSPOT, l.x, l.y)) {
            model.remove(DengueModel.CLEAN_WATERSPOT, l.x, l.y);
            logger.warning(agName + " is trying to clear, the waterspot at " + l.x + "x" + l.y + "!");
          } else if (model.hasObject(DengueModel.INFECTED_WATERSPOT, l.x, l.y)) {
            model.remove(DengueModel.INFECTED_WATERSPOT, l.x, l.y);
            logger.warning(agName + " is trying to clear, the waterspot at " + l.x + "x" + l.y + "!");
          } else {
            logger.warning(agName + " is trying to clear, but there is no waterspot at " + l.x + "x" + l.y + "!");
          }
        // Lay eggs
        } else if (action.equals(lay_eggs)) {
          Location l = model.getAgPos(agId);
          if (model.hasObject(DengueModel.CLEAN_WATERSPOT, l.x, l.y)) {
            model.remove(DengueModel.CLEAN_WATERSPOT, l.x, l.y);
            model.add(DengueModel.INFECTED_WATERSPOT, l.x, l.y);
          } else if (model.hasObject(DengueModel.INFECTED_WATERSPOT, l.x, l.y)) {
            logger.warning(agName + " is trying to infect, but waterspot already is infected at " + l.x + "x" + l.y + "!");
          } else {
            logger.warning(agName + " is trying to infect, but there is no waterspot at " + l.x + "x" + l.y + "!");
          }
          
          
        // Sting
        } else if(action.getFunctor().equals("sting")) {
          Location l = new Location(Integer.parseInt(action.getTerm(0).toString()), Integer.parseInt(action.getTerm(1).toString()));
          int otheragId = model.getAgAtPos(l);
          if (otheragId < 5) {
            addPercept("person" + otheragId, Literal.parseLiteral("sting"));
            sickPeople.put("person" + otheragId, 10);
          } else if(otheragId <= 9) {
            addPercept("inspector" + (otheragId - 4), Literal.parseLiteral("sting"));
            sickPeople.put("inspector" + (otheragId - 4), 10);
          } else {
            logger.warning(agName + " is trying to sting, but there is no agent at " + l.x + "x" + l.y + "!");
          }
        // Mate
        } else if (action.equals(mate)) {
          logger.info(agName + " trying to mate");
        // Awareness
        } else if(action.getFunctor().equals("awareness")) {
          Location l = new Location(Integer.parseInt(action.getTerm(0).toString()), Integer.parseInt(action.getTerm(1).toString()));
          int otheragId = model.getAgAtPos(l);
          if (otheragId >= 0 && otheragId < 5) {
            addPercept("person" + otheragId, Literal.parseLiteral("aware"));
            logger.info("person" + otheragId + " has been warned about dengue by " + agName);
          } else {
            logger.warning(agName + " is trying to bring awareness, but there is no person at " + l.x + "x" + l.y + "!");
          }
        // Movement
        } else if (action.equals(up)) {
            model.move(Move.UP, agId);
        } else if (action.equals(down)) {
            model.move(Move.DOWN, agId);
        } else if (action.equals(right)) {
            model.move(Move.RIGHT, agId);
        } else if (action.equals(left)) {
            model.move(Move.LEFT, agId);
        } else if(action.equals(random)){
          // Make random movement
          //logger.info(agName + " doing action " + action.getFunctor());
          Random random = new Random();
          int move = random.nextInt(4);
          if(move == 0){
            model.move(Move.UP, agId);
          } else if(move==1){
            model.move(Move.DOWN, agId);
          } else if(move==2){
            model.move(Move.RIGHT, agId);
          } else {
            model.move(Move.LEFT, agId);
          }
          //logger.info(agName + " has finished action " + action.getFunctor());
        } else {
          logger.info(agName + " executing: " + action + ", but not implemented!");
        }
        //informAgsEnvironmentChanged();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "error executing " + action + " for " + agName + " (ag code:"+agId+")", e);
      }
      return true;
    }
    
    /** creates the agents perception */
    void updatePercepts(String agName, int agId) {
      clearAllPercepts();
      Location l = model.getAgPos(agId);
      Literal pos = ASSyntax.createLiteral("pos", ASSyntax.createNumber(l.x), ASSyntax.createNumber(l.y));
      
      addPercept(agName, pos);
      // Person has # cells vision
      if(agName.contains("person"))
      {
        updateAgPercept(agName, agId, l.x - 1, l.y - 1);
        updateAgPercept(agName, agId, l.x - 1, l.y);
        updateAgPercept(agName, agId, l.x - 1, l.y + 1);
        updateAgPercept(agName, agId, l.x, l.y - 1);
        updateAgPercept(agName, agId, l.x, l.y);
        updateAgPercept(agName, agId, l.x, l.y + 1);
        updateAgPercept(agName, agId, l.x + 1, l.y - 1);
        updateAgPercept(agName, agId, l.x + 1, l.y);
        updateAgPercept(agName, agId, l.x + 1, l.y + 1);
      }
      // Inspector has <> vision
      else if(agName.contains("inspector"))
      {
        updateAgPercept(agName, agId, l.x - 1, l.y - 1);
        updateAgPercept(agName, agId, l.x - 1, l.y);
        updateAgPercept(agName, agId, l.x - 2, l.y);
        updateAgPercept(agName, agId, l.x - 1, l.y + 1);
        updateAgPercept(agName, agId, l.x, l.y - 1);
        updateAgPercept(agName, agId, l.x, l.y - 2);
        updateAgPercept(agName, agId, l.x, l.y);
        updateAgPercept(agName, agId, l.x, l.y + 1);
        updateAgPercept(agName, agId, l.x, l.y + 2);
        updateAgPercept(agName, agId, l.x + 1, l.y - 1);
        updateAgPercept(agName, agId, l.x + 1, l.y);
        updateAgPercept(agName, agId, l.x + 2, l.y);
        updateAgPercept(agName, agId, l.x + 1, l.y + 1);
      }
      // Mosquito has + vision
      else if(agName.contains("mosquito"))
      {
        addPercept(agName, Literal.parseLiteral("gender(" + (agId % 2 == 0 ? "male" : "female") + ")"));
        updateAgPercept(agName, agId, l.x - 1, l.y);
        updateAgPercept(agName, agId, l.x, l.y - 1);
        updateAgPercept(agName, agId, l.x, l.y);
        updateAgPercept(agName, agId, l.x, l.y + 1);
        updateAgPercept(agName, agId, l.x + 1, l.y);
      }
    }
    
    public static Atom aOBSTACLE  = new Atom("obstacle");
    public static Atom aWATERSPOT = new Atom("waterspot");
    public static Atom aPERSON    = new Atom("person");
    public static Atom aINSPECTOR = new Atom("inspector");
    public static Atom aMMOSQUITO  = new Atom("mmosquito");
    public static Atom aFMOSQUITO  = new Atom("fmosquito");
    public static Atom aEMPTY     = new Atom("empty");
    
    private void updateAgPercept(String agName, int agId, int x, int y) {
      if (model == null || !model.inGrid(x,y)) return;
      if (model.hasObject(DengueModel.OBSTACLE, x, y)) {
        addPercept(agName, createCellPerception(x, y, aOBSTACLE));
      } else if (model.hasObject(DengueModel.CLEAN_WATERSPOT, x, y) || model.hasObject(DengueModel.INFECTED_WATERSPOT, x, y)) {
        addPercept(agName, createCellPerception(x, y, aWATERSPOT));
      } else {
        int otherag = model.getAgAtPos(x, y);
        if(otherag >= 0 && otherag != agId)
        {
          if(otherag < 5){
            addPercept(agName, createCellPerception(x, y, aPERSON));
          } else if (otherag >= 5 && otherag <= 9 ){
            addPercept(agName, createCellPerception(x, y, aINSPECTOR));
          } else {
            addPercept(agName, createCellPerception(x, y, otherag % 2 == 0 ? aMMOSQUITO : aFMOSQUITO));
          }
        } else {
          addPercept(agName, createCellPerception(x, y, aEMPTY));
        }
      }
    }
    
    public static Literal createCellPerception(int x, int y, Atom obj) {
      return ASSyntax.createLiteral("cell", ASSyntax.createNumber(x), ASSyntax.createNumber(y), obj); 
    }
    
  class DengueModel extends GridWorldModel {
    
    public static final int   CLEAN_WATERSPOT  = 16;
    public static final int   INFECTED_WATERSPOT  = 32;
    
    private DengueModel() {
      super(GRID_SIZE, GRID_SIZE, 15);
      
      /* Initial location of Agents (Randomically) */
      Random random = new Random();
      
      this.scenario3();
      
      int i = 0;
      while (i<15){
      	int x = random.nextInt(GRID_SIZE);
      	int y = random.nextInt(GRID_SIZE);
      	if (isFree(x, y)) {
      		setAgPos(i, x, y);
      		i++;
      	}
      }
      clearPercepts();
    }
    
    public void scenario1(){
        add(OBSTACLE, 0, 0);
        add(OBSTACLE, 0, GRID_SIZE - 1);
        add(OBSTACLE, GRID_SIZE - 1, 0);
        add(OBSTACLE, GRID_SIZE - 1, GRID_SIZE - 1);
        
        add(OBSTACLE, 3, 5);
        add(OBSTACLE, 4, 5);
        add(OBSTACLE, 5, 5);
        add(OBSTACLE, 6, 5);
        
        add(CLEAN_WATERSPOT, 4, 6);
        add(CLEAN_WATERSPOT, 2, 0);
        add(CLEAN_WATERSPOT, 8, 4);

        add(INFECTED_WATERSPOT, 6, 0);
        add(INFECTED_WATERSPOT, 4, 8);
        add(INFECTED_WATERSPOT, 1, 5);
    }
    
    public void scenario2(){
        add(CLEAN_WATERSPOT, 5, 1);
        add(CLEAN_WATERSPOT, 9, 6);
        
        add(INFECTED_WATERSPOT, 3, 8);
        add(INFECTED_WATERSPOT, 2, 0);
        add(INFECTED_WATERSPOT, 4, 5);
        add(INFECTED_WATERSPOT, 7, 4);
    }
    
    public void scenario3(){
    	add(OBSTACLE, 6, 2);
        add(OBSTACLE, 7, 2);
        add(OBSTACLE, 8, 2);
        add(OBSTACLE, 9, 2);
        
        add(OBSTACLE, 1, 6);
        add(OBSTACLE, 2, 6);
        add(OBSTACLE, 3, 6);
        add(OBSTACLE, 4, 6);
        
        add(CLEAN_WATERSPOT, 5, 1);
        add(CLEAN_WATERSPOT, 2, 9);
        add(CLEAN_WATERSPOT, 6, 5);
        
        add(INFECTED_WATERSPOT, 5, 6);
        add(INFECTED_WATERSPOT, 4, 0);
        add(INFECTED_WATERSPOT, 6, 2);
    }
    
    boolean move(Move dir, int ag) {
      Location l = getAgPos(ag);
      switch (dir) {
        case UP:
          if (isFree(l.x, l.y - 1)) {
            setAgPos(ag, l.x, l.y - 1);
          }
          break;
        case DOWN:
          if (isFree(l.x, l.y + 1)) {
            setAgPos(ag, l.x, l.y + 1);
          }
          break;
        case RIGHT:
          if (isFree(l.x + 1, l.y)) {
            setAgPos(ag, l.x + 1, l.y);
          }
          break;
        case LEFT:
          if (isFree(l.x - 1, l.y)) {
            setAgPos(ag, l.x - 1, l.y);
          }
          break;
      }
      return true;
    }
  }
    
  class DengueView extends GridWorldView {
  
    private static final long serialVersionUID = 575313892689481648L;
  
    public DengueView(DengueModel model) {
      super(model, "Dengue Fever", 600);
      defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
      setVisible(true);
      repaint();
    }
      
    @Override
    public void draw(Graphics g, int x, int y, int object) {
    	sickPeopleMaintenance();
      switch (object) {
      	case  DengueModel.CLEAN_WATERSPOT:    drawWaterspot(g, x, y, false);  break;
      	case  DengueModel.INFECTED_WATERSPOT: drawWaterspot(g, x, y, true);  break;
      }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
      String label = "";
      if(id < 5){
    	c = Color.blue;
    	if(sickPeople.containsKey("person"+(id + 1)))
    		c = Color.BLACK;
    		
        label = "P-"+(id+1);
      } else if (id >= 5 && id <= 9 ){
    	c = Color.red;
      	if(sickPeople.containsKey("inspector"+(id + 1)))
    		c = Color.BLACK;
      	
        label = "I-"+(id-4);
        c = Color.red;
      } else {
        label = "M-"+(id-9);
        c = (id % 2 == 0 ? Color.green : Color.ORANGE);
      }
      
      super.drawAgent(g, x, y, c, -1);
      g.setColor(Color.white);
      super.drawString(g, x, y, defaultFont, label);
    }
    
    public void drawWaterspot(Graphics g, int x, int y, boolean infected) {
      g.setColor(Color.cyan);
      g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
      if (infected)
      {
        g.setColor(Color.darkGray);
        super.drawString(g, x, y, defaultFont, "Eggs");
      } else {
        g.setColor(Color.gray);
        super.drawString(g, x, y, defaultFont, "Clear");
      }
    }
  }
}

