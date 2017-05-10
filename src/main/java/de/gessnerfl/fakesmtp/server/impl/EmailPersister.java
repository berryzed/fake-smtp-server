package de.gessnerfl.fakesmtp.server.impl;

import de.gessnerfl.fakesmtp.model.Email;
import de.gessnerfl.fakesmtp.repository.EmailRepository;
import de.gessnerfl.fakesmtp.server.EmailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = IOException.class)
public class EmailPersister implements SimpleMessageListener {
    private final EmailFactory emailFactory;
    private final EmailRepository emailRepository;

    @Autowired
    public EmailPersister(EmailFactory emailFactory, EmailRepository emailRepository) {
        this.emailFactory = emailFactory;
        this.emailRepository = emailRepository;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
        Email email = emailFactory.convert(from, recipient, data);
        emailRepository.save(email);
    }
}