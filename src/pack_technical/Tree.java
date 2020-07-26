package pack_technical;

import pack_boids.BoidGeneric;

public class Tree {
    //root.depth is always 0
    private Node root;
    private int maxTreeDepth;
    private int maxNodeChildren = 12;

    public Node getRoot() {
        return root;
    }

    public int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    public void setMaxTreeDepth(int maxTreeDepth) {
        this.maxTreeDepth = maxTreeDepth;
    }

    public int getMaxNodeChildren() {
        return maxNodeChildren;
    }

    public void setMaxNodeChildren(int maxNodeChildren) {
        this.maxNodeChildren = maxNodeChildren;
    }


    public Tree(int maxTreeDepth, BoidGeneric attackBoid) {
        resetRoot(attackBoid);
        this.maxTreeDepth = maxTreeDepth;
    }

    public void resetRoot(BoidGeneric attackBoid) {
        this.root = new Node(0, "ROOT", 0, 0, attackBoid);
        this.root.addPresetActionNode(Node.Action.TOWARDS_TARGET);
    }

    public Node addChild(Node node, InnerSimulation innerSimulation) {
        Node childNode = node.addChild(innerSimulation);
        childNode.addPresetActionNode(Node.Action.TOWARDS_TARGET);
        return childNode;
    }

    public Node UCT(Node currentNode, double epsilon) {
        do {
            if(currentNode.getChildren().size() < maxNodeChildren){
                return currentNode;
            }

            Node selectedNode = currentNode.getRandomChild();
            double randomNum = Math.random();
            for(Node node : currentNode.getChildren()) {
                if(randomNum < epsilon) {
                    continue;
                }
                selectedNode = node.calcUCT() > selectedNode.calcUCT() ? node : selectedNode;
            }
            currentNode = selectedNode;
        } while(true);
    }


    public Node bestAvgVal() {
        if(root.getChildren().size() == 0){
            return root;
        }
        double bestNode = root.getChildren().get(0).calcUCT();
        int bestNodePos = 0;
        for (int i=0; i<root.getChildren().size()-1; i++){
            if(root.getChildren().get(i).calcUCT() > bestNode){
                bestNode = root.getChildren().get(i).calcUCT();
                bestNodePos = i;
            }
            if(root.getChild(i).getSimulationValue() >= 1){
                return root.getChild(i);
            }
        }
        //System.out.println("Node Name: " + root.children.get(bestNodePos).name);
        return root.getChild(bestNodePos);
    }
}