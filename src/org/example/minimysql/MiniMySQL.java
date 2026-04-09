package org.example.minimysql;

import org.example.minimysql.net.NioAcceptor;

import java.io.IOException;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class MiniMySQL {
    private static final int PORT = 3307;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting MiniMySQL (Modular) on port " + PORT + "...");
        NioAcceptor acceptor = new NioAcceptor(PORT);
        acceptor.start();
    }
}
