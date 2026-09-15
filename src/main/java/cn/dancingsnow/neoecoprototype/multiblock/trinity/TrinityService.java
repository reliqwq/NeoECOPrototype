package cn.dancingsnow.neoecoprototype.multiblock.trinity;

/** Read-only view of one Trinity wing; it never owns or mutates the source cluster. */
public interface TrinityService {
    Module module();

    int componentCount();

    boolean online();

    default String status() {
        return online() ? "online" : "offline";
    }

    enum Module {
        STORAGE, COMPUTATION, CRAFTING
    }
}
