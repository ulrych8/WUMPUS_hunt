package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/*
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
*/

import org.graphstream.graph.ElementNotFoundException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploSoloBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;


	public ExploSoloBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
	}

	@Override
	public void action() {
		System.out.println("---------------->"+this.myAgent.getLocalName()+" behaviour state is "+((ExploreSoloAgent)this.myAgent).getBehaviourState());
		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> iterTemp = iter.next();
  				//liste des observations
  				List<Couple<Observation, Integer>> coupleObsInt = iterTemp.getRight();
  				//noeud associé
  				String nodeId = iterTemp.getLeft();
  				
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.openNodes.add(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);	
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
					}
					if (nextNode==null) nextNode=nodeId;
				}
				//Si explo behavior a la main sur le choix du prochain noeud on verifie que ca pue pas
				if (! ((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("TRACKING") ) {
					for (Couple<Observation, Integer> info:coupleObsInt){
	  					if ( info.getLeft().getName().equals("Stench") ) {
	  						((AbstractDedaleAgent)this.myAgent).moveTo(nodeId);
	  						System.out.println("Le choix du nouveau noeud a été pris ajout de behaviour");
	  						((ExploreSoloAgent)this.myAgent).setBehaviourState("TRACKING");
	      					return;
	      				}
	      			}
				}
				
			}

			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				//Explo finished
				finished=true;
				System.out.println("Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
				}
				
				
				
				/***************************************************
				** 		ADDING the API CALL to illustrate their use **
				*****************************************************/

				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
				
				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:

						System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
						System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						System.out.println(this.myAgent.getLocalName()+" - My expertise is: "+((AbstractDedaleAgent) this.myAgent).getMyExpertise());
						System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
						System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
						System.out.println(this.myAgent.getLocalName()+" - The agent grabbed : "+((AbstractDedaleAgent) this.myAgent).pick());
						System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						b=true;
						break;
					default:
						break;
					}
				}

				//If the agent picked (part of) the treasure
				if (b){
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println(this.myAgent.getLocalName()+" - State of the observations after picking "+lobs2);
					
					//Trying to store everything in the tanker
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					
				}
				
				//Trying to store everything in the tanker
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
				//System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());


				/************************************************
				 * 				END API CALL ILUSTRATION
				 *************************************************/
				/*------------------------------------------
				 * 				CHOOSING NEXT DIRECTION
				 -------------------------------------------*/
				if ( ((ExploreSoloAgent)this.myAgent).getBehaviourState().equals("EXPLORATION") ) {
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}
			}

		}
	}
		
	public void updateMap(ArrayList<List<String>> newEdges) {
		
		//add edges
		for (List<String> newEdge : newEdges) {
			//edge0
			String edge0 = newEdge.get(0);
			String edge0attribute = newEdge.get(1);
			//edge1
			String edge1= newEdge.get(2);
			String edge1attribute = newEdge.get(3);
			
			
			//TEST POUR EDGE0
			if (edge0attribute.contentEquals("closed")){
				if (!this.closedNodes.contains(edge0)) {  //maybe add getFullNode check
					if (this.openNodes.contains(edge0)){
						this.openNodes.remove(edge0);
					}
					this.closedNodes.add(edge0);
					this.myMap.addNode(edge0, MapAttribute.closed);				
				}
			}else {		//donc dans open nodes
				if (!this.closedNodes.contains(edge0)) {  //maybe add getFullNode check
					if (!this.openNodes.contains(edge0)){
						this.openNodes.add(edge0);
						this.myMap.addNode(edge0, MapAttribute.open);
					}
				}
			}
			
			//TEST POUR EDGE1
			if (edge1attribute.contentEquals("closed")){
				if (!this.closedNodes.contains(edge1)) {  //maybe add getFullNode check
					if (this.openNodes.contains(edge1)){
						this.openNodes.remove(edge1);
					}
					this.closedNodes.add(edge1);
					this.myMap.addNode(edge1, MapAttribute.closed);				
				}
			}else{		//donc dans open nodes
				if (!this.closedNodes.contains(edge1)) {  //maybe add getFullNode check
					if (!this.openNodes.contains(edge1)){
						this.openNodes.add(edge1);
						this.myMap.addNode(edge1, MapAttribute.open);
					}
				}
			}
			
			//try to add the edge
			this.myMap.addEdge(edge0, edge1);
		}
	}
		
	
	public MapRepresentation getMyMap() {
		return this.myMap;
	}
	
	
	@Override
	public boolean done() {
		return finished;
	}

}
