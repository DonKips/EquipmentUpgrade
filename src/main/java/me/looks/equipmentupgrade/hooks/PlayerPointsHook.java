package me.looks.equipmentupgrade.hooks;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

import java.util.UUID;

public class PlayerPointsHook {
     private final PlayerPointsAPI playerPointsAPI;

     public PlayerPointsHook() {
         this.playerPointsAPI = PlayerPoints.getInstance().getAPI();
     }

     public int look(UUID uuid) {
         return playerPointsAPI.look(uuid);
     }
    public void take(UUID uuid, int count) {
        playerPointsAPI.take(uuid, count);
    }

}
