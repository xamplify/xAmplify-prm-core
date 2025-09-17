/**
 * 
 */
package com.xtremand.rest.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "com.xtremand.user.dao", "com.xtremand.user.service" })
public class HibernateConfig {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment env;

	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setPackagesToScan(new String[] { "com.xtremand.user.bom", "com.xtremand.mail.bom",
				"com.xtremand.video.bom", "com.xtremand.common.bom", "com.xtremand.campaign.bom",
				"com.xtremand.social.bom", "com.xtremand.log.bom", "com.xtremand.partner.bom",
				"com.xtremand.deal.registration", "com.xtremand.marketo.bom", "com.xtremand.form.bom",
				"com.xtremand.util.bom", "com.xtremand.form.submit.bom", "com.xtremand.form.bom.email.template",
				"com.xtremand.landing.page.bom", "com.xtremand.landing.page.analytics.bom", "com.xtremand.vendor.bom",
				"com.xtremand.partnership.bom", "com.xtremand.drip.email.bom", "com.xtremand.integration.bom",
				"com.xtremand.form.submit.dto", "com.xtremand.demo.request.bom", "com.xtremand.sales.person.bom",
				"com.xtremand.gdpr.setting.bom", "com.xtremand.salesforce.bom",
				"com.xtremand.dashboard.analytics.views.bom", "com.xtremand.dashboard.buttons.bom",
				"com.xtremand.vanity.email.templates.bom", "com.xtremand.*.bom", "com.xtremand.views",
				"com.xtremand.team.member.group.bom", "com.xtremand.highlevel.analytics.bom",
				"com.xtremand.custom.css.bom", "com.xtremand.*.*.bom", "com.xtremand.vanity.login.templates.bom",
				"com.xtremand.vendor.journey.landing.page.bom", "com.xtremand.custom.html.block.bom" });

		sessionFactory.setHibernateProperties(hibernateProperties());
		return sessionFactory;
	}

	@Bean
	public HibernateTransactionManager transactionManager() {
		HibernateTransactionManager txManager = new HibernateTransactionManager();
		txManager.setSessionFactory(sessionFactory().getObject());
		return txManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@SuppressWarnings("serial")
	Properties hibernateProperties() {
		return new Properties() {
			{
				setProperty("hibernate.hbm2ddl.auto", "false");
				setProperty("hibernate.dialect", env.getProperty("jdbc.hibernate.dialect"));
				setProperty("hibernate.globally_quoted_identifiers", "false");
				setProperty("hibernate.show_sql", env.getProperty("show.sql"));
				setProperty("hibernate.format_sql", env.getProperty("format.sql"));
				setProperty(" hibernate.jdbc.batch_size", "30");
				/******** The Below Four Properties For Second Level Cache ********/
				setProperty("hibernate.cache.use_second_level_cache", "false");
				// setProperty("hibernate.cache.use_query_cache", "true");
				// setProperty("hibernate.cache.region.factory_class",
				// "org.hibernate.cache.ehcache.EhCacheRegionFactory");
				// setProperty("hibernate.cache.provider_class",
				// "org.hibernate.cache.EhCacheProvider");
				setProperty("javax.persistence.validation.mode", "none");
				setProperty("hibernate.order_updates", "true");
				setProperty("hibernate.batch_versioned_data", "true");
				// setProperty("hibernate.order_inserts", "true");// Uncommenting this line is
				// creating problem while inserting into xt_campaign_partner table;
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
