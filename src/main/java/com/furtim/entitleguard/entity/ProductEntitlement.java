package com.furtim.entitleguard.entity;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ProductEntitlement {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "orderItemId")
	private OrderItem orderItem;
	
	@ManyToOne
	@JoinColumn(name = "entitlementId")
	private Entitlement entitlement;
	
	@ManyToOne
	@JoinColumn(name = "entitlementDocumentsId")
	private Documents entitlementDocuments;
    
    private Integer entitlementPeriodValue;
    
    private String entitlementPeriodType;
    
    private String entitlementPolicyDescription;
	
	private LocalDate entitlementExpiryDate;
	
	
}
