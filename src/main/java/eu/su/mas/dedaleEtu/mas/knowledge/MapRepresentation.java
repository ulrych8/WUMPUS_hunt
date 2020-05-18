package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.*;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;

/**
 * <pre>
 * This simple topology representation only deals with the graph, not its content.
 * The knowledge representation is not well written (at all), it is just given as a minimal example.
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * </pre>
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {
		agent,open,closed
	}

	private static final long serialVersionUID = -1333959882640838272L;
	
	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: green;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);
		
		openGui();
		
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id Id of the node
	 * @param mapAttribute associated state of the node
	 */
	public void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
		//System.out.println("check this : "+String(sg.getAllNodes()));
		//System.out.println("check that fella : "+sg.toString());
	}

	/**
	 * Add the edge if not already existing.
	 * @param idNode1 one side of the edge
	 * @param idNode2 the other side of the edge
	 */
	public void addEdge(String idNode1,String idNode2){
		try {
			this.nbEdges++;
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (EdgeRejectedException e){
			//Do not add an already existing one
			this.nbEdges--;
		}

	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow
	 */
	public List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		shortestPath.remove(0);//remove the current position
		return shortestPath;
	}
	
	public List<String> getShortestPathUnoccupied(String idFrom,String idTo, String idOccupied){
		//System.out.println("id from is : "+idFrom);
		//System.out.println("id to is : "+idTo);
		//System.out.println("id Occupied is : "+idOccupied);
		List<String> shortestPath=new ArrayList<String>();
		Graph gOccupied = Graphs.clone(g);
		if (!idOccupied.isEmpty()) {
			try {
			gOccupied.removeNode(idOccupied);	//enleve le noeud occupé
			}catch(ElementNotFoundException e) {
				System.out.println("Element idOccupied not found in the graph...");
			}
		}
		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(gOccupied);
		System.out.println("See if node can be catch : "+gOccupied.getNode(idTo));
		List<Node> path = new ArrayList<Node>();
		try {
			dijkstra.setSource(gOccupied.getNode(idFrom));
			dijkstra.compute();//compute the distance to all nodes from idFrom
			path=dijkstra.getPath(gOccupied.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
			//System.out.println("path dans le try ");
		}catch(NullPointerException E) {
			System.out.println("\n\t\t Nullpointer execption\n");
		}catch(IllegalStateException E) {
			System.out.println("\n\t IllegalStateException execption\n");
		}finally {
			//si aucun chemin défini on va jusqu'au golem meme si c est pas possible
			if (! (path.size() > 0)) {
				dijkstra = new Dijkstra();//number of edge
				dijkstra.init(g);
				dijkstra.setSource(g.getNode(idFrom));
				dijkstra.compute();//compute the distance to all nodes from idFrom
				try {
					path=dijkstra.getPath(g.getNode(idTo)).getNodePath();
				}catch(NullPointerException E) {
					path=dijkstra.getPath(g.getNode(idOccupied)).getNodePath();
				}
			}
		}
		//System.out.println("path is now : "+path);
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		shortestPath.add(idOccupied); //to make sure the remove doesnt make this list empty
		dijkstra.clear();
		
		//System.out.println("The shortest past list is of size : "+shortestPath.size());
		
		shortestPath.remove(0);//remove the current position
		return shortestPath;
	}

	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),(MapAttribute)n.getAttribute("ui.class"));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}

		closeGui();

		this.g=null;
		
	}
	
	public String getMapString() {	
		Iterator<Edge> iterE=this.g.edges().iterator();
		String edgesString = "";
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			//sg.addEdge(e.getId(), sn.getId(), tn.getId());
			edgesString += sn+"@"+sn.getAttribute("ui.class")+"@"+tn+"@"+tn.getAttribute("ui.class")+",";
			//System.out.println("\n%%%%%%%%%%%%%%%%%%\n");
			//System.out.println(sn+" is "+sn.getAttribute(""));
		}
		return edgesString;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public void loadSavedData(){
		
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);
		
		openGui();
		
		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	public Node getFullNode(String id){
		return this.g.getNode(id);
	}

	public int getNbEdges() {
		return this.nbEdges;
	}
	
	public Graph getGraph() {
		return this.g;
	}
	
	
	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);////GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);
		g.display();
	}
}