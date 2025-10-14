package com.demo.llmstreaming.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流式数据响应VO
 */
@Data
@ApiModel(description = "流式数据响应")
public class StreamResponseVO {

    @ApiModelProperty(value = "数据ID")
    private String dataId;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "数据内容")
    private String content;

    @ApiModelProperty(value = "是否完成")
    private Boolean finished;

    @ApiModelProperty(value = "时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "序列号")
    private Integer sequence;

    public StreamResponseVO() {
        this.timestamp = LocalDateTime.now();
        this.finished = false;
    }

    public StreamResponseVO(String dataId, String dataType, String content) {
        this();
        this.dataId = dataId;
        this.dataType = dataType;
        this.content = content;
    }

    public StreamResponseVO(String dataId, String dataType, String content, Integer sequence) {
        this(dataId, dataType, content);
        this.sequence = sequence;
    }

    public static StreamResponseVO createData(String dataId, String dataType, String content, Integer sequence) {
        return new StreamResponseVO(dataId, dataType, content, sequence);
    }

    public static StreamResponseVO createEnd(String dataId) {
        StreamResponseVO response = new StreamResponseVO();
        response.setDataId(dataId);
        response.setDataType("END");
        response.setFinished(true);
        return response;
    }

    public static StreamResponseVO createError(String dataId, String errorMessage) {
        StreamResponseVO response = new StreamResponseVO();
        response.setDataId(dataId);
        response.setDataType("ERROR");
        response.setErrorMessage(errorMessage);
        response.setFinished(true);
        return response;
    }
}

