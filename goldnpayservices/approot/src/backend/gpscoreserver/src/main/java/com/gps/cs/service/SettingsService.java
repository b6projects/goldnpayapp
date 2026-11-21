package com.gps.cs.service;

import com.gps.cs.entity.AppSetting;
import com.gps.cs.entity.SettingScope;
import com.gps.cs.exception.ApiException;
import com.gps.cs.repository.AppSettingRepository;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Key/value settings in three scopes. Only APP settings are public. Credentials never live here. */
@Service
@RequiredArgsConstructor
public class SettingsService {
    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");
    private final AppSettingRepository repo;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public Map<String, String> get(SettingScope scope) {
        return toMap(repo.findByScopeOrderByKeyAsc(scope));
    }

    @Transactional(readOnly = true)
    public Map<String, String> publicApp() {
        return toMap(repo.findByScopeAndPublicSettingTrueOrderByKeyAsc(SettingScope.APP));
    }

    @Transactional
    public Map<String, String> put(SettingScope scope, Map<String, String> values) {
        values.forEach((k, v) -> {
            if (!KEY.matcher(k).matches()) {
                throw ApiException.badRequest("INVALID_SETTING_KEY", "Invalid setting key: " + k);
            }
            if (v != null && v.length() > 2000) {
                throw ApiException.badRequest("SETTING_TOO_LONG", "Value too long for " + k);
            }
            AppSetting s = repo.findById(k).orElseGet(() -> {
                AppSetting n = new AppSetting();
                n.setKey(k);
                n.setScope(scope);
                n.setPublicSetting(scope == SettingScope.APP);
                return n;
            });
            if (s.getScope() != scope) {
                throw ApiException.conflict("SETTING_SCOPE_MISMATCH", "Key " + k + " belongs to another settings group");
            }
            s.setValue(v);
            repo.save(s);
        });
        audit.log("SETTINGS_UPDATED", "Settings", scope, String.join(",", values.keySet()));
        return get(scope);
    }

    @Transactional(readOnly = true)
    public boolean flag(SettingScope scope, String key) {
        return repo.findById(key).filter(s -> s.getScope() == scope).map(s -> "true".equalsIgnoreCase(s.getValue()))
                .orElse(false);
    }

    private static Map<String, String> toMap(Iterable<AppSetting> rows) {
        Map<String, String> m = new TreeMap<>();
        rows.forEach(s -> m.put(s.getKey(), s.getValue()));
        return m;
    }
}
