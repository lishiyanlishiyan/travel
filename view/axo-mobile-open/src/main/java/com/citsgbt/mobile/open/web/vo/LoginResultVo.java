package com.citsgbt.mobile.open.web.vo;

import com.citsamex.app.spi.data.caller.response.profile.LoginResponse;
import com.citsamex.app.spi.data.dto.air.international.IntlAirQueryDefaultSetupDto;
import com.citsamex.app.spi.data.dto.base.CommonDto;
import com.citsamex.app.spi.data.dto.carrental.CarRentalDefaultSetupDto;
import com.citsamex.app.spi.data.dto.company.CompanyDefaultSetupDto;
import com.citsamex.app.spi.data.dto.company.CompanyDto;
import com.citsamex.app.spi.data.dto.company.CompanyOtherProductDto;
import com.citsamex.app.spi.data.dto.company.CompanyProductDto;
import com.citsamex.app.spi.data.dto.hotel.HotelDefaultSetupDto;
import com.citsamex.app.spi.data.dto.master.ProductCodeDto;
import com.citsamex.app.spi.data.dto.master.air.DomAirQueryDefaultSetupDto;
import com.citsamex.app.spi.data.dto.other.OtherDefaultSetupDto;
import com.citsamex.app.spi.data.dto.profile.ProfileDefaultSetupDto;
import com.citsamex.app.spi.data.dto.train.TrainDefaultSetupDto;
import com.citsamex.app.spi.data.dto.train.TrainGlobalDefaultSetupDto;
import com.citsgbt.mobile.core.web.vo.SsoParamsDto;

import java.util.List;

public class LoginResultVo extends CommonDto {

	private String accessToken;

	private LoginResponse loginResponse;

	private CompanyDto company;

	private CompanyDefaultSetupDto companyDefaultSetup;

	private DomAirQueryDefaultSetupDto domAirDefaultSetup;

	private IntlAirQueryDefaultSetupDto intlAirDefaultSetup;

	private HotelDefaultSetupDto hotelDefaultSetup;

	private TrainDefaultSetupDto trainDefaultSetup;

	private TrainGlobalDefaultSetupDto trainGlobalDefaultSetup;

	private ProfileDefaultSetupDto profileDefaultSetup;

	private CarRentalDefaultSetupDto carRentalDefaultSetup;

	private OtherDefaultSetupDto otherDefaultSetup;

	private List<CompanyOtherProductDto> companyOtherProducts;

	private List<CompanyProductDto> companyProducts;

	private List<ProductCodeDto> allProducts;

	private SsoParamsDto ssoParams;

	public LoginResponse getLoginResponse() {
		return loginResponse;
	}

	public void setLoginResponse(LoginResponse loginResponse) {
		this.loginResponse = loginResponse;
	}

	public CompanyDto getCompany() {
		return company;
	}

	public void setCompany(CompanyDto company) {
		this.company = company;
	}

	public CompanyDefaultSetupDto getCompanyDefaultSetup() {
		return companyDefaultSetup;
	}

	public void setCompanyDefaultSetup(CompanyDefaultSetupDto companyDefaultSetup) {
		this.companyDefaultSetup = companyDefaultSetup;
	}

	public DomAirQueryDefaultSetupDto getDomAirDefaultSetup() {
		return domAirDefaultSetup;
	}

	public void setDomAirDefaultSetup(DomAirQueryDefaultSetupDto domAirDefaultSetup) {
		this.domAirDefaultSetup = domAirDefaultSetup;
	}

	public IntlAirQueryDefaultSetupDto getIntlAirDefaultSetup() {
		return intlAirDefaultSetup;
	}

	public void setIntlAirDefaultSetup(IntlAirQueryDefaultSetupDto intlAirDefaultSetup) {
		this.intlAirDefaultSetup = intlAirDefaultSetup;
	}

	public HotelDefaultSetupDto getHotelDefaultSetup() {
		return hotelDefaultSetup;
	}

	public void setHotelDefaultSetup(HotelDefaultSetupDto hotelDefaultSetup) {
		this.hotelDefaultSetup = hotelDefaultSetup;
	}

	public TrainDefaultSetupDto getTrainDefaultSetup() {
		return trainDefaultSetup;
	}

	public void setTrainDefaultSetup(TrainDefaultSetupDto trainDefaultSetup) {
		this.trainDefaultSetup = trainDefaultSetup;
	}

	public TrainGlobalDefaultSetupDto getTrainGlobalDefaultSetup() {
		return trainGlobalDefaultSetup;
	}

	public void setTrainGlobalDefaultSetup(TrainGlobalDefaultSetupDto trainGlobalDefaultSetup) {
		this.trainGlobalDefaultSetup = trainGlobalDefaultSetup;
	}

	public CarRentalDefaultSetupDto getCarRentalDefaultSetup() {
		return carRentalDefaultSetup;
	}

	public void setCarRentalDefaultSetup(CarRentalDefaultSetupDto carRentalDefaultSetup) {
		this.carRentalDefaultSetup = carRentalDefaultSetup;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public ProfileDefaultSetupDto getProfileDefaultSetup() {
		return profileDefaultSetup;
	}

	public void setProfileDefaultSetup(ProfileDefaultSetupDto profileDefaultSetup) {
		this.profileDefaultSetup = profileDefaultSetup;
	}

	public OtherDefaultSetupDto getOtherDefaultSetup() {
		return otherDefaultSetup;
	}

	public void setOtherDefaultSetup(OtherDefaultSetupDto otherDefaultSetup) {
		this.otherDefaultSetup = otherDefaultSetup;
	}

	public List<CompanyOtherProductDto> getCompanyOtherProducts() {
		return companyOtherProducts;
	}

	public void setCompanyOtherProducts(List<CompanyOtherProductDto> companyOtherProducts) {
		this.companyOtherProducts = companyOtherProducts;
	}

	public List<CompanyProductDto> getCompanyProducts() {
		return companyProducts;
	}

	public void setCompanyProducts(List<CompanyProductDto> companyProducts) {
		this.companyProducts = companyProducts;
	}

	public List<ProductCodeDto> getAllProducts() {
		return allProducts;
	}

	public void setAllProducts(List<ProductCodeDto> allProducts) {
		this.allProducts = allProducts;
	}

	public SsoParamsDto getSsoParams() {
		return ssoParams;
	}

	public void setSsoParams(SsoParamsDto ssoParams) {
		this.ssoParams = ssoParams;
	}
}
