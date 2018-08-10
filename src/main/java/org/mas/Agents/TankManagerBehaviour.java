package org.mas.Agents;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TankManagerBehaviour extends Behaviour{
    private static final Logger logger = LoggerFactory.getLogger(TankManagerBehaviour.class);
    TankManagerAgent agent;

    public TankManagerBehaviour(TankManagerAgent agent){
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage message= agent.blockingReceive();
        if(message.getSender().getLocalName().equals(agent.waterLevelAgent)){
            try {
                WaterLevelAgent.WaterLevelStateMessage msg = (WaterLevelAgent.WaterLevelStateMessage) message.getContentObject();
                logger.info("Water Level State Update Recieved : " + msg.state + " @ " + msg.rate);
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
