/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.fields;

import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Preference {

    CHAT("Visualizar bate-papo", Rank.MEMBER, 0x1),
    TELL("Mensagens privadas", Rank.MEMBER, 0x2),
    PARTY("Convite para party", Rank.MEMBER, 0x3),
    CLAN("Convite para clan", Rank.MEMBER, 0x4),
    CLAN_CHAT("Bate-papo do clan", Rank.MEMBER, 0x5),
    EXTRA_INFO("Visualizar informações extra", Rank.MEMBER, 0x6),
    STATISTICS("Visibilidade de estatísticas", Rank.MEMBER, 0x7),

    FRIEND_REQUEST("Solicitações de amizade", Rank.MEMBER, 0xe),

    LOBBY_PLAYER_VISIBILITY("Visualizar jogadores", Rank.MEMBER, 0x8),
    LOBBY_ANNOUNCE_JOIN("Anunciar ao entrar no lobby", Rank.VIP, 0x9),

    REPORTS("Visualizar reports", Rank.TRIAL_MODERATOR, 0xa),
    STAFFLOG("Visualizar logs da staff", Rank.PRIMARY_MOD, 0xb),
    STAFFCHAT("Visualizar bate-papo da staff", Rank.HELPER, 0xc),
    ANTICHEAT("Visualizar logs do anti cheat", Rank.TRIAL_MODERATOR, 0xd);

    private final String name;
    private final Rank rank;
    private final int bitIndex;

}