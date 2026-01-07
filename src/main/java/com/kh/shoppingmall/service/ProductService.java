package com.kh.shoppingmall.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.shoppingmall.dao.AttachmentDao;
import com.kh.shoppingmall.dao.CartDao;
import com.kh.shoppingmall.dao.CategoryDao;
import com.kh.shoppingmall.dao.ProductCategoryMapDao;
import com.kh.shoppingmall.dao.ProductDao;
import com.kh.shoppingmall.dao.ProductOptionDao;
import com.kh.shoppingmall.dao.ReviewDao;
import com.kh.shoppingmall.dao.WishlistDao;
import com.kh.shoppingmall.dao.OrderDetailDao;
import com.kh.shoppingmall.dto.AttachmentDto;
import com.kh.shoppingmall.dto.CategoryDto;
import com.kh.shoppingmall.dto.ProductDto;
import com.kh.shoppingmall.dto.ProductOptionDto;
import com.kh.shoppingmall.error.TargetNotfoundException; 

@Service
public class ProductService {

	@Autowired private ProductDao productDao;
	@Autowired private CategoryDao categoryDao;
	@Autowired private ProductOptionDao productOptionDao;
	@Autowired private ProductCategoryMapDao productCategoryMapDao;
	@Autowired private AttachmentDao attachmentDao;
	@Autowired private WishlistDao wishlistDao;
	@Autowired private AttachmentService attachmentService;
	@Autowired private ReviewDao reviewDao;
	@Autowired private CartDao cartDao; 
	@Autowired private OrderDetailDao orderDetailDao;

	//상품 등록(SKU 방식)
	@Transactional
    public int register(ProductDto productDto, 
                        List<ProductOptionDto> optionList, // SKU 목록
                        List<Integer> categoryNoList, 
                        MultipartFile thumbnailFile
    ) throws Exception {

        //썸네일 저장
        int thumbnailNo = attachmentService.save(thumbnailFile);
        productDto.setProductThumbnailNo(thumbnailNo);

        //상품 번호 발급 + 등록 (DAO에서 시퀀스 처리 권장)
        int productNo = productDao.sequence(); 
        productDto.setProductNo(productNo);
        
        productDao.insert(productDto);

        //옵션(SKU) 등록
        if (optionList != null && !optionList.isEmpty()) {
            for (ProductOptionDto sku : optionList) {
                sku.setProductNo(productNo);
                productOptionDao.insert(sku);
            }
        }

        //카테고리 매핑 등록
        if (categoryNoList != null && !categoryNoList.isEmpty()) {
            for (Integer categoryNo : categoryNoList) {
                productCategoryMapDao.insert(productNo, categoryNo);
            }
        }
        return productNo;
    }

	//상품 옵션 조회
	public List<ProductOptionDto> getOptionsByProduct(int productNo) {
		return productOptionDao.selectListByProduct(productNo);
	}

	 //상품 수정 (SKU 방식)
    @Transactional
    public void update(
            ProductDto productDto,
            List<Integer> newCategoryNoList,
            MultipartFile newThumbnailFile,
            List<Integer> deleteAttachmentNoList
    ) throws Exception {

        int productNo = productDto.getProductNo();
        
        //썸네일 교체 로직
        ProductDto currentProduct = productDao.selectOne(productNo);
        Integer oldThumbnailNo = (currentProduct != null) ? currentProduct.getProductThumbnailNo() : null;

        Integer newThumbnailNo = null;
        if (newThumbnailFile != null && !newThumbnailFile.isEmpty()) {
            newThumbnailNo = attachmentService.save(newThumbnailFile);
            productDto.setProductThumbnailNo(newThumbnailNo);
        }
        
        //상품 기본 정보 업데이트
        productDao.update(productDto); 

        //기존 썸네일 삭제
        if (newThumbnailFile != null && !newThumbnailFile.isEmpty()) {
            if (oldThumbnailNo != null && oldThumbnailNo > 0 && !oldThumbnailNo.equals(newThumbnailNo)) {
                attachmentService.delete(oldThumbnailNo);
            }
        }

        //카테고리 매핑 수정
        List<Integer> oldCategoryList = productCategoryMapDao.selectCategoryNosByProductNo(productNo);
        for (Integer oldCat : oldCategoryList) {
            if (!newCategoryNoList.contains(oldCat)) productCategoryMapDao.delete(productNo, oldCat);
        }
        for (Integer newCat : newCategoryNoList) {
            if (!oldCategoryList.contains(newCat)) productCategoryMapDao.insert(productNo, newCat);
        }
        
        //상세 이미지 삭제 처리
        if (deleteAttachmentNoList != null) {
            for (Integer attachmentNo : deleteAttachmentNoList) {
                attachmentService.delete(attachmentNo);
            }
        }
    }

	//상품 단일 조회
	public ProductDto getProduct(int productNo) {
		return productDao.selectOne(productNo);
	}

	//상품 삭제
    @Transactional
    public void delete(int productNo) {
        //System.out.println("🚨 상품 삭제 시작: productNo = " + productNo);

        //삭제할 파일 정보 수집 (썸네일)
        ProductDto product = productDao.selectOne(productNo);
        if (product == null) {
            throw new TargetNotfoundException("상품이 존재하지 않습니다.");
        }
        Integer thumbnailNo = product.getProductThumbnailNo();

        //삭제할 파일 정보 수집 (상세 이미지)
        List<AttachmentDto> detailImages = attachmentDao.selectListByProductNo(productNo); // (AttachmentDao에 이 메소드 필요)

        //자식 테이블 데이터 삭제 (모든 FK)
        
        //카테고리 매핑 삭제
        productCategoryMapDao.deleteByProductNo(productNo); 
        System.out.println("카테고리 매핑 삭제 완료.");
        
        //옵션 종속 데이터 삭제 (cart, order_detail)
        clearOptionDependencies(productNo);
        
        //옵션(SKU) 삭제
        productOptionDao.deleteByProduct(productNo);
        System.out.println("상품 옵션(SKU) 및 cart, order_detail 삭제 완료.");
        
        //리뷰 삭제
        reviewDao.deleteByProductNo(productNo);
        System.out.println("리뷰 삭제 완료.");

        //찜 목록 삭제
        wishlistDao.deleteByProductNo(productNo);
        System.out.println("찜 목록 삭제 완료.");

        //상품 삭제 (DB)
        productDao.delete(productNo);
        System.out.println("DB 상품 삭제 완료.");

        //썸네일 삭제
        if (thumbnailNo != null && thumbnailNo > 0) {
            try {
                attachmentService.delete(thumbnailNo); 
            } catch (Exception e) {
                System.err.println("썸네일 파일 삭제 중 오류: " + e.getMessage());
            }
        }

        //상세 이미지 삭제
        for (AttachmentDto attach : detailImages) {
            try {
                attachmentService.delete(attach.getAttachmentNo());
            } catch (Exception e) {
                System.err.println("상세 이미지 파일 삭제 중 오류: " + e.getMessage());
            }
        }
        System.out.println("상품 삭제 전체 완료: productNo = " + productNo);
    }

	//위시리스트 카운트
	public Map<Integer, Integer> getWishlistCounts() {
		return wishlistDao.selectProductWishlistCounts();
	}

	//필터 검색
	public List<ProductDto> getFilteredProducts(String column, String keyword, Integer categoryNo, String order) {
		List<ProductDto> list;
		if (categoryNo != null) {
			List<Integer> categoryNos = new ArrayList<>();
			categoryNos.add(categoryNo);
			List<CategoryDto> children = categoryDao.selectChildren(categoryNo);
			for (CategoryDto c : children)
				categoryNos.add(c.getCategoryNo());
			list = productDao.selectByCategories(categoryNos, column, order); // (DAO에 이 메소드 필요)
		} else {
			list = getProductList(column, keyword, order);
		}

		return list;
	}

	public List<ProductDto> getProductList(String column, String keyword, String order) {
		boolean isSearch = column != null && !column.isEmpty() && keyword != null && !keyword.isEmpty();
		if (isSearch)
			return productDao.selectList(column, keyword, order);
		else
			return productDao.selectList(column, null, order);
	}
	
	//SKU 옵션 전용 수정
	@Transactional
	public boolean updateOptions(int productNo, List<ProductOptionDto> newOptionList) {
		try {
            //cart, order_detail 먼저 삭제
            clearOptionDependencies(productNo);
			
			//해당 상품의 기존 옵션(SKU) 모두 삭제
			productOptionDao.deleteByProduct(productNo);
			
			//새 옵션(SKU) 목록 다시 등록
			if (newOptionList != null && !newOptionList.isEmpty()) {
				for (ProductOptionDto sku : newOptionList) {
					//옵션 번호를 시퀀스에서 발급받아 DTO에 설정
			        int optionNo = productOptionDao.sequence();
			        sku.setOptionNo(optionNo);
			        
			        sku.setProductNo(productNo);
			        
			        //이제 optionNo가 포함된 DTO로 insert를 호출
			        productOptionDao.insert(sku); 
				}
			}
			return true; // 성공
		} catch (Exception e) {
			throw new RuntimeException("옵션 업데이트 중 오류가 발생했습니다.", e);
		}
	}
    
    //상품 옵션(SKU)에 종속된 cart, order_detail 삭제
    private void clearOptionDependencies(int productNo) {
        //삭제될 상품의 옵션(SKU) 번호 목록 조회
        List<ProductOptionDto> options = productOptionDao.selectListByProduct(productNo);
        if (options == null || options.isEmpty()) {
            return; // 삭제할 옵션이 없음
        }
        
        //optionNo 목록 변환
        List<Integer> optionNoList = options.stream()
                                            .map(ProductOptionDto::getOptionNo)
                                            .collect(Collectors.toList());

        //장바구니에서 해당 옵션들 삭제
        cartDao.deleteByOptionNoList(optionNoList);
        
        //주문 상세에서 해당 옵션들 삭제
        orderDetailDao.deleteByOptionNoList(optionNoList);
    }
}

