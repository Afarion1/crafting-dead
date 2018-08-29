package com.craftingdead.network.mod.client;

import java.util.ArrayList;
import java.util.List;

import com.craftingdead.mod.client.ModClient;
import com.craftingdead.mod.common.core.CraftingDead;
import com.recastproductions.network.impl.Session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class SessionModClient extends Session<NetClientHandlerModClient> {

	private final ModClient modClient;

	private List<String> news = new ArrayList<String>();

	public SessionModClient(Channel ch, NetClientHandlerModClient handler, ModClient modClient) {
		super(ch, handler);
		this.modClient = modClient;
		CraftingDead.LOGGER.info("Connected to the Crafting Dead Network!");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("====================disconnect");
	}

	public void setNews(List<String> news) {
		news = new ArrayList<String>(news);
	}

	public List<String> getNews() {
		return this.news;
	}

	public ModClient getModClient() {
		return this.modClient;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		CraftingDead.LOGGER.warn("Exception in session", t);
	}

}
