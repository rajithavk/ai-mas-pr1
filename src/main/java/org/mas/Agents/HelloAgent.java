package org.mas.Agents;
import jade.core.Agent;

public class HelloAgent extends Agent {
    @Override
    public void setup(){
        System.out.println("Hello " + getAID().getName());

    }
}
