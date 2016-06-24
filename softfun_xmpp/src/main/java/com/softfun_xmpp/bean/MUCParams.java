package com.softfun_xmpp.bean;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 * Created by 范张 on 2016-06-23.
 */
public class MUCParams {
    private PacketListener messageListener;
    private ParticipantStatusListener participantStatusListener;

    public PacketListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(PacketListener messageListener) {
        this.messageListener = messageListener;
    }

    public ParticipantStatusListener getParticipantStatusListener() {
        return participantStatusListener;
    }

    public void setParticipantStatusListener(ParticipantStatusListener participantStatusListener) {
        this.participantStatusListener = participantStatusListener;
    }

}
