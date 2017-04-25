package mega.privacy.android.app.lollipop.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.GroupChatInfoActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NonContactInfo;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaUser;

public class ChatController {

    Context context;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    DatabaseHandler dbH;
    MegaPreferences prefs = null;

    public ChatController(Context context){
        log("ChatController created");
        this.context = context;
        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }
        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(context);
        }
    }

    public void leaveChat(MegaChatRoom chat){
        if(context instanceof ManagerActivityLollipop){
            megaChatApi.leaveChat(chat.getChatId(), (ManagerActivityLollipop) context);
        }
        else if(context instanceof GroupChatInfoActivityLollipop){
            megaChatApi.leaveChat(chat.getChatId(), (GroupChatInfoActivityLollipop) context);
        }
        else if(context instanceof ChatActivityLollipop){
            megaChatApi.leaveChat(chat.getChatId(), (ChatActivityLollipop) context);
        }
    }

    public void leaveChat(long chatId){
        if(context instanceof ManagerActivityLollipop){
            megaChatApi.leaveChat(chatId, (ManagerActivityLollipop) context);
        }
        else if(context instanceof GroupChatInfoActivityLollipop){
            megaChatApi.leaveChat(chatId, (GroupChatInfoActivityLollipop) context);
        }
        else if(context instanceof ChatActivityLollipop){
            megaChatApi.leaveChat(chatId, (ChatActivityLollipop) context);
        }
    }

    public void clearHistory(MegaChatRoom chat){
        log("clearHistory: "+chat.getTitle());
        if(context instanceof ManagerActivityLollipop){
            megaChatApi.clearChatHistory(chat.getChatId(), (ManagerActivityLollipop) context);
        }
        else if(context instanceof ChatActivityLollipop){
            megaChatApi.clearChatHistory(chat.getChatId(), (ChatActivityLollipop) context);
        }
        else if(context instanceof ContactInfoActivityLollipop){
            megaChatApi.clearChatHistory(chat.getChatId(), (ContactInfoActivityLollipop) context);
        }
        else if(context instanceof GroupChatInfoActivityLollipop){
            megaChatApi.clearChatHistory(chat.getChatId(), (GroupChatInfoActivityLollipop) context);
        }
    }

    public void clearHistory(long chatId){
        log("clearHistory: "+chatId);
        if(context instanceof ManagerActivityLollipop){
            megaChatApi.clearChatHistory(chatId, (ManagerActivityLollipop) context);
        }
        else if(context instanceof ChatActivityLollipop){
            megaChatApi.clearChatHistory(chatId, (ChatActivityLollipop) context);
        }
        else if(context instanceof ContactInfoActivityLollipop){
            megaChatApi.clearChatHistory(chatId, (ContactInfoActivityLollipop) context);
        }
        else if(context instanceof GroupChatInfoActivityLollipop){
            megaChatApi.clearChatHistory(chatId, (GroupChatInfoActivityLollipop) context);
        }
    }

    public void deleteMessages(ArrayList<AndroidMegaChatMessage> messages, MegaChatRoom chat){
        log("deleteMessages: "+messages.size());
        MegaChatMessage messageToDelete;
        if(messages!=null){
            for(int i=0; i<messages.size();i++){
                messageToDelete = megaChatApi.deleteMessage(chat.getChatId(), messages.get(i).getMessage().getMsgId());
                if(messageToDelete==null){
                    log("The message cannot be deleted");
                }
            }
        }
    }

    public void alterParticipantsPermissions(long chatid, long uh, int privilege){
        log("alterParticipantsPermissions: "+uh);
        megaChatApi.updateChatPermissions(chatid, uh, privilege, (GroupChatInfoActivityLollipop) context);
    }

    public void removeParticipant(long chatid, long uh){
        log("removeParticipant: "+uh);
        if(context==null){
            log("Context is NULL");
        }
        megaChatApi.removeFromChat(chatid, uh, (GroupChatInfoActivityLollipop) context);
    }

    public void changeTitle(long chatid, String title){
        megaChatApi.setChatTitle(chatid, title, (GroupChatInfoActivityLollipop) context);
    }

    public void muteChats(ArrayList<MegaChatListItem> chats){
        for(int i=0; i<chats.size();i++){
            muteChat(chats.get(i));
            ((ManagerActivityLollipop)context).showMuteIcon(chats.get(i));
        }
    }

    public void muteChat(long chatHandle){
        log("muteChat");
        ChatItemPreferences chatPrefs = dbH.findChatPreferencesByHandle(Long.toString(chatHandle));
        if(chatPrefs==null){
            Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);

            chatPrefs = new ChatItemPreferences(Long.toString(chatHandle), Boolean.toString(false), defaultRingtoneUri.toString(), defaultSoundUri.toString());
            dbH.setChatItemPreferences(chatPrefs);
        }
        else{
            chatPrefs.setNotificationsEnabled(Boolean.toString(false));
            dbH.setNotificationEnabledChatItem(Boolean.toString(false), Long.toString(chatHandle));
        }
    }

    public void muteChat(MegaChatListItem chat){
        log("muteChatITEM");
        muteChat(chat.getChatId());
    }

    public void unmuteChat(MegaChatListItem chat){
        log("UNmuteChatITEM");
        unmuteChat(chat.getChatId());
    }

    public void unmuteChat(long chatHandle){
        log("UNmuteChat");
        ChatItemPreferences chatPrefs = dbH.findChatPreferencesByHandle(Long.toString(chatHandle));
        if(chatPrefs==null){
            Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);

            chatPrefs = new ChatItemPreferences(Long.toString(chatHandle), Boolean.toString(true), defaultRingtoneUri.toString(), defaultSoundUri.toString());
            dbH.setChatItemPreferences(chatPrefs);
        }
        else{
            chatPrefs.setNotificationsEnabled(Boolean.toString(true));
            dbH.setNotificationEnabledChatItem(Boolean.toString(true), Long.toString(chatHandle));
        }
    }

    public void enableChat(){
        dbH.setEnabledChat(true+"");
    }

    public String createManagementString(AndroidMegaChatMessage androidMessage, MegaChatRoom chatRoom) {
        log("createManagementString");

        MegaChatMessage message = androidMessage.getMessage();
        long userHandle = message.getUserHandle();

        if (message.getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS) {
            log("ALTER PARTICIPANT MESSAGE!!");

            if (message.getHandleOfAction() == megaApi.getMyUser().getHandle()) {
                log("me alter participant");

                StringBuilder builder = new StringBuilder();
                builder.append(context.getString(R.string.chat_me_text) + ": ");

                int privilege = message.getPrivilege();
                log("Privilege of me: " + privilege);
                String textToShow = "";
                String fullNameAction = getParticipantFullName(message.getUserHandle(), chatRoom);

                if (fullNameAction.trim().length() <= 0) {
                    log("No name!");
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                    if (nonContact != null) {
                        fullNameAction = nonContact.getFullName();
                    } else {
                        log("Ask for name non-contact");
                        fullNameAction = "Participant left";
//                            log("1-Call for nonContactName: "+ message.getUserHandle());
//                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
                    }
                }

                if (privilege != MegaChatRoom.PRIV_RM) {
                    log("I was added");
                    textToShow = String.format(context.getString(R.string.non_format_message_add_participant), context.getString(R.string.chat_I_text), fullNameAction);
                } else {
                    log("I was removed or left");
                    if (message.getUserHandle() == message.getHandleOfAction()) {
                        log("I left the chat");
                        textToShow = String.format(context.getString(R.string.non_format_message_participant_left_group_chat), context.getString(R.string.chat_I_text));

                    } else {
                        textToShow = String.format(context.getString(R.string.non_format_message_remove_participant), context.getString(R.string.chat_I_text), fullNameAction);
                    }
                }

                builder.append(textToShow);
                return builder.toString();

            } else {
                log("CONTACT Message type ALTER PARTICIPANTS");

                int privilege = message.getPrivilege();
                log("Privilege of the user: " + privilege);

                String fullNameTitle = getParticipantFullName(message.getHandleOfAction(), chatRoom);

                if (fullNameTitle.trim().length() <= 0) {
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getHandleOfAction() + "");
                    if (nonContact != null) {
                        fullNameTitle = nonContact.getFullName();
                    } else {
                        fullNameTitle = "Unknown name";
//                            log("3-Call for nonContactName: "+ message.getUserHandle());
//                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getHandleOfAction());
//                        megaChatApi.getUserFirstname(message.getHandleOfAction(), listener);
//                        megaChatApi.getUserLastname(message.getHandleOfAction(), listener);
                    }
                }

                StringBuilder builder = new StringBuilder();
                builder.append(fullNameTitle + ": ");

                String textToShow = "";
                if (privilege != MegaChatRoom.PRIV_RM) {
                    log("Participant was added");
                    if (message.getUserHandle() == megaApi.getMyUser().getHandle()) {
                        log("By me");
                        textToShow = String.format(context.getString(R.string.non_format_message_add_participant), fullNameTitle, context.getString(R.string.chat_me_text));
                    } else {
//                        textToShow = String.format(context.getString(R.string.message_add_participant), message.getHandleOfAction()+"");
                        log("By other");
                        String fullNameAction = getParticipantFullName(message.getUserHandle(), chatRoom);

                        if (fullNameAction.trim().length() <= 0) {
                            NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                            if (nonContact != null) {
                                fullNameAction = nonContact.getFullName();
                            } else {
                                fullNameAction = "Unknown name";
                                log("2-Call for nonContactName: " + message.getUserHandle());
//                                    ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                                    megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                                    megaChatApi.getUserLastname(message.getUserHandle(), listener);
                            }

                        }
                        textToShow = String.format(context.getString(R.string.non_format_message_add_participant), fullNameTitle, fullNameAction);

                    }
                }//END participant was added
                else {
                    log("Participant was removed or left");
                    if (message.getUserHandle() == megaApi.getMyUser().getHandle()) {
                        textToShow = String.format(context.getString(R.string.non_format_message_remove_participant), fullNameTitle, context.getString(R.string.chat_me_text));
                    } else {

                        if (message.getUserHandle() == message.getHandleOfAction()) {
                            log("The participant left the chat");

                            textToShow = String.format(context.getString(R.string.non_format_message_participant_left_group_chat), fullNameTitle);

                        } else {
                            log("The participant was removed");
                            String fullNameAction = getParticipantFullName(message.getUserHandle(), chatRoom);

                            if (fullNameAction.trim().length() <= 0) {
                                NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                                if (nonContact != null) {
                                    fullNameAction = nonContact.getFullName();
                                } else {
                                    fullNameAction = "Unknown name";
                                    log("4-Call for nonContactName: " + message.getUserHandle());
//                                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                                        megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                                        megaChatApi.getUserLastname(message.getUserHandle(), listener);
                                }

                            }

                            textToShow = String.format(context.getString(R.string.non_format_message_remove_participant), fullNameTitle, fullNameAction);
                        }
//                        textToShow = String.format(context.getString(R.string.message_remove_participant), message.getHandleOfAction()+"");
                    }
                } //END participant removed

                builder.append(textToShow);
                return builder.toString();

            } //END CONTACT MANAGEMENT MESSAGE
        } else if (message.getType() == MegaChatMessage.TYPE_PRIV_CHANGE) {
            log("PRIVILEGE CHANGE message");
            if (message.getHandleOfAction() == megaApi.getMyUser().getHandle()) {
                log("a moderator change my privilege");
                int privilege = message.getPrivilege();
                log("Privilege of the user: " + privilege);

                StringBuilder builder = new StringBuilder();
                builder.append(context.getString(R.string.chat_me_text) + ": ");

                String privilegeString = "";
                if (privilege == MegaChatRoom.PRIV_MODERATOR) {
                    privilegeString = context.getString(R.string.administrator_permission_label_participants_panel);
                } else if (privilege == MegaChatRoom.PRIV_STANDARD) {
                    privilegeString = context.getString(R.string.standard_permission_label_participants_panel);
                } else if (privilege == MegaChatRoom.PRIV_RO) {
                    privilegeString = context.getString(R.string.observer_permission_label_participants_panel);
                } else {
                    log("Change to other");
                    privilegeString = "Unknow";
                }

                String textToShow = "";

                if (message.getUserHandle() == megaApi.getMyUser().getHandle()) {
                    log("I changed my Own permission");
                    textToShow = String.format(context.getString(R.string.non_format_message_permissions_changed), context.getString(R.string.chat_I_text), privilegeString, context.getString(R.string.chat_me_text));
                } else {
                    log("I was change by someone");
                    String fullNameAction = getParticipantFullName(message.getUserHandle(), chatRoom);

                    if (fullNameAction.trim().length() <= 0) {
                        NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                        if (nonContact != null) {
                            fullNameAction = nonContact.getFullName();
                        } else {
                            fullNameAction = "Unknown name";
                            log("5-Call for nonContactName: " + message.getUserHandle());
//                                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                                megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                                megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }

                    }
                    textToShow = String.format(context.getString(R.string.non_format_message_permissions_changed), context.getString(R.string.chat_I_text), privilegeString, fullNameAction);
                }

                builder.append(textToShow);
                return builder.toString();
            } else {
                log("Participant privilege change!");
                log("Message type PRIVILEGE CHANGE: " + message.getContent());

                String fullNameTitle = getParticipantFullName(message.getHandleOfAction(), chatRoom);

                if (fullNameTitle.trim().length() <= 0) {
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getHandleOfAction() + "");
                    if (nonContact != null) {
                        fullNameTitle = nonContact.getFullName();
                    } else {
                        fullNameTitle = "Unknown name";
                        log("6-Call for nonContactName: " + message.getUserHandle());
//                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getHandleOfAction());
//                            megaChatApi.getUserFirstname(message.getHandleOfAction(), listener);
//                            megaChatApi.getUserLastname(message.getHandleOfAction(), listener);
                    }
                }

                StringBuilder builder = new StringBuilder();
                builder.append(fullNameTitle + ": ");

                int privilege = message.getPrivilege();
                String privilegeString = "";
                if (privilege == MegaChatRoom.PRIV_MODERATOR) {
                    privilegeString = context.getString(R.string.administrator_permission_label_participants_panel);
                } else if (privilege == MegaChatRoom.PRIV_STANDARD) {
                    privilegeString = context.getString(R.string.standard_permission_label_participants_panel);
                } else if (privilege == MegaChatRoom.PRIV_RO) {
                    privilegeString = context.getString(R.string.observer_permission_label_participants_panel);
                } else {
                    log("Change to other");
                    privilegeString = "Unknow";
                }

                String textToShow = "";
                if (message.getUserHandle() == megaApi.getMyUser().getHandle()) {
                    log("The privilege was change by me");
                    textToShow = String.format(context.getString(R.string.non_format_message_permissions_changed), fullNameTitle, privilegeString, context.getString(R.string.chat_me_text));

                } else {
                    log("By other");
                    String fullNameAction = getParticipantFullName(message.getUserHandle(), chatRoom);

                    if (fullNameAction.trim().length() <= 0) {
                        NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                        if (nonContact != null) {
                            fullNameAction = nonContact.getFullName();
                        } else {
                            fullNameAction = "Unknown name";
                            log("8-Call for nonContactName: " + message.getUserHandle());
//                                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                                megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                                megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }
                    }

                    textToShow = String.format(context.getString(R.string.non_format_message_permissions_changed), fullNameTitle, privilegeString, fullNameAction);
                }

                builder.append(textToShow);
                return builder.toString();
            }
        } else {
            //OTHER TYPE OF MESSAGES
            if (message.getUserHandle() == megaApi.getMyUser().getHandle()) {
                log("MY message!!:");
                log("MY message handle!!: " + message.getMsgId());

                StringBuilder builder = new StringBuilder();
                builder.append(context.getString(R.string.chat_me_text) + ": ");

                if (message.getType() == MegaChatMessage.TYPE_NORMAL) {
                    log("Message type NORMAL: " + message.getMsgId());

                    String messageContent = "";
                    if (message.getContent() != null) {
                        messageContent = message.getContent();
                    }

                    if (message.isEdited()) {
                        log("Message is edited");
                        String textToShow = messageContent + " " + context.getString(R.string.edited_message_text);
                        builder.append(textToShow);
                        return builder.toString();
                    } else if (message.isDeleted()) {
                        log("Message is deleted");

                        String textToShow = context.getString(R.string.text_deleted_message);
                        builder.append(textToShow);
                        return builder.toString();

                    } else {
                        builder.append(messageContent);
                        return builder.toString();
                    }
                } else if (message.getType() == MegaChatMessage.TYPE_TRUNCATE) {
                    log("Message type TRUNCATE");

                    String textToShow = context.getString(R.string.history_cleared_message);
                    builder.append(textToShow);
                    return builder.toString();
                } else if (message.getType() == MegaChatMessage.TYPE_CHAT_TITLE) {
                    log("Message type TITLE CHANGE: " + message.getContent());

                    String messageContent = message.getContent();
                    String textToShow = String.format(context.getString(R.string.non_format_change_title_messages), context.getString(R.string.chat_I_text), messageContent);
                    builder.append(textToShow);
                    return builder.toString();

                } else {
                    log("Type message: " + message.getType());
                    return "";
                }
            } else {
                log("Contact message!!");

                String fullNameTitle = getParticipantFullName(userHandle, chatRoom);

                if (fullNameTitle.trim().length() <= 0) {
//                        String userHandleString = megaApi.userHandleToBase64(message.getUserHandle());
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle() + "");
                    if (nonContact != null) {
                        fullNameTitle = nonContact.getFullName();
                    } else {
                        fullNameTitle = "Unknown name";
//
//                                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
//                                megaChatApi.getUserFirstname(message.getUserHandle(), listener);
//                                megaChatApi.getUserLastname(message.getUserHandle(), listener);
                    }
                }

                StringBuilder builder = new StringBuilder();
                builder.append(fullNameTitle + ": ");

                if (message.getType() == MegaChatMessage.TYPE_NORMAL) {

                    String messageContent = "";
                    if (message.getContent() != null) {
                        messageContent = message.getContent();
                    }

                    if (message.isEdited()) {
                        log("Message is edited");

                        String textToShow = messageContent + " " + context.getString(R.string.edited_message_text);
                        builder.append(textToShow);
                        return builder.toString();
                    } else if (message.isDeleted()) {
                        log("Message is deleted");
                        String textToShow = "";
                        if(chatRoom.isGroup()){
                            textToShow = String.format(context.getString(R.string.non_format_text_deleted_message_by), fullNameTitle);
                        }
                        else{
                            textToShow = context.getString(R.string.text_deleted_message);
                        }

                        builder.append(textToShow);
                        return builder.toString();

                    } else {
                        builder.append(messageContent);
                        return builder.toString();

                    }
                } else if (message.getType() == MegaChatMessage.TYPE_TRUNCATE) {
                    log("Message type TRUNCATE");

                    if (chatRoom.isGroup()) {
                        String textToShow = String.format(context.getString(R.string.non_format_history_cleared_by), fullNameTitle);
                        builder.append(textToShow);
                        return builder.toString();

                    } else {
                        String textToShow = context.getString(R.string.history_cleared_message);
                        builder.append(textToShow);
                        return builder.toString();
                    }
                } else if (message.getType() == MegaChatMessage.TYPE_CHAT_TITLE) {
                    log("Message type CHANGE TITLE " + message.getContent());

                    String messageContent = message.getContent();

                    String textToShow = String.format(context.getString(R.string.non_format_change_title_messages), fullNameTitle, messageContent);
                    builder.append(textToShow);
                    return builder.toString();
                } else {
                    log("Type message: " + message.getType());
                    log("Content: " + message.getContent());
                    return "";
                }
            }
        }
    }

    public String getFirstName(long userHandle, MegaChatRoom chatRoom){
        log("getFullName: "+userHandle);
        int privilege = chatRoom.getPeerPrivilegeByHandle(userHandle);
        log("privilege is: "+privilege);
        if(privilege==MegaChatRoom.PRIV_UNKNOWN||privilege==MegaChatRoom.PRIV_RM){
            log("Not participant any more!");
            String handleString = megaApi.handleToBase64(userHandle);
            MegaUser contact = megaApi.getContact(handleString);
            if(contact!=null){
                if(contact.getVisibility()==MegaUser.VISIBILITY_VISIBLE){
                    log("Is contact!");
                    return getContactFirstName(userHandle);
                }
                else{
                    log("Old contact");
                    return getNonContactFirstName(userHandle);
                }
            }
            else{
                log("Non contact");
                return getNonContactFirstName(userHandle);
            }
        }
        else{
            log("Is participant");
            return getParticipantFirstName(userHandle, chatRoom);
        }
    }

    public String getFullName(long userHandle, MegaChatRoom chatRoom){
        log("getFullName: "+userHandle);
        int privilege = chatRoom.getPeerPrivilegeByHandle(userHandle);
        log("privilege is: "+privilege);
        if(privilege==MegaChatRoom.PRIV_UNKNOWN||privilege==MegaChatRoom.PRIV_RM){
            log("Not participant any more!");
            String handleString = megaApi.handleToBase64(userHandle);
            MegaUser contact = megaApi.getContact(handleString);
            if(contact!=null){
                if(contact.getVisibility()==MegaUser.VISIBILITY_VISIBLE){
                    log("Is contact!");
                    return getContactFullName(userHandle);
                }
                else{
                    log("Old contact");
                    return getNonContactFullName(userHandle);
                }
            }
            else{
                log("Non contact");
                return getNonContactFullName(userHandle);
            }
        }
        else{
            log("Is participant");
            return getParticipantFullName(userHandle, chatRoom);
        }
    }

    public String getContactFirstName(long userHandle){
        MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(userHandle));
        if(contactDB!=null){

            String name = contactDB.getName();

            if(name==null){
                name="";
            }

            if (name.trim().length() <= 0){
                String lastName = contactDB.getLastName();
                if(lastName==null){
                    lastName="";
                }
                if (lastName.trim().length() <= 0){
                    log("1- Full name empty");
                    log("2-Put email as fullname");
                    String mail = contactDB.getMail();
                    if(mail==null){
                        mail="";
                    }
                    if (mail.trim().length() <= 0){
                        return "";
                    }
                    else{
                        return mail;
                    }
                }
                else{
                    return lastName;
                }

            }
            else{
                return name;
            }
        }
        return "";
    }

    public String getContactFullName(long userHandle){
        MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(userHandle));
        if(contactDB!=null){

            String name = contactDB.getName();
            String lastName = contactDB.getLastName();

            if(name==null){
                name="";
            }
            if(lastName==null){
                lastName="";
            }
            String fullName = "";

            if (name.trim().length() <= 0){
                fullName = lastName;
            }
            else{
                fullName = name + " " + lastName;
            }

            if (fullName.trim().length() <= 0){
                log("1- Full name empty");
                log("2-Put email as fullname");
                String mail = contactDB.getMail();
                if(mail==null){
                    mail="";
                }
                if (mail.trim().length() <= 0){
                    return "";
                }
                else{
                    return mail;
                }
            }

            return fullName;
        }
        return "";
    }

    public String getNonContactFirstName(long userHandle){
        NonContactInfo nonContact = dbH.findNonContactByHandle(userHandle+"");

        if(nonContact!=null){

            String name = nonContact.getFirstName();

            if(name==null){
                name="";
            }

            if (name.trim().length() <= 0){
                String lastName = nonContact.getLastName();
                if(lastName==null){
                    lastName="";
                }
                if (lastName.trim().length() <= 0){
                    log("1- Full name empty");
                    log("2-Put email as fullname");
                    String mail = nonContact.getEmail();
                    if(mail==null){
                        mail="";
                    }
                    if (mail.trim().length() <= 0){
                        return "";
                    }
                    else{
                        return mail;
                    }
                }
                else{
                    return lastName;
                }

            }
            else{
                return name;
            }
        }
        return "";
    }

    public String getNonContactFullName(long userHandle){
        NonContactInfo nonContact = dbH.findNonContactByHandle(userHandle+"");
        if(nonContact!=null){
            String fullName = nonContact.getFullName();

            if(fullName!=null){
                if(fullName.isEmpty()){
                    log("1-Put email as fullname");
                    String email = nonContact.getEmail();
                    if(email!=null){
                        if(email.isEmpty()){
                            return "";
                        }
                        else{
                            if (email.trim().length() <= 0){
                                return "";
                            }
                            else{
                                return email;
                            }
                        }
                    }
                    else{
                        return "";
                    }
                }
                else{
                    if (fullName.trim().length() <= 0){
                        log("2-Put email as fullname");
                        String email = nonContact.getEmail();
                        if(email!=null){
                            if(email.isEmpty()){
                                return "";
                            }
                            else{
                                if (email.trim().length() <= 0){
                                    return "";
                                }
                                else{
                                    return email;
                                }
                            }
                        }
                        else{
                            return "";
                        }
                    }
                    else{
                        return fullName;
                    }
                }
            }
            else{
                log("3-Put email as fullname");
                String email = nonContact.getEmail();
                if(email!=null){
                    if(email.isEmpty()){
                        return "";
                    }
                    else{
                        if (email.trim().length() <= 0){
                            return "";
                        }
                        else{
                            return email;
                        }
                    }
                }
                else{
                    return "";
                }
            }
        }
        return "";
    }

    public String getParticipantFirstName(long userHandle, MegaChatRoom chatRoom){
        log("getParticipantFirstName: "+userHandle);
        String firstName = chatRoom.getPeerFirstnameByHandle(userHandle);

        if(firstName==null){
            firstName="";
        }

        if (firstName.trim().length() <= 0){
            String lastName = chatRoom.getPeerLastnameByHandle(userHandle);
            if(lastName==null){
                lastName="";
            }
            if (lastName.trim().length() <= 0){
                log("1- Full name empty");
                log("2-Put email as fullname");
                String mail = chatRoom.getPeerEmailByHandle(userHandle);
                if(mail==null){
                    mail="";
                }
                if (mail.trim().length() <= 0){
                    return "";
                }
                else{
                    return mail;
                }
            }
            else{
                return lastName;
            }

        }
        else{
            return firstName;
        }
    }

    public String getParticipantFullName(long userHandle, MegaChatRoom chatRoom){
        log("getParticipantFullName: "+userHandle);
        String fullName = chatRoom.getPeerFullnameByHandle(userHandle);

        if(fullName!=null){
            if(fullName.isEmpty()){
                log("1-Put email as fullname");
                String participantEmail = chatRoom.getPeerEmailByHandle(userHandle);
                return participantEmail;
            }
            else{
                if (fullName.trim().length() <= 0){
                    log("2-Put email as fullname");
                    String participantEmail = chatRoom.getPeerEmailByHandle(userHandle);
                    return participantEmail;
                }
                else{
                    return fullName;
                }
            }
        }
        else{
            log("3-Put email as fullname");
            String participantEmail = chatRoom.getPeerEmailByHandle(userHandle);
            return participantEmail;
        }
    }

    public void pickFileToSend(){
        log("pickFileToSend");
        Intent intent = new Intent(context, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_SELECT_FILE);
//        ArrayList<String> longArray = new ArrayList<String>();
//        for (int i=0; i<users.size(); i++){
//            longArray.add(users.get(i).getEmail());
//        }
//        intent.putStringArrayListExtra("SELECTED_CONTACTS", longArray);
        ((ChatActivityLollipop) context).startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_FILE);
    }

    public static void log(String message) {
        Util.log("ChatController", message);
    }
}
