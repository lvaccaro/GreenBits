package com.greenaddress.greenapi;

/**
 * Created by Riccardo Casatta @RCasatta on 26/07/18.
 */
public class BlockExplorer {
    private String address;
    private String tx;

    public BlockExplorer(String address, String tx) {
        this.address = address;
        this.tx = tx;
    }

    public String getAddress() {
        return address;
    }

    public String getTx() {
        return tx;
    }

    @Override
    public String toString() {
        return "BlockExplorer{" +
                "address='" + address + '\'' +
                ", tx='" + tx + '\'' +
                '}';
    }
}
