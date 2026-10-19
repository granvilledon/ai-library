package com.book.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.book.backend.common.R;
import com.book.backend.pojo.Charts;
import com.book.backend.pojo.dto.chart.GenChartByAiRequest;
import com.book.backend.pojo.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图表分析服务接口
 */
public interface ChartService extends IService<Charts> {

    /**
     * AI生成图表和分析结论
     *
     * @param multipartFile       上传的数据文件
     * @param genChartByAiRequest 图表生成请求
     * @return 生成的图表和分析结果
     */
    R<BiResponse> genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest);
}
