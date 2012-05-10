package com.guyu.bean;


import org.freyja.v2.annotation.Column;
import org.freyja.v2.annotation.Id;
import org.freyja.v2.annotation.SubColumn;
import org.freyja.v2.annotation.Table;

@Table(name = "t_hero")
public class Hero {
	@Id
	private Integer id;
	
	@SubColumn(isSubColumn=true)
	@Column(name = "user_id")
	private Integer userId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
