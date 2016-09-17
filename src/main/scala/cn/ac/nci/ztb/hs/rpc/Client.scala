package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import cn.ac.nci.ztb.hs.io.Writable
import io.netty.bootstrap.Bootstrap
import io.netty.channel.{Channel, ChannelFuture, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

import scala.language.postfixOps

/**
  * Created by Young on 16-9-1.
  */
abstract class Client(address : InetSocketAddress,
                      invoker : RpcInvocationHandler[_ <: Writable]) extends Service {

  lazy val client : Bootstrap = new Bootstrap

  private val nextRequestId = new AtomicInteger

  private lazy val workerGroup = new NioEventLoopGroup(Configuration.
    getIntOrDefault("rpc.client.handler", 4))

  private var state = State.UNINITED

  private var channelFuture : ChannelFuture = _

  private var ch : Channel = _

  def getInvoker = invoker

  override def init = {
    state.synchronized {
      if (state eq State.UNINITED) {
        client group workerGroup channel classOf[NioSocketChannel] handler getInitializer
        RPC.logger info getClass + "已完成初始化。"
        state = State.INITED
        true
      } else {
        RPC.logger error "由于Client Service状态错误" + getClass + "初始化失败。"
        false
      }
    }
  }

  override def start = {
    state.synchronized {
      if (state eq State.INITED) {
        channelFuture = client connect address sync()
        ch = channelFuture channel()
        RPC.logger info getClass + "已完成启动。"
        state = State.STARTED
        true
      } else {
        RPC.logger error "由于Client Service状态错误" + getClass + "启动失败。"
        false
      }
    }
  }

  override def stop = {
    state.synchronized {
      if (state eq State.STARTED) {
        ch closeFuture() sync()
        workerGroup shutdownGracefully()
        state = State.STOPED
        RPC.logger info getClass + "已停止。"
        true
      } else {
        RPC.logger error "由于Client Service状态错误" + getClass + "无法停止。"
        false
      }
    }
  }

  def send(msg: AnyRef) {
    ch.synchronized {
      RPC.logger debug "Prepare send msg whose type is " + msg getClass;
      ch writeAndFlush msg
    }
  }

  def getNextRequestId = nextRequestId.getAndIncrement

  def getInitializer : ChannelInitializer[SocketChannel]
}
