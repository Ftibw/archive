package com.yufone.dmbd.action.client.activity;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * @author : Ftibw
 * @date : 2019/1/24 19:17
 */
public class ClientActivityCondition implements Condition {

    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment env = ctx.getEnvironment();
        boolean flag = false;
        try {
            int port = Integer.parseInt(env.getProperty("activity.client.port"));
            flag = port > 0 && port < 0xffff;
        } catch (Exception ignored) {
        }
        return flag;
    }
}
