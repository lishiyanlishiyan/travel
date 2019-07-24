package com.citsgbt.mobile.open.service.impl.login;

import com.citsamex.app.spi.data.caller.request.master.BaseCompanyParam;
import com.citsamex.app.spi.data.caller.request.order.BaseTaNoCompanyParam;
import com.citsamex.app.spi.data.caller.response.air.international.IntlAirQueryDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.carrental.CarRentalDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.company.CompanyDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.company.CompanyOtherProductsResponse;
import com.citsamex.app.spi.data.caller.response.company.GetCompanyResponse;
import com.citsamex.app.spi.data.caller.response.company.GetProductsResponse;
import com.citsamex.app.spi.data.caller.response.hotel.HotelDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.master.air.domestic.DomAirQueryDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.other.OtherDefaultSetupResponse;
import com.citsamex.app.spi.data.caller.response.train.TrainDefaultSetupResponse;
import com.citsamex.app.spi.data.dto.company.CompanyProductDto;
import com.citsamex.app.spi.data.dto.master.ProductCodeDto;
import com.citsamex.app.spi.data.dto.profile.ProfileDefaultSetupDto;
import com.citsamex.app.spi.data.dto.profile.UserBasicDto;
import com.citsamex.app.spi.interfaces.targets.air.international.IntlAirOrderTargetService;
import com.citsamex.app.spi.interfaces.targets.carrental.CarRentalTargetService;
import com.citsamex.app.spi.interfaces.targets.company.CompanyConfigTargetService;
import com.citsamex.app.spi.interfaces.targets.company.CompanyTargetService;
import com.citsamex.app.spi.interfaces.targets.hotel.HotelTargetService;
import com.citsamex.app.spi.interfaces.targets.master.air.domestic.DomAirQueryDefaultSetupTargetService;
import com.citsamex.app.spi.interfaces.targets.other.OtherTargetService;
import com.citsamex.app.spi.interfaces.targets.profile.ProfileTargetService;
import com.citsamex.app.spi.interfaces.targets.train.TrainTargetService;
import com.citsgbt.mobile.consts.SystemConfigConstants;
import com.citsgbt.mobile.core.spi.ProductType;
import com.citsgbt.mobile.core.utils.lang.DozerUtils;
import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerUtils;
import com.citsgbt.mobile.open.service.login.InitLoginService;
import com.citsgbt.mobile.open.web.vo.LoginResultVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author gary
 */
@Service
public class InitLoginServiceImpl implements InitLoginService {

	@Autowired
	private CompanyTargetService companyTargetService;
	@Autowired
	private IntlAirOrderTargetService intlAirOrderTargetService;
	@Autowired
	private CompanyConfigTargetService companyConfigTargetService;
	@Autowired
	private DomAirQueryDefaultSetupTargetService domAirQueryDefaultSetupTargetService;
	@Autowired
	private TrainTargetService trainTargetService;
	@Autowired
	private HotelTargetService hotelTargetService;
	@Autowired
	private ProfileTargetService profileTargetService;
	@Autowired
	private CarRentalTargetService carRentalTargetService;
	@Autowired
	private OtherTargetService otherTargetService;

	@Override
	public void initLogin(LoginResultVo loginResult) {
		UserBasicDto accountDto = loginResult.getLoginResponse().getUserBasic();
		this.reCalcLoginResult(loginResult, accountDto.getCompanyCode(), accountDto.getUserId());
	}

	@Override
	public void reCalcLoginResult(LoginResultVo loginResult, String companyCode, String loginUserId) {
		BaseCompanyParam param = new BaseCompanyParam();
		param.setCompanyCode(companyCode);
		param.setSystemKey(SystemConfigConstants.SYSTEM_KEY);
		ServiceCallerUtils.doWithUserId(loginUserId, userId -> {
			// 公司信息
			GetCompanyResponse companyResponse = companyTargetService.getCompanyByCode(param);
			loginResult.setCompany(companyResponse.getCompany());
			// 公司默认配置
			CompanyDefaultSetupResponse companyDefaultSetup = companyTargetService.getCompanyDefaultSetup(param);
			loginResult.setCompanyDefaultSetup(companyDefaultSetup.getCompanyDefaultSetup());
			// 公司产品配置
			GetProductsResponse companyProductResponse = this.companyTargetService.getCompanyProducts(param);
			loginResult.setCompanyProducts(companyProductResponse.getProductCodeDtos());
			loginResult.setAllProducts(companyProductResponse.getProducts());
			// profile配置
			ProfileDefaultSetupDto profileDefaultSetupDto = profileTargetService.getProfileDefaultSetup(param).getProfileDefaultSetupDto();
			loginResult.setProfileDefaultSetup(profileDefaultSetupDto);
			Set<String> supportedProducts = getSupportedProducts(loginResult.getCompanyProducts(), loginResult.getAllProducts());
			if (!supportedProducts.isEmpty()) {
				calcDefaultSetup(loginResult, supportedProducts, param);
			}
		});
	}

	protected void calcDefaultSetup(LoginResultVo loginResult, Set<String> supportedProducts, BaseCompanyParam param) {
		// 国际机票默认配置
		if (supportedProducts.contains(ProductType.INTL_AIR.getValue())) {
			IntlAirQueryDefaultSetupResponse intlAirDefaultSetupResponse = this.intlAirOrderTargetService.getIntlAirQueryDefaultSetup(param);
			loginResult.setIntlAirDefaultSetup(intlAirDefaultSetupResponse.getIntlAirQueryDefaultSetup());
		}
		// 其他默认配置
		if (supportedProducts.contains(ProductType.OTHERS.getValue())) {
			CompanyOtherProductsResponse otherProductResponse = this.companyConfigTargetService.getOtherProductConfig(param);
			loginResult.setCompanyOtherProducts(otherProductResponse.getCompanyOtherProductDtos());
			OtherDefaultSetupResponse otherDefaultSetupResponse = this.otherTargetService.getOtherDefaultSetup(param);
			loginResult.setOtherDefaultSetup(otherDefaultSetupResponse.getDefaultSetup());
		}
		// 国内机票默认配置
		if (supportedProducts.contains(ProductType.DOM_AIR.getValue())) {
			DomAirQueryDefaultSetupResponse domairDefaultSetupResponse = this.domAirQueryDefaultSetupTargetService.getDomAirQueryDefaultSetup(param);
			loginResult.setDomAirDefaultSetup(domairDefaultSetupResponse.getDomAirQueryDefaultSetupDto());
		}
		// 火车默认配置
		if (supportedProducts.contains(ProductType.TRAIN.getValue())) {
			TrainDefaultSetupResponse trainProductResponse = this.trainTargetService.getTrainDefaultSetup(param);
			loginResult.setTrainDefaultSetup(trainProductResponse.getDefaultSetup());
			loginResult.setTrainGlobalDefaultSetup(trainProductResponse.getGlobalDefaultSetup());
		}
		// 酒店
		if (supportedProducts.contains(ProductType.HOTEL.getValue())) {
			HotelDefaultSetupResponse hotelProductResponse = this.hotelTargetService.getHotelDefaultSetup(DozerUtils.map(param, BaseTaNoCompanyParam.class));
			loginResult.setHotelDefaultSetup(hotelProductResponse.getDefaultSetup());
		}
		// 租车
		if (supportedProducts.contains(ProductType.CAR_RENTAL.getValue())) {
			CarRentalDefaultSetupResponse carRentalDefaultSetup = this.carRentalTargetService.getCarRentalDefaultSetup(param);
			loginResult.setCarRentalDefaultSetup(carRentalDefaultSetup.getCarRentalDefaultSetup());
		}
	}

	protected Set<String> getSupportedProducts(List<CompanyProductDto> companyProducts, List<ProductCodeDto> allProducts) {
		Set<String> productTypes = new HashSet<>();
		if (CollectionUtils.isNotEmpty(companyProducts)) {
			Set<Long> companyProductIds = companyProducts.stream().map(companyProduct -> companyProduct.getProductId().longValue())
					.collect(Collectors.toSet());
			allProducts.forEach(product -> {
				if (companyProductIds.contains(product.getId())) {
					productTypes.add(product.getProductType());
				}
			});
		}
		return productTypes;
	}
}
