/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DieCause {

    COMBAT("hg.game.user_death.combat.regexp", false, false),
    DIE("hg.game.user_death.die.regexp", true, true),
    KILLED("hg.game.user_death.killed", true, true),
    SURRENDER("hg.game.user_death.surrender.regexp", false, false),
    TIMEOUT("hg.game.user_death.timeout.regexp", false, false),
    SUICIDE("hg.game.user_death.suicide.regexp", true, true),
    FINAL_KILL("hg.game.user_death.die.regexp", true, false);

    private final String message;
    private final boolean needKit;
    private final boolean respawnable;

}
