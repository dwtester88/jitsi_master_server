/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.android.gui.chat;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.event.*;
import net.java.sip.communicator.service.filehistory.*;
import net.java.sip.communicator.service.metahistory.*;
import net.java.sip.communicator.service.msghistory.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;

import org.jitsi.android.gui.*;

import org.jitsi.android.gui.contactlist.model.MetaContactRenderer;
import org.jitsi.android.util.java.awt.event.*;
import org.jitsi.android.util.javax.swing.event.*;
import org.jitsi.android.util.javax.swing.text.*;
import org.jitsi.service.resources.*;
import org.jitsi.util.*;

import java.io.*;

import java.util.*;

/**
 * 
 * @author Yana Stamcheva
 * @author Pawel Domas
 */
public class ChatSession
    implements Chat, MessageListener
{
    /**
     * The logger
     */
    private final static Logger logger = Logger.getLogger(ChatSession.class);

    /**
     * Number of history messages to be returned from loadHistory call.
     * Limits the amount of messages being loaded at one time.
     */
    private static final int HISTORY_CHUNK_SIZE = 30;

    /**
     * The underlying <tt>MetaContact</tt>, we're chatting with.
     */
    private final MetaContact metaContact;

    /**
     * The current chat transport.
     */

    //mychange private nonstatic final variable to public static non final
    private static Contact currentChatTransport;

    // mychange variable is assigned
    public static Contact currentChatTransport1;

    // mychange variable is assigned
    public static Contact sourcereqest_contact;

    /**
     * The chat history filter.
     */
    protected final String[] chatHistoryFilter
        = new String[]{ MessageHistoryService.class.getName(),
                        FileHistoryService.class.getName()};

    /**
     * Messages cache used by this session.
     */
    private List<ChatMessage> msgCache = new LinkedList<ChatMessage>();

    /**
     * Synchronization root for messages cache.
     */
    private final Object cacheLock = new Object();

    /**
     * Flag indicates if the history has been cached(it must be done only once
     * and next messages are cached through the listeners mechanism).
     */
    private boolean historyLoaded = false;

    private final List<ChatSessionListener> msgListeners
            = new ArrayList<ChatSessionListener>();

    /**
     * Field used by the <tt>ChatController</tt> to keep track of last edited
     * message content.
     */
    private String editedText;

    /**
     * Field used by the <tt>ChatController</tt> to remember if user was
     * recently correcting the message.
     */
    private String correctionUID;
    /**
     * Creates a chat session with the given <tt>MetaContact</tt>.
     *
     * @param metaContact the <tt>MetaContact</tt> we're chatting with
     */
    public ChatSession(MetaContact metaContact)
    {
        //mychange to get some information
        logger.info("mychange chatsession is having metacontact "+metaContact.getDisplayName());
        logger.info("mychange chatsession is having metacontact "+metaContact.getMetaUID());
        logger.info("mychange chatsession is having metacontact "+metaContact.getDefaultContact().getAddress());
        logger.info("mychange chatsession is having metacontact detail "+metaContact.getContacts().next().getDisplayName());
       /* while (metaContact.getContacts().hasNext()){
            logger.info("mychange chatsession is having metacontact detail "+metaContact.getContacts().next().getAddress());
           }*/

        this.metaContact = metaContact;
        currentChatTransport = metaContact.getDefaultContact(
            OperationSetBasicInstantMessaging.class);

        // Prevent from creating chats without the chat op set.
        if(currentChatTransport == null)
        {
            throw new NullPointerException();
        }

        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();
            OperationSetBasicInstantMessaging imOpSet
                    = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetBasicInstantMessaging.class);
            if (imOpSet != null)
            {
                imOpSet.addMessageListener(this);
            }
        }
    }

    /*//mychange method to convert image to base64string
    public static String encodeImage()
    {
        File imagefile = new File(
                Environment.getExternalStorageDirectory(),
                "test.png");
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;
    }*/


    //mychange method to convert image to base64string and send it to user
    //mychange method to convert image to base64string
    public static String convertimage() {
        String imageDataString = null;
        try {
            File imagefile = new File(
                    Environment.getExternalStorageDirectory(),
                    "test.jpg");
            // Reading a Image file from file system
            FileInputStream imageInFile = new FileInputStream(imagefile);
            byte imageData[] = new byte[(int) imagefile.length()];
            imageInFile.read(imageData);
            //* Converting Image byte array into Base64 String
            imageDataString = encodeImage(imageData);
            //* Converting a Base64 String into Image byte array
            // byte[] imageByteArray = decodeImage(imageDataString);
            //* Write a image byte array into file system
            //FileOutputStream imageOutFile = new FileOutputStream("/Users/jeeva/Pictures/wallpapers/water-drop-after-convert.jpg");
            //imageOutFile.write(imageByteArray);
            //imageInFile.close();
            //imageOutFile.close();
            logger.info("Image Successfully Manipulated!");
        } catch (FileNotFoundException e) {
            logger.info("Image not found" + e);
        } catch (IOException ioe) {
            logger.info("Exception while reading the Image " + ioe);
        }
        return imageDataString;
    }
    //mychange
    // * Encodes the byte array into base64 string
    // * @param imageByteArray - byte array
     //* @return String a {@link java.lang.String}
    public static String encodeImage(byte[] imageByteArray){
            return Base64.encodeToString(imageByteArray,Base64.DEFAULT);
    }
     //* Decodes the base64 string into byte array
     //* @param imageDataString - a {@link java.lang.String}
     //* @return byte array
    public static byte[] decodeImage(String imageDataString) {
        return Base64.decode(imageDataString,Base64.DEFAULT);
    }




                /**
                 * Returns the chat identifier.
                 *
                 * @return the chat identifier
                 */
    public String getChatId()
    {
        return metaContact.getMetaUID();
    }

    /**
     * Returns the underlying <tt>MetaContact</tt>, we're chatting with.
     *
     * @return the underlying <tt>MetaContact</tt>, we're chatting with
     */
    public MetaContact getMetaContact()
    {
        return metaContact;
    }

    /**
     * Sens the given message through the current chat transport.
     *
     * @param message the message to send
     */



    //mychange nonstatic method to static
    public static void sendMessage(final String message, final String sourcerequest)
    {
        /*if (StringUtils.isNullOrEmpty(message)) {
            logger.info("mychange message is blank " + message);
            return;
        }*/

        //mychange


        ProtocolProviderService pps
                = MetaContactRenderer.contactsmetacontact.get(1).getDefaultContact().getProtocolProvider();


        /* //mychange below code is the original code of above change
        ProtocolProviderService pps
                = currentChatTransport.getProtocolProvider();*/

        if(pps == null)
        {
            logger.error("No protocol provider returned by "
                                 + currentChatTransport);
            return;
        }

        final OperationSetBasicInstantMessaging imOpSet
            = pps.getOperationSet(OperationSetBasicInstantMessaging.class);

        new Thread()
        {
            @Override
            public void run()
            {
                Message msg = imOpSet.createMessage(message);



                //mychange logging some info for details
                logger.info("mychange chatsession message getcontent "+msg.getContent());
                logger.info("mychange chatsession message getencoding "+msg.getEncoding());
                logger.info("mychange chatsession message getrawdata "+msg.getRawData().toString());
                logger.info("mychange chatsession message getmessageuid "+msg.getMessageUID());
                //AndroidGUIActivator.getContactListService().

                 //imOpSet.sendInstantMessage( currentChatTransport,ContactResource.BASE_RESOURCE,msg);

                //mychange this is sending message after encoding
               /*//mychange
                MetaContactRenderer metaContactRenderer = new MetaContactRenderer();
                MetaContact metaContact2 = metaContactRenderer.contactsmetacontact.get(1);
                currentChatTransport1 = metaContact2.getDefaultContact(
                OperationSetBasicInstantMessaging.class);*/
                //mychange here we are transmitting the same message to all the users ahich are sotred in the caontacmetacontact arraylist
                //to do so we are making currentchattransport as the contact object to do so we us the arraylist as below and getting the
                /*for(int i =0;i<MetaContactRenderer.contactsmetacontact.size();i++) {
                    currentChatTransport1 = MetaContactRenderer.contactsmetacontact.get(i).getDefaultContact();
                    imOpSet.sendInstantMessage(currentChatTransport1, ContactResource.BASE_RESOURCE, msg);
                    logger.info("mychange chatsession message sent is " + msg.getContent() +
                            " CurrentChatTransport is destination address " + currentChatTransport1.getAddress() + " contact count is ");
                }*/


                //mychange here image is encoded
               // msg = imOpSet.createMessage(convertimage());

                if(sourcerequest.equals("broadcast")) {
                    for (int i = 0; i < MetaContactRenderer.contactsmetacontact.size(); i++) {
                        currentChatTransport1 = MetaContactRenderer.contactsmetacontact.get(i).getDefaultContact();
                        imOpSet.sendInstantMessage(currentChatTransport1, ContactResource.BASE_RESOURCE, msg);
                        logger.info("mychange chatsession message sent is " + msg.getContent() +
                                " CurrentChatTransport is destination address " + currentChatTransport1.getAddress() + " contact count is ");
                    }
                }
                else{

                    for(int i =0;i<MetaContactRenderer.contactsmetacontact.size();i++) {
                        logger.info("door contact is " + MetaContactRenderer.contactsmetacontact.get(i).getDisplayName());

                        if (MetaContactRenderer.contactsmetacontact.get(i).getDisplayName().contains(sourcerequest)) {
                            sourcereqest_contact = MetaContactRenderer.contactsmetacontact.get(i).getDefaultContact();
                        }
                    }
                    imOpSet.sendInstantMessage(sourcereqest_contact, ContactResource.BASE_RESOURCE, msg);
                    logger.info("mychange chatsession message sent is " + msg.getContent() +
                            " CurrentChatTransport is destination address " + sourcereqest_contact.getAddress() + " contact count is ");



                }


            }
        }.start();
    }

    /**
     * Stores recently edited message text.
     * @param editedText recently edited message text.
     */
    public void setEditedText(String editedText)
    {
        this.editedText = editedText;
    }

    /**
     * Returns recently edited message text.
     * @return recently edited message text.
     */
    public String getEditedText()
    {
        return editedText;
    }

    /**
     * Stores the UID of recently corrected message.
     * @param correctionUID the UID of recently corrected message.
     */
    public void setCorrectionUID(String correctionUID)
    {
        this.correctionUID = correctionUID;
    }

    /**
     * Gets the UID of recently corrected message.
     * @return the UID of recently corrected message.
     */
    public String getCorrectionUID()
    {
        return correctionUID;
    }

    public void dispose()
    {
        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();

            OperationSetBasicInstantMessaging imOpSet
                    = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetBasicInstantMessaging.class);

            if (imOpSet != null)
            {
                imOpSet.removeMessageListener(this);
            }
        }
    }

    /**
     * Adds the given <tt>MessageListener</tt> to listen for message events in
     * this chat session.
     *
     * @param l the <tt>MessageListener</tt> to add
     */
    public void addMessageListener(ChatSessionListener l)
    {
        if(!msgListeners.contains(l))
            msgListeners.add(l);
    }

    /**
     * Removes the given <tt>MessageListener</tt> from this chat session.
     *
     * @param l the <tt>MessageListener</tt> to remove
     */
    public void removeMessageListener(ChatSessionListener l)
    {
        msgListeners.remove(l);
    }

    /**
     * Adds the given <tt>MessageListener</tt> to listen for message events in
     * this chat session.
     *
     * @param l the <tt>MessageListener</tt> to add
     */
    public void addTypingListener(TypingNotificationsListener l)
    {
        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();

            OperationSetTypingNotifications typingOpSet
                = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetTypingNotifications.class);

            if (typingOpSet != null)
            {
                typingOpSet.addTypingNotificationsListener(l);
            }
        }
    }

    /**
     * Removes the given <tt>MessageListener</tt> from this chat session.
     *
     * @param l the <tt>MessageListener</tt> to remove
     */
    public void removeTypingListener(TypingNotificationsListener l)
    {
        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();

            OperationSetTypingNotifications typingOpSet
                = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetTypingNotifications.class);

            if (typingOpSet != null)
            {
                typingOpSet.removeTypingNotificationsListener(l);
            }
        }
    }

    /**
     * Adds the given <tt>MessageListener</tt> to listen for message events in
     * this chat session.
     *
     * @param l the <tt>MessageListener</tt> to add
     */
    public void addContactStatusListener(ContactPresenceStatusListener l)
    {
        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();

            OperationSetPresence presenceOpSet
                = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetPresence.class);

            if (presenceOpSet != null)
            {
                presenceOpSet.addContactPresenceStatusListener(l);
            }
        }
    }

    /**
     * Removes the given <tt>MessageListener</tt> from this chat session.
     *
     * @param l the <tt>MessageListener</tt> to remove
     */
    public void removeContactStatusListener(ContactPresenceStatusListener l)
    {
        Iterator<Contact> protoContacts = metaContact.getContacts();

        while (protoContacts.hasNext())
        {
            Contact protoContact = protoContacts.next();

            OperationSetPresence presenceOpSet
                = protoContact.getProtocolProvider().getOperationSet(
                    OperationSetPresence.class);

            if (presenceOpSet != null)
            {
                presenceOpSet.removeContactPresenceStatusListener(l);
            }
        }
    }

    /**
     * Returns a collection of last messages.
     *
     * @return a collection of last messages.
     */
    public Collection<ChatMessage> getHistory(boolean init)
    {
        // If chat is initializing and we have cached messages including history
        // then just return the cache
        if(init && historyLoaded)
        {
            return msgCache;
        }

        final MetaHistoryService metaHistory
            = AndroidGUIActivator.getMetaHistoryService();

        // If the MetaHistoryService is not registered we have nothing to do
        // here. The history could be "disabled" from the user
        // through one of the configuration forms.
        if (metaHistory == null)
            return msgCache;

        Collection<Object> history;
        if(msgCache.size() == 0)
        {
            history = metaHistory.findLast(chatHistoryFilter,
                                           metaContact,
                                           HISTORY_CHUNK_SIZE);
        }
        else
        {
            ChatMessage oldest;
            synchronized (cacheLock)
            {
                 oldest = msgCache.get(0);
            }
            history = metaHistory.findLastMessagesBefore(
                    chatHistoryFilter, metaContact,
                    oldest.getDate(), HISTORY_CHUNK_SIZE);
        }

        // Convert events into messages
        Iterator<Object> iterator = history.iterator();
        ArrayList<ChatMessage> historyMsgs = new ArrayList<ChatMessage>();

        while (iterator.hasNext())
        {
            Object o = iterator.next();

            if(o instanceof MessageDeliveredEvent)
            {
                historyMsgs.add(
                        ChatMessageImpl.getMsgForEvent(
                                (MessageDeliveredEvent) o));

                logger.info("message received is " +ChatMessageImpl.getMsgForEvent((MessageDeliveredEvent) o));
            }
            else if(o instanceof MessageReceivedEvent)
            {
                historyMsgs.add(
                        ChatMessageImpl.getMsgForEvent(
                                (MessageReceivedEvent) o));
            }
            else
            {
                logger.error("Unexpected event in history: "+o);
            }
        }

        synchronized (cacheLock)
        {
            if(!historyLoaded)
            {
                // We have something cached and we want
                // to merge it with the history.
                // Do it only when we haven't merged it yet(ever).
                msgCache = mergeMsgLists(historyMsgs, msgCache, -1);
                historyLoaded = true;
            }
            else
            {
                // Otherwise just append the history
                msgCache.addAll(0, historyMsgs);
            }

            if(init)
                return msgCache;
            else
                return historyMsgs;
        }
    }

    /**
     * Merges given lists of messages. Output list is ordered by received date.
     * @param list1 first list to merge.
     * @param list2 the second list to merge.
     * @param msgLimit output list size limit.
     * @return merged list of messages contained in given lists ordered by the
     *         date. Output list size is limited to given <tt>msgLimit</tt>.
     */
    private List<ChatMessage> mergeMsgLists(List<ChatMessage> list1,
                                            List<ChatMessage> list2,
                                            int msgLimit)
    {
        if(msgLimit == -1)
            msgLimit = Integer.MAX_VALUE;

        List<ChatMessage> output = new LinkedList<ChatMessage>();
        int list1Idx = list1.size()-1;
        int list2Idx = list2.size()-1;

        while(list1Idx >= 0 && list2Idx >= 0 && output.size() < msgLimit)
        {
            ChatMessage list1Msg = list1.get(list1Idx);
            ChatMessage list2Msg = list2.get(list2Idx);

            if(list1Msg.getDate().after(list2Msg.getDate()))
            {
                output.add(0, list1Msg);
                list1Idx--;
            }
            else
            {
                output.add(0, list2Msg);
                list2Idx--;
            }
        }

        // Input remaining list 1 messages
        while(list1Idx >= 0 && output.size() < msgLimit)
            output.add(0, list1.get(list1Idx--));

        // Input remaining list 2 messages
        while(list2Idx >= 0 && output.size() < msgLimit)
            output.add(0, list2.get(list2Idx--));

        return output;
    }

    @Override
    public boolean isChatFocused()
    {
        return getChatId().equals(ChatSessionManager.getCurrentChatId());
    }

    @Override
    public String getMessage()
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void setChatVisible(boolean b)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void setMessage(String s)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void addChatFocusListener(ChatFocusListener chatFocusListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void removeChatFocusListener(ChatFocusListener chatFocusListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void addChatEditorKeyListener(KeyListener keyListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void removeChatEditorKeyListener(KeyListener keyListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void addChatEditorMenuListener(ChatMenuListener chatMenuListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void addChatEditorCaretListener(CaretListener caretListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void addChatEditorDocumentListener(DocumentListener documentListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void removeChatEditorMenuListener(ChatMenuListener chatMenuListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void removeChatEditorCaretListener(CaretListener caretListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void removeChatEditorDocumentListener(
            DocumentListener documentListener)
    {
        throw new RuntimeException("Not supported yet");
    }

    /**
     * Adds a message to this <tt>Chat</tt>.
     *
     * @param contactName the name of the contact sending the message
     * @param date the time at which the message is sent or received
     * @param messageType the type of the message
     * @param message the message text
     * @param contentType the content type
     */
    @Override
    public void addMessage(String contactName, Date date, String messageType,
                           String message, String contentType)
    {
        int chatMsgType = chatTypeToChatMsgType(messageType);
        ChatMessageImpl chatMsg
                = new ChatMessageImpl( contactName, date,
                                       chatMsgType, message,
                                       contentType );

        synchronized (cacheLock)
        {
            cacheNextMsg(chatMsg);

            for(ChatSessionListener l : msgListeners)
            {
                l.messageAdded(chatMsg);
            }
        }
    }

    @Override
    public void addChatLinkClickedListener(
            ChatLinkClickedListener chatLinkClickedListener)
    {
        ChatSessionManager.addChatLinkListener(chatLinkClickedListener);
    }

    @Override
    public void removeChatLinkClickedListener(
            ChatLinkClickedListener chatLinkClickedListener)
    {
        ChatSessionManager.removeChatLinkListener(chatLinkClickedListener);
    }

    @Override
    public Highlighter getHighlighter()
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public int getCaretPosition()
    {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public void promptRepaint()
    {
        throw new RuntimeException("Not supported yet");
    }

    public static int chatTypeToChatMsgType(String msgType)
    {
        if(msgType.equals(Chat.ACTION_MESSAGE))
        {
            return ChatMessage.ACTION_MESSAGE;
        }
        else if(msgType.equals(Chat.ERROR_MESSAGE))
        {
            return ChatMessage.ERROR_MESSAGE;
        }
        else if(msgType.equals(Chat.HISTORY_INCOMING_MESSAGE))
        {
            return ChatMessage.HISTORY_INCOMING_MESSAGE;
        }
        else if(msgType.equals(Chat.HISTORY_OUTGOING_MESSAGE))
        {
            return ChatMessage.HISTORY_OUTGOING_MESSAGE;
        }
        else if(msgType.equals(Chat.INCOMING_MESSAGE))
        {
            return ChatMessage.INCOMING_MESSAGE;
        }
        else if(msgType.equals(Chat.OUTGOING_MESSAGE))
        {
            return ChatMessage.OUTGOING_MESSAGE;
        }
        else if(msgType.equals(Chat.SMS_MESSAGE))
        {
            return ChatMessage.SMS_MESSAGE;
        }
        else if(msgType.equals(Chat.STATUS_MESSAGE))
        {
            return ChatMessage.STATUS_MESSAGE;
        }
        else if(msgType.equals(SYSTEM_MESSAGE))
        {
            return ChatMessage.SYSTEM_MESSAGE;
        }
        else
        {
            throw new IllegalArgumentException(
                    "Not supported msg type: "+msgType);
        }
    }

    /**
     * Returns the shortened display name of this chat.
     *
     * @return the shortened display name of this chat
     */
    public String getShortDisplayName()
    {
        String contactDisplayName = metaContact.getDisplayName().trim();

        int atIndex = contactDisplayName.indexOf("@");

        if (atIndex > -1)
            contactDisplayName = contactDisplayName.substring(0, atIndex);

        int spaceIndex = contactDisplayName.indexOf(" ");

        if (spaceIndex > -1)
            contactDisplayName = contactDisplayName.substring(0, spaceIndex);

        return contactDisplayName;
    }

    @Override
    public void messageReceived(MessageReceivedEvent messageReceivedEvent)
    {
        if(!metaContact.containsContact(
                messageReceivedEvent.getSourceContact()))
        {
            return;
        }
        synchronized (cacheLock)
        {
            for(MessageListener l : msgListeners)
            {
                l.messageReceived(messageReceivedEvent);
            }
            cacheNextMsg(ChatMessageImpl.getMsgForEvent(messageReceivedEvent));
        }
    }

    /**
     * Caches next message.
     * @param newMsg the next message to cache.
     */
    private void cacheNextMsg(ChatMessageImpl newMsg)
    {
        msgCache.add(newMsg);
    }

    @Override
    public void messageDelivered(MessageDeliveredEvent messageDeliveredEvent)
    {
        if(!metaContact.containsContact(
                messageDeliveredEvent.getDestinationContact()))
        {
            return;
        }

        synchronized (cacheLock)
        {
            for(MessageListener l : msgListeners)
            {
                l.messageDelivered(messageDeliveredEvent);
            }
            cacheNextMsg(ChatMessageImpl.getMsgForEvent(messageDeliveredEvent));
        }
    }

    @Override
    public void messageDeliveryFailed(MessageDeliveryFailedEvent evt)
    {
        for(MessageListener l : msgListeners)
        {
            l.messageDeliveryFailed(evt);
        }

        // Insert error message
        logger.error(evt.getReason());

        String errorMsg;

        Message sourceMessage = (Message) evt.getSource();

        Contact sourceContact = evt.getDestinationContact();

        MetaContact metaContact = AndroidGUIActivator.getContactListService()
                .findMetaContactByContact(sourceContact);

        ResourceManagementService rms
                = AndroidGUIActivator.getResourcesService();

        if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.OFFLINE_MESSAGES_NOT_SUPPORTED)
        {
            errorMsg = rms.getI18NString(
                    "service.gui.MSG_DELIVERY_NOT_SUPPORTED",
                    new String[] {sourceContact.getDisplayName()});
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.NETWORK_FAILURE)
        {
            errorMsg = rms.getI18NString(
                    "service.gui.MSG_NOT_DELIVERED");
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.PROVIDER_NOT_REGISTERED)
        {
            errorMsg = rms.getI18NString(
                    "service.gui.MSG_SEND_CONNECTION_PROBLEM");
        }
        else if (evt.getErrorCode()
                == MessageDeliveryFailedEvent.INTERNAL_ERROR)
        {
            errorMsg = rms.getI18NString(
                    "service.gui.MSG_DELIVERY_INTERNAL_ERROR");
        }
        else
        {
            errorMsg = rms.getI18NString(
                    "service.gui.MSG_DELIVERY_ERROR");
        }

        String reason = evt.getReason();
        if (reason != null)
            errorMsg += " " + rms.getI18NString(
                    "service.gui.ERROR_WAS",
                    new String[]{reason});

        addMessage(
                metaContact.getDisplayName(),
                new Date(),
                Chat.OUTGOING_MESSAGE,
                sourceMessage.getContent(),
                sourceMessage.getContentType());

        addMessage(
                metaContact.getDisplayName(),
                new Date(),
                Chat.ERROR_MESSAGE,
                errorMsg,
                OperationSetBasicInstantMessaging.DEFAULT_MIME_TYPE);
    }

    /**
     * Returns <tt>OperationSetMessageCorrection</tt> if this chat session
     * supports message corrections and <tt>null</tt> otherwise.
     *
     * @return <tt>OperationSetMessageCorrection</tt> if this chat session
     * supports message corrections and <tt>null</tt> otherwise.
     */
    private OperationSetMessageCorrection getOpSetMessageCorrection()
    {
        ProtocolProviderService pps = currentChatTransport.getProtocolProvider();

        OperationSetContactCapabilities capOpSet
                = pps.getOperationSet(OperationSetContactCapabilities.class);
        if ( capOpSet != null
                && capOpSet.getOperationSet(
                        currentChatTransport,
                        OperationSetMessageCorrection.class) == null)
        {
            return null;
        }

        return pps.getOperationSet(OperationSetMessageCorrection.class);
    }

    /**
     * Corrects the message identified by given UID with new message body.
     * @param uidToCorrect the UID of the message to correct.
     * @param message new message body to be applied.
     */
    public void correctMessage(String uidToCorrect, String message)
    {
        try
        {
            OperationSetMessageCorrection mcOpSet = getOpSetMessageCorrection();

            Message msg = mcOpSet.createMessage(message);
            mcOpSet.correctMessage(
                currentChatTransport, null, msg, uidToCorrect);
        }
        catch(Exception e)
        {
            logger.error("Message correction error for UID: "
                             + uidToCorrect + ", new content: " + message, e);
        }
    }

    /**
     * Returns <code>true</code> if this chat session supports typing
     * notifications, otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if this chat session supports typing
     * notifications, otherwise returns <code>false</code>.
     */
    public boolean allowsTypingNotifications()
    {
        ProtocolProviderService protocolProviderService
            = currentChatTransport.getProtocolProvider();

        if(protocolProviderService == null)
            return false;

        Object tnOpSet = protocolProviderService
                .getOperationSet(OperationSetTypingNotifications.class);

        return tnOpSet != null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean sendTypingNotification(int typingState)
    {
        // If this chat transport does not support sms messaging we do
        // nothing here.
        if (!allowsTypingNotifications())
            return false;

        ProtocolProviderService protocolProvider
            = currentChatTransport.getProtocolProvider();
        OperationSetTypingNotifications tnOperationSet
            = protocolProvider.getOperationSet(
                OperationSetTypingNotifications.class);

        // if protocol is not registered or contact is offline don't
        // try to send typing notifications
        if(protocolProvider.isRegistered()
                && currentChatTransport.getPresenceStatus().getStatus()
                >= PresenceStatus.ONLINE_THRESHOLD)
        {
            try
            {
                tnOperationSet.sendTypingNotification(
                    currentChatTransport, typingState);
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Failed to send typing notifications.", ex);

                return false;
            }
        }

        return false;
    }

    /**
     * Extends <tt>MessageListener</tt> interface in order to provide
     * notifications about injected messages without the need of event objects.
     *
     * @author Pawel Domas
     */
    public interface ChatSessionListener
        extends MessageListener
    {
        public void messageAdded(ChatMessage msg);
    }
}
