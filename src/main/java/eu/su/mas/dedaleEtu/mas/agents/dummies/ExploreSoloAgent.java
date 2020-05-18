package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.PingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.trackingHelperBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.trackingSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 * @author hc
 *
 */

public class ExploreSoloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	protected String behaviourState = "EXPLORATION"; //either this or HELPER or TRACKING

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		ExploSoloBehaviour exploSoloBeha = new ExploSoloBehaviour(this,this.myMap);
		lb.add(exploSoloBeha);
		trackingHelperBehaviour trackingHelperBeha = new trackingHelperBehaviour(this, exploSoloBeha);
		lb.add(trackingHelperBeha);
		trackingSoloBehaviour trackingSoloBeha = new trackingSoloBehaviour(this, trackingHelperBeha);
		lb.add(trackingSoloBeha);
		
		lb.add(new PingBehaviour(this));
		
		lb.add(new ReceiveMessageBehaviour(this,exploSoloBeha,trackingHelperBeha,trackingSoloBeha));
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	public String getBehaviourState() {
		return this.behaviourState;
	}
	
	public void setBehaviourState(String behaviourState) {
		this.behaviourState = behaviourState;
	}
	
	
}
