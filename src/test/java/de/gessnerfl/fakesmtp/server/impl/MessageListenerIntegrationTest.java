package de.gessnerfl.fakesmtp.server.impl;

import de.gessnerfl.fakesmtp.TestResourceUtil;
import de.gessnerfl.fakesmtp.repository.EmailRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@Transactional
@ActiveProfiles("integrationtest")
@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageListenerIntegrationTest {
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private MessageListener sut;

    @Before
    public void setup(){
        emailRepository.deleteAll();
    }

    @Test
    public void shouldCreateEmailForEmlFileWithSubject() throws Exception {
        var testFilename = "mail-with-subject.eml";
        var data = TestResourceUtil.getTestFile(testFilename);
        var rawData = TestResourceUtil.getTestFileContent(testFilename);

        sut.deliver(SENDER, RECEIVER, data);

        var mails = emailRepository.findAll();
        assertThat(mails, hasSize(1));

        var mail = mails.get(0);

        assertNotNull(mail.getId());
        assertEquals(SENDER, mail.getFromAddress());
        assertEquals(RECEIVER, mail.getToAddress());
        assertEquals("This is the mail title", mail.getSubject());
        assertEquals(rawData, mail.getRawData());
        assertFalse(mail.getHtmlContent().isPresent());
        assertTrue(mail.getPlainContent().isPresent());
        assertEquals("This is the message content", mail.getPlainContent().get().getData());
        assertNotNull(mail.getReceivedOn());
    }

    @Test
    public void shouldCreateEmailForEmlFileWithoutSubject() throws Exception {
        var testFilename = "mail-without-subject.eml";
        var data = TestResourceUtil.getTestFile(testFilename);
        var rawData = TestResourceUtil.getTestFileContent(testFilename);

        sut.deliver(SENDER, RECEIVER, data);

        var mails = emailRepository.findAll();
        assertThat(mails, hasSize(1));

        var mail = mails.get(0);

        assertNotNull(mail.getId());
        assertEquals(SENDER, mail.getFromAddress());
        assertEquals(RECEIVER, mail.getToAddress());
        assertEquals(EmailFactory.UNDEFINED, mail.getSubject());
        assertEquals(rawData, mail.getRawData());
        assertFalse(mail.getHtmlContent().isPresent());
        assertTrue(mail.getPlainContent().isPresent());
        assertEquals("This is the message content", mail.getPlainContent().get().getData());
        assertNotNull(mail.getReceivedOn());
    }

    @Test
    public void shouldCreateMailForPlainText() throws Exception {
        var rawData = "this is just some dummy content";
        var data = new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));

        sut.deliver(SENDER, RECEIVER, data);

        var mails = emailRepository.findAll();
        assertThat(mails, hasSize(1));

        var mail = mails.get(0);

        assertNotNull(mail.getId());
        assertEquals(SENDER, mail.getFromAddress());
        assertEquals(RECEIVER, mail.getToAddress());
        assertEquals(EmailFactory.UNDEFINED, mail.getSubject());
        assertEquals(rawData, mail.getRawData());
        assertFalse(mail.getHtmlContent().isPresent());
        assertTrue(mail.getPlainContent().isPresent());
        assertEquals(rawData, mail.getPlainContent().get().getData());
        assertNotNull(mail.getReceivedOn());
    }
}