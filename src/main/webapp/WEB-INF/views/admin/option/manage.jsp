<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="/WEB-INF/views/template/header.jsp" />


<script>
$(function() {
    var skuIndex = ${optionList.size()}; // 기존 목록 수에 이어서 인덱스 시작
	
 	//옵션 정의 칸 추가 버튼
    $("#btn-add-option-item").on("click", function() {
        var html = 
            '<div class="option-item cell flex-box mt-10" style="gap: 10px;">' +
                '<input type="text" class="field opt-name" placeholder="옵션 이름 (예: 색상)">' +
                '<input type="text" class="field flex-fill opt-values" placeholder="옵션 값 (콤마로 구분)">' +
                '<button type="button" class="btn btn-neutral btn-remove-option">X</button>' +
            '</div>';
        $("#option-definition-container").append(html);
    });

    //옵션 정의 칸 삭제
    $("#option-definition-container").on("click", ".btn-remove-option", function() {
        $(this).closest(".option-item").remove();
    });
    
    //조합 생성하기 버튼
    $("#generate-sku-btn").on("click", function() {
        var allOptions = []; // 모든 옵션 데이터 담기

        $(".option-item").each(function() {
            var name = $(this).find(".opt-name").val().trim();
            var values = $(this).find(".opt-values").val().trim().split(',')
                            .map(s => s.trim()).filter(s => s);
            
            if (values.length > 0) {
                allOptions.push({ name: name, values: values });
            }
        });

        if (allOptions.length === 0) {
            alert("최소 하나 이상의 옵션 값을 입력하세요.");
            return;
        }

        // 재고 초기화 확인
        if($("#sku-table-body tr").length > 0) {
            if(!confirm("기존에 생성된 조합이 삭제됩니다. 계속하시겠습니까?")) return;
            $("#sku-table-body").empty();
            skuIndex = 0;
        }

        // 데카르트 곱 알고리즘으로 조합 생성
        var combinations = generatePowerSet(allOptions);
        
        combinations.forEach(function(combinedName) {
            addSkuRow(combinedName, 0);
        });
    });

        // N개의 배열을 조합하는 재귀 함수
        function generatePowerSet(options) {
            var results = [];

            function helper(idx, currentName) {
                if (idx === options.length) {
                    results.push(currentName);
                    return;
                }

                var option = options[idx];
                option.values.forEach(function(val) {
                    var nextName = currentName;
                    if (nextName !== "") nextName += " / ";
                    nextName += (option.name ? option.name + ": " : "") + val;
                    
                    helper(idx + 1, nextName);
                });
            }

            helper(0, "");
            return results;
        }

    //테이블에 행 추가
    function addSkuRow(name, stock) {
        var html = '<tr>' +
            '<td>' +
                '<input type="text" name="optionList[' + skuIndex + '].optionName" class="field" value="' + name + '" readonly>' +
            '</td>' +
            '<td>' +
                '<input type="number" name="optionList[' + skuIndex + '].optionStock" class="field" value="' + stock + '" min="0">' +
            '</td>' +
            '<td>' +
                '<span class="btn-delete-sku"><i class="fa-solid fa-trash"></i></span>' +
            '</td>' +
        '</tr>';
        $("#sku-table-body").append(html);
        skuIndex++;
    }

    //행 삭제 버튼
    $("#sku-table-body").on("click", ".btn-delete-sku", function() {
        if(confirm("이 옵션 조합을 삭제하시겠습니까?")) {
            $(this).closest("tr").remove();
        }
    });

    //색상/사이즈 선택 팝업 함수
    function promptSelectOption(current) {
        var options = ["색상", "사이즈"];
        var msg = "옵션 이름 선택 (현재: " + current + ")\n";
        for (var i = 0; i < options.length; i++) {
            msg += (i + 1) + ". " + options[i] + "\n";
        }
        var choice = prompt(msg, "1");
        if (choice === null) return null;
        var idx = parseInt(choice);
        if (isNaN(idx) || idx < 1 || idx > options.length) return current;
        return options[idx - 1];
    }
});
</script>

<style>
/* ... (기존 CSS와 공통 CSS 사용) ... */
.option-group { border: 1px solid #ddd; padding: 15px; margin-bottom: 15px; border-radius: 5px; }
.option-values { font-style: italic; color: #555; }
#sku-table th, #sku-table td { text-align: left; padding: 10px; }
#sku-table input[type="text"], #sku-table input[type="number"] { width: 90%; }
.btn-delete-sku { color: #e74c3c; cursor: pointer; }
</style>

<div class="container w-800">
    <h1>"${product.productName}" - 옵션 관리</h1>

    <div class="option-group">
        <div class="flex-box" style="justify-content: space-between; align-items: center;">
            <h3>옵션 조합 정의</h3>
            <button type="button" id="btn-add-option-item" class="btn btn-neutral btn-sm">+ 항목 추가</button>
        </div>
        <p class="gray">옵션 값은 콤마(,)로 구분하세요.</p>
        
        <div id="option-definition-container">
            <div class="option-item cell flex-box mt-10" style="gap: 10px;">
                <input type="text" class="field opt-name" placeholder="옵션 이름 (예: 색상)">
                <input type="text" class="field flex-fill opt-values" placeholder="옵션 값 (예: 빨강, 파랑)">
            </div>
        </div>
        
        <button type="button" id="generate-sku-btn" class="btn btn-neutral w-100 mt-15">조합 생성하기</button>
    </div>

    <form action="save" method="post">
        <input type="hidden" name="productNo" value="${product.productNo}">
        <table id="sku-table" class="table table-border w-100">
            <tbody id="sku-table-body">
                </tbody>
        </table>
        <button type="submit" class="btn btn-positive w-100 mt-20 mb-40">전체 SKU 저장하기</button>
    </form>
</div>


<jsp:include page="/WEB-INF/views/template/footer.jsp" />
