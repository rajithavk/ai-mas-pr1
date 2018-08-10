package org.mas.Agents;

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

        logger.info("Starting to pump from " + root + " to " + location + " @ rate : " + rate );
    }


    public void pumpRate(Integer delta){
        rate += delta;
        logger.info("Changed to pump from " + root + " to " + location + " @ rate : " + rate );
    }


    @Override
    public void takeDown(){
        logger.info("Stopping Pumping from " + root + " to " + location + " @ rate : " + rate );
    }

    public static class PumpBehavior extends Behaviour{

        PumpAgent agent;

        public PumpBehavior(PumpAgent agent){
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage message = agent.blockingReceive();
            if(message.getSender().getName().equals(agent.getRoot())){
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
