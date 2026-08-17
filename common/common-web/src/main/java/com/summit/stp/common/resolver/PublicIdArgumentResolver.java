package com.summit.stp.common.resolver;

import com.summit.stp.common.annotation.PublicId;
import com.summit.stp.common.codec.IdCodec;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class PublicIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final IdCodec idCodec;

    public PublicIdArgumentResolver(IdCodec idCodec) {
        this.idCodec = idCodec;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PublicId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String paramName = parameter.getParameterName();
        String rawValue = null;

        // 优先检查 @PathVariable
        if (parameter.hasParameterAnnotation(PathVariable.class)) {
            PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
            if (pathVariable != null && !pathVariable.value().isEmpty()) {
                paramName = pathVariable.value();
            } else if (pathVariable != null && !pathVariable.name().isEmpty()) {
                paramName = pathVariable.name();
            }
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> uriTemplateVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                if (uriTemplateVariables != null) {
                    rawValue = uriTemplateVariables.get(paramName);
                }
            }
        }

        // 其次检查 RequestParam 或常规 Parameter
        if (rawValue == null) {
            if (parameter.hasParameterAnnotation(RequestParam.class)) {
                RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
                if (requestParam != null && !requestParam.value().isEmpty()) {
                    paramName = requestParam.value();
                } else if (requestParam != null && !requestParam.name().isEmpty()) {
                    paramName = requestParam.name();
                }
            }
            if (paramName != null) {
                rawValue = webRequest.getParameter(paramName);
            }
        }

        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }

        return idCodec.decode(rawValue);
    }
}
