package org.fuzzydb.spring.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;

@Persistent
public class ObjectIdItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;

	private final Map<String, Object> attributes = new HashMap<String,Object>();

	@Id
	private ObjectId id;

	private String[] newspapers;

	private String smoke;

	private Date journeyDate;

	/**
	 * Public constructor needed by some frameworks
	 */
	public ObjectIdItem() {
	}

	public ObjectIdItem(String desc) {
		this.description = desc;
	}

	public Object getAttr(String name) {
		return attributes.get(name);
	}

	public void setAttr(String name, Object value) {
		attributes.put(name, value);
	}

	public String getDescription() {
		return description;
	}

	public Date getJourneyDate() {
		return journeyDate;
	}

	public void setJourneyDate(Date journeyDate) {
		this.journeyDate = journeyDate;
	}

	public String[] getNewspapers() {
		return newspapers;
	}

	public void setNewspapers(String[] newspapers) {
		this.newspapers = newspapers;
	}

	public String getSmoke() {
		return smoke;
	}

	public void setSmoke(String smoke) {
		this.smoke = smoke;
	}

	public ObjectId getId() {
		return id;
	}

	@Override
	public String toString() {
		return description;
	}
}