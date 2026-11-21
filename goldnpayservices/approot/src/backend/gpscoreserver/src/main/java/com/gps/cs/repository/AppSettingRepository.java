package com.gps.cs.repository;

import com.gps.cs.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
    List<AppSetting> findByScopeOrderByKeyAsc(SettingScope scope);
    List<AppSetting> findByScopeAndPublicSettingTrueOrderByKeyAsc(SettingScope scope);
}
