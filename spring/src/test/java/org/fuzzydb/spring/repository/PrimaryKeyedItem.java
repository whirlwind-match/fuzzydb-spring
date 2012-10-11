package org.fuzzydb.spring.repository;

import java.io.Serializable;

import org.fuzzydb.core.annotations.Key;

public class PrimaryKeyedItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@Key(unique=true)
	private final String email;

	private final String passHash;

	public PrimaryKeyedItem(String email, String passHash) {
		this.email = email;
		this.passHash = passHash;
	}

	public String getEmail() {
		return email;
	}

	public String getPassHash() {
		return passHash;
	}
}
