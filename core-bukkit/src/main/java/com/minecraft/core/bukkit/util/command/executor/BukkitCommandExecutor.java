/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.executor;

import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.bukkit.util.command.command.BukkitCommand;
import com.minecraft.core.command.argument.eval.ArgumentEvaluator;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.message.MessageType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The BukkitCommandExecutor is the main executor of each
 * method that is listed as a Command, it invokes the method
 * and executes everything inside.
 */
public class BukkitCommandExecutor implements CommandExecutor<CommandSender> {

    private final Method method;
    @Getter
    private final Object holder;

    @Getter
    private final ArgumentEvaluator<CommandSender> evaluator;

    @Setter
    private BukkitCommand command;

    /**
     * Creates a new BukkitCommandExecutor with the provided
     * Command method to execute and Command holder
     *
     * @param frame  BukkitFrame
     * @param method Method
     * @param holder Object
     */
    public BukkitCommandExecutor(BukkitFrame frame, Method method, Object holder) {
        final Class<?> returnType = method.getReturnType();

        if (!returnType.equals(Void.TYPE)
                && !returnType.equals(Boolean.TYPE)) {
            throw new CommandException("Illegal return type, '" + method.getName());
        }

        this.method = method;
        this.holder = holder;

        this.evaluator = new ArgumentEvaluator<>(frame.getMethodEvaluator().evaluateMethod(method));
    }

    /**
     * Executes the command with the provided context
     * <p>Returns false if the execution wasn't successful</p>
     *
     * @param context Context
     * @return boolean
     */
    @Override
    public boolean execute(Context<CommandSender> context) {
        final Object result = invokeCommand(context);

        if (result != null && result.getClass().equals(Boolean.TYPE)) {
            return ((boolean) result);
        }

        return false;
    }

    /**
     * Invokes the command method and returns the
     * result of dispatching that method.
     *
     * @param context Context
     * @return Object
     */
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
        } catch (InvocationTargetException targetException) {
            final Throwable throwable = targetException.getTargetException();

            if (!(throwable instanceof CommandException)) {
                targetException.printStackTrace();
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

                context.info(messageType.getMessageKey(), StringUtils.replace(message, "{label}", context.getLabel()));
                return true;
            }

            targetException.printStackTrace();
            context.info(MessageType.ERROR.getMessageKey());
        } catch (Exception e) {
            e.printStackTrace();
            context.info(MessageType.ERROR.getMessageKey());
        }

        return false;
    }

}
