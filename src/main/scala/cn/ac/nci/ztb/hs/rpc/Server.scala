package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ChannelFuture, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
  * Created by Young on 16-9-1.
  */
abstract class Server(address : InetSocketAddress) extends Service {

  private val server : ServerBootstrap = new ServerBootstrap

  private var state = State.UNINITED

  private var channelFuture : ChannelFuture = null

  private lazy val bossGroup = new NioEventLoopGroup

  private lazy val workerGroup = new NioEventLoopGroup(
    Configuration getIntOrDefault("rpc.server.handler", 8))

  override def init = {
    state.synchronized {
      if (state equals State.UNINITED) {
        server group(bossGroup, workerGroup) channel
          classOf[NioServerSocketChannel] childHandler getInitializer
        RPC.logger info getClass + "已完成初始化。"
        state = State.INITED
      } else {
        RPC.logger error "由于Server服务状态错误" + getClass.toString + "初始化失败。"
        false
      }
    }
    true
  }

  override def start = {
    state.synchronized {
      if (state equals State.INITED) {
        channelFuture = server bind address sync()
        state = State.STARTED
      } else false
    }
    true
  }

  override def stop = {
    state.synchronized {
      if (state equals State.STARTED) {
        channelFuture.channel.closeFuture.sync()
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        state = State.STOPED
      } else false
    }
    true
  }

  def addProtocolAndInstance[T <: AnyRef](clazz : Class[T], instance : T) : Boolean

  def getInitializer : ChannelInitializer[SocketChannel]
}
