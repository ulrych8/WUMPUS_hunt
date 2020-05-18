package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graphstream.graph.ElementNotFoundException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class trackingHelperBehaviour extends SimpleBehaviour {
	
	private boolean finished = false;
	private String idNodeGolem = "";
	private String idOccupied = "";
	private ExploSoloBehaviour exploSoloBeha;
	
    public trackingHelperBehaviour(final AbstractDedaleAgent myagent, ExploSoloBehaviour exploSoloBeha){
        super(myagent);
        this.exploSoloBeha = exploSoloBeha;
    }

    @Override
    public void action() {
    	//0) check if trackingSoloBehaviour didn't started
    	if (!((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("HELPER")) {
    		//this.done();
    		return;
    	}
        if (this.idNodeGolem.isEmpty() && this.idOccupied.isEmpty()) {
        	return;
        }
        //1) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		MapRepresentation myMap = this.exploSoloBeha.getMyMap();
		try {
			myMap.getGraph().getNode(this.idOccupied);
		}catch(NullPointerException E){
			//nous sommes dans le cas où notre agent n'as pas encore recu la map
			return;
		}
		System.out.println("je suis "+this.myAgent.getLocalName()+" en "+myPosition+" et idNodeGolem is "+this.idNodeGolem+" , idOccupied is "+this.idOccupied);
		
		//broadcast helper info
		this.broadcastHelperInfo(this.idNodeGolem);
		System.out.println(" \n\tMessage broadcast helper 3333333333333333333333333333333333333333333 \n");
		
		List<String> listPath = myMap.getShortestPathUnoccupied(myPosition, this.idNodeGolem, this.idOccupied);
	
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~je suis "+this.myAgent.getLocalName()+" en "+myPosition+"Le chemin à faire est "+listPath+"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		String nextNode = listPath.get(0);
		if (myPosition.equals(this.idNodeGolem) ) {
			System.out.println("\n\n\n ######################################## Nothing to find on idNodeGolem##############################\n\n\n");
			((ExploreSoloAgent)this.myAgent).setBehaviourState("EXPLORATION");
			//this.done();
			return;
		}
		System.out.println("----------------------------------Je dois aller au "+nextNode+" and my position is"+myPosition+"----------------------------------");
		try {
			this.myAgent.doWait(250);
		} catch (Exception e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
    }
    
    public void setNodesId(String idNodeGolem, String idOccupied) {
    	this.idNodeGolem = idNodeGolem;
    	this.idOccupied = idOccupied;
    }
    
    public void broadcastHelperInfo(String nodeIdGolem) {
    	//System.out.println("*********************************"+ this.myAgent.getLocalName()+" broadcast msg ******************************************");
    	//System.out.println("********************************* info : "+nodeIdGolem+" "+nodeIdOccupied +"******************************************");
    	//System.out.println(" alors que en "+((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
    	
    	//message for EXPLORER
    	final ACLMessage msgHelpInfo = new ACLMessage(ACLMessage.PROPAGATE);
        msgHelpInfo.setSender(this.myAgent.getAID());
        msgHelpInfo.setContent(nodeIdGolem);
        
        if (!(myAgent.getLocalName().equals("Explo1"))) {
        	msgHelpInfo.addReceiver(new AID("Explo1", AID.ISLOCALNAME));
        }
        if (!(myAgent.getLocalName().equals("Explo2"))) {
        	msgHelpInfo.addReceiver(new AID("Explo2", AID.ISLOCALNAME));
        }
        if (!(myAgent.getLocalName().equals("Explo3"))) {
        	msgHelpInfo.addReceiver(new AID("Explo3", AID.ISLOCALNAME));
        }

        
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msgHelpInfo);
    }
    
    @Override
	public boolean done() {
		return finished;
	}
}