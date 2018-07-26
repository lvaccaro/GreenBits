package com.greenaddress.greenapi;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Riccardo Casatta @RCasatta on 26/07/18.
 */
public class Network2 {
    private String name;
    private NetworkParameters network;
    private boolean liquid;
    private String gaitWampUrl;
    private List<String> gaitWampCertPins;
    private List<BlockExplorer> blockExplorers;
    private String depositPubkey;
    private String depositChainCode;
    private String gaitOnion;
    private List<String> defaultPeers;

    public Network2(final String json) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            final Map map = objectMapper.readValue(json, Map.class);
            name = map.get("name").toString();
            final Object network = map.get("network");
            switch (network == null ? "mainnet" : network.toString()) {
                case "mainnet" : this.network = MainNetParams.get(); break;
                case "testnet" : this.network = TestNet3Params.get(); break;
                case "regtest" : this.network = RegTestParams.get(); break;
            }
            final Object liquid = map.get("liquid");
            this.liquid = liquid==null ? false : (boolean) liquid;
            gaitWampUrl = map.get("gait_wamp_url").toString();
            gaitWampCertPins = (List<String>) map.get("gait_wamp_cert_pins");

            blockExplorers = new ArrayList<>();
            for (Map<String,Object> m : (List<Map<String,Object>>) map.get("blockexplorers")) {
                blockExplorers.add(new BlockExplorer(m.get("address").toString(), m.get("tx").toString()));
            }
            depositPubkey = map.get("deposit_pubkey").toString();
            depositChainCode = map.get("deposit_chain_code").toString();
            gaitOnion = map.get("gait_onion").toString();
            defaultPeers =  map.get("default_peers") == null ? new ArrayList<>() : (List<String>) map.get("default_peers") ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Network2{" +
                "name='" + name + '\'' +
                ", network='" + network + '\'' +
                ", liquid=" + liquid +
                ", gaitWampUrl='" + gaitWampUrl + '\'' +
                ", gaitWampCertPins=" + gaitWampCertPins +
                ", blockExplorers=" + blockExplorers +
                ", depositPubkey='" + depositPubkey + '\'' +
                ", depositChainCode='" + depositChainCode + '\'' +
                ", gaitOnion='" + gaitOnion + '\'' +
                ", defaultPeers=" + defaultPeers +
                '}';
    }

    public String getName() {
        return name;
    }

    public NetworkParameters getNetwork() {
        return network;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public String getGaitWampUrl() {
        return gaitWampUrl;
    }

    public List<String> getGaitWampCertPins() {
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

    public List<String> getDefaultPeers() {
        return defaultPeers;
    }
}
