package org.mas.Agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
        ACLMessage message = agent.blockingReceive();

        if(message.getSender().getLocalName().equals(agent.waterLevelAgent)){
            try {
                WaterLevelAgent.WaterLevelStateMessage msg = (WaterLevelAgent.WaterLevelStateMessage) message.getContentObject();
                logger.info("Water Level Trigger Received : " + msg.state + " @FlowRate " + msg.rate + " L/s");
                if(msg.state == WaterLevelAgent.State.OVER && msg.rate > 0){
                    startEmergencyDrain(msg);
                    agent.STATE = TankManagerAgent.State.BLOCKING;
                }
                else if(msg.state == WaterLevelAgent.State.UNDER && msg.rate < 0){
                    stopEmergencyDrain();
                    agent.STATE = TankManagerAgent.State.ACCEPTING;
                }else if(msg.state == WaterLevelAgent.State.THRESHOLD && msg.rate >0){
                    agent.STATE = TankManagerAgent.State.OFFERING;
                    getNeighbors();
                }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }else {

            switch (agent.STATE) {
                case ACCEPTING:
                    if (message != null) {
                        if (message.getConversationId().equals("OFFER")) {
                            if (message.getPerformative() == ACLMessage.PROPOSE) {
                                ACLMessage message1 = message.createReply();
                                if (!agent.outSet.contains(message.getSender())) {
                                    message1.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                } else
                                    message1.setPerformative(ACLMessage.REFUSE);

                                agent.send(message1);
                            }
                        }
                    }
                    break;
                case OFFERING:
                    if (message.getConversationId().equals("OFFER")) {
                        if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            addPump(message.getSender());
                        }
                    }
                    break;
                case BLOCKING:
                    break;
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

    public AID[] getNeighbors(){
        AID[] agents=null;
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agent.GROUP);
        dfd.addServices(sd);
        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
        message.setConversationId("OFFER");
        message.setReplyWith(agent.getLocalName());
        message.setSender(agent.getAID());
        try{
            DFAgentDescription[] result = DFService.search(agent,dfd);
            for(DFAgentDescription d : result){
                message.addReceiver(d.getName());
            }
            agent.send(message);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return agents;
    }

    public void addPump(AID aid){
        jade.wrapper.AgentContainer container    =   agent.getContainerController();
        AgentController agentController;
        try {
            agentController = container.createNewAgent(agent.getLocalName()+"-"+ aid.getLocalName() +"-pump",PumpAgent.class.getName(),new Object[]{agent.getLocalName(),aid.getLocalName(),10});
            agentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

}
