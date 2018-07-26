package com.greenaddress.greenapi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Riccardo Casatta @RCasatta on 26/07/18.
 */
public class Network2 {
    private String name;
    private String network;  //NO
    private boolean liquid;
    private String gaitWampUrl;
    private String[] gaitWampCertPins;
    private List<BlockExplorer> blockExplorers;
    private String depositPubkey;
    private String depositChainCode;
    private String gaitOnion;
    private String[] defaultPeers;

    public Network2(Map<String,Object> map) {
        name = map.get("name").toString();
    }

    @Override
    public String toString() {
        return "Network2{" +
                "name='" + name + '\'' +
                ", network='" + network + '\'' +
                ", liquid=" + liquid +
                ", gaitWampUrl='" + gaitWampUrl + '\'' +
                ", gaitWampCertPins=" + Arrays.toString(gaitWampCertPins) +
                ", blockExplorers=" + blockExplorers +
                ", depositPubkey='" + depositPubkey + '\'' +
                ", depositChainCode='" + depositChainCode + '\'' +
                ", gaitOnion='" + gaitOnion + '\'' +
                ", defaultPeers=" + Arrays.toString(defaultPeers) +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getNetwork() {
        return network;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public String getGaitWampUrl() {
        return gaitWampUrl;
    }

    public String[] getGaitWampCertPins() {
        return gaitWampCertPins;
    }

    public List<BlockExplorer> getBlockExplorers() {
        return blockExplorers;
    }

    public String getDepositPubkey() {
        return depositPubkey;
    }

    public String getDepositChainCode() {
        return depositChainCode;
    }

    public String getGaitOnion() {
        return gaitOnion;
    }

    public String[] getDefaultPeers() {
        return defaultPeers;
    }
}
