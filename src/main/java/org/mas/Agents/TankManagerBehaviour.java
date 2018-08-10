package org.mas.Agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
                logger.info("Water Level Trigger Received : " + msg.state + " @FlowRate " + msg.rate + " L/s");
                if(msg.state == WaterLevelAgent.State.OVER && msg.rate > 0){
                    startEmergencyDrain(msg);
                }
                else if(msg.state == WaterLevelAgent.State.UNDER && msg.rate < 0){
                    stopEmergencyDrain();
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

    public void startEmergencyDrain(WaterLevelAgent.WaterLevelStateMessage waterLevelStateMessage) {
        logger.warn("Starting Emergency Drain Procedures");

        jade.wrapper.AgentContainer container    =   agent.getContainerController();
        AgentController agentController;
        try {
            agentController = container.createNewAgent(agent.getLocalName()+"-emergency-pump",PumpAgent.class.getName(),new Object[]{agent.getLocalName(),agent.parentTank,waterLevelStateMessage.rate});
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void stopEmergencyDrain(){
        logger.warn("Stopping Emergency Drain Procedures");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        try {
            message.setContentObject(new PumpAgent.PumpStopRequestMessage());
            message.addReceiver(new AID(agent.getLocalName()+"-emergency-pump",AID.ISLOCALNAME));
            message.setSender(agent.getAID());
            agent.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
