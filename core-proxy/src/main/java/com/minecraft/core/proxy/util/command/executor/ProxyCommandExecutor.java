/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command.executor;

import com.minecraft.core.command.argument.eval.ArgumentEvaluator;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.proxy.util.command.ProxyFrame;
import com.minecraft.core.proxy.util.command.command.ProxyCommand;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ProxyCommandExecutor implements CommandExecutor<CommandSender> {

    private final Method method;
    private final Object holder;

    @Getter
    private final ArgumentEvaluator<CommandSender> evaluator;

    @Setter
    private ProxyCommand command;

    public ProxyCommandExecutor(ProxyFrame frame, Method method, Object holder) {
        final Class<?> returnType = method.getReturnType();

        if (!returnType.equals(Void.TYPE)
                && !returnType.equals(Boolean.TYPE)) {
            throw new CommandException("Illegal return type, '" + method.getName());
        }

        this.method = method;
        this.holder = holder;

        this.evaluator = new ArgumentEvaluator<>(frame.getMethodEvaluator().evaluateMethod(method));
    }

    @Override
    public boolean execute(Context<CommandSender> context) {
        final Object result = invokeCommand(context);

        if (result != null && result.getClass().equals(Boolean.TYPE)) {
            return ((boolean) result);
        }

        return false;
    }

    public Object invokeCommand(Context<CommandSender> context) {
        try {
            if (evaluator.getArgumentList().size() == 0) {
                return method.invoke(holder);
            }

            final Object[] parameters;

            try {
                parameters = evaluator.parseArguments(context);
            } catch (Exception e) {
                throw new InvocationTargetException(new CommandException(MessageType.INCORRECT_USAGE, null));
            }

            return method.invoke(holder, parameters);
        } catch (InvocationTargetException e) {
            final Throwable throwable = e.getTargetException();

            if (!(throwable instanceof CommandException)) {
                e.printStackTrace();
                context.sendMessage("§cAn internal error occurred, please contact the development team.");
                return false;
            }

            final CommandException exception = (CommandException) throwable;
            final MessageType messageType = exception.getMessageType();

            String message = throwable.getMessage();

            if (messageType != null) {
                if (message == null) {
                    message = messageType.getDefault(command);
                }

                context.info(messageType.getMessageKey(), message.replace("{label}", context.getLabel()));
            } else {
                e.printStackTrace();
                context.info(MessageType.ERROR.getMessageKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.info(MessageType.ERROR.getMessageKey());
        }

        return false;
    }
}
