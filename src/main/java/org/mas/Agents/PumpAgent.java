package org.mas.Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class PumpAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(PumpAgent.class);
    private String root;
    private Integer rate = 0;
    private String location;

    protected String getRoot(){
        return root;
    }


    @Override
    public void setup(){
        root = (String) this.getArguments()[0];
        location = (String) this.getArguments()[1];
        rate     = (Integer) this.getArguments()[2];

        if(location.equals("-")){
            logger.info("Starting to pump from " + root + " to River" + " @ rate : " + rate + " L/s");
        }else
            logger.info("Starting to pump from " + root + " to " + location + " @ rate : " + rate + " L/s");

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(root+"-waterLevelAgent",AID.ISLOCALNAME));
        message.setSender(new AID(root,AID.ISLOCALNAME));
        message.setContent("{\"outRate\" : " + rate + " }");
        send(message);

        ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
        message1.addReceiver(new AID(location+"-waterLevelAgent",AID.ISLOCALNAME));
        message1.setSender(new AID(root,AID.ISLOCALNAME));
        message1.setContent("{\"inRate\" : " + rate + " }");
        send(message1);

        addBehaviour(new PumpBehavior(this));
    }


    public void pumpRate(Integer delta){
        rate += delta;
        if(location.equals("-")){
            logger.info("Starting to pump from " + root + " to River" + " @ rate : " + rate + " L/s");
        }else
            logger.info("Starting to pump from " + root + " to " + location + " @ rate : " + rate + " L/s");
    }


    @Override
    public void takeDown(){
        if(location.equals("-")){
            logger.info("Stopping to pump from " + root + " to River" + " @ rate : " + rate + " L/s");
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID(root+"-waterLevelAgent",AID.ISLOCALNAME));
            message.setSender(new AID(root,AID.ISLOCALNAME));
            message.setContent("{\"outRate\" : " + (-1*rate) + " }");
            send(message);
        }else {
            logger.info("Stopping to pump from " + root + " to " + location + " @ rate : " + rate + " L/s");

            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID(root + "-waterLevelAgent", AID.ISLOCALNAME));
            message.setSender(new AID(root, AID.ISLOCALNAME));
            message.setContent("{\"outRate\" : " + (-1 * rate) + " }");
            send(message);

            ACLMessage message1 = new ACLMessage(ACLMessage.INFORM);
            message1.addReceiver(new AID(location + "-waterLevelAgent", AID.ISLOCALNAME));
            message1.setSender(new AID(root, AID.ISLOCALNAME));
            message1.setContent("{\"inRate\" : " + (-1 * rate) + " }");
            send(message1);
        }
    }

    public static class PumpBehavior extends Behaviour{

        PumpAgent agent;

        public PumpBehavior(PumpAgent agent){
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage message = agent.blockingReceive();
            try {
                Serializable msg = message.getContentObject();
                if(msg instanceof PumpChangeRequestMessage){
                    agent.pumpRate(((PumpChangeRequestMessage) msg).rate);
                }
                else if(msg instanceof PumpStopRequestMessage){
                    agent.doDelete();
                }


            } catch (UnreadableException e) {
                e.printStackTrace();
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }


    //Messages

    static class PumpChangeRequestMessage implements Serializable{
        public PumpChangeRequestMessage(Integer rate){
            this.rate = rate;
        }
        Integer rate;
    }

    static class PumpStopRequestMessage implements Serializable{

    }
}
