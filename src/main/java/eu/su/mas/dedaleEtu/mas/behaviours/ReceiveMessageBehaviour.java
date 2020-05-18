package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.trackingHelperBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

/**
 * This behaviour is a one Shot.
 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
 * 
 * @author Cédric Herpson
 *
 */
public class ReceiveMessageBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	
	private boolean finished=false;

	private ExploSoloBehaviour exploSoloBeha;
	private trackingHelperBehaviour trackingHelperBeha;
	private trackingSoloBehaviour trackingSoloBeha;
	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent
	 */
	public ReceiveMessageBehaviour(final Agent myagent, ExploSoloBehaviour exploSoloBeha, trackingHelperBehaviour trackingHelperBeha, trackingSoloBehaviour trackingSoloBeha) {
		super(myagent);
		this.exploSoloBeha = exploSoloBeha;
		this.trackingHelperBeha = trackingHelperBeha;
		this.trackingSoloBeha = trackingSoloBeha;
	}
	

	public void action() {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);			
		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		//1) receive the message for map
		final MessageTemplate msgMapTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage msgMap = this.myAgent.receive(msgMapTemplate);
		//1) receive the message for map
		final MessageTemplate msgTrackingInfoTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msgTrackingInfo = this.myAgent.receive(msgTrackingInfoTemplate);
		//1) receive the message for map
		final MessageTemplate msgHelpInfoTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msgHelpInfo = this.myAgent.receive(msgHelpInfoTemplate);
		
		if (msgTrackingInfo != null) {
			//check if not tracking
			System.out.println("_______________________________________"+this.myAgent.getLocalName()+" recois broadcast___________________________________________");
			System.out.println(this.myAgent.getLocalName()+" est en mode "+((ExploreSoloAgent)this.myAgent).getBehaviourState());
			
			if ( ((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("EXPLORATION") ) 
			{
				System.out.println(this.myAgent.getLocalName()+"<----TrackingInfo received from "+msgTrackingInfo.getSender().getLocalName()+" ,content= "+msgTrackingInfo.getContent());
	        	String[] trackingInfo = msgTrackingInfo.getContent().split(",");
	        	this.trackingHelperBeha.setNodesId( /*Golem*/ trackingInfo[0], /*obstacle*/trackingInfo[1]);
	        	((ExploreSoloAgent)this.myAgent).setBehaviourState("HELPER");
			}
			else if (((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("HELPER")) 
			{
				System.out.println(this.myAgent.getLocalName()+"<----TrackingInfo received from "+msgTrackingInfo.getSender().getLocalName()+" ,content= "+msgTrackingInfo.getContent());
	        	String[] trackingInfo = msgTrackingInfo.getContent().split(",");
	        	this.trackingHelperBeha.setNodesId( /*Golem*/ trackingInfo[0], /*obstacle*/trackingInfo[1]);
			}else if (((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("TRACKING")) 
			{
				System.out.println("\n|||||||||||||||||||||||||||||||||||||||||||||||"+this.myAgent.getLocalName()+" a bien recu le msg ||||||||||||||||||||||||||\n");
				System.out.println(this.myAgent.getLocalName()+"<----TrackingInfo between Trackers received from "+msgTrackingInfo.getSender().getLocalName()+" ,content= "+msgTrackingInfo.getContent());
				String[] trackingInfo = msgTrackingInfo.getContent().split(",");
				String idOccupied = trackingInfo[1];
				if (! this.trackingSoloBeha.getOccupiedNeighbor().contains(idOccupied)) {
					this.trackingSoloBeha.addOccupiedNeighbor(idOccupied);
				}
			}
		}
		
		if (msgHelpInfo != null) {
			System.out.println("_________________"+this.myAgent.getLocalName()+" recois broadcast___________________");
			System.out.println(this.myAgent.getLocalName()+" est en mode "+((ExploreSoloAgent)this.myAgent).getBehaviourState());
			if ( ((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("EXPLORATION") ) 
			{
				System.out.println(this.myAgent.getLocalName()+"<----helpInfo received from "+msgHelpInfo.getSender().getLocalName()+" ,content= "+msgHelpInfo.getContent());
	        	System.out.println("\n\n\n\n||||||||||||    RECOIT BIEN L INFO DU HELPER   |||||||||\n\n\n");
				String helpInfo = msgHelpInfo.getContent();
	        	this.trackingHelperBeha.setNodesId( /*Golem*/ helpInfo, /*obstacle*/"");
	        	((ExploreSoloAgent)this.myAgent).setBehaviourState("HELPER");
			}
		}
		
		if(msgMap != null) {
			System.out.println(this.myAgent.getLocalName()+"<----Map received from "+msgMap.getSender().getLocalName()+" ,content= "+msgMap.getContent());
			
			//System.out.println("\n\n   --------  ATTENTION MESSAGE   -------------\n\n");
			
			String mapString = String.valueOf(msgMap.getContent());
        	String[] sEdges = mapString.split(",");
        	List<String> tempEdges = Arrays.asList(sEdges);
        	
        	ArrayList<List<String>> Edges = new ArrayList<List<String>>();

        	
        	for (String stringEdge : tempEdges) {
        		List<String> temp = Arrays.asList(stringEdge.split("@"));
        		Edges.add(temp);
        	}
        	
        	//update map !
        	this.exploSoloBeha.updateMap(Edges);
        	
			//this.finished=true;
		}else if (msg != null) {		
			System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
			
			if (msg.getContent().matches("fillMyMap")) {
				System.out.println("The case of fill my map begin");
				//myAgent.addBehaviour(new passPlans(myAgent));
				final ACLMessage msg2 = new ACLMessage(ACLMessage.REQUEST);
	            msg2.setSender(this.myAgent.getAID());
	            msg2.addReceiver(new AID(msg.getSender().getLocalName(),AID.ISLOCALNAME));

            	String egdesString = this.exploSoloBeha.getMyMap().getMapString();

				msg2.setContent(egdesString );

	            this.myAgent.send(msg2);
			}
			//this.finished=true;
		}else{
			block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
		}
	}

	public boolean done() {
		return finished;
	}

}

