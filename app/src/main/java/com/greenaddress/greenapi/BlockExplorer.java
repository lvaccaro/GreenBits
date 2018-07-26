package com.greenaddress.greenapi;

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
