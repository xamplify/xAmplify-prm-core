package com.xtremand.company.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "xt_domain_colors")
@Data
@EqualsAndHashCode(callSuper = false)
public class DomainColor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String SEQUENCE = "xt_domain_colors_sequence";

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @SequenceGenerator(name =SEQUENCE, sequenceName =SEQUENCE,allocationSize = 1)
    @Column(name = "domain_color_id", nullable = false)
    private Integer domainColorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id",nullable = false,unique = true,foreignKey = @ForeignKey(name = "xt_domain_color_company_fkey"))
    private CompanyProfile company;

    @Column(name = "background_color", length = 50)
    private String backgroundColor;

    @Column(name = "header_color", length = 50)
    private String headerColor;

    @Column(name = "footer_color", length = 50)
    private String footerColor;

    @Column(name = "text_color", length = 50)
    private String textColor;

    @Column(name = "button_color", length = 50)
    private String buttonColor;
  
    @Column(name = "company_website")
    private String website;
    
    @Column(name = "logo_color1")
    private String logoColor1;
    
    @Column(name = "logo_color2")
    private String logoColor2;
    
    @Column(name = "logo_color3")
    private String logoColor3;
    
    @Column(name = "header_text_color", length = 50)
    private String headertextColor;
    
    @Column(name = "footer_text_color", length = 50)
    private String footertextColor;
}

