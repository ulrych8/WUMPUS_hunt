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

public class trackingSoloBehaviour extends SimpleBehaviour {
	
	private boolean finished = false;
	private List<String> occupiedNeighbor = new ArrayList<String>();
	private trackingHelperBehaviour trackingHelperBeha;
	private String supposedIdNodeGolem = "";

	public trackingSoloBehaviour(final AbstractDedaleAgent myagent, trackingHelperBehaviour trackingHelperBeha){
        super(myagent);
        this.trackingHelperBeha = trackingHelperBeha;
    }

    @Override
    public void action() {
        if (((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("TRACKING")) {
	        //0) Retrieve the current position
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
			if (myPosition!=null){
				//List of observable from the agent's current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 
				try {
					this.myAgent.doWait(500);
				} catch (Exception e) {
					e.printStackTrace();
				}*/
		
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
	  			String nextNode=null;
	  			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
	  			while( iter.hasNext() ){
	  				Couple<String, List<Couple<Observation, Integer>>> iterTemp = iter.next();
	  				//liste des observations
	  				List<Couple<Observation, Integer>> coupleObsInt = iterTemp.getRight();
	  				//noeud associé
	  				String nodeId = iterTemp.getLeft();
	  				if (! this.occupiedNeighbor.contains(nodeId)) {
		  				if (! nodeId.contentEquals(myPosition)) {
			  				for (Couple<Observation, Integer> info:coupleObsInt){
			  					if ( info.getLeft().getName().equals("Stench") ) {
			  						this.supposedIdNodeGolem = nodeId;
			  						boolean tryMove = ((AbstractDedaleAgent)this.myAgent).moveTo(nodeId);
			  						//boradcast les infos  ( prendre le move en compte ?) 
			  						this.broadcastTrackingInfo(nodeId,myPosition);
			  						//on reinitialise les voisins si on a bougé
			  						if (tryMove) {
			  							this.occupiedNeighbor.clear();
			  						}
			  						
			  						if (this.occupiedNeighbor.size()>0) {
			  							System.out.println("\n\nList de voisin : "+this.occupiedNeighbor+" de agent "+this.myAgent.getLocalName() +"\n_\n\n");
			  						}
			      					return;
			      				}
			      			}
		  				}
	  				}
	      		}
	  			//Arrive ici si plus de trace du golem
	  			System.out.println("\n\n\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+this.myAgent.getLocalName()+" passe de tracker a helper\n\n\n");
	  			this.trackingHelperBeha.setNodesId( /*Golem*/ this.supposedIdNodeGolem , /*obstacle*/"");
	  			((ExploreSoloAgent)this.myAgent).setBehaviourState("HELPER");
			}
        }
    }
    
    public void broadcastTrackingInfo(String nodeIdGolem, String nodeIdOccupied) {
    	//System.out.println("*********************************"+ this.myAgent.getLocalName()+" broadcast msg ******************************************");
    	//System.out.println("********************************* info : "+nodeIdGolem+" "+nodeIdOccupied +"******************************************");
    	//System.out.println(" alors que en "+((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
    	//message for EXPLORING AND HELPER
    	final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    	msg.setSender(this.myAgent.getAID());
    	msg.setContent(nodeIdGolem+","+nodeIdOccupied); //mettre info sur le trackage
    	
        if (!(myAgent.getLocalName().equals("Explo1"))) {
        	msg.addReceiver(new AID("Explo1", AID.ISLOCALNAME));
        }
        if (!(myAgent.getLocalName().equals("Explo2"))) {
        	msg.addReceiver(new AID("Explo2", AID.ISLOCALNAME));
        }
        if (!(myAgent.getLocalName().equals("Explo3"))) {
        	msg.addReceiver(new AID("Explo3", AID.ISLOCALNAME));
        }
        
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }
    
    public List<String> getOccupiedNeighbor() {
    	return this.occupiedNeighbor;
    }
    
    public void addOccupiedNeighbor(String nodeId) {
    	this.occupiedNeighbor.add(nodeId);
    }
    
    public void setSupposedIdNodeGolem(String idNodeStench) {
    	this.supposedIdNodeGolem = idNodeStench;
    }
    
    @Override
	public boolean done() {
		return finished;
	}
}