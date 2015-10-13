package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.model.ChatRoomModel;
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
    }

    public void updateRoomsInDb() {

        List<EntityBareJid> roomsJidList = DbHelper.getRoomsJidFromDb();
        for (EntityBareJid roomJid : roomsJidList) {
              joinRoom(roomJid);
        }


        /*List<BookmarkedConference> bookmarkedConferences = null;
        List<BookmarkedURL> bookmarkedUrls = null;
        BookmarkManager bookmarkManager = null;
        try {
            bookmarkManager = BookmarkManager.getBookmarkManager(connection);
            bookmarkedUrls = bookmarkManager.getBookmarkedURLs();
            bookmarkedConferences = bookmarkManager.getBookmarkedConferences();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }*/


        List<HostedRoom> rooms = null;
        List<EntityBareJid> rooms2 = null;
        try {
            //Iterator joinedRooms = MultiUserChat.getJoinedRooms(connection, connection.getUser());
            rooms2 = manager.getJoinedRooms(connection.getUser());
            rooms = manager.getHostedRooms(JidCreate.domainBareFrom(RiaConstants.XMPP_SERVICE_NAME));
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        /*try {
            for (HostedRoom entityBareJid : manager.getHostedRooms(RiaConstants.XMPP_SERVICE_NAME)) {
                //addRoomByJidToDb(entityBareJid);
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }*/
    }

    void addRoomByJidToDb(EntityBareJid entityBareJid/*, List<String> contactJids*/) {
        ChatRoomModel chatRoomModel = new ChatRoomModel();
        chatRoomModel.threadIdCol = entityBareJid.asBareJid().toString();
        chatRoomModel.name = entityBareJid.getLocalpart().toString();//entityBareJid.substring(0, entityBareJid.indexOf("@"));
        chatRoomModel.save();
        /*try {

            RoomInfo roomInfo = manager.getRoomInfo(entityBareJid);
            if (roomInfo != null) {
                chatRoomModel.name = roomInfo.getName();
            }
            else {
                chatRoomModel.name = entityBareJid.asStringString();
            }

            chatRoomModel.save();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }*/
    }

    public void createRoom(String roomName, ArrayList<String> jidArrayList) {
        try {
            //List<String> domains = manager.getServiceNames();
            String bareJid = roomName + "@" + RiaConstants.ROOM_DOMAIN;
            MultiUserChat muc = manager.getMultiUserChat(JidCreate.entityBareFrom(bareJid));
            muc.create(Resourcepart.from(userAppPreference.getFirstSecondName()));
            muc.sendConfigurationForm(new Form(DataForm.Type.submit));
            for (String jidStr : jidArrayList) {
                muc.invite(jidStr, "");
            }

            XmppUtils.changeCurrentUserStatus(new Presence(Presence.Type.available), userAppPreference.getUserStringKey(), connection);
           // muc.join(userAppPreference.getFirstSecondName());

            ChatRoomModel chatRoomModel = new ChatRoomModel();
            chatRoomModel.name = roomName;
            chatRoomModel.threadIdCol = bareJid;
            chatRoomModel.save();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }
    }

    @Override
    public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
        final EntityBareJid roomBareJid = room.getRoom();
        joinRoom(roomBareJid);
        addRoomByJidToDb(roomBareJid);
    }

    void joinRoom(EntityBareJid roomJid) {
        MultiUserChat multiUserChat = manager.getMultiUserChat(roomJid);
        try {
            String nickName = (userAppPreference.getFirstSecondName());
            multiUserChat.join(Resourcepart.from(nickName));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(RiaXmppService.TAG, e.getMessage());
        }
    }

    Message createMessage(String entityJidTo, String entityJidFrom, String messageText) {
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
                    //String entityJidFrom = (userAppPreference.getUserStringKey());
                    //Message message = createMessage(roomJid, entityJidFrom, messageText);
                    //DbHelper.addMessageToDb(message, MessageContainer.CHAT_GROUP, roomJid, true);
                    muc.sendMessage(messageText);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(RiaXmppService.TAG, e.getMessage());
                }
            }
        }).start();
    }
}
