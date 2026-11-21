package com.gps.os.repository;

import com.gps.os.entity.AppSetting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
    List<AppSetting> findByScopeAndPublicSettingTrueOrderByKeyAsc(String scope);
    List<AppSetting> findByScope(String scope);
}
