package com.example.bootnetty;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootNettyApplication implements CommandLineRunner {

	private final NettyServer nettyServer;

	public BootNettyApplication(NettyServer nettyServer) {
		this.nettyServer = nettyServer;
	}

	public static void main(String[] args) {
		SpringApplication.run(BootNettyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		nettyServer.start();
	}
}
