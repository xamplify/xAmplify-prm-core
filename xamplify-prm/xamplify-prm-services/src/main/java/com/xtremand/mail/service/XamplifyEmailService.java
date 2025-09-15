package com.xtremand.mail.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.mail.exception.MailException;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;

@Service("mailService")
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class XamplifyEmailService extends MailService {

	private static final Logger logger = LoggerFactory.getLogger(XamplifyEmailService.class);

	@Value("${mail.host}")
	private String smtpHost;

	@Value("${mail.port}")
	private int smtpPort;

	@Value("${mail.username}")
	private String smtpUsername;

	@Value("${mail.password}")
	private String smtpPassword;

	@Value("${mail.from.name}")
	private String mailSender;

	@Value("${mail.from.email}")
	private String senderEmail;

	@Value("${spring.profiles.active:local}")
	private String profiles;

	@Value("${email.notification:true}")
	private boolean emailNotification;

	@Value("${xamplify.scheduler.email:no-reply@example.com}")
	private String xAmplifySchedulerEmail;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private AsyncComponent asyncComponent;

	public void sendMail(MailService.EmailBuilder builder) throws MailException {
		try {
			if (emailNotification) {
				prepareEmailContent(builder);
			} else {
				String errorMessage = "Email option is disabled in (" + profiles + ") environment. Email not sent.";
				logger.error(errorMessage);
			}

			// Save email history async
			if (builder.isTeamMemberEmail()) {
				asyncComponent.saveTeamMemberEmailsHistory(builder);
			} else if (builder.isPlaybookWorkflowExists() && builder.getWorkflowId() != null
					&& builder.getLearningTrackId() != null && builder.getUserId() != null) {
				asyncComponent.savePlaybookWorkflowEmailHistory(builder);
			} else {
				logEmailSentSuccessfullyMessage(builder, emailNotification);
			}

		} catch (Exception ex) {
			logger.error("sendMail(" + builder.toString() + ")", ex);
			throw new MailException(ex);
		}
	}

	private void logEmailSentSuccessfullyMessage(MailService.EmailBuilder builder, boolean sendEmailOptionEnable) {
		if (sendEmailOptionEnable) {
			String message = "Email sent successfully To " + builder.getTo() + " [" + builder.getSubject() + "]";
			String ccMessage = "Email sent successfully CC " + builder.getCCEmailIds() + " [" + builder.getSubject()
					+ "]";
			logger.debug(ccMessage + " - " + new Date());
			logger.debug(message + " - " + new Date());
		}
	}

	/** Core method that uses JavaMail */
	private void prepareEmailContent(MailService.EmailBuilder builder) {
		sendEmailUsingJavaMail(builder);
	}

	private void sendEmailUsingJavaMail(MailService.EmailBuilder builder) {
		if (!emailNotification) {
			logger.error("Email API disabled. No emails will be sent. {}", new Date());
			return;
		}

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", String.valueOf(smtpPort));

		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpUsername, smtpPassword);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);

			// From
			String fromEmailId = builder.getFrom();
			String senderName = builder.getSenderName() != null ? builder.getSenderName() : mailSender;
			message.setFrom(new InternetAddress(senderEmail, senderName));

			// Reply-To handling if DNS not configured
			if (!userDao.isEmailDnsConfigured(fromEmailId) && !xAmplifySchedulerEmail.equals(fromEmailId)) {
				message.setReplyTo(new InternetAddress[] { new InternetAddress(fromEmailId) });
			}

			// To
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(builder.getTo()));

			// CC
			addRecipients(message, builder.getCCEmailIds(), Message.RecipientType.CC);

			// BCC
			addRecipients(message, builder.getBCCEmailIds(), Message.RecipientType.BCC);

			message.setSubject(builder.getSubject());

			// Body + Attachments
			Multipart multipart = new MimeMultipart();

			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(builder.getBody(), "text/html; charset=utf-8");
			multipart.addBodyPart(bodyPart);

			if (XamplifyUtils.isNotEmptyList(builder.getAttachments())) {
				for (MultipartFile file : builder.getAttachments()) {
					MimeBodyPart attachmentPart = new MimeBodyPart();
					DataSource source = new ByteArrayDataSource(file.getBytes(), file.getContentType());
					attachmentPart.setDataHandler(new DataHandler(source));
					attachmentPart.setFileName(file.getOriginalFilename());
					multipart.addBodyPart(attachmentPart);
				}
			}

			message.setContent(multipart);

			// Send
			Transport.send(message);
			builder.setStatusCode(200);

		} catch (MessagingException | IOException e) {
			builder.setStatusCode(500);
			throw new MailException(e);
		}
	}

	private void addRecipients(MimeMessage message, List<String> emailIds, Message.RecipientType type)
			throws MessagingException {
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			for (String emailId : emailIds) {
				message.addRecipient(type, new InternetAddress(emailId));
			}
		}
	}
}
