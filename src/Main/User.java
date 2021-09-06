/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Main;

import java.util.HashMap;

public class User {

	private String nickname;
	private Role role = Role.USER;
	public enum Role {
		ADMIN,
		USER
	}
	private Long last_seen_time;
	private HashMap<String, Object> info;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Long getLast_seen_time() {
		return last_seen_time;
	}

	public void setLast_seen_time(Long last_seen_time) {
		this.last_seen_time = last_seen_time;
	}

	public HashMap<String, Object> getInfo() {
		return info;
	}

	public void setInfo(HashMap<String, Object> info) {
		this.info = info;
	}

	public User(String nickname, Role role, Long last_seen_time, HashMap<String, Object> info) {
		this.nickname = nickname;
		this.role = role;
		this.last_seen_time = last_seen_time;
		this.info = info;
	}

	public boolean isAdmin(){
		return role.equals(User.Role.ADMIN);
	}
}
