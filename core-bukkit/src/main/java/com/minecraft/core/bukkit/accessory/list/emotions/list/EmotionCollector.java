package com.minecraft.core.bukkit.accessory.list.emotions.list;

import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.emotions.EmotionsAccessory;
import com.minecraft.core.bukkit.accessory.list.particles.ParticlesAccessory;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum EmotionCollector {

    CRAZY("Louco", "45b0595a511c4b377437a516fefbea2ffcf35585ae53465b0e86024a215eb",
            new EmotionsAccessory("1k001", "Louco", "emotion.crazy.accessory", Rank.VIP, new ItemFactory().setSkullURL("45b0595a511c4b377437a516fefbea2ffcf35585ae53465b0e86024a215eb").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    PASSIONATE("Apaixonado", "42737e99e4c0596a3712e7711baecae8d1ddb774ac1cf531896862380753e16",
            new EmotionsAccessory("1k002", "Apaixonado", "emotion.passionate.accessory", Rank.VIP, new ItemFactory().setSkullURL("42737e99e4c0596a3712e7711baecae8d1ddb774ac1cf531896862380753e16").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    SURPRISE("Surpreso", "45868529fbf4be629371275b1138dab929576021716ee737db12634aa125af3",
            new EmotionsAccessory("1k003", "Surpreso", "emotion.surprise.accessory", Rank.VIP, new ItemFactory().setSkullURL("45868529fbf4be629371275b1138dab929576021716ee737db12634aa125af3").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    SMILING("Sorrindo", "41ac21d93ce17f2b7ee2e0e07a983eeb4a539e341ce5c77c36c722f77a2235",
            new EmotionsAccessory("1k004", "Sorrindo", "emotion.smiling.accessory", Rank.VIP, new ItemFactory().setSkullURL("41ac21d93ce17f2b7ee2e0e07a983eeb4a539e341ce5c77c36c722f77a2235").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    NEUTRAL("Neutro", "4db4c8dfdc7792d24c5334a5a2d1d467b6e10abfabd637e4a80ccb05b9ccfbd",
            new EmotionsAccessory("1k005", "Neutro", "emotion.neutral.accessory", Rank.VIP, new ItemFactory().setSkullURL("4db4c8dfdc7792d24c5334a5a2d1d467b6e10abfabd637e4a80ccb05b9ccfbd").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    CRYING("Chorando", "732fe121a63eaabd99ced6d1acc91798652d1ee8084d2f9127d8a315cad5ce4",
            new EmotionsAccessory("1k006", "Chorando", "emotion.crying.accessory", Rank.VIP, new ItemFactory().setSkullURL("732fe121a63eaabd99ced6d1acc91798652d1ee8084d2f9127d8a315cad5ce4").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    DISGUST("Desgosto", "5785abea5135083893f1a2f1bfaf9f4972553f9af24d6bd81da68a910736",
            new EmotionsAccessory("1k007", "Desgosto", "emotion.disgust.accessory", Rank.VIP, new ItemFactory().setSkullURL("5785abea5135083893f1a2f1bfaf9f4972553f9af24d6bd81da68a910736").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    DISAPPOINTED("Desapontado", "7d9f0f19ef161bfabaeaa49c52880becfe82fb962bbb217d1743d0f1b53a95",
            new EmotionsAccessory("1k008", "Desapontado", "emotion.disappointed.accessory", Rank.VIP, new ItemFactory().setSkullURL("7d9f0f19ef161bfabaeaa49c52880becfe82fb962bbb217d1743d0f1b53a95").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    COOL("Suave", "7ef575629a2689d63a3a3e91bd342ec3f78b4f397687c0833bf6d64bf26d12e8",
            new EmotionsAccessory("1k009", "Suave", "emotion.cool.accessory", Rank.VIP, new ItemFactory().setSkullURL("7ef575629a2689d63a3a3e91bd342ec3f78b4f397687c0833bf6d64bf26d12e8").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    HAPPY("Feliz", "8d8f5fb387ca66fc2f65b91fcb231604548e8565895bb96c676984205e6f19",
            new EmotionsAccessory("1k010", "Feliz", "emotion.happy.accessory", Rank.VIP, new ItemFactory().setSkullURL("8d8f5fb387ca66fc2f65b91fcb231604548e8565895bb96c676984205e6f19").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    SAD("Triste", "38243241ca150e14149485c6f8409c1679f85b70aaf08438000ad410c2e7a31",
            new EmotionsAccessory("1k011", "Triste", "emotion.sad.accessory", Rank.VIP, new ItemFactory().setSkullURL("38243241ca150e14149485c6f8409c1679f85b70aaf08438000ad410c2e7a31").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    ANGRY("Raiva", "b1d4bea366aca58dd5b22e940bcdd4ba45bf88421f6d831158b879f2c8abce18",
            new EmotionsAccessory("1k012", "Raiva", "emotion.angry.accessory", Rank.VIP, new ItemFactory().setSkullURL("b1d4bea366aca58dd5b22e940bcdd4ba45bf88421f6d831158b879f2c8abce18").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    LAUGHING_CRYING("Chorando de Rir", "655234f7126abd570b69209c9185fc18be78edd2b6c4661b49fd41ed09fe47db",
            new EmotionsAccessory("1k013", "Chorando de Rir", "emotion.laughing_crying.accessory", Rank.VIP, new ItemFactory().setSkullURL("655234f7126abd570b69209c9185fc18be78edd2b6c4661b49fd41ed09fe47db").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    POOP("Cocô", "f650232c1d8b337965087693449c02c960ca4393b048346a4f0b2e18e58fa",
            new EmotionsAccessory("1k014", "Cocô", "emotion.poop.accessory", Rank.VIP, new ItemFactory().setSkullURL("f650232c1d8b337965087693449c02c960ca4393b048346a4f0b2e18e58fa").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    NERVOUS("Nervoso", "6ccd5472a46f46e4cdfda9adea2320cccfbae11198b6aae163d17c4b5b4666d",
            new EmotionsAccessory("1k015", "Nervoso", "emotion.nervous.accessory", Rank.VIP, new ItemFactory().setSkullURL("6ccd5472a46f46e4cdfda9adea2320cccfbae11198b6aae163d17c4b5b4666d").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    EMBARRASSED("Embaraçoso", "f720df911c052377065408db78a25c678f791eb944c063935ae86dbe51c71b",
            new EmotionsAccessory("1k016", "Embaraçoso", "emotion.embarrassed.accessory", Rank.VIP, new ItemFactory().setSkullURL("f720df911c052377065408db78a25c678f791eb944c063935ae86dbe51c71b").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    SLEEPING("Dormindo", "9c4a41554342b2cbfbd895e9ec7089ae861fec8f51693822ec1eb3ca3f18",
            new EmotionsAccessory("1k017", "Dormindo", "emotion.sleeping.accessory", Rank.VIP, new ItemFactory().setSkullURL("9c4a41554342b2cbfbd895e9ec7089ae861fec8f51693822ec1eb3ca3f18").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    HEART("Coração", "2869bdd9a8f77eeff75d8f67ed0322bd9c16dd494972314ed707dd10a3139a58",
            new EmotionsAccessory("1k018", "Coração", "emotion.heart.accessory", Rank.VIP, new ItemFactory().setSkullURL("2869bdd9a8f77eeff75d8f67ed0322bd9c16dd494972314ed707dd10a3139a58").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    EMPTY("Vazio", "7ad0dd537148e18b388078cc79ec5e452e58e333fa52fff6531057fd3c815029",
            new EmotionsAccessory("1k019", "Vazio", "emotion.empty.accessory", Rank.VIP, new ItemFactory().setSkullURL("7ad0dd537148e18b388078cc79ec5e452e58e333fa52fff6531057fd3c815029").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    WINK("Piscando", "f4ea2d6f939fefeff5d122e63dd26fa8a427df90b2928bc1fa89a8252a7e",
            new EmotionsAccessory("1k020", "Piscando", "emotion.wink.accessory", Rank.VIP, new ItemFactory().setSkullURL("f4ea2d6f939fefeff5d122e63dd26fa8a427df90b2928bc1fa89a8252a7e").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    DEAD("Morto", "b371e4e1cf6a1a36fdae27137fd9b8748e6169299925f9af2be301e54298c73",
            new EmotionsAccessory("1k021", "Morto", "emotion.dead.accessory", Rank.VIP, new ItemFactory().setSkullURL("b371e4e1cf6a1a36fdae27137fd9b8748e6169299925f9af2be301e54298c73").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    SCARED("Assustado", "636e26c44659e8148ed58aa79e4d60db595f426442116f81b5415c2446ed8",
            new EmotionsAccessory("1k022", "Assustado", "emotion.scared.accessory", Rank.VIP, new ItemFactory().setSkullURL("636e26c44659e8148ed58aa79e4d60db595f426442116f81b5415c2446ed8").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.COMMON)),
    KISSY("Beijando", "545bd18a2aaf469fad72e52cde6cfb02bfbaa5bfed2a8151277f779ebcdcec1",
            new EmotionsAccessory("1k023", "Beijando", "emotion.kissy.accessory", Rank.VIP, new ItemFactory().setSkullURL("545bd18a2aaf469fad72e52cde6cfb02bfbaa5bfed2a8151277f779ebcdcec1").getStack(), "", true, AccessoryType.EMOTIONS, AccessoryRarity.UNCOMMON));

    private String display, url;
    private EmotionsAccessory accessory;
}