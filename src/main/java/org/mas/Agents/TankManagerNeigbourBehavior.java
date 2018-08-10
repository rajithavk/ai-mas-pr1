package org.mas.Agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TankManagerNeigbourBehavior extends Behaviour {
    private static final Logger logger = LoggerFactory.getLogger(TankManagerNeigbourBehavior.class);
    TankManagerAgent agent;
    MessageTemplate template;


    public TankManagerNeigbourBehavior(TankManagerAgent agent){
        super(agent);
        this.agent = agent;

    }

    @Override
    public void action() {

//        switch (agent.STATE){
//
//            case ACCEPTING:
//                template = MessageTemplate.MatchConversationId("OFFER");
//                ACLMessage message = agent.receive(template);
//                if(message!=null){
//                    if(message.getPerformative()==ACLMessage.PROPOSE) {
//                        ACLMessage message1 = message.createReply();
//                        if (!agent.outSet.contains(message.getSender())) {
//                            message1.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//                        }else
//                            message1.setPerformative(ACLMessage.REFUSE);
//
//                        agent.send(message1);
//                    }
//                }
//                break;
//            case OFFERING:
//                template = MessageTemplate.and(MessageTemplate.MatchConversationId("OFFER"),MessageTemplate.MatchInReplyTo(agent.getLocalName()));
//                ACLMessage reply = agent.receive(template);
//                if(reply!=null){
//                    if(reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
//                        addPump(reply.getSender());
//                    }
//                }
//
//                break;
//            case BLOCKING:
//                break;
//        }
    }

    @Override
    public boolean done() {
        return false;
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
