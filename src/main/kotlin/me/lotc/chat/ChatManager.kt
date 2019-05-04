package me.lotc.chat

import me.lotc.chat.channel.Channel
import me.lotc.chat.channel.GlobalChannel
import me.lotc.chat.channel.LocalChannel
import me.lotc.chat.user.Chatter
import me.lotc.chat.user.chat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import net.md_5.bungee.api.ChatColor.*

class ChatManager {
    val primordial : Channel = GlobalChannel("OOC", "Global Out Of Character", "ooc",
        false, GRAY, DARK_GRAY, false, 3, false)
    val channels get() = channelAliases.values as Collection<Channel>

    private val channelAliases = ConcurrentHashMap<String, Channel>()
    private val players = ConcurrentHashMap<UUID, Chatter>()

    init{
        channelAliases[primordial.cmd] = primordial
    }

    internal fun onEnable(channels: List<Channel>){
        for(c in channels){
            addChannel(c)
            (c as? LocalChannel)?.looc?.run(::addChannel)
        }
    }

    private fun addChannel(c: Channel){
        if(channelAliases.containsKey(c.cmd)) throw IllegalStateException("Duplicate channel command tag found: " + c.cmd)
        channelAliases[c.cmd] = c
    }

    fun channelsFor(player: CommandSender) : Collection<Channel> {
        return channels.filter { it.canJoin(player) }
    }

    internal fun join(player: Player){
        val chat = Chatter(player)
        chat.loadSettings()
        players[player.uniqueId] = chat
    }

    internal fun quit(player: Player){
        player.chat.saveSettings()
        players.remove(player.uniqueId)
    }

    fun getChatSettings(player : Player) : Chatter {
        return players[player.uniqueId]!!
    }

    fun getByAlias(alias: String) : Channel? {
        return channelAliases[alias.toLowerCase()]
    }

    fun getPlayers() : Collection<Chatter> {
        return players.values
    }

}