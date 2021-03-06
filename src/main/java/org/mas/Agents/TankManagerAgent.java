package org.mas.Agents;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class TankManagerAgent extends jade.core.Agent{
    private static final Logger logger = LoggerFactory.getLogger(TankManagerAgent.class);
    private int THRESHOLD = 500;
    public String parentTank;
    public String waterLevelAgent;
    protected String GROUP = "";
    protected HashSet<AID> outSet = new HashSet<>();
    protected HashSet<AID> inSet = new HashSet<>();

    enum State{
        ACCEPTING,
        OFFERING,
        BLOCKING
    }

    protected State STATE = State.ACCEPTING;

    @Override
    public void setup(){
        logger.info("Starting " + this.getAID().getLocalName());
        GROUP = (String) this.getArguments()[1];
        parentTank = (String) this.getArguments()[0];


        waterLevelAgent = getAID().getLocalName()+"-waterLevelAgent";

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(GROUP);
        sd.setName(getAID().getLocalName());
        dfd.addServices(sd);
        try{
            DFService.register(this,dfd);
            createAgents();

        }catch (FIPAException e){
            e.printStackTrace();
        } catch (StaleProxyException s) {
            s.printStackTrace();
        }

        addBehaviour(new TankManagerBehaviour(this));
//        addBehaviour(new TankManagerNeigbourBehavior(this));

    }

    private void createAgents() throws StaleProxyException {
        jade.wrapper.AgentContainer container    =   getContainerController();
        AgentController agentController = container.createNewAgent(waterLevelAgent,WaterLevelAgent.class.getName(),new Object[]{getAID().getLocalName(),THRESHOLD});
        agentController.start();
    }


    public void takeDown(){
        try{
            DFService.deregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
