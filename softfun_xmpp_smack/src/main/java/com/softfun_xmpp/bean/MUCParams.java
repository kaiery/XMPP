package com.softfun_xmpp.bean;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 * Created by 范张 on 2016-06-23.
 */
public class MUCParams {
    private MessageListener messageListener;
    private ParticipantStatusListener participantStatusListener;

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ParticipantStatusListener getParticipantStatusListener() {
        return participantStatusListener;
    }

    public void setParticipantStatusListener(ParticipantStatusListener participantStatusListener) {
        this.participantStatusListener = participantStatusListener;
    }

}
