package kr.co.fitview.api.app.docs

import kr.co.fitview.api.app.global.entity.Role
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object RestDocsPagination {
    const val AUTHORIZATION = "Authorization"

    fun paginationByPage(basePath: String = "data.pagination"): Array<FieldDescriptor> {
        return arrayOf(
            fieldWithPath("$basePath").type(JsonFieldType.OBJECT)
                .description("페이지 정보: 현재 페이지, 총 페이지 수, 다음/이전 페이지 여부 등"),
            fieldWithPath("$basePath.page").type(JsonFieldType.NUMBER)
                .description("현재 페이지 번호. (1부터 시작)"),
            fieldWithPath("$basePath.size").type(JsonFieldType.NUMBER)
                .description("한 페이지에 표시되는 데이터 수"),
            fieldWithPath("$basePath.totalElements").type(JsonFieldType.NUMBER)
                .description("전체 요소 수 (Slice인 경우 null)"),
            fieldWithPath("$basePath.totalPages").type(JsonFieldType.NUMBER)
                .description("전체 페이지 수 (Slice인 경우 null)"),
            fieldWithPath("$basePath.hasNext").type(JsonFieldType.BOOLEAN)
                .description("다음 페이지 존재 여부"),
            fieldWithPath("$basePath.hasPrevious").type(JsonFieldType.BOOLEAN)
                .description("이전 페이지 존재 여부")
        )
    }

    fun paginationBySlice(basePath: String = "data.pagination"): Array<FieldDescriptor> {
        return arrayOf(
            fieldWithPath("$basePath").type(JsonFieldType.OBJECT)
                .description("페이지 정보: 현재 페이지, 총 페이지 수, 다음/이전 페이지 여부 등"),
            fieldWithPath("$basePath.page").type(JsonFieldType.NUMBER)
                .description("현재 페이지 번호. (1부터 시작)"),
            fieldWithPath("$basePath.size").type(JsonFieldType.NUMBER)
                .description("한 페이지에 표시되는 데이터 수"),
            fieldWithPath("$basePath.totalElements").type(JsonFieldType.NUMBER).optional()
                .description("전체 요소 수 (Slice인 경우 null)"),
            fieldWithPath("$basePath.totalPages").type(JsonFieldType.NUMBER).optional()
                .description("전체 페이지 수 (Slice인 경우 null)"),
            fieldWithPath("$basePath.hasNext").type(JsonFieldType.BOOLEAN)
                .description("다음 페이지 존재 여부"),
            fieldWithPath("$basePath.hasPrevious").type(JsonFieldType.BOOLEAN)
                .description("이전 페이지 존재 여부")
        )
    }

    fun paginationByCursor(basePath: String = "data.pagination"): Array<FieldDescriptor> {
        return arrayOf(
            fieldWithPath("$basePath").type(JsonFieldType.OBJECT)
                .description("커서 기반 페이지 정보: 페이지 크기, 마지막 요소 ID, 다음 페이지 존재 여부 등"),
            fieldWithPath("$basePath.size").type(JsonFieldType.NUMBER)
                .description("한 페이지에 표시되는 데이터 수"),
            fieldWithPath("$basePath.cursorId").type(JsonFieldType.NUMBER).optional()
                .description("다음 페이지를 조회할 때 기준이 되는 마지막 or 처음 요소 ID. 데이터가 없으면 null"),
            fieldWithPath("$basePath.hasNext").type(JsonFieldType.BOOLEAN)
                .description("다음 페이지 존재 여부")
        )
    }
}