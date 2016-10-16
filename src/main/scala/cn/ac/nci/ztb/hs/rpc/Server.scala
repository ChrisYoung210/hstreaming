package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import cn.ac.nci.ztb.hs.exception.HSRuntimeException
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

  private var channelFuture : ChannelFuture = null

  private lazy val bossGroup = new NioEventLoopGroup

  private lazy val workerGroup = new NioEventLoopGroup(
    Configuration getIntOrDefault("rpc.server.handler", 8))

  override def init = {
    synchronized {
      state match {
        case State.UNINITED =>
          server group(bossGroup, workerGroup) channel
            classOf[NioServerSocketChannel] childHandler getInitializer
          RPC.logger info getClass + " has initialized."
          state = State.INITED
        case State.STOPED =>
          val e = new IllegalStateException("The server cannot be re-initial, " +
            "because it has been shutdown.")
          RPC.logger error(e.getMessage, e)
          throw e
        case _ =>
          RPC.logger warn s"Expect initial server, but current state is $state."
      }
    }
    this
  }

  override def start = {
    synchronized {
      state match {
        case State.INITED =>
          channelFuture = server bind address sync()
          RPC.logger info "Start server completely."
          state = State.STARTED
        case State.STARTED => RPC.logger warn "Server cannot be re-start."
        case _ =>
          val e = new IllegalStateException(s"The server cannot start, because its state is $state.")
          RPC.logger error(e.getMessage, e)
          throw e
      }
    }
    this
  }

  override def stop = {
    synchronized {
      if (channelFuture != null) channelFuture.channel().closeFuture().sync()
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
    state = State.STOPED
    this
  }

  def addProtocolAndInstance[T <: AnyRef](clazz : Class[T], instance : T) : Boolean

  def getInitializer : ChannelInitializer[SocketChannel]
}
