public class greedyAI implements LostCitiesRivals2PInput {
//    LostCitiesRivals2PGameState cloned = gs.deepCopyUsingSerialization();

    @Override
    public int nextMove(LostCitiesRivals2PGameState gs) throws CloneNotSupportedException {
        if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.REVEAL) {
            if (gs.heuristic(2)[0] < 2 ) return 0; //if cards wanted are less than 2 keep revealing cards
            else if (gs.heuristic(2)[0] >= 2 && gs.heuristic(2)[2]>=2) return gs.heuristic(2)[0];
            else return 0;
        }
        else if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.BET) {
            if (gs.heuristic(2)[2] - gs.heuristic(1)[2]<=2
                    && gs.heuristic(2)[2] -gs.heuristic(1)[2]>=0
                    && gs.getGoldP2()-gs.getGoldP1()>=0)return gs.getMinAcceptableBet();
            else if (gs.heuristic(2)[2] - gs.heuristic(1)[2] >= 3 ) return gs.heuristic(2)[0];
            else if (gs.heuristic(2)[0] >3) return gs.heuristic(2)[0];
            else return -1;
        }
        else if (gs.getGameStatus() == LostCitiesRivals2PGameState.GameStatus.SELECT) return gs.heuristic(2)[1];
        return 0; //Shouldnt reach this.
    }
}
