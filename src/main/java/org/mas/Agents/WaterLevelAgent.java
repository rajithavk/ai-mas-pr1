package org.mas.Agents;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class WaterLevelAgent extends Agent {

    public String rootAgent;
    private static final Logger logger = LoggerFactory.getLogger(WaterLevelAgent.class);
    int currentLevel = 0;
    int inRate = 0;
    int outRate = 0;
    int threshold = 500;

    static enum State {
        UNDER,
        THRESHOLD,
        OVER
    }

    public int getRate(){
        return (inRate - outRate);
    }

    public State computeLevel(){
        currentLevel += inRate - outRate;

        if(currentLevel<0) currentLevel=0;

        if( currentLevel < threshold)
            return State.UNDER;
        else if(currentLevel >= threshold && currentLevel < (threshold + 100))
            return State.THRESHOLD;
        else
            return State.OVER;
    }

    @Override
    public void setup(){
        this.rootAgent = (String) getArguments()[0];
        this.threshold = (Integer) getArguments()[1];
        logger.info("Starting Water Level Agent for " + this.rootAgent);
        addBehaviour(new WaterLevelAgentBehaviour(this,1000));
        addBehaviour(new WaterLevelAgentUpdateBehaviour(this));
    }


    public static class WaterLevelAgentBehaviour extends TickerBehaviour{
        WaterLevelAgent agent;
        State STATE = State.UNDER;
        public WaterLevelAgentBehaviour(WaterLevelAgent a, long period) {
            super(a, period);
            agent = a;
        }

        @Override
        protected void onTick() {

            State s = agent.computeLevel();

            switch (STATE){
                case UNDER:
                    if(s == State.THRESHOLD){
                        logger.info(s.toString());
                        sendMessage(new WaterLevelStateMessage(agent.getRate(),s));
                        STATE = s;
                    }
                    break;
                case THRESHOLD:
                    if(s == State.OVER){
                        logger.info(s.toString());
                        sendMessage(new WaterLevelStateMessage(agent.getRate(),s));
                        STATE = s;
                    }
                    break;
                case OVER:
                    if(s == State.UNDER){
                        logger.info(s.toString());
                        sendMessage(new WaterLevelStateMessage(agent.getRate(),s));
                        STATE = s;
                    }
                    break;
            }
        }


        private void sendMessage(WaterLevelStateMessage msg){
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID(agent.rootAgent,AID.ISLOCALNAME));
            message.setSender(agent.getAID());
            try {
                message.setContentObject(msg);
                agent.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class WaterLevelStateMessage implements Serializable{
        public int rate;
        public State state;
        public WaterLevelStateMessage(int rate, State state){
            this.rate = rate;
            this.state = state;
        }
    }

    public static class WaterLevelUpdateMessage implements Serializable{
        private Integer currentLevel;
        private Integer inRate;
        private Integer outRate;
        private Integer threshold;

        public void setCurrentLevel(Integer currentLevel) {
            this.currentLevel = currentLevel;
        }

        public void setInRate(Integer inRate) {
            this.inRate = inRate;
        }

        public void setOutRate(Integer outRate) {
            this.outRate = outRate;
        }

        public void setThreshold(Integer threshold) {
            this.threshold = threshold;
        }
    }


    public static class WaterLevelAgentUpdateBehaviour extends Behaviour{

        WaterLevelAgent agent;

        public WaterLevelAgentUpdateBehaviour(WaterLevelAgent agent){
            super(agent);
            this.agent = agent;
        }
        @Override
        public void action() {
            ACLMessage message = myAgent.receive();
            if(message != null){
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    WaterLevelUpdateMessage message1 = mapper.readValue(message.getContent(),WaterLevelUpdateMessage.class);
                    if(message1 == null) return;
                    if(message1.currentLevel !=null) agent.currentLevel = message1.currentLevel;
                    if(message1.inRate !=null) agent.inRate = message1.inRate;
                    if(message1.outRate !=null) agent.outRate = message1.outRate;
                    if(message1.threshold !=null) agent.threshold = message1.threshold;

                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else
                block();
        }

        @Override
        public boolean done() {
            return false;
        }
    }

}
