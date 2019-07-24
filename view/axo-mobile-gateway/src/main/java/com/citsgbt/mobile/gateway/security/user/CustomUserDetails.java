package com.citsgbt.mobile.gateway.security.user;

import com.citsamex.app.spi.data.dto.company.CompanyProductDto;
import com.citsamex.app.spi.data.dto.master.tc.TCRoleDto;
import com.citsamex.app.spi.data.dto.profile.LoginCheckDto;
import com.citsamex.app.spi.data.dto.profile.UserBasicDto;
import com.citsamex.app.spi.data.dto.profile.UserMasterDto;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Created on 2014/8/6 16:13.<br>
 *
 * @author gary.fu
 */
public class CustomUserDetails implements UserDetails {

	private String companyCode;

	private String loginName;

	private final UserBasicDto accountDto;

	private String accessIp;

	private List<CompanyProductDto> productCodes;

	private String noticeMessage;

	private Set<GrantedAuthority> authorities;

	private Set<String> resourceCodes = new HashSet<>();

	private String username;

	private LoginCheckDto loginCheck;

	/**
	 * 用户信息
	 *
	 * @param accountDto
	 */
	public CustomUserDetails(UserBasicDto accountDto, String usernmae, List<CompanyProductDto> productCodes, LoginCheckDto loginCheck, String noticeMessage) {
		Validate.notNull(accountDto, "用户信息不能为空");
		this.accountDto = accountDto;
		this.username = usernmae;
		this.productCodes = productCodes;
		this.loginCheck = loginCheck;
		this.noticeMessage = noticeMessage;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (authorities == null && accountDto.getNewUserRoleMenu() != null) {
			authorities = new HashSet<>();
			for (TCRoleDto role : accountDto.getNewUserRoleMenu().getUserRoles()) {
				resourceCodes.add(role.getRoleCode());
				authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
			}
		}
		return authorities;
	}

	public String getUserSalt() {
		if (accountDto instanceof UserMasterDto) {
			return ((UserMasterDto) accountDto).getUserSalt();
		}
		return null;
	}

	@Override
	public String getPassword() {
		if (accountDto instanceof UserMasterDto) {
			return ((UserMasterDto) accountDto).getPassword();
		}
		return null;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return accountDto != null && Arrays.asList("0", "2").contains(accountDto.getStatusCode());
	}

	public UserBasicDto getAccountDto() {
		return accountDto;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public Set<String> getResourceCodes() {
		return resourceCodes;
	}

	public void setResourceCodes(Set<String> resourceCodes) {
		this.resourceCodes = resourceCodes;
	}

	public List<CompanyProductDto> getProductCodes() {
		return productCodes;
	}

	public void setProductCodes(List<CompanyProductDto> productCodes) {
		this.productCodes = productCodes;
	}

	public String getNoticeMessage() {
		return noticeMessage;
	}

	public void setNoticeMessage(String noticeMessage) {
		this.noticeMessage = noticeMessage;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CustomUserDetails) {
			return new EqualsBuilder().append(((CustomUserDetails) obj).getUsername(), this.getUsername()).build();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getUsername()).build();
	}

	public LoginCheckDto getLoginCheck() {
		return loginCheck;
	}

	public void setLoginCheck(LoginCheckDto loginCheck) {
		this.loginCheck = loginCheck;
	}
}
