package org.seasar.cms.mailsender.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.aopalliance.intercept.MethodInvocation;
import org.seasar.cms.mailsender.InconsistencyRuntimeException;
import org.seasar.cms.mailsender.Mailsender;
import org.seasar.cms.mailsender.TemplateEvaluator;
import org.seasar.cms.mailsender.annotation.BodyTemplate;
import org.seasar.cms.mailsender.annotation.Subject;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;

import com.ozacc.mail.Mail;

public class MailsenderInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = 1L;

    private Mailsender mailsender;

    private TemplateEvaluator<Object> templateEvaluator;

    @Binding(bindingType = BindingType.MUST)
    public void setMailsender(Mailsender mailsender) {
        this.mailsender = mailsender;
    }

    @SuppressWarnings("unchecked")
    @Binding(bindingType = BindingType.MUST)
    public void setTemplateEvaluator(TemplateEvaluator templateEvaluator) {
        this.templateEvaluator = templateEvaluator;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!Modifier.isAbstract(method.getModifiers())) {
            return invocation.proceed();
        }

        Class<?> configurationType = templateEvaluator.getConfigurationType();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = invocation.getArguments();

        List<Mail> mailList = new ArrayList<Mail>();
        boolean mailSpecified = false;
        boolean multipleMailSpecified = false;
        List<MimeMessage> mimeMessageList = new ArrayList<MimeMessage>();
        boolean mimeMessageSpecified = false;
        Object configuration = templateEvaluator.getDefaultConfiguration();
        boolean configurationSpecified = false;
        Object root = null;
        boolean rootSpecified = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Object parameter = parameters[i];
            if (parameter == null) {
                continue;
            }

            if (Mail.class.isAssignableFrom(parameterType)) {
                mailList.add((Mail) parameter);
                if (!mailSpecified) {
                    mailSpecified = true;
                } else {
                    multipleMailSpecified = true;
                }
                continue;
            } else if (MimeMessage.class.isAssignableFrom(parameterType)) {
                mimeMessageList.add((MimeMessage) parameter);
                mimeMessageSpecified = true;
                continue;
            } else if (parameterType.isArray()) {
                if (Mail.class.isAssignableFrom(parameterType
                        .getComponentType())) {
                    for (Mail mail : ((Mail[]) parameter)) {
                        if (mail != null) {
                            mailList.add(mail);
                        }
                    }
                    mailSpecified = true;
                    multipleMailSpecified = true;
                    continue;
                } else if (MimeMessage.class.isAssignableFrom(parameterType
                        .getComponentType())) {
                    for (MimeMessage mimeMessage : ((MimeMessage[]) parameter)) {
                        if (mimeMessage != null) {
                            mimeMessageList.add(mimeMessage);
                        }
                    }
                    mimeMessageSpecified = true;
                    continue;
                }
            } else if (List.class.isAssignableFrom(parameterType)) {
                Type genericParameterType = method.getGenericParameterTypes()[i];
                if (genericParameterType instanceof ParameterizedType) {
                    Type listParameterType = ((ParameterizedType) genericParameterType)
                            .getActualTypeArguments()[0];
                    if (listParameterType instanceof Class) {
                        Class<?> listParameterClass = (Class<?>) listParameterType;
                        if (Mail.class.isAssignableFrom(listParameterClass)) {
                            @SuppressWarnings("unchecked")
                            List<? extends Mail> mailListParameter = (List<? extends Mail>) parameter;
                            for (Mail mail : mailListParameter) {
                                if (mail != null) {
                                    mailList.add(mail);
                                }
                            }
                            mailSpecified = true;
                            multipleMailSpecified = true;
                            continue;
                        } else if (MimeMessage.class
                                .isAssignableFrom(listParameterClass)) {
                            @SuppressWarnings("unchecked")
                            List<? extends MimeMessage> mimeMessageListParameter = (List<? extends MimeMessage>) parameter;
                            for (MimeMessage mimeMessage : mimeMessageListParameter) {
                                if (mimeMessage != null) {
                                    mimeMessageList.add(mimeMessage);
                                }
                            }
                            mimeMessageSpecified = true;
                            continue;
                        }
                    }
                }
            } else if (configurationType.isAssignableFrom(parameterType)) {
                if (configurationSpecified) {
                    throw new InconsistencyRuntimeException(
                            "Multiple configuration objects are specified");
                }
                configuration = parameter;
                configurationSpecified = true;
                continue;
            }

            if (rootSpecified) {
                throw new InconsistencyRuntimeException(
                        "Multiple root objects are specified");
            }
            root = parameter;
            rootSpecified = true;
        }
        if (mailSpecified && mimeMessageSpecified) {
            throw new InconsistencyRuntimeException(
                    "Both Mail and MimeMessage are specified");
        }

        Subject subject = method.getAnnotation(Subject.class);
        if (subject != null) {
            if (multipleMailSpecified) {
                throw new InconsistencyRuntimeException(
                        "@Subject exists despite that multiple Mail parameters are specified");
            } else if (mailSpecified) {
                if (mailList.size() == 1) {
                    Mail mail = mailList.get(0);
                    // Mail#getSubject()は、subjectフィールドがnullでも空文字列を返すようなので…。
                    if (mail.getSubject() == null
                            || mail.getSubject().length() == 0) {
                        mail.setSubject(templateEvaluator
                                .evaluateStringTemplate(configuration, subject
                                        .value(), root));
                    }
                } else if (mailList.size() == 0) {
                    ;
                } else {
                    throw new RuntimeException(
                            "Logic error. Please contact Mailsender's committer");
                }
            } else if (mimeMessageSpecified) {
                throw new InconsistencyRuntimeException(
                        "@Subject exists despite that MimeMessage object is specified");
            } else {
                if (method.getReturnType() != String.class) {
                    throw new InconsistencyRuntimeException(
                            "@Subject exists, no Mail object is specified, but return type is not String");
                }
                return templateEvaluator.evaluateStringTemplate(configuration,
                        subject.value(), root);
            }
        }

        BodyTemplate bodyTemplate = method.getAnnotation(BodyTemplate.class);
        if (bodyTemplate != null) {
            if (multipleMailSpecified) {
                throw new InconsistencyRuntimeException(
                        "@BodyTemplate exists despite that multiple Mail parameters are specified");
            } else if (mailSpecified) {
                if (mailList.size() == 1) {
                    Mail mail = mailList.get(0);
                    // Mail#getText()は、textフィールドがnullでも空文字列を返すようなので…。
                    if (mail.getText() == null || mail.getText().length() == 0) {
                        mail.setText(templateEvaluator
                                .evaluateResourceTemplate(configuration,
                                        constructTemplatePath(method,
                                                bodyTemplate.value()), root));
                    }
                    mailsender.send(mail);
                } else if (mailList.size() == 0) {
                    ;
                } else {
                    throw new RuntimeException(
                            "Logic error. Please contact Mailsender's committer");
                }
            } else if (mimeMessageSpecified) {
                throw new InconsistencyRuntimeException(
                        "@BodyTemplate exists despite that MimeMessage object is specified");
            } else {
                if (method.getReturnType() != String.class) {
                    throw new InconsistencyRuntimeException(
                            "@BodyTemplate exists, no Mail object is specified, but return type is not String");
                }
                return templateEvaluator.evaluateResourceTemplate(
                        configuration, constructTemplatePath(method,
                                bodyTemplate.value()), root);
            }
        } else {
            if (mailSpecified) {
                mailsender.send(mailList.toArray(new Mail[0]));
            } else if (mimeMessageSpecified) {
                mailsender.send(mimeMessageList.toArray(new MimeMessage[0]));
            } else {
                throw new InconsistencyRuntimeException(
                        "Neither Mail nor MimeMessage is specified");
            }
        }

        return null;
    }

    protected String constructTemplatePath(Method method, String templateName) {
        return method.getDeclaringClass().getName().replace('.', '/').concat(
                ".").concat(templateName);
    }
}
