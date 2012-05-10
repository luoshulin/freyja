package com.guyu.bean;

import java.util.Date;

import org.freyja.v2.annotation.Column;
import org.freyja.v2.annotation.Id;
import org.freyja.v2.annotation.SubColumn;
import org.freyja.v2.annotation.Table;


@Table(name = "t_user", isSubTable = true)
public class User {

	@Id
	private Integer id;

	@Column(name = "open_id")
	@SubColumn(isSubColumn = true)
	private String openId;

	@Column(name = "nick_name")
	private String nickName;

	/** 性别，1男0女 */
	private Integer sex;

	/** 游戏币、金币 */
	private Integer gold;

	/** 谷雨币 */
	private Integer guyuBean;

	/** 积分 */
	private Integer score;

	/** 经验 */
	private Integer experience;

	/** 等级 */
	private Integer level;

	/** 头衔ID */
	private Integer titleId;

	/** 头像 */
	private String headUrl;

	/** 连续登陆天数 */
	private Integer continueLoginCount;

	/** 登陆总天数 */
	private Integer loginCount;

	@Column(name = "createTime")
	private Date createTime;

	@Column(name = "last_login_time")
	private Date lastLoginTime;

	private String lastLoginIp;

	/** 是否完成新手引导 0：未完成 1: 已完成 2:跳过 */
	private Integer newGuide;

	/** 赠送金币次数 */
	private Integer sendNum;

	private String city;

	/** 是否是机器人，0不是 1是 */
	private Integer robot;

	/** 已领取的黄钻vip奖励,0未领取 */
	private Integer vipAward;

	//

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Integer getGold() {
		return gold;
	}

	public void setGold(Integer gold) {
		this.gold = gold;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}


	public Integer getExperience() {
		return experience;
	}

	public void setExperience(Integer experience) {
		this.experience = experience;
	}

	public Integer getContinueLoginCount() {
		return continueLoginCount;
	}

	public void setContinueLoginCount(Integer continueLoginCount) {
		this.continueLoginCount = continueLoginCount;
	}

	public Integer getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount;
	}

	public Integer getNewGuide() {
		return newGuide;
	}

	public void setNewGuide(Integer newGuide) {
		this.newGuide = newGuide;
	}

	public Integer getGuyuBean() {
		return guyuBean;
	}

	public void setGuyuBean(Integer guyuBean) {
		this.guyuBean = guyuBean;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public Integer getSendNum() {
		return sendNum;
	}

	public void setSendNum(Integer sendNum) {
		this.sendNum = sendNum;
	}

	public Integer getRobot() {
		return robot;
	}

	public void setRobot(Integer robot) {
		this.robot = robot;
	}

	public Integer getTitleId() {
		return titleId;
	}

	public void setTitleId(Integer titleId) {
		this.titleId = titleId;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getVipAward() {
		return vipAward;
	}

	public void setVipAward(Integer vipAward) {
		this.vipAward = vipAward;
	}

}
