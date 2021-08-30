package ru.privetdruk.l2jspace.gameserver.model;

import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.enums.ServiceType;

/**
 * This class holds info about expirable services.
 *
 * @author GKR
 */
public final class ExpirableService {
    private final ServiceType type;
    private long expirationDate;
    private final boolean isAccountBased;

    public ExpirableService(ServiceType type, long expirationDate, boolean isAccountBased) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.isAccountBased = isAccountBased;
    }

    public void setExpirationDate(long date) {
        expirationDate = date;
    }

    public ServiceType getType() {
        return type;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public boolean isTypeOf(ServiceType type) {
        return (type == this.type);
    }

    public boolean isExpired() {
        return ((expirationDate > 0) && (expirationDate <= Chronos.currentTimeMillis()));
    }

    public boolean isTemporary() {
        return (expirationDate == 0);
    }

    public boolean isUnlimited() {
        return (expirationDate == -1);
    }

    public boolean isAccountBased() {
        return isAccountBased;
    }
}
