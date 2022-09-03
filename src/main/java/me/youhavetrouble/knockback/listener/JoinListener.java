package me.youhavetrouble.knockback.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import me.youhavetrouble.knockback.BanException;
import me.youhavetrouble.knockback.BanRecord;
import me.youhavetrouble.knockback.Knockback;
import me.youhavetrouble.knockback.PluginMessage;
import net.kyori.adventure.text.Component;

public class JoinListener {

    @Subscribe(order = PostOrder.FIRST, async = true)
    public void onPlayerJoin(LoginEvent event) {
        try {
            BanRecord banRecord = Knockback.getPlayerBan(event.getPlayer().getUniqueId());
            if (banRecord == null) return;
            event.setResult(ResultedEvent.ComponentResult.denied(PluginMessage.getBannedMessage(banRecord)));
        } catch (BanException e) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text("")));
        }
    }


}
