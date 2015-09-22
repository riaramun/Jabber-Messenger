package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.model.ChatRoomModel;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.services.RiaXmppService;
import ru.rian.riamessenger.utils.DbHelper;
import ru.rian.riamessenger.utils.XmppUtils;

/**
 * Created by Roman on 9/17/2015.
 */

@RequiredArgsConstructor
public class MUCManager implements InvitationListener {

    final Context context;
    final AbstractXMPPConnection connection;
    final UserAppPreference userAppPreference;
    MultiUserChatManager manager;

    public void init() {
        manager = MultiUserChatManager.getInstanceFor(connection);
        manager.addInvitationListener(this);
        updateRoomsInDb();
    }

    public void updateRoomsInDb() {
        for (EntityBareJid entityBareJid : manager.getJoinedRooms()) {
            try {
                // RoomInfo roomInfo = manager.getRoomInfo(entityBareJid);
                addRoomByJidToDb(entityBareJid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void addRoomByJidToDb(EntityBareJid entityBareJid/*, List<String> contactJids*/) {
        ChatRoomModel chatRoomModel = new ChatRoomModel();
        String jidStr = entityBareJid.asEntityBareJidString();
        chatRoomModel.threadIdCol = jidStr;
        chatRoomModel.name = jidStr.substring(0, jidStr.indexOf("@"));
        chatRoomModel.save();
        /*try {

            RoomInfo roomInfo = manager.getRoomInfo(entityBareJid);
            if (roomInfo != null) {
                chatRoomModel.name = roomInfo.getName();
            }
            else {
                chatRoomModel.name = entityBareJid.asEntityBareJidString();
            }

            chatRoomModel.save();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }*/
    }

    public void createRoom(String roomName, ArrayList<String> jidArrayList) {
        try {
            List<DomainBareJid> domains = manager.getXMPPServiceDomains();
            EntityBareJid bareJid = JidCreate.entityBareFrom(roomName + "@" + RiaConstants.ROOM_DOMAIN);
            MultiUserChat muc = manager.getMultiUserChat(bareJid);
            Resourcepart resourcepart = Resourcepart.from(roomName);
            muc.create(resourcepart);
            for (String jidStr : jidArrayList) {
                muc.invite(jidStr, "");
            }
           // muc.sendRegistrationForm(new Form(DataForm.Type.submit));

            Resourcepart nickName = Resourcepart.from(userAppPreference.getFirstSecondName());
            XmppUtils.changeCurrentUserStatus(new Presence(Presence.Type.available), userAppPreference.getJidStringKey(), connection);
            muc.join(nickName);

            ChatRoomModel chatRoomModel = new ChatRoomModel();
            chatRoomModel.name = roomName;
            chatRoomModel.threadIdCol = bareJid.asEntityBareJidString();
            chatRoomModel.save();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }
        updateRoomsInDb();
    }

    @Override
    public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {

        final EntityBareJid roomBareJid = room.getRoom().asEntityBareJid();
        //MultiUserChat multiUserChat = manager.getMultiUserChat(roomBareJid);

        try {
            Resourcepart nickName = Resourcepart.from(userAppPreference.getFirstSecondName());
            room.join(nickName);
            //final MultiUserChat muc = manager.getMultiUserChat(roomBareJid);
            // muc.join(Resourcepart.from(userAppPreference.getFirstSecondName()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }
        addRoomByJidToDb(roomBareJid);
        room.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(Message message) {
                //EntityJid entityJidFrom = JidCreate.bareFrom(userAppPreference.getJidStringKey()).asEntityJidIfPossible();
                //Message message = createMessage(bareJid, entityJidFrom, messageText);
                DbHelper.addMessageToDb(message, MessageContainer.CHAT_GROUP, roomBareJid.toString(), true);
            }
        });
    }

    Message createMessage(EntityJid entityJidTo, EntityJid entityJidFrom, String messageText) {
        Message message = null;
        try {
            message = new Message();
            message.setBody(messageText);
            message.setFrom(entityJidFrom);
            message.setTo(entityJidTo);
            message.setStanzaId(StanzaIdUtil.newStanzaId());
            message.setType(Message.Type.groupchat);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }
        return message;
    }

    public void sendMessageToServer(final String roomJid, final String messageText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EntityBareJid bareJid = JidCreate.entityBareFrom(roomJid);
                    MultiUserChat muc = manager.getMultiUserChat(bareJid);
                    EntityJid entityJidFrom = JidCreate.bareFrom(userAppPreference.getJidStringKey()).asEntityJidIfPossible();
                    Message message = createMessage(bareJid, entityJidFrom, messageText);
                    DbHelper.addMessageToDb(message, MessageContainer.CHAT_GROUP, bareJid.toString(), true);
                    muc.sendMessage(messageText);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(RiaXmppService.TAG, e.getMessage());
                }
            }
        }).start();
    }
}
