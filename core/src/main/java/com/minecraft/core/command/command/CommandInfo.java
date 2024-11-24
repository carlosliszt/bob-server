/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.command;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class CommandInfo {

    /**
     * Defines the command name, sub-commands are split with dots
     * <p><p>
     * <b>Example:</b><p>
     * parentcommand<p>
     * parentcommand.subcommand<p>
     */
    @NonNull
    @Expose
    private final String name;

    /**
     * Defines the array of aliases of the command,
     * if it doesn't have aliases it return a empty
     * array of strings
     */
    @NonNull
    @Builder.Default
    @Expose
    private final String[] aliases = new String[0];
    /**
     * Tells the executor how to run the command,
     * some implementations might ignore this option as they are async by default.
     * This option requires an executor.
     */
    @Builder.Default
    @Expose
    private final boolean async = false;
    /**
     * Defines the description of the command,
     * if it wasn't provided, it returns a empty
     * String
     */
    @Setter
    @Builder.Default
    @Expose
    private String description = "";
    /**
     * Defines the command usage for the MessageuHolder,
     * if it's empty, returns a empty String
     */
    @Setter
    @Builder.Default
    @Expose
    private String usage = "";
    /**
     * Defines the rank required to execute
     * the command, if it's empty the default rank
     * is Member
     */
    @Setter
    @Builder.Default
    @SerializedName("rank")
    @Expose
    private Rank rank = Rank.MEMBER;
    /**
     * Defines the Platform of the command,
     * if it's empty, it returns a ALL platform.
     */
    @Setter
    @NonNull
    @Builder.Default
    @Expose
    private Platform platform = Platform.BOTH;
    @Setter@NonNull
    @Expose(serialize = false, deserialize = false)
    private Class<?> holder;

    public CommandInfo(Command command, Class<?> holder) {
        this(
                command.name(),
                command.aliases(),
                command.async(),
                command.description(),
                command.usage(),
                command.rank(),
                command.platform(),
                holder
        );
    }

}