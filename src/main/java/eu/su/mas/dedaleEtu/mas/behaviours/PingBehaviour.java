package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PingBehaviour extends SimpleBehaviour {

    private List<String> receiver;
    private HashMap<String, Object> content;


    public PingBehaviour(Agent myAgent){
        super(myAgent);
    }

    @Override
    public void action() {

        final ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setSender(this.myAgent.getAID());
        
        msg.setContent("fillMyMap");
        
        if (!(myAgent.getLocalName().equals("Explo1")))
        	msg.addReceiver(new AID("Explo1", AID.ISLOCALNAME));
        if (!(myAgent.getLocalName().equals("Explo2")))
        	msg.addReceiver(new AID("Explo2", AID.ISLOCALNAME));
        if (!(myAgent.getLocalName().equals("Explo3")))
        	msg.addReceiver(new AID("Explo3", AID.ISLOCALNAME));

        
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    }

    @Override
    public boolean done() {
        return false;
    }
}