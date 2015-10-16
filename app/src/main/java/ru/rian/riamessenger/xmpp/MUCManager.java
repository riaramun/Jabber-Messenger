package ru.rian.riamessenger.xmpp;

import android.content.Context;
import android.database.sqlite.SQLiteDiskIOException;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.query.Select;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.id.StanzaIdUtil;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import ru.rian.riamessenger.RiaXmppService;
import ru.rian.riamessenger.common.DbColumns;
import ru.rian.riamessenger.common.RiaConstants;
import ru.rian.riamessenger.model.ChatRoomModel;
import ru.rian.riamessenger.model.ChatRoomOccupantModel;
import ru.rian.riamessenger.model.MessageContainer;
import ru.rian.riamessenger.model.RosterEntryModel;
import ru.rian.riamessenger.prefs.UserAppPreference;
import ru.rian.riamessenger.utils.DbHelper;

/**
 * Created by Roman on 9/17/2015.
 */

@RequiredArgsConstructor
public class MUCManager implements InvitationListener, StanzaListener {

    final Context context;
    final AbstractXMPPConnection connection;
    final UserAppPreference userAppPreference;
    MultiUserChatManager manager;

    public void init() {
        manager = MultiUserChatManager.getInstanceFor(connection);
        manager.addInvitationListener(this);
        connection.addSyncStanzaListener(this, StanzaTypeFilter.PRESENCE);
    }

    public void kickUserFromRoom(String threadId, String userJid) {
        MultiUserChat multiUserChat = null;
        try {
            multiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(threadId));
            Occupant occupant = multiUserChat.getOccupant(JidCreate.entityFullFrom(userJid));
            multiUserChat.kickParticipant(occupant.getNick(), "");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

    }

    public void inviteUserToRoom(String threadId, String userJid) {
        MultiUserChat multiUserChat = null;
        try {
            multiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(threadId));
            multiUserChat.invite(userJid, "");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public List<String> getRoomParticipants(String threadId) {
        List<EntityFullJid> usersJids = null;
        List<String> usersIds = new ArrayList<>();
        try {
            //RoomInfo roomInfo = manager.getRoomInfo(JidCreate.entityBareFrom(threadId));
            MultiUserChat multiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(threadId));
            usersJids = multiUserChat.getOccupants();

            for (EntityFullJid entityFullJid : usersJids) {
                usersIds.add(entityFullJid.toString());
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        return usersIds;
    }

    /*
    The methods called on the start, it gets saved rooms,
    reconnects them or recreate them if the room if is me
     */
    public void recoverRoomsFromDb() {

        List<ChatRoomModel> chatRooms = DbHelper.getChatRooms();
        for (ChatRoomModel chatRoomModel : chatRooms) {
            EntityBareJid roomJid = null;
            try {
                roomJid = JidCreate.entityBareFrom(chatRoomModel.threadIdCol);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
            MultiUserChat muc = manager.getMultiUserChat(roomJid);
            if (chatRoomModel.ownerJidCol.equals(userAppPreference.getUserStringKey())) {
                List<ChatRoomOccupantModel> chatRoomOccupantModels = DbHelper.getRoomOccupants(chatRoomModel.threadIdCol);
                if (chatRoomOccupantModels.size() > 0) {
                    List<String> usersJid = new ArrayList();
                    for (ChatRoomOccupantModel model : chatRoomOccupantModels) {
                        usersJid.add(model.bareJid);
                    }
                    //create Room And Invite Users
                    try {
                        muc.createOrJoin(Resourcepart.from(userAppPreference.getFirstSecondName()));
                        muc.sendConfigurationForm(new Form(DataForm.Type.submit));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (String jidStr : usersJid) {
                        try {
                            muc.invite(jidStr, "");
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                //join room
                try {
                    muc.join(Resourcepart.from(userAppPreference.getFirstSecondName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void createRoomAndSaveToDb(String roomName, ArrayList<String> jidArrayList) {
        String bareJid = roomName + "@" + RiaConstants.ROOM_DOMAIN;
        EntityBareJid roomJid = null;
        try {
            roomJid = JidCreate.entityBareFrom(bareJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        if (roomJid != null) {
            MultiUserChat muc = manager.getMultiUserChat(roomJid);
            try {
                muc.createOrJoin(Resourcepart.from(userAppPreference.getFirstSecondName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                muc.sendConfigurationForm(new Form(DataForm.Type.submit));
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (String jidStr : jidArrayList) {
                try {
                    muc.invite(jidStr, "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DbHelper.addRoomToDb(roomJid, userAppPreference.getUserStringKey(), jidArrayList);
        }

    }

    @Override
    public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
        final EntityBareJid roomBareJid = room.getRoom();
        joinRoom(roomBareJid);
        //List<String> usersJids = getRoomParticipants(roomBareJid.asEntityBareJidString());
        DbHelper.addRoomToDb(roomBareJid, "", null);
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
        try {
            EntityBareJid bareJid = JidCreate.entityBareFrom(roomJid);
            MultiUserChat muc = manager.getMultiUserChat(bareJid);
            muc.sendMessage(messageText);
            String entityJidFrom = userAppPreference.getFirstSecondName();
            Message message = createMessage(roomJid, entityJidFrom, messageText);
            DbHelper.addMessageToDb(message, MessageContainer.CHAT_GROUP, JidCreate.entityBareFrom(roomJid), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        Presence presence = (Presence) packet;
        if (presence != null && presence.getFrom() != null) {
            //Log.i("Service", "presence = " + presence.getStatus() + " mode " + presence.getMode() + " from " + presence.getFrom());
            String bareJid = presence.getFrom().asBareJid().toString();//XmppUtils.entityJid(presence.getFrom());
            if (!TextUtils.isEmpty(bareJid)) {
                RosterEntryModel rosterEntryModel = new Select().from(RosterEntryModel.class).where(DbColumns.FromJidCol + "='" + bareJid + "'").executeSingle();
                if (rosterEntryModel != null) {
                    rosterEntryModel.setPresence(presence);
                    try {
                        rosterEntryModel.save();
                    } catch (SQLiteDiskIOException e) {
                        Log.i("Service", e.getMessage());
                    }
                    if (rosterEntryModel.presence == RosterEntryModel.UserStatus.USER_STATUS_AVAILIBLE.ordinal()) {
                        List<ChatRoomOccupantModel> chatRoomOccupantModels = DbHelper.getRoomOccupantModelByUser(rosterEntryModel.bareJid);
                        for (ChatRoomOccupantModel model : chatRoomOccupantModels) {
                            if (model.chatRoomModel.ownerJidCol.equals(userAppPreference.getUserStringKey())) {
                                inviteUserToRoom(model.chatRoomModel.threadIdCol, model.bareJid);
                            }
                        }
                    }
                }
            }
        }
    }
}
