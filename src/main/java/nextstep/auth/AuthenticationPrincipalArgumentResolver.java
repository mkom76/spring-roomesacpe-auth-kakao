package nextstep.auth;

import nextstep.exception.AuthorizationException;
import nextstep.member.MemberDao;
import org.springframework.core.MethodParameter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    MemberDao memberDao;
    JwtTokenProvider jwtTokenProvider;

    public AuthenticationPrincipalArgumentResolver(JdbcTemplate jdbcTemplate) {
        memberDao = new MemberDao(jdbcTemplate);
        jwtTokenProvider = new JwtTokenProvider();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // TODO: 유효한 로그인인 경우 LoginMember 만들어서 응답하기

        HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();

        String token = AuthorizationExtractor.extract(httpServletRequest);

        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthorizationException();
        }

        String username = jwtTokenProvider.getPrincipal(token);

        return memberDao.findByUsername(username);
    }
}