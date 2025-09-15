package com.xtremand.notification.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xtremand.common.bom.Notification;
import com.xtremand.notification.dao.NotificationDAO;

/**
 *  @author Manas Ranjan Sahoo
 *  @since 20/02/2018
 */

@Service
public class NotificationService {
	
	
	@Autowired
	private NotificationDAO notificationDAO;
	
	

	public List<Notification> listNotifications(Integer userId) {
		return notificationDAO.listNotifications(userId);
	}

	public void markAllAsRead(Integer userId) {
		notificationDAO.markAllAsRead(userId);
	}

	public Integer unreadCount(Integer userId) {
		return notificationDAO.unreadCount(userId);
	}

	public void markAsRead(Integer id) {
		notificationDAO.markAsRead(id);
	}
}
